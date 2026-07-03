package org.catrobat.catroid.content.bricks;

import com.badlogic.gdx.scenes.scene2d.Action;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.ActionFactory;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import java.util.ArrayList;
import java.util.List;

public class InstantBrick extends BrickBaseType implements CompositeBrick {
    private static final long serialVersionUID = 1L;

    private transient EndBrick endBrick = new EndBrick(this);
    private List<Brick> nestedBricks = new ArrayList<>();

    public InstantBrick() {
    }

    @Override
    public boolean hasSecondaryList() {
        return false;
    }

    @Override
    public List<Brick> getNestedBricks() {
        return nestedBricks;
    }

    @Override
    public List<Brick> getSecondaryNestedBricks() {
        return null;
    }

    public boolean addBrick(Brick brick) {
        return nestedBricks.add(brick);
    }

    @Override
    public void setCommentedOut(boolean commentedOut) {
        super.setCommentedOut(commentedOut);
        for (Brick brick : nestedBricks) {
            brick.setCommentedOut(commentedOut);
        }
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        InstantBrick clone = (InstantBrick) super.clone();
        clone.endBrick = new EndBrick(clone);
        clone.nestedBricks = new ArrayList<>();
        for (Brick brick : nestedBricks) {
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
        for (Brick brick : nestedBricks) {
            brick.addToFlatList(bricks);
        }
        bricks.add(endBrick);
    }

    @Override
    public void setParent(Brick parent) {
        super.setParent(parent);
        for (Brick brick : nestedBricks) {
            brick.setParent(this);
        }
    }

    @Override
    public List<Brick> getDragAndDropTargetList() {
        return nestedBricks;
    }

    @Override
    public boolean removeChild(Brick brick) {
        if (nestedBricks.remove(brick)) {
            return true;
        }
        for (Brick childBrick : nestedBricks) {
            if (childBrick.removeChild(brick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_instant;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        ScriptSequenceAction nestedSequence = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());
        for (Brick brick : nestedBricks) {
            if (!brick.isCommentedOut()) {
                brick.addActionToSequence(sprite, nestedSequence);
            }
        }

        Action action = sprite.getActionFactory().createInstantAction(sprite, sequence, nestedSequence);
        sequence.addAction(action);
    }

    @Override
    public void addRequiredResources(final ResourcesSet requiredResourcesSet) {
        super.addRequiredResources(requiredResourcesSet);
        for (Brick brick : nestedBricks) {
            brick.addRequiredResources(requiredResourcesSet);
        }
    }
}
