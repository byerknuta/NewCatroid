package org.catrobat.catroid.editor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.raptor.GameObject;
import org.catrobat.catroid.raptor.SceneData;
import org.catrobat.catroid.raptor.SceneManager;
import org.catrobat.catroid.raptor.ThreeDManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EditorActivity extends AppCompatActivity implements AndroidFragmentApplication.Callbacks, HierarchyAdapter.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private SceneManager sceneManager;
    private InspectorManager inspectorManager;
    private ListView hierarchyListView;
    private RecyclerView hierarchyRecyclerView;
    private HierarchyAdapter hierarchyAdapter;
    private final List<HierarchyAdapter.HierarchyItem> hierarchyItems = new ArrayList<>();
    private final List<GameObject> hierarchyObjects = new ArrayList<>();

    public EditorListener editorListener;
    private Gizmo gizmo;

    private ItemTouchHelper touchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_activity);

        setupToolbarAndDrawer();

        if (savedInstanceState == null) {
            EditorFragment fragment = new EditorFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();
            this.editorListener = fragment.getListener();
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public void onEditorReady(SceneManager manager, ThreeDManager TDmanager) {
        this.sceneManager = manager;
        this.threeDManager = TDmanager;
        this.inspectorManager = new InspectorManager(this, sceneManager, threeDManager);

        EditorFragment fragment = (EditorFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            this.editorListener = fragment.getListener();
            this.gizmo = editorListener.getGizmo();
            if (this.gizmo != null) {
                this.inspectorManager.setGizmo(this.gizmo);
            }
        }

        runOnUiThread(this::setupUI);
    }

    public void onObjectSelected(GameObject go) {
        runOnUiThread(() -> {
            if (inspectorManager != null) {
                inspectorManager.populateInspector(go);
            }
            if (go != null && drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END);
            }

            if (hierarchyAdapter != null) {
                hierarchyAdapter.setSelectedObject(go);
            }
            if (editorListener.getGizmo() != null) {
                editorListener.getGizmo().setSelectedObject(go);
            }
        });
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                final View mainContent = findViewById(R.id.main_content_coordinator);
                final View leftDrawer = findViewById(R.id.left_drawer);

                if (mainContent != null && drawerView != null) {
                    if (drawerView.getId() == leftDrawer.getId()) {
                        mainContent.setTranslationX(drawerView.getWidth() * slideOffset);
                    } else {
                        mainContent.setTranslationX(-drawerView.getWidth() * slideOffset);
                    }
                }
            }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerView.requestLayout();
            }
            @Override public void onDrawerClosed(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
        });

        toggle.syncState();
    }

    private void setupUI() {
        hierarchyRecyclerView = findViewById(R.id.hierarchy_recyclerview);
        DraggableLinearLayoutManager layoutManager = new DraggableLinearLayoutManager(this);
        hierarchyRecyclerView.setLayoutManager(layoutManager);
        hierarchyAdapter = new HierarchyAdapter(this);
        hierarchyRecyclerView.setAdapter(hierarchyAdapter);


        ItemTouchHelper.Callback callback = new HierarchyDragCallback(
                hierarchyAdapter, sceneManager, this, layoutManager);

        this.touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(hierarchyRecyclerView);


        hierarchyRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(hierarchyRecyclerView.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public void onLongPress(MotionEvent e) {

                            View child = hierarchyRecyclerView.findChildViewUnder(e.getX(), e.getY());
                            if (child != null) {
                                RecyclerView.ViewHolder vh = hierarchyRecyclerView.getChildViewHolder(child);
                                if (vh != null) {


                                    drawerLayout.requestDisallowInterceptTouchEvent(true);

                                    touchHelper.startDrag(vh);
                                }
                            }
                        }
                    });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                gestureDetector.onTouchEvent(e);


                if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                    drawerLayout.requestDisallowInterceptTouchEvent(false);
                }

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        findViewById(R.id.btn_add_empty).setOnClickListener(v -> {
            GameObject newGo = sceneManager.createGameObject("Empty");
            updateAndSelect(newGo);
        });

        findViewById(R.id.btn_add_cube).setOnClickListener(v -> {
            GameObject newGo = sceneManager.createPrimitive("cube");
            if (newGo != null) {
                updateAndSelect(newGo);
            }
        });

        findViewById(R.id.btn_add_sphere).setOnClickListener(v -> {
            GameObject newGo = sceneManager.createPrimitive("sphere");
            if (newGo != null) {
                updateAndSelect(newGo);
            }
        });

        updateHierarchy();

        setupCameraButton(R.id.btn_cam_w, 0, 0, 1);
        setupCameraButton(R.id.btn_cam_s, 0, 0, -1);
        setupCameraButton(R.id.btn_cam_a, -1, 0, 0);
        setupCameraButton(R.id.btn_cam_d, 1, 0, 0);
        setupCameraButton(R.id.btn_cam_q, 0, -1, 0);
        setupCameraButton(R.id.btn_cam_e, 0, 1, 0);

        Button shiftButton = findViewById(R.id.btn_cam_shift);
        shiftButton.setOnTouchListener((v, event) -> {
            if (editorListener == null) return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editorListener.onCameraAccelerate(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                editorListener.onCameraAccelerate(false);
            }
            return true;
        });
    }

    @Override
    public void onItemClick(GameObject gameObject) {
        gizmo.setSelectedObject(gameObject);
        onObjectSelected(gameObject);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setupCameraButton(int buttonId, float vx, float vy, float vz) {
        Button button = findViewById(buttonId);
        button.setOnTouchListener((v, event) -> {
            if (editorListener == null) return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editorListener.onCameraMove(vx, vy, vz);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                editorListener.onCameraMove(-vx, -vy, -vz);
            }
            return true;
        });
    }

    private void updateAndSelect(GameObject go) {
        updateHierarchy();
        onObjectSelected(go);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public InspectorManager getInspectorManager() {
        return inspectorManager;
    }

    public void updateHierarchy() {
        if (sceneManager == null || hierarchyAdapter == null) return;

        hierarchyItems.clear();


        List<GameObject> rootObjects = new ArrayList<>();
        for (GameObject go : sceneManager.getAllGameObjects().values()) {
            if (go.parentId == null) {
                rootObjects.add(go);
            }
        }
        rootObjects.sort(Comparator.comparing(go -> go.name.toLowerCase()));


        for (GameObject root : rootObjects) {
            addGameObjectToHierarchyList(root, 0);
        }


        hierarchyAdapter.updateData(hierarchyItems);
    }

    private void addGameObjectToHierarchyList(GameObject go, int depth) {
        String prefix = String.join("", Collections.nCopies(depth, "    "));
        String displayName = prefix + go.name;

        hierarchyItems.add(new HierarchyAdapter.HierarchyItem(go, displayName));

        if (!go.childrenIds.isEmpty()) {
            List<GameObject> children = new ArrayList<>();
            for (String childId : go.childrenIds) {
                GameObject child = sceneManager.findGameObject(childId);
                if (child != null) children.add(child);
            }
            children.sort(Comparator.comparing(child -> child.name.toLowerCase()));

            for (GameObject child : children) {
                addGameObjectToHierarchyList(child, depth + 1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_tools_menu, menu);
        getMenuInflater().inflate(R.menu.editor_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (editorListener != null) {
            if (id == R.id.tool_hand) {
                editorListener.setCurrentTool(EditorTool.HAND);
                return true;
            } else if (id == R.id.tool_translate) {
                editorListener.setCurrentTool(EditorTool.TRANSLATE);
                return true;
            } else if (id == R.id.tool_rotate) {
                editorListener.setCurrentTool(EditorTool.ROTATE);
                return true;
            } else if (id == R.id.tool_scale) {
                editorListener.setCurrentTool(EditorTool.SCALE);
                return true;
            }
        }
        if (id == R.id.action_save_scene) {
            showSaveSceneDialog();
            return true;
        } else if (id == R.id.action_load_scene) {
            showLoadSceneDialog();
            return true;
        } else if (id == R.id.action_scene_settings) {
            showSceneSettingsDialog();
            return true;
        } else if (id == R.id.action_clear_scene) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear scene?")
                    .setMessage("The scene will be reset, and you may lose your changes. Continue?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        EditorStateManager.clearCache();
                        if (editorListener != null) {
                            editorListener.resetEngine();
                        }
                        Toast.makeText(this, "Scene Cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        } else if (id == R.id.action_exit) {
            showExitConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Editor")
                .setMessage("Do you want to save your changes before exiting?")
                .setPositiveButton("Save & Exit", (dialog, which) -> {
                    showSaveSceneDialog(this::finish);
                })
                .setNeutralButton("Exit without Saving", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void cacheCurrentScene() {
        if (sceneManager != null) {
            SceneData currentSceneData = sceneManager.getCurrentSceneData();
            Json json = sceneManager.getJson();
            String sceneJson = json.toJson(currentSceneData);
            EditorStateManager.cacheScene(sceneJson);
            Gdx.app.log("EditorActivity", "Scene JSON cached.");
        }
    }

    private void showSaveSceneDialog() {
        showSaveSceneDialog(null);
    }

    private void showSaveSceneDialog(Runnable onSaveComplete) {
        final EditText input = new EditText(this);
        input.setHint("my_level_1");

        new AlertDialog.Builder(this)
                .setTitle("Save Scene As...")
                .setMessage("Enter a file name (without extension):")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String fileName = input.getText().toString();
                    if (fileName.isEmpty()) {
                        Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String fileNameWithExt = fileName + ".rscene";

                    File projectFile = new File(ProjectManager.getInstance().getCurrentProject().getFilesDir(), fileNameWithExt);
                    FileHandle fileHandle = Gdx.files.absolute(projectFile.getAbsolutePath());

                    sceneManager.saveScene(fileHandle);
                    Toast.makeText(this, "Scene saved to " + fileNameWithExt, Toast.LENGTH_SHORT).show();

                    if (onSaveComplete != null) {
                        onSaveComplete.run();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showLoadSceneDialog() {
        File projectFilesDir = ProjectManager.getInstance().getCurrentProject().getFilesDir();
        File[] allFiles = projectFilesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".rscene"));

        if (allFiles == null || allFiles.length == 0) {
            Toast.makeText(this, "No saved scenes (.rscene) found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] sceneNames = new String[allFiles.length];
        for(int i = 0; i < allFiles.length; i++) {
            sceneNames[i] = allFiles[i].getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Load Scene")
                .setItems(sceneNames, (dialog, which) -> {
                    File selectedFile = allFiles[which];
                    FileHandle fileHandle = Gdx.files.absolute(selectedFile.getAbsolutePath());

                    EditorFragment fragment = (EditorFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (fragment != null && fragment.getListener() != null) {
                        ThreeDManager.SceneSettings settings = new ThreeDManager.SceneSettings();
                        try {
                            String sceneJson = fileHandle.readString();
                            Json json = new Json();
                            SceneData sceneData = json.fromJson(SceneData.class, sceneJson);
                            if (sceneData != null && sceneData.renderSettings != null) {
                                settings = sceneData.renderSettings;
                            }
                        } catch (Exception e) {
                            Gdx.app.error("EditorActivity", "Could not parse scene settings, using defaults.", e);
                        }

                        fragment.getListener().resetEngine(fileHandle, settings);

                        updateHierarchy();
                        onObjectSelected(null);
                        Toast.makeText(this, "Loading scene: " + selectedFile.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void onEngineReset(SceneManager manager, ThreeDManager engine) {
        this.sceneManager = manager;
        this.threeDManager = engine;
        this.inspectorManager = new InspectorManager(this, sceneManager, threeDManager);

        EditorFragment fragment = (EditorFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment.getListener() != null) {
            this.gizmo = fragment.getListener().getGizmo();
            if (this.gizmo != null) {
                this.inspectorManager.setGizmo(this.gizmo);
            }
        }

        runOnUiThread(this::setupUI);
    }

    private int libGdxColorToAndroidColor(com.badlogic.gdx.graphics.Color gdxColor) {
        int r = (int)(gdxColor.r * 255);
        int g = (int)(gdxColor.g * 255);
        int b = (int)(gdxColor.b * 255);
        int a = (int)(gdxColor.a * 255);
        return android.graphics.Color.argb(a, r, g, b);
    }

    private void showSceneSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scene_settings, null);
        Button colorButton = dialogView.findViewById(R.id.btn_sky_color);
        SeekBar ambientSeekBar = dialogView.findViewById(R.id.seekbar_ambient_light);
        TextView ambientValueText = dialogView.findViewById(R.id.text_ambient_value);
        TextView skyboxPathText = dialogView.findViewById(R.id.text_skybox_path);
        Button selectSkyboxButton = dialogView.findViewById(R.id.btn_select_skybox);
        ImageButton clearSkyboxButton = dialogView.findViewById(R.id.btn_clear_skybox);

        EditText shadowDistEdit = dialogView.findViewById(R.id.edit_shadow_distance);
        android.widget.Spinner shadowResSpinner = dialogView.findViewById(R.id.spinner_shadow_resolution);
        Button applyShadowsBtn = dialogView.findViewById(R.id.btn_apply_shadows);


        String[] resolutions = {"128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, resolutions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shadowResSpinner.setAdapter(adapter);


        if (threeDManager != null) {
            shadowDistEdit.setText(String.valueOf(threeDManager.getShadowSize()));

            String currentRes = String.valueOf((int) threeDManager.getShadowResolution());
            for (int i = 0; i < resolutions.length; i++) {
                if (resolutions[i].equals(currentRes)) {
                    shadowResSpinner.setSelection(i);
                    break;
                }
            }
        }

        applyShadowsBtn.setOnClickListener(v -> {
            try {
                float size = Float.parseFloat(shadowDistEdit.getText().toString());
                int res = Integer.parseInt(shadowResSpinner.getSelectedItem().toString());


                if (threeDManager != null) {
                    threeDManager.setShadowSettings(size, res);
                    Toast.makeText(this, "Shadow settings applied.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid shadow values.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });


        float currentIntensity = sceneManager.ambientIntensity;
        com.badlogic.gdx.graphics.Color skyColor = new Color(sceneManager.skyR, sceneManager.skyG, sceneManager.skyB, 1f);
        int initialAndroidColor = libGdxColorToAndroidColor(skyColor);

        colorButton.setBackgroundColor(initialAndroidColor);

        skyboxPathText.setText(sceneManager.skyboxPath != null && !sceneManager.skyboxPath.isEmpty() ? sceneManager.skyboxPath : "None");
        clearSkyboxButton.setVisibility(sceneManager.skyboxPath != null && !sceneManager.skyboxPath.isEmpty() ? View.VISIBLE : View.GONE);

        selectSkyboxButton.setOnClickListener(v -> showSkyboxPicker(skyboxPathText, clearSkyboxButton));
        clearSkyboxButton.setOnClickListener(v -> {
            sceneManager.setSkybox(null);
            skyboxPathText.setText("None");
            clearSkyboxButton.setVisibility(View.GONE);
        });

        ambientSeekBar.setProgress((int)(currentIntensity * 100));
        ambientValueText.setText(String.format("%.2f", currentIntensity));

        ambientSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float intensity = progress / 100.0f;
                ambientValueText.setText(String.format("%.2f", intensity));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float intensity = seekBar.getProgress() / 100.0f;
                Gdx.app.postRunnable(() -> sceneManager.setBackgroundLightIntensity(intensity));
            }
        });

        colorButton.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Choose Sky Color")
                    .initialColor(initialAndroidColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("OK", (dialog, selectedColor, allColors) -> {
                        colorButton.setBackgroundColor(selectedColor);

                        int a = android.graphics.Color.alpha(selectedColor);
                        int r = android.graphics.Color.red(selectedColor);
                        int g = android.graphics.Color.green(selectedColor);
                        int b = android.graphics.Color.blue(selectedColor);

                        final float libgdx_r = r / 255f;
                        final float libgdx_g = g / 255f;
                        final float libgdx_b = b / 255f;

                        Gdx.app.postRunnable(() -> sceneManager.setSkyColor(libgdx_r, libgdx_g, libgdx_b));})
                    .setNegativeButton("Cancel", null)
                    .build()
                    .show();
        });

        EditText pointLightsEditor = dialogView.findViewById(R.id.edit_point_lights);
        EditText spotLightsEditor = dialogView.findViewById(R.id.edit_spot_lights);
        EditText dirLightsEditor = dialogView.findViewById(R.id.edit_dir_lights);
        EditText bonesEditor = dialogView.findViewById(R.id.edit_bones);
        Button applyButton = dialogView.findViewById(R.id.btn_apply_performance);


        final ThreeDManager.SceneSettings currentSettings = threeDManager.getSceneSettings();

        pointLightsEditor.setText(String.valueOf(currentSettings.numPointLights));
        spotLightsEditor.setText(String.valueOf(currentSettings.numSpotLights));
        dirLightsEditor.setText(String.valueOf(currentSettings.numDirectionalLights));
        bonesEditor.setText(String.valueOf(currentSettings.numBones));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Scene Settings")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();

        applyButton.setOnClickListener(v -> {
            try {
                ThreeDManager.SceneSettings newSettings = new ThreeDManager.SceneSettings();
                newSettings.numPointLights = Integer.parseInt(pointLightsEditor.getText().toString());
                newSettings.numSpotLights = Integer.parseInt(spotLightsEditor.getText().toString());
                newSettings.numDirectionalLights = Integer.parseInt(dirLightsEditor.getText().toString());
                newSettings.numBones = Math.min(110, Integer.parseInt(bonesEditor.getText().toString()));

                new AlertDialog.Builder(this)
                        .setTitle("Restart 3D Engine")
                        .setMessage("Applying these settings requires reloading the 3D editor. Unsaved changes might be lost. Continue?")
                        .setPositiveButton("Restart", (d, which) -> {
                            if (editorListener != null) {
                                editorListener.resetEngine(null, newSettings);
                                Toast.makeText(this, "3D Engine restarted with new settings.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSkyboxPicker(TextView pathTextView, View clearButton) {
        File projectFilesDir = ProjectManager.getInstance().getCurrentProject().getFilesDir();
        File[] allFiles = projectFilesDir.listFiles();
        if (allFiles == null) {
            Toast.makeText(this, "Cannot access project files.", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<String> textureFiles = new ArrayList<>();
        for (File file : allFiles) {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".hdr")) {
                textureFiles.add(file.getName());
            }
        }

        if (textureFiles.isEmpty()) {
            Toast.makeText(this, "No suitable image files found in project.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Skybox Texture")
                .setItems(textureFiles.toArray(new String[0]), (dialog, which) -> {
                    String selectedFile = textureFiles.get(which);
                    sceneManager.skyboxPath = selectedFile;
                    sceneManager.setSkybox(selectedFile);
                    pathTextView.setText(selectedFile);
                    clearButton.setVisibility(View.VISIBLE);
                })
                .show();
    }

    private ThreeDManager threeDManager;

    @Override
    protected void onPause() {
        cacheCurrentScene();
        super.onPause();
    }

    @Override
    public void exit() {}
}