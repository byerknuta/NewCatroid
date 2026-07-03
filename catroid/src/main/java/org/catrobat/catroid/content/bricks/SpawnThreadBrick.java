package org.catrobat.catroid.content.bricks;

import com.badlogic.gdx.scenes.scene2d.Action;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import java.util.ArrayList;
import java.util.List;

public class SpawnThreadBrick extends FormulaBrick implements CompositeBrick {

    private static final long serialVersionUID = 1L;
    private transient EndBrick endBrick = new EndBrick(this);
    private List<Brick> threadBricks = new ArrayList<>();

    public SpawnThreadBrick() {
        super();
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_spawn_thread_id_edit);
    }

    public SpawnThreadBrick(Formula threadId) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, threadId);
    }

    @Override
    public boolean hasSecondaryList() {
        return false;
    }

    @Override
    public List<Brick> getNestedBricks() {
        return threadBricks;
    }

    @Override
    public List<Brick> getSecondaryNestedBricks() {
        return null;
    }

    public boolean addBrick(Brick brick) {
        return threadBricks.add(brick);
    }

    @Override
    public void setCommentedOut(boolean commentedOut) {
        super.setCommentedOut(commentedOut);
        for (Brick brick : threadBricks) {
            brick.setCommentedOut(commentedOut);
        }
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        SpawnThreadBrick clone = (SpawnThreadBrick) super.clone();
        clone.endBrick = new EndBrick(clone);
        clone.threadBricks = new ArrayList<>();
        for (Brick brick : threadBricks) {
            clone.addBrick(brick.clone());
        }
        return clone;
    }

    @Override
    public boolean consistsOfMultipleParts() {
        return true;
    }

    @Override
    public List<Brick> getAllParts() {
        List<Brick> bricks = new ArrayList<>();
        bricks.add(this);
        bricks.add(endBrick);
        return bricks;
    }

    @Override
    public void addToFlatList(List<Brick> bricks) {
        super.addToFlatList(bricks);
        for (Brick brick : threadBricks) {
            brick.addToFlatList(bricks);
        }
        bricks.add(endBrick);
    }

    @Override
    public void setParent(Brick parent) {
        super.setParent(parent);
        for (Brick brick : threadBricks) {
            brick.setParent(this);
        }
    }

    @Override
    public List<Brick> getDragAndDropTargetList() {
        return threadBricks;
    }

    @Override
    public boolean removeChild(Brick brick) {
        if (threadBricks.remove(brick)) {
            return true;
        }
        for (Brick childBrick : threadBricks) {
            if (childBrick.removeChild(brick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_spawn_thread;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        Action action = sprite.getActionFactory().createSpawnThreadAction(
                sprite,
                sequence,
                getFormulaWithBrickField(BrickField.IF_CONDITION),
                threadBricks,
                sequence.getScript()
        );
        sequence.addAction(action);
    }

    @Override
    public void addRequiredResources(final ResourcesSet requiredResourcesSet) {
        super.addRequiredResources(requiredResourcesSet);
        for (Brick brick : threadBricks) {
            brick.addRequiredResources(requiredResourcesSet);
        }
    }
}
