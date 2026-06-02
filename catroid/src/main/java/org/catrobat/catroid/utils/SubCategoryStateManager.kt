package org.catrobat.catroid.utils

import android.content.Context

object SubCategoryStateManager {
    private const val PREFS_NAME = "subcategory_states_prefs"

    fun isExpanded(context: Context, title: String, default: Boolean = true): Boolean {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(title, default)
    }

    fun setExpanded(context: Context, title: String, expanded: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(title, expanded).apply()
    }
}
