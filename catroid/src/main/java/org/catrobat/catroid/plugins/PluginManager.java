package org.catrobat.catroid.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PluginManager {
    private static final String TAG = PluginManager.class.getSimpleName();
    private static final String PLUGINS_DIR_NAME = "plugins";
    private static final String DISABLED_PLUGINS_KEY = "disabled_plugins_set";
    private static volatile PluginManager instance;

    private final Context context;
    private final File pluginsDir;

    private PluginManager(Context context) {
        this.context = context.getApplicationContext();
        this.pluginsDir = new File(this.context.getFilesDir(), PLUGINS_DIR_NAME);
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
    }

    public static PluginManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PluginManager.class) {
                if (instance == null) {
                    instance = new PluginManager(context);
                }
            }
        }
        return instance;
    }

    public boolean installPluginFromUri(Uri uri) {

        File tempDir = new File(context.getCacheDir(), "plugin_install_temp");
        if (tempDir.exists()) {
            deleteRecursive(tempDir);
        }
        tempDir.mkdirs();

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File newFile = new File(tempDir, entry.getName());


                if (!newFile.getCanonicalPath().startsWith(tempDir.getCanonicalPath() + File.separator)) {
                    throw new SecurityException("Zip Path Traversal Vulnerability");
                }

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {

                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to unzip plugin archive", e);
            deleteRecursive(tempDir);
            return false;
        }


        File manifestFile = new File(tempDir, "plugin.json");
        if (!manifestFile.exists()) {
            Log.e(TAG, "Install failed: plugin.json not found in the archive.");
            deleteRecursive(tempDir);
            return false;
        }

        try {
            String json = readFileToString(manifestFile);
            JSONObject manifest = new JSONObject(json);
            String packageName = manifest.getString("packageName");


            File destDir = new File(pluginsDir, packageName);
            if (destDir.exists()) {
                Log.e(TAG, "Install failed: Plugin with package name '" + packageName + "' already exists.");
                deleteRecursive(tempDir);
                return false;
            }


            if (!tempDir.renameTo(destDir)) {
                throw new IOException("Failed to move plugin directory.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Install failed: Invalid manifest or could not move plugin directory.", e);
            deleteRecursive(tempDir);
            return false;
        }

        Log.d(TAG, "Plugin installed successfully!");
        return true;
    }

    public List<PluginInfo> getInstalledPlugins() {
        List<PluginInfo> pluginInfos = new ArrayList<>();
        File[] pluginDirs = pluginsDir.listFiles();

        if (pluginDirs == null) {
            return pluginInfos;
        }

        Set<String> disabledPlugins = getDisabledPluginSet();

        for (File pluginDir : pluginDirs) {
            if (pluginDir.isDirectory()) {
                File manifestFile = new File(pluginDir, "plugin.json");
                if (manifestFile.exists()) {
                    try {
                        String json = readFileToString(manifestFile);
                        JSONObject manifest = new JSONObject(json);

                        PluginInfo info = new PluginInfo(
                                manifest.getString("packageName"),
                                manifest.getString("name"),
                                manifest.getString("version"),
                                manifest.getString("description"),
                                pluginDir
                        );
                        info.isEnabled = !disabledPlugins.contains(info.packageName);
                        pluginInfos.add(info);

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse manifest for plugin in " + pluginDir.getName(), e);
                    }
                }
            }
        }
        return pluginInfos;
    }

    public void setPluginEnabled(String packageName, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> disabledPlugins = new HashSet<>(getDisabledPluginSet());

        if (enabled) {
            disabledPlugins.remove(packageName);
        } else {
            disabledPlugins.add(packageName);
        }

        prefs.edit().putStringSet(DISABLED_PLUGINS_KEY, disabledPlugins).apply();
    }

    public boolean deletePlugin(PluginInfo pluginInfo) {
        setPluginEnabled(pluginInfo.packageName, true);
        return deleteRecursive(pluginInfo.pluginDirectory);
    }




    private Set<String> getDisabledPluginSet() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(DISABLED_PLUGINS_KEY, new HashSet<>());
    }

    private String readFileToString(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, StandardCharsets.UTF_8);
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    public PluginInfo getPluginByPackageName(String packageName) {
        for (PluginInfo plugin : getInstalledPlugins()) {
            if (plugin.packageName.equals(packageName)) {
                return plugin;
            }
        }
        return null;
    }
}
