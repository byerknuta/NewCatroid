package org.catrobat.catroid.ui.dialogs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.stage.StageActivity;
import org.catrobat.catroid.ui.recyclerview.fragment.ScriptFragment;

import java.util.ArrayList;
import java.util.List;

public class RuntimeConsoleActivity extends AppCompatActivity {
    private Sprite originalSprite;
    private Sprite sandboxSprite;
    private Spinner spriteSpinner;
    private List<Sprite> availableSprites;
    private ScriptFragment scriptFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int orientation = getIntent().getIntExtra("orientation", android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setRequestedOrientation(orientation);

        overridePendingTransition(0, 0);

        super.onCreate(savedInstanceState);

        float density = getResources().getDisplayMetrics().density;

        originalSprite = ProjectManager.getInstance().getCurrentSprite();
        sandboxSprite = new Sprite("CONSOLE_SANDBOX");
        sandboxSprite.addScript(new StartScript());
        ProjectManager.getInstance().setCurrentSprite(sandboxSprite);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.parseColor("#04223F"));

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(Color.parseColor("#002B4D"));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Sandbox Console");
        setSupportActionBar(toolbar);

        if (StageActivity.getActiveStageListener() != null) {
            availableSprites = StageActivity.getActiveStageListener().getSpritesFromStage();
        } else {
            availableSprites = new ArrayList<>();
        }
        List<String> spriteNames = new ArrayList<>();
        for (Sprite s : availableSprites) {
            spriteNames.add(s.getName());
        }

        spriteSpinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spriteNames);
        spriteSpinner.setAdapter(spinnerAdapter);

        Toolbar.LayoutParams spinnerParams = new Toolbar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.gravity = Gravity.END;
        spinnerParams.setMarginEnd((int) (16 * density));
        toolbar.addView(spriteSpinner, spinnerParams);

        FrameLayout fragmentContainer = new FrameLayout(this);
        fragmentContainer.setId(R.id.fragment_container);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        LinearLayout mainContent = new LinearLayout(this);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        mainContent.addView(toolbar);
        mainContent.addView(fragmentContainer, containerParams);

        rootLayout.addView(mainContent);

        RelativeLayout fabContainer = new RelativeLayout(this);
        fabContainer.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        int fabSizePx = (int) (65 * density);
        int marginPx = (int) (12 * density);

        FloatingActionButton addFab = new FloatingActionButton(this);
        addFab.setId(R.id.button_add);
        addFab.setCustomSize(fabSizePx);
        addFab.setCompatElevation(6 * density);
        addFab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_background));
        addFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_plus));
        addFab.setSupportImageTintList(ContextCompat.getColorStateList(this, R.color.solid_white));

        RelativeLayout.LayoutParams addParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        addParams.setMargins(marginPx, marginPx, marginPx, marginPx);
        addFab.setLayoutParams(addParams);
        addFab.setOnClickListener(v -> {
            if (scriptFragment != null) {
                scriptFragment.handleAddButton();
            }
        });

        FloatingActionButton playFab = new FloatingActionButton(this);
        playFab.setId(R.id.button_play);
        playFab.setCustomSize(fabSizePx);
        playFab.setCompatElevation(6 * density);
        playFab.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.button_background));
        playFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play));
        playFab.setSupportImageTintList(ContextCompat.getColorStateList(this, R.color.solid_white));

        RelativeLayout.LayoutParams playParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        playParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        playParams.addRule(RelativeLayout.ABOVE, addFab.getId());
        playParams.setMargins(marginPx, marginPx, marginPx, 0);
        playFab.setLayoutParams(playParams);
        playFab.setOnClickListener(v -> executeScript());

        fabContainer.addView(addFab);
        fabContainer.addView(playFab);

        rootLayout.addView(fabContainer);
        setContentView(rootLayout);

        scriptFragment = new ScriptFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, scriptFragment, ScriptFragment.TAG)
                .commit();
    }

    private void executeScript() {
        if (availableSprites.isEmpty()) {
            Toast.makeText(this, "No sprites on stage!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = spriteSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= availableSprites.size()) return;

        Sprite targetSprite = availableSprites.get(selectedIndex);

        boolean executedSomething = false;
        for (Script script : sandboxSprite.getScriptList()) {
            if (script.getBrickList() != null && !script.getBrickList().isEmpty()) {
                if (StageActivity.getActiveStageListener() != null) {
                    StageActivity.getActiveStageListener().executeConsoleScript(targetSprite, script);
                    executedSomething = true;
                }
            }
        }

        if (!executedSomething) {
            Toast.makeText(this, "Add some blocks first!", Toast.LENGTH_SHORT).show();
            return;
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalSprite != null) {
            ProjectManager.getInstance().setCurrentSprite(originalSprite);
        }
    }
}
