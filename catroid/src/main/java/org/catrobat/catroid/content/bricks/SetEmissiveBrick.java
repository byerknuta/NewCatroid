package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

public class SetEmissiveBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    public SetEmissiveBrick() {
        addAllowedBrickField(BrickField.OBJECT_NAME, R.id.brick_set_emissive_object);
        addAllowedBrickField(BrickField.RED, R.id.brick_set_emissive_r);
        addAllowedBrickField(BrickField.GREEN, R.id.brick_set_emissive_g);
        addAllowedBrickField(BrickField.BLUE, R.id.brick_set_emissive_b);
        addAllowedBrickField(BrickField.ALPHA, R.id.brick_set_emissive_a);
        addAllowedBrickField(BrickField.INTENSITY, R.id.brick_set_emissive_intensity);
        addAllowedBrickField(BrickField.MATERIAL_TEXTURE_COLOR, R.id.brick_set_emissive_texture);
    }

    public SetEmissiveBrick(String objectName, float r, float g, float b, float a, float intensity, String texturePath) {
        this(new Formula(objectName), new Formula(r), new Formula(g), new Formula(b), new Formula(a), new Formula(intensity), new Formula(texturePath));
    }

    public SetEmissiveBrick(Formula objectName, Formula r, Formula g, Formula b, Formula a, Formula intensity, Formula texturePath) {
        this();
        setFormulaWithBrickField(BrickField.OBJECT_NAME, objectName);
        setFormulaWithBrickField(BrickField.RED, r);
        setFormulaWithBrickField(BrickField.GREEN, g);
        setFormulaWithBrickField(BrickField.BLUE, b);
        setFormulaWithBrickField(BrickField.ALPHA, a);
        setFormulaWithBrickField(BrickField.INTENSITY, intensity);
        setFormulaWithBrickField(BrickField.MATERIAL_TEXTURE_COLOR, texturePath);
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_set_emissive;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        sequence.addAction(sprite.getActionFactory()
                .createSetEmissiveAction(sprite, sequence,
                        getFormulaWithBrickField(BrickField.OBJECT_NAME),
                        getFormulaWithBrickField(BrickField.RED),
                        getFormulaWithBrickField(BrickField.GREEN),
                        getFormulaWithBrickField(BrickField.BLUE),
                        getFormulaWithBrickField(BrickField.ALPHA),
                        getFormulaWithBrickField(BrickField.INTENSITY),
                        getFormulaWithBrickField(BrickField.MATERIAL_TEXTURE_COLOR)));
    }
}
