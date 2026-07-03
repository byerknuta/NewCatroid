/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2022 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.ui.recyclerview.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextWatcher
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import org.catrobat.catroid.ProjectManager
import org.catrobat.catroid.R
import org.catrobat.catroid.common.SharedPreferenceKeys.INDEXING_VARIABLE_PREFERENCE_KEY
import org.catrobat.catroid.common.SharedPreferenceKeys.SORT_VARIABLE_PREFERENCE_KEY
import org.catrobat.catroid.content.bricks.ScriptBrick
import org.catrobat.catroid.content.bricks.UserDefinedReceiverBrick
import org.catrobat.catroid.formulaeditor.UserData
import org.catrobat.catroid.formulaeditor.UserList
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.ui.BottomBar
import org.catrobat.catroid.ui.UiUtils
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment
import org.catrobat.catroid.ui.recyclerview.adapter.DataListAdapter
import org.catrobat.catroid.ui.recyclerview.adapter.RVAdapter
import org.catrobat.catroid.ui.recyclerview.adapter.multiselection.MultiSelectionManager
import org.catrobat.catroid.ui.recyclerview.dialog.TextInputDialog
import org.catrobat.catroid.ui.recyclerview.dialog.textwatcher.DuplicateInputTextWatcher
import org.catrobat.catroid.ui.recyclerview.viewholder.CheckableViewHolder
import org.catrobat.catroid.userbrick.UserDefinedBrickInput
import org.catrobat.catroid.utils.ToastUtil
import org.catrobat.catroid.utils.UserDataUtil.renameUserData
import java.util.Collections

