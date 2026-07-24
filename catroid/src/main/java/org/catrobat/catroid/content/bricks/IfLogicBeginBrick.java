package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;

import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.ActionFactory;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.Operators;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.VisibleForTesting;

public class IfLogicBeginBrick extends FormulaBrick implements CompositeBrick {

    private static final long serialVersionUID = 1L;

    private transient ElseBrick elseBrick = new ElseBrick(this);
    private transient EndBrick endBrick = new EndBrick(this, R.layout.brick_if_end_if);

    protected List<Brick> ifBranchBricks = new ArrayList<>();
    protected List<ElseIfBranch> elseIfBranches = new ArrayList<>();
    protected List<Brick> elseBranchBricks = new ArrayList<>();

    public IfLogicBeginBrick() {
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_if_begin_edit_text);
    }

    public IfLogicBeginBrick(Formula formula) {
        this();
        setFormulaWithBrickField(BrickField.IF_CONDITION, formula);
    }

    private Object readResolve() {
        if (elseIfBranches == null) {
            elseIfBranches = new ArrayList<>();
        }
        if (ifBranchBricks == null) {
            ifBranchBricks = new ArrayList<>();
        }
        if (elseBranchBricks == null) {
            elseBranchBricks = new ArrayList<>();
        }
        return this;
    }

    public List<ElseIfBranch> getElseIfBranches() {
        if (elseIfBranches == null) {
            elseIfBranches = new ArrayList<>();
        }
        return elseIfBranches;
    }

    public void addElseIfBranch() {
        getElseIfBranches().add(new ElseIfBranch(new Formula(true)));
    }

    public void removeElseIfBranch(ElseIfBranch branch) {
        getElseIfBranches().remove(branch);
    }

    @Override
    public View getView(Context context) {
        View v = super.getView(context);

        View addBtn = v.findViewById(R.id.brick_if_add_else_if_button);
        if (addBtn != null) {
            addBtn.setOnClickListener(click -> {
                addElseIfBranch();
                ElseIfSeparatorBrick.refreshScriptList(v);
            });
        }
        return v;
    }

    @Override
    public boolean hasSecondaryList() {
        return true;
    }

    @Override
    public List<Brick> getNestedBricks() {
        return ifBranchBricks;
    }

    @Override
    public List<Brick> getSecondaryNestedBricks() {
        return elseBranchBricks;
    }

    public boolean addBrickToIfBranch(Brick brick) {
        return ifBranchBricks.add(brick);
    }

    public boolean addBrickToElseBranch(Brick brick) {
        return elseBranchBricks.add(brick);
    }

    @Override
    public void setCommentedOut(boolean commentedOut) {
        super.setCommentedOut(commentedOut);
        for (Brick brick : ifBranchBricks) {
            brick.setCommentedOut(commentedOut);
        }
        for (ElseIfBranch branch : getElseIfBranches()) {
            for (Brick brick : branch.getBranchBricks()) {
                brick.setCommentedOut(commentedOut);
            }
        }
        for (Brick brick : elseBranchBricks) {
            brick.setCommentedOut(commentedOut);
        }
    }

    @Override
    public Brick clone() throws CloneNotSupportedException {
        IfLogicBeginBrick clone = (IfLogicBeginBrick) super.clone();
        clone.elseBrick = new ElseBrick(clone);
        clone.endBrick = new EndBrick(clone, R.layout.brick_if_end_if);
        clone.ifBranchBricks = new ArrayList<>();
        clone.elseIfBranches = new ArrayList<>();
        clone.elseBranchBricks = new ArrayList<>();

        for (Brick brick : ifBranchBricks) {
            clone.addBrickToIfBranch(brick.clone());
        }
        for (ElseIfBranch branch : getElseIfBranches()) {
            clone.elseIfBranches.add(branch.clone());
        }
        for (Brick brick : elseBranchBricks) {
            clone.addBrickToElseBranch(brick.clone());
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
        for (ElseIfBranch branch : getElseIfBranches()) {
            ElseIfSeparatorBrick separator = branch.getSeparatorBrick(this);
            separator.setParentIfBrick(this);
            bricks.add(separator);
        }
        bricks.add(elseBrick);
        bricks.add(endBrick);
        return bricks;
    }

    @Override
    public void addToFlatList(List<Brick> bricks) {
        super.addToFlatList(bricks);
        for (Brick brick : ifBranchBricks) {
            brick.addToFlatList(bricks);
        }

        for (ElseIfBranch branch : getElseIfBranches()) {
            ElseIfSeparatorBrick separator = branch.getSeparatorBrick(this);
            separator.setParentIfBrick(this);
            bricks.add(separator);
            for (Brick brick : branch.getBranchBricks()) {
                brick.addToFlatList(bricks);
            }
        }

        bricks.add(elseBrick);
        for (Brick brick : elseBranchBricks) {
            brick.addToFlatList(bricks);
        }
        bricks.add(endBrick);
    }

    @Override
    public void setParent(Brick parent) {
        super.setParent(parent);
        for (Brick brick : ifBranchBricks) {
            brick.setParent(this);
        }
        for (ElseIfBranch branch : getElseIfBranches()) {
            ElseIfSeparatorBrick separator = branch.getSeparatorBrick(this);
            separator.setParentIfBrick(this);
            for (Brick brick : branch.getBranchBricks()) {
                brick.setParent(separator);
            }
        }
        for (Brick brick : elseBranchBricks) {
            brick.setParent(elseBrick);
        }
    }

    @Override
    public List<Brick> getDragAndDropTargetList() {
        return ifBranchBricks;
    }

    @Override
    public boolean removeChild(Brick brick) {
        if (ifBranchBricks.remove(brick)) return true;
        if (elseBranchBricks.remove(brick)) return true;

        for (ElseIfBranch branch : getElseIfBranches()) {
            if (branch.getBranchBricks().remove(brick)) return true;
        }

        for (Brick childBrick : ifBranchBricks) {
            if (childBrick.removeChild(brick)) return true;
        }
        for (ElseIfBranch branch : getElseIfBranches()) {
            for (Brick childBrick : branch.getBranchBricks()) {
                if (childBrick.removeChild(brick)) return true;
            }
        }
        for (Brick childBrick : elseBranchBricks) {
            if (childBrick.removeChild(brick)) return true;
        }
        return false;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_if_begin_if;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        ScriptSequenceAction ifSequence = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());
        for (Brick brick : ifBranchBricks) {
            if (!brick.isCommentedOut()) {
                brick.addActionToSequence(sprite, ifSequence);
            }
        }

        ScriptSequenceAction elseSequence = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());
        for (Brick brick : elseBranchBricks) {
            if (!brick.isCommentedOut()) {
                brick.addActionToSequence(sprite, elseSequence);
            }
        }

        ScriptSequenceAction currentElseChain = elseSequence;
        List<ElseIfBranch> branches = getElseIfBranches();

        for (int i = branches.size() - 1; i >= 0; i--) {
            ElseIfBranch branch = branches.get(i);
            ScriptSequenceAction branchSeq = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());

            for (Brick brick : branch.getBranchBricks()) {
                if (!brick.isCommentedOut()) {
                    brick.addActionToSequence(sprite, branchSeq);
                }
            }

            ScriptSequenceAction wrapperSeq = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());
            Action elseIfAction = sprite.getActionFactory().createIfLogicAction(
                    sprite, wrapperSeq, branch.getCondition(), branchSeq, currentElseChain
            );
            wrapperSeq.addAction(elseIfAction);
            currentElseChain = wrapperSeq;
        }

        Action action = sprite.getActionFactory().createIfLogicAction(
                sprite, sequence, getFormulaWithBrickField(BrickField.IF_CONDITION), ifSequence, currentElseChain
        );

        sequence.addAction(action);
    }

    @Override
    public void addRequiredResources(final ResourcesSet requiredResourcesSet) {
        super.addRequiredResources(requiredResourcesSet);
        for (Brick brick : ifBranchBricks) {
            brick.addRequiredResources(requiredResourcesSet);
        }
        for (ElseIfBranch branch : getElseIfBranches()) {
            for (Brick brick : branch.getBranchBricks()) {
                brick.addRequiredResources(requiredResourcesSet);
            }
        }
        for (Brick brick : elseBranchBricks) {
            brick.addRequiredResources(requiredResourcesSet);
        }
    }

    @VisibleForTesting
    public static class ElseBrick extends BrickBaseType {

        ElseBrick(IfLogicBeginBrick ifBrick) {
            parent = ifBrick;
        }

        @Override
        public boolean isCommentedOut() {
            return parent.isCommentedOut();
        }

        @Override
        public boolean consistsOfMultipleParts() {
            return true;
        }

        @Override
        public List<Brick> getAllParts() {
            return parent.getAllParts();
        }

        @Override
        public void addToFlatList(List<Brick> bricks) {
            parent.addToFlatList(bricks);
        }

        @Override
        public List<Brick> getDragAndDropTargetList() {
            return ((IfLogicBeginBrick) parent).elseBranchBricks;
        }

        @Override
        public int getPositionInDragAndDropTargetList() {
            return -1;
        }

        @Override
        public int getViewResource() {
            return R.layout.brick_if_else;
        }

        @Override
        public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
        }

        @Override
        public UUID getBrickID() {
            return parent.getBrickID();
        }
    }
}

