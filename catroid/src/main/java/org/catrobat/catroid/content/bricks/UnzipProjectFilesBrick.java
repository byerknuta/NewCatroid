package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class UnzipProjectFilesBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public UnzipProjectFilesBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_unzip_project_files_edit_archive_name);
    }

    public UnzipProjectFilesBrick(String archiveName) {
        this(new Formula(archiveName));
    }

    public UnzipProjectFilesBrick(Formula archiveName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, archiveName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_unzip_project_files;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createUnzipProjectFilesAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1)));
    }
}