class DataListFragment : Fragment(),
    ActionMode.Callback, RVAdapter.SelectionListener,
    RVAdapter.OnItemClickListener<UserData<*>> {
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(NONE, DELETE)
    internal annotation class ActionModeType

    private var recyclerView: RecyclerView? = null
    private var adapter: DataListAdapter? = null
    private var actionMode: ActionMode? = null
    private var isObserverRegistered = false
    private var formulaEditorDataInterface: FormulaEditorDataInterface? = null
    private var parentScriptBrick: ScriptBrick? = null
    private var emptyView: TextView? = null
    private var sortData = false
    private var indexVariable = false

    private var originalUserDefinedBrickInputs = listOf<UserDefinedBrickInput>()
    private var originalGlobalVars = mutableListOf<UserVariable>()
    private var originalLocalVars = mutableListOf<UserVariable>()
    private var originalMultiplayerVars = mutableListOf<UserVariable>()
    private var originalGlobalLists = mutableListOf<UserList>()
    private var originalLocalLists = mutableListOf<UserList>()

    private var currentSearchQuery = ""

    @ActionModeType
    var actionModeType = NONE
    fun setFormulaEditorDataInterface(formulaEditorDataInterface: FormulaEditorDataInterface?) {
        this.formulaEditorDataInterface = formulaEditorDataInterface
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        when (actionModeType) {
            DELETE -> mode.title = getString(R.string.am_delete)
            NONE -> return false
        }
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.context_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        adapter?.showCheckBoxes(true)
        adapter?.updateDataSet()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.confirm -> handleContextualAction()
            else -> return false
        }
        return true
    }

    fun shouldShowEmptyView(): Boolean = adapter!!.itemCount == 0

    fun setShowEmptyView(visible: Boolean) {
        emptyView!!.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private var observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            setShowEmptyView(shouldShowEmptyView())
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        resetActionModeParameters()
        adapter?.clearSelection()
    }

    private fun handleContextualAction() {
        if (adapter?.selectedItems?.isEmpty() != false) {
            actionMode?.finish()
            return
        }
        when (actionModeType) {
            DELETE -> showDeleteAlert(adapter!!.selectedItems)
            NONE -> throw IllegalStateException("ActionModeType not set Correctly")
        }
    }

    private fun resetActionModeParameters() {
        actionModeType = NONE
        actionMode = null
        adapter?.showCheckBoxes(false)
        adapter?.allowMultiSelection = true
        BottomBar.showAddButton(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val originalView = inflater.inflate(R.layout.fragment_list_view, container, false)
        recyclerView = originalView.findViewById(R.id.recycler_view)
        emptyView = originalView.findViewById(R.id.empty_view)
        setHasOptionsMenu(true)

        val context = requireContext()
        val density = resources.displayMetrics.density

        val searchViewId = View.generateViewId()

        val containerLayout = android.widget.FrameLayout(context).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        recyclerView?.let { rv ->
            val topPadding = (72 * density).toInt()
            rv.setPadding(
                rv.paddingLeft,
                topPadding,
                rv.paddingRight,
                rv.paddingBottom
            )
            rv.clipToPadding = false

            rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    containerLayout.findViewById<View>(searchViewId)?.clearFocus()
                }
            })
        }

        val cardView = com.google.android.material.card.MaterialCardView(context).apply {
            radius = 12 * density
            cardElevation = 4 * density
            strokeWidth = 0
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.button_background))

            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginHorizontal = (16 * density).toInt()
                val marginVertical = (8 * density).toInt()
                setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)
            }
        }

        val searchView = androidx.appcompat.widget.SearchView(context).apply {
            id = searchViewId
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
            queryHint = getString(R.string.search) + "..."
            isIconified = false

            val searchPlate = findViewById<View>(androidx.appcompat.R.id.search_plate)
            searchPlate?.background = null

            val searchAutoComplete = findViewById<android.widget.EditText>(androidx.appcompat.R.id.search_src_text)
            searchAutoComplete?.apply {
                background = null
                setTextColor(ContextCompat.getColor(context, R.color.solid_white))
                setHintTextColor(ContextCompat.getColor(context, R.color.spinner_icon_and_inactive_elements))
                setPadding((8 * density).toInt(), paddingTop, paddingRight, paddingBottom)
            }

            val searchMagIcon = findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_mag_icon)
            searchMagIcon?.apply {
                val params = layoutParams as? android.widget.LinearLayout.LayoutParams
                params?.setMargins((12 * density).toInt(), 0, (4 * density).toInt(), 0)
                layoutParams = params
            }

            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    currentSearchQuery = query ?: ""
                    applyFilterAndSetAdapter()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    currentSearchQuery = newText ?: ""
                    applyFilterAndSetAdapter()
                    return true
                }
            })

            clearFocus()
        }

        cardView.addView(searchView)
        containerLayout.addView(originalView)
        containerLayout.addView(cardView)

        return containerLayout
    }

    override fun onActivityCreated(savedInstance: Bundle?) {
        super.onActivityCreated(savedInstance)
        initializeAdapter()
    }

    override fun onResume() {
        super.onResume()
        activeInstance = this
        initializeAdapter()
        (activity as AppCompatActivity?)?.supportActionBar?.setTitle(R.string.formula_editor_data)
        adapter?.notifyDataSetChanged()
        setShowEmptyView(shouldShowEmptyView())

        BottomBar.showBottomBar(activity)
        BottomBar.hidePlayButton(activity)
        BottomBar.hideAiAssistButton(activity)
    }

    override fun onPause() {
        super.onPause()
        if (activeInstance == this) {
            activeInstance = null
        }
        if (isObserverRegistered && adapter != null) {
            try {
                adapter?.unregisterAdapterDataObserver(observer)
            } catch (_: IllegalStateException) {
            }
            isObserverRegistered = false
        }
    }

    override fun onStop() {
        super.onStop()
        finishActionMode()
        BottomBar.hideBottomBar(activity)
    }

    private fun initializeAdapter() {
        arguments?.getSerializable(PARENT_SCRIPT_BRICK_BUNDLE_ARGUMENT)
            .let { parentScriptBrick = it as ScriptBrick? }

        val currentProject = ProjectManager.getInstance().currentProject
        val currentSprite = ProjectManager.getInstance().currentSprite

        originalUserDefinedBrickInputs = if (parentScriptBrick is UserDefinedReceiverBrick) {
            (parentScriptBrick as UserDefinedReceiverBrick).userDefinedBrick.userDefinedBrickInputs
        } else {
            emptyList()
        }
        originalGlobalVars = currentProject.userVariables
        originalLocalVars = currentSprite.userVariables
        originalMultiplayerVars = currentProject.multiplayerVariables
        originalGlobalLists = currentProject.userLists
        originalLocalLists = currentSprite.userLists

        indexAndSort()

        applyFilterAndSetAdapter()
    }

    private fun applyFilterAndSetAdapter() {
        val query = currentSearchQuery.trim()

        val filteredUserDefinedInputs = if (query.isEmpty()) {
            originalUserDefinedBrickInputs.toList()
        } else {
            originalUserDefinedBrickInputs.filter { it.name.contains(query, ignoreCase = true) }
        }

        val filteredMultiplayerVars = if (query.isEmpty()) {
            originalMultiplayerVars.toMutableList()
        } else {
            originalMultiplayerVars.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }

        val filteredGlobalVars = if (query.isEmpty()) {
            originalGlobalVars.toMutableList()
        } else {
            originalGlobalVars.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }

        val filteredLocalVars = if (query.isEmpty()) {
            originalLocalVars.toMutableList()
        } else {
            originalLocalVars.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }

        val filteredGlobalLists = if (query.isEmpty()) {
            originalGlobalLists.toMutableList()
        } else {
            originalGlobalLists.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }

        val filteredLocalLists = if (query.isEmpty()) {
            originalLocalLists.toMutableList()
        } else {
            originalLocalLists.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }

        if (isObserverRegistered && adapter != null) {
            try {
                adapter!!.unregisterAdapterDataObserver(observer)
            } catch (_: IllegalStateException) {
            }
            isObserverRegistered = false
        }

        adapter = DataListAdapter(
            filteredUserDefinedInputs, filteredMultiplayerVars, filteredGlobalVars,
            filteredLocalVars, filteredGlobalLists, filteredLocalLists
        )

        try {
            adapter!!.registerAdapterDataObserver(observer)
            isObserverRegistered = true
        } catch (_: IllegalStateException) {
        }

        recyclerView?.adapter = adapter
        adapter!!.setSelectionListener(this)
        adapter!!.setOnItemClickListener(this)

        setShowEmptyView(shouldShowEmptyView())
    }

    fun indexAndSort() {
        val currentProject = ProjectManager.getInstance().currentProject
        val currentSprite = ProjectManager.getInstance().currentSprite

        var userDefinedBrickInputs = listOf<UserDefinedBrickInput>()
        if (parentScriptBrick is UserDefinedReceiverBrick) {
            userDefinedBrickInputs =
                (parentScriptBrick as UserDefinedReceiverBrick).userDefinedBrick.userDefinedBrickInputs
        }

        val globalVars = currentProject.userVariables
        val localVars = currentSprite.userVariables
        val multiplayerVars = currentProject.multiplayerVariables
        val globalLists = currentProject.userLists
        val localLists = currentSprite.userLists

        indexVariable = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(INDEXING_VARIABLE_PREFERENCE_KEY, false)

        if (!indexVariable) {
            initialIndexing(userDefinedBrickInputs, globalVars, localVars, multiplayerVars,
                            globalLists, localLists)
            indexVariable = true
            PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(INDEXING_VARIABLE_PREFERENCE_KEY, indexVariable)
                .apply()
        }

        sortVariableAndList(userDefinedBrickInputs, globalVars, localVars, multiplayerVars,
                            globalLists, localLists)
        adapter?.notifyDataSetChanged()
    }

    @Suppress("LongParameterList")
    private fun sortVariableAndList(
        userDefinedBrickInputs: List<UserDefinedBrickInput>,
        globalVars: MutableList<UserVariable>,
        localVars: MutableList<UserVariable>,
        multiplayerVars: MutableList<UserVariable>,
        globalLists: MutableList<UserList>,
        localLists: MutableList<UserList>
    ) {
        sortData = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(SORT_VARIABLE_PREFERENCE_KEY, false)

        if (sortData) {
            Collections.sort(userDefinedBrickInputs) { item1:
            UserDefinedBrickInput, item2: UserDefinedBrickInput ->
                item1.name.compareTo(item2.name)
            }
        } else {
            Collections.sort(userDefinedBrickInputs) { item1:
            UserDefinedBrickInput, item2: UserDefinedBrickInput ->
                item1.initialIndex.compareTo(item2.initialIndex)
            }
        }

        sortUserVariable(multiplayerVars, sortData)
        sortUserVariable(globalVars, sortData)
        sortUserVariable(localVars, sortData)
        sortUserList(globalLists, sortData)
        sortUserList(localLists, sortData)
    }

    fun sortUserVariable(data: MutableList<UserVariable>, sorted: Boolean) {
        if (sorted) {
            data.sortWith(Comparator { item1: UserVariable, item2: UserVariable ->
                item1.name.compareTo(item2.name)
            })
        } else {
            data.sortWith(Comparator { item1: UserVariable, item2: UserVariable ->
                item1.initialIndex.compareTo(item2.initialIndex)
            })
        }
    }

    fun sortUserList(data: MutableList<UserList>, sorted: Boolean) {
        if (sorted) {
            data.sortWith(Comparator { item1: UserList, item2: UserList ->
                item1.name.compareTo(item2.name)
            })
        } else {
            data.sortWith(Comparator { item1: UserList, item2: UserList ->
                item1.initialIndex.compareTo(item2.initialIndex)
            })
        }
    }

    @Suppress("LongParameterList")
    private fun initialIndexing(
        userDefinedBrickInputs: List<UserDefinedBrickInput>,
        globalVars: MutableList<UserVariable>,
        localVars: MutableList<UserVariable>,
        multiplayerVars: MutableList<UserVariable>,
        globalLists: MutableList<UserList>,
        localLists: MutableList<UserList>
    ) {
        if (userDefinedBrickInputs.isNotEmpty()) {
            for ((counter, userDefinedBrickInput) in userDefinedBrickInputs.withIndex()) {
                if (userDefinedBrickInput.initialIndex == -1) {
                    userDefinedBrickInput.initialIndex = counter
                }
            }
        }
        setUserVariableIndex(globalVars)
        setUserVariableIndex(localVars)
        setUserVariableIndex(multiplayerVars)
        setUserListIndex(globalLists)
        setUserListIndex(localLists)
    }

    private fun setUserVariableIndex(data: MutableList<UserVariable>) {
        if (data.size > 0) {
            for ((counter, localList) in data.withIndex()) {
                if (localList.initialIndex == -1) {
                    localList.initialIndex = counter
                }
            }
        }
    }

    private fun setUserListIndex(data: MutableList<UserList>) {
        if (data.size > 0) {
            for ((counter, localList) in data.withIndex()) {
                if (localList.initialIndex == -1) {
                    localList.initialIndex = counter
                }
            }
        }
    }

    private fun onAdapterReady() {
        recyclerView?.adapter = adapter
        adapter?.setSelectionListener(this)
        adapter?.setOnItemClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_sort, menu)
    }

    fun notifyDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        for (index in 0 until menu.size()) {
            menu.getItem(index).isVisible = false
        }
        menu.findItem(R.id.delete)?.isVisible = true
        if (context != null) {
            sortData = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SORT_VARIABLE_PREFERENCE_KEY, false)
            menu.findItem(R.id.sort)
                .setTitle(if (sortData) R.string.undo_sort else R.string.sort)
                .isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> startActionMode(DELETE)
            R.id.sort -> {
                sortData = !sortData
                PreferenceManager.getDefaultSharedPreferences(activity)
                    .edit()
                    .putBoolean(SORT_VARIABLE_PREFERENCE_KEY, sortData)
                    .apply()
                indexAndSort()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startActionMode(@ActionModeType type: Int) {
        BottomBar.hideAddButton(activity)
        if (adapter?.items?.isEmpty() != false) {
            ToastUtil.showError(requireActivity(), R.string.am_empty_list)
        } else {
            actionModeType = type
            actionMode = requireActivity().startActionMode(this)
        }
    }

    private fun finishActionMode() {
        adapter?.clearSelection()
        if (actionModeType != NONE) {
            actionMode?.finish()
        }
    }

    private fun showDeleteAlert(selectedItems: List<UserData<*>>) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.deletion_alert_title)
            .setMessage(R.string.deletion_alert_text)
            .setPositiveButton(R.string.delete) { _: DialogInterface?, _: Int ->
                deleteItems(selectedItems)
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }

    private fun deleteItems(selectedItems: List<UserData<*>>) {
        finishActionMode()
        val currentProject = ProjectManager.getInstance().currentProject
        val currentSprite = ProjectManager.getInstance().currentSprite

        for (item in selectedItems) {
            adapter?.remove(item)
            if (item is UserVariable) {
                currentProject.userVariables.remove(item)
                currentSprite.userVariables.remove(item)
                currentProject.multiplayerVariables.remove(item)
            } else if (item is UserList) {
                currentProject.userLists.remove(item)
                currentSprite.userLists.remove(item)
            }
        }
        ProjectManager.getInstance().currentProject.deselectElements(selectedItems)
        ToastUtil.showSuccess(activity, resources.getQuantityString(R.plurals.deleted_Items,
            selectedItems.size, selectedItems.size))
        initializeAdapter()
    }

    private fun renameItem(item: UserData<*>, name: String?) {
        val previousName = item.name
        updateUserDataReferences(previousName, name, item)
        renameUserData(item, name ?: "")
        indexAndSort()
        finishActionMode()
        if (item is UserVariable) {
            formulaEditorDataInterface?.onVariableRenamed(previousName, name)
        } else {
            formulaEditorDataInterface?.onListRenamed(previousName, name)
        }
        initializeAdapter()
    }

    private fun editItem(item: UserData<*>, value: String?) {
        updateUserVariableValue(value, item)
        adapter?.updateDataSet()
        finishActionMode()
        initializeAdapter()
    }

    private fun showRenameDialog(selectedItems: List<UserData<*>>) {
        val item = selectedItems[0]
        val builder = TextInputDialog.Builder(requireContext())
        val items = adapter!!.items

        builder.setHint(getString(R.string.data_label))
            .setText(item.name)
            .setTextWatcher(DuplicateInputTextWatcher(items))
            .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, textInput: String? ->
                renameItem(item, textInput)
            }
        builder.setTitle(R.string.rename_data_dialog)
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditDialog(selectedItems: List<UserData<*>>) {
        val item = selectedItems[0]
        val builder = TextInputDialog.Builder(requireContext())

        builder.setHint(getString(R.string.data_value))
            .setText(item.value.toString())
            .setPositiveButton(getString(R.string.save)) { _: DialogInterface?, textInput: String? ->
                editItem(item, textInput)
            }
        builder.setTitle(getString(R.string.edit) + " '" + item.name + "'")
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onSelectionChanged(selectedItemCnt: Int) {
        if (actionModeType == DELETE) {
            actionMode?.title = getString(R.string.am_delete) + " " + selectedItemCnt
        }
    }

    override fun onItemClick(item: UserData<*>, selectionManager: MultiSelectionManager?) {
        if (actionModeType == NONE) {
            val formulaEditorFragment =
                fragmentManager?.findFragmentByTag(FormulaEditorFragment.FORMULA_EDITOR_FRAGMENT_TAG) as FormulaEditorFragment?
            formulaEditorFragment?.setChosenUserDataItem(item)
            fragmentManager?.popBackStack()
        }
    }

    override fun onItemLongClick(item: UserData<*>, holder: CheckableViewHolder) {
        onItemClick(item, null)
    }

    interface FormulaEditorDataInterface {
        fun onVariableRenamed(previousName: String?, newName: String?)

        fun onListRenamed(previousName: String?, newName: String?)
    }

    companion object {
        @JvmField
        val TAG: String = DataListFragment::class.java.simpleName
        const val PARENT_SCRIPT_BRICK_BUNDLE_ARGUMENT: String = "parent_script_brick"
        private const val NONE = 0
        private const val DELETE = 1

        @JvmStatic
        var activeInstance: DataListFragment? = null
            private set

        @JvmStatic
        fun refreshActiveInstance() {
            activeInstance?.initializeAdapter()
        }

        @JvmStatic
        fun updateUserDataReferences(oldName: String?, newName: String?, item: UserData<*>?) {
            ProjectManager.getInstance().currentProject.updateUserDataReferences(oldName, newName, item)
        }

        @JvmStatic
        fun updateUserVariableValue(value: String?, item: UserData<*>) {
            item.value = value
        }
    }

    override fun onSettingsClick(item: UserData<*>, view: View?) {
        if (item is UserDefinedBrickInput) {
            return
        }
        val itemList: MutableList<UserData<*>> = ArrayList()
        itemList.add(item)
        val hiddenOptionsMenu = mutableListOf<Int>(
            R.id.copy, R.id.show_details, R.id.from_library, R.id.from_local, R.id.new_group,
            R.id.new_scene, R.id.cast_button, R.id.backpack, R.id.project_options, R.id.project_files, R.id.project_libs
        )
        if (item is UserVariable) {
            val popupMenu = UiUtils.createSettingsPopUpMenu(view, requireContext(),
                                                            R.menu.menu_project_activity,
                                                            hiddenOptionsMenu.toIntArray())
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.rename -> showRenameDialog(ArrayList(listOf(item)))
                    R.id.delete -> showDeleteAlert(ArrayList(listOf(item)))
                    R.id.edit -> showEditDialog(ArrayList(listOf(item)))
                }
                true
            }
            popupMenu.show()
        } else {
            hiddenOptionsMenu.add(R.id.edit)
            val popupMenu = UiUtils.createSettingsPopUpMenu(view, requireContext(),
                                                            R.menu.menu_project_activity,
                                                            hiddenOptionsMenu.toIntArray())
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.rename -> showRenameDialog(ArrayList(listOf(item)))
                    R.id.delete -> showDeleteAlert(ArrayList(listOf(item)))
                }
                true
            }
            popupMenu.show()
        }
    }
}
