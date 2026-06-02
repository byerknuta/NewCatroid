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
 * along with this program.  If not, see <http:
 */
package org.catrobat.catroid;

import static org.catrobat.catroid.common.FlavoredConstants.DEFAULT_ROOT_DIRECTORY;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danvexteam.lunoscript_annotations.LunoClass;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.mlsdk.common.MLApplication;

import org.catrobat.catroid.formulaeditor.CustomFormulaManager;
import org.catrobat.catroid.koin.CatroidKoinHelperKt;
import org.catrobat.catroid.plugins.PluginEventBus;
import org.catrobat.catroid.plugins.PluginExecutor;
import org.catrobat.catroid.plugins.PluginOverlayManager;
import org.catrobat.catroid.utils.FileMetaDataExtractor;
import org.catrobat.catroid.utils.ThemeEngine;
import org.catrobat.catroid.utils.Utils;

import java.io.File;
import java.util.Locale;

@LunoClass
public class CatroidApplication extends Application {

	private static final String TAG = CatroidApplication.class.getSimpleName();

	private static Context context;
	public static String defaultSystemLanguage;

	private static GoogleAnalytics googleAnalytics;
	private static Tracker googleTracker;

	public static boolean IS_SAFE_MODE = false;
	private boolean pluginsLoaded = false;

	public static CatroidApplication current;

	@TargetApi(30)
	@Override
	public void onCreate() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean forceSafeMode = prefs.getBoolean("force_safe_mode", false);

		if (forceSafeMode) {
			IS_SAFE_MODE = true;
			prefs.edit()
					.remove("force_safe_mode")
					.apply();
		}

		super.onCreate();
		Log.d(TAG, "CatroidApplication onCreate");
		Log.d(TAG, "git commit info: " + BuildConfig.GIT_COMMIT_INFO);

		if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectNonSdkApiUsage()
					.penaltyLog()
					.build());
		}

		context = getApplicationContext();

		CatroidKoinHelperKt.start(this, CatroidKoinHelperKt.getMyModules());

		Utils.fetchSpeechRecognitionSupportedLanguages(this);

		defaultSystemLanguage = Locale.getDefault().toLanguageTag();

		googleAnalytics = GoogleAnalytics.getInstance(this);
		googleAnalytics.setDryRun(BuildConfig.DEBUG);

		setupHuaweiMobileServices();
		CustomFormulaManager.INSTANCE.initialize();



		registerActivityLifecycleCallbacks(new OverlayLifecycleCallbacks());

		current = this;
	}

	public void loadPluginsIfNotLoaded() {
		if (pluginsLoaded) {
			return;
		}
		pluginsLoaded = true;


		if (IS_SAFE_MODE) {
			Log.w(TAG, "SAFE MODE is active. Skipping plugin loading.");

			new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
				Toast.makeText(this, "Safe Mode. Plugins disabled", Toast.LENGTH_LONG).show();
			});
			return;
		}

		Log.d(TAG, "Loading all enabled plugins now.");
		PluginExecutor.getInstance(this).loadAndRunAllEnabledPlugins();
	}

	private void setupHuaweiMobileServices() {
		if (AGConnectInstance.getInstance() == null) {
			AGConnectInstance.initialize(this);
		}

		String apiKey = AGConnectServicesConfig.fromContext(this).getString("client/api_key");
		MLApplication.getInstance().setApiKey(apiKey);
	}

	public synchronized Tracker getDefaultTracker() {
		if (googleTracker == null) {
			googleTracker = googleAnalytics.newTracker(R.xml.global_tracker);
		}

		return googleTracker;
	}

	public static Context getAppContext() {
		return CatroidApplication.context;
	}

	private void resetThemeSettings() {

		String prefsName = "live_theme_settings";


		String toolbarKey = "theme_toolbar_background";
		String backgroundKey = "theme_primary_background";

		Log.d("ThemeReset", "Сброс настроек темы...");

		SharedPreferences themePrefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = themePrefs.edit();


		editor.remove(toolbarKey);
		editor.remove(backgroundKey);


		editor.apply();

		Log.d("ThemeReset", "Настройки темы успешно сброшены.");
	}

	private static class OverlayLifecycleCallbacks implements ActivityLifecycleCallbacks {

		@Override
		public void onActivityResumed(@NonNull Activity activity) {
			PluginOverlayManager.getInstance().attach(activity);
			String activityName = activity.getClass().getSimpleName();
			PluginEventBus.getInstance().dispatch("Activity.onShow", activityName);

			if (!IS_SAFE_MODE) {
				ThemeEngine.applyTheme(activity);
			}
		}

		@Override
		public void onActivityPaused(@NonNull Activity activity) {
			String activityName = activity.getClass().getSimpleName();
			PluginEventBus.getInstance().dispatch("Activity.onHide", activityName);

			PluginOverlayManager.getInstance().detach(activity);
		}



		@Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
		@Override public void onActivityStarted(@NonNull Activity activity) {}
		@Override public void onActivityStopped(@NonNull Activity activity) {}
		@Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
		@Override public void onActivityDestroyed(@NonNull Activity activity) {}
	}
}
