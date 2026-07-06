package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class ZipProjectFilesBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public ZipProjectFilesBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_zip_project_files_edit_files);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_zip_project_files_edit_archive_name);
    }

    public ZipProjectFilesBrick(String files, String archiveName) {
        this(new Formula(files), new Formula(archiveName));
    }

    public ZipProjectFilesBrick(Formula files, Formula archiveName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, files);
        setFormulaWithBrickField(BrickField.VALUE_2, archiveName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_zip_project_files;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createZipProjectFilesAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
