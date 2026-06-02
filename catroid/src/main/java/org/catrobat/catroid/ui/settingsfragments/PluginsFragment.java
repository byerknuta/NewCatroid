package org.catrobat.catroid.ui.settingsfragments;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import org.catrobat.catroid.R;
import org.catrobat.catroid.plugins.PluginInfo;
import org.catrobat.catroid.plugins.PluginManager;
import java.util.List;

public class PluginsFragment extends PreferenceFragmentCompat {

    public static final String TAG = PluginsFragment.class.getSimpleName();
    private PluginManager pluginManager;
    private PreferenceCategory installedPluginsCategory;

    
    private ActivityResultLauncher<Intent> importPluginLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        importPluginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            installPlugin(data.getData());
                        }
                    }
                });
    }

    
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.plugins_preferences, rootKey);

        pluginManager = PluginManager.getInstance(getActivity());

        installedPluginsCategory = findPreference("plugins_installed_category");

        Preference importButton = findPreference("plugin_import_button");
        if (importButton != null) {
            importButton.setOnPreferenceClickListener(preference -> {
                openFilePicker();
                return true;
            });
        }

        populatePluginList();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importPluginLauncher.launch(intent);
    }

    private void installPlugin(Uri uri) {
        boolean success = pluginManager.installPluginFromUri(uri);
        if (success) {
            Toast.makeText(getActivity(), "Плагин успешно установлен! (не забудьте перезапустить приложение)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Ошибка установки. Убедитесь, что вы выбрали корректный .nplug файл и он не поврежден.", Toast.LENGTH_LONG).show();
        }
        populatePluginList();
    }

    private void populatePluginList() {
        
        installedPluginsCategory.removeAll();
        List<PluginInfo> plugins = pluginManager.getInstalledPlugins();

        if (plugins.isEmpty()) {
            Preference emptyPref = new Preference(getContext());
            emptyPref.setTitle("Нет установленных плагинов");
            emptyPref.setEnabled(false);
            installedPluginsCategory.addPreference(emptyPref);
            return;
        }

        for (final PluginInfo plugin : plugins) {
            Preference pluginPref = new Preference(getContext()); 
            pluginPref.setKey(plugin.packageName);
            pluginPref.setSummary(plugin.description);

            String status = plugin.isEnabled ? "[ВКЛ]" : "[ВЫКЛ]";
            pluginPref.setTitle(status + " " + plugin.name + " (v" + plugin.version + ")");

            pluginPref.setOnPreferenceClickListener(preference -> {
                showPluginActionsDialog(plugin);
                return true;
            });
            installedPluginsCategory.addPreference(pluginPref);
        }
    }

    private void showPluginActionsDialog(final PluginInfo plugin) {
        final String enableDisableAction = plugin.isEnabled ? "Отключить" : "Включить";

        
        final CharSequence[] items;
        if (plugin.hasSettings()) {
            items = new CharSequence[]{enableDisableAction, "Удалить", "Настроить"};
        } else {
            items = new CharSequence[]{enableDisableAction, "Удалить"};
        }

        new AlertDialog.Builder(getActivity())
                .setTitle("Действия для: " + plugin.name)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) { 
                        pluginManager.setPluginEnabled(plugin.packageName, !plugin.isEnabled);
                        populatePluginList();
                    } else if (which == 1) { 
                        showDeleteConfirmationDialog(plugin);
                    } else if (which == 2) { 
                        openPluginSettings(plugin);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void openPluginSettings(PluginInfo plugin) {
        PluginSettingsFragment settingsFragment = new PluginSettingsFragment();

        
        Bundle args = new Bundle();
        args.putString("plugin_package_name", plugin.packageName);
        settingsFragment.setArguments(args);

        
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null) 
                .commit();
    }

    private void showDeleteConfirmationDialog(final PluginInfo plugin) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Удаление плагина")
                .setMessage("Вы уверены, что хотите навсегда удалить '" + plugin.name + "'?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    pluginManager.deletePlugin(plugin);
                    populatePluginList();
                    Toast.makeText(requireContext(), "Плагин удален! (не забудьте перезапустить приложение)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Отмена", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Плагины");
    }
}
