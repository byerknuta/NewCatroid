package org.catrobat.catroid.content.bricks;

import org.catrobat.catroid.formulaeditor.Formula;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ElseIfBranch implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private Formula condition;
    private List<Brick> branchBricks = new ArrayList<>();
    private transient ElseIfSeparatorBrick separatorBrick;

    public ElseIfBranch() {
        this(new Formula("1 == 1"));
    }

    public ElseIfBranch(Formula condition) {
        this.condition = condition;
    }

    public Formula getCondition() {
        return condition;
    }

    public void setCondition(Formula condition) {
        this.condition = condition;
    }

    public List<Brick> getBranchBricks() {
        if (branchBricks == null) {
            branchBricks = new ArrayList<>();
        }
        return branchBricks;
    }

    public ElseIfSeparatorBrick getSeparatorBrick(IfLogicBeginBrick parentIfBrick) {
        if (separatorBrick == null) {
            separatorBrick = new ElseIfSeparatorBrick(parentIfBrick, this);
        }
        return separatorBrick;
    }

    @Override
    public ElseIfBranch clone() throws CloneNotSupportedException {
        ElseIfBranch clone = (ElseIfBranch) super.clone();
        clone.condition = this.condition != null ? this.condition.clone() : new Formula("1 == 1");
        clone.branchBricks = new ArrayList<>();
        for (Brick brick : getBranchBricks()) {
            clone.branchBricks.add(brick.clone());
        }
        clone.separatorBrick = null;
        return clone;
    }
}
