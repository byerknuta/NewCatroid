package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class Export3dObjectToGlbBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public Export3dObjectToGlbBrick() {
        addAllowedBrickField(BrickField.VALUE_1, R.id.brick_export_3d_glb_id_field);
        addAllowedBrickField(BrickField.VALUE_2, R.id.brick_export_3d_glb_file_field);
    }

    public Export3dObjectToGlbBrick(String objectId, String destFileName) {
        this(new Formula(objectId), new Formula(destFileName));
    }

    public Export3dObjectToGlbBrick(Formula objectId, Formula destFileName) {
        this();
        setFormulaWithBrickField(BrickField.VALUE_1, objectId);
        setFormulaWithBrickField(BrickField.VALUE_2, destFileName);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_export_3d_glb;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createExport3dObjectToGlbAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.VALUE_1),
                        getFormulaWithBrickField(BrickField.VALUE_2)));
    }
}
