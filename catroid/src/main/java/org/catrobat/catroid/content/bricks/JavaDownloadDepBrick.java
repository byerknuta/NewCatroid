package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.UserVariable;

public class JavaDownloadDepBrick extends UserVariableBrickWithFormula {
    private static final long serialVersionUID = 1L;
    private boolean recursive = true;

    public JavaDownloadDepBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_java_download_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_java_download_repos_field);
    }

    public JavaDownloadDepBrick(Formula libraryId, Formula repositories, UserVariable resultVariable, boolean recursive) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, libraryId);
        setFormulaWithBrickField(BrickField.VALUE_2, repositories);
        this.userVariable = resultVariable;
        this.recursive = recursive;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_java_download_dep;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.java_download_log_spinner;
    }

    @Override
    public View getView(Context context) {
        super.getView(context);

        CheckBox cbRecursive = view.findViewById(R.id.brick_java_download_recursive_checkbox);
        if (cbRecursive != null) {
            cbRecursive.setChecked(recursive);
            cbRecursive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                recursive = isChecked;
            });
        }

        return view;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createDownloadDependencyAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2),
                        userVariable,
                        recursive));
    }
}
