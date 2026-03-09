package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class MLLoadBrick extends FormulaBrick {
    public MLLoadBrick() {
        addAllowedBrickField(BrickField.STRING, R.id.brick_ml_edit_filename);
    }
    public MLLoadBrick(String fileName) { this(new Formula(fileName)); }
    public MLLoadBrick(Formula fileName) {
        this();
        setFormulaWithBrickField(BrickField.STRING, fileName);
    }
    @Override public int getViewResource() { return R.layout.brick_ml_file_load; }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory().createMLFileAction(sprite, sequence, getFormulaWithBrickField(BrickField.STRING), false));
    }
}