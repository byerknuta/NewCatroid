package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.ui.UiUtils;
import org.catrobat.catroid.ui.recyclerview.adapter.BrickAdapter;
import org.catrobat.catroid.ui.recyclerview.fragment.ScriptFragment;

import java.util.List;
import java.util.UUID;

public class ElseIfSeparatorBrick extends FormulaBrick {
    private static final long serialVersionUID = 1L;

    private transient IfLogicBeginBrick parentIfBrick;
    private transient ElseIfBranch branchOwner;

    public ElseIfSeparatorBrick() {
        addAllowedBrickField(BrickField.IF_CONDITION, R.id.brick_else_if_edit_text);
    }

    public ElseIfSeparatorBrick(IfLogicBeginBrick parentIfBrick, ElseIfBranch branchOwner) {
        this();
        setParentIfBrick(parentIfBrick);
        this.branchOwner = branchOwner;
        if (branchOwner != null && branchOwner.getCondition() != null) {
            setFormulaWithBrickField(BrickField.IF_CONDITION, branchOwner.getCondition());
        }
    }

    public void setParentIfBrick(IfLogicBeginBrick parentIfBrick) {
        this.parentIfBrick = parentIfBrick;
        setParent(parentIfBrick);
    }

    @Override
    public Script getScript() {
        if (parentIfBrick != null && parentIfBrick.getScript() != null) {
            return parentIfBrick.getScript();
        }
        if (parent != null && parent.getScript() != null) {
            return parent.getScript();
        }
        return super.getScript();
    }

    public static void refreshScriptList(View view) {
        if (view == null) return;
        try {
            FragmentActivity activity = UiUtils.getActivityFromView(view);
            if (activity == null) return;

            Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

            for (Fragment f : activity.getSupportFragmentManager().getFragments()) {
                if (f instanceof ScriptFragment) {
                    ScriptFragment sf = (ScriptFragment) f;
                    BrickAdapter adapter = sf.getAdapter();
                    if (adapter != null && currentSprite != null) {
                        adapter.updateItems(currentSprite);
                        sf.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public View getView(Context context) {
        View v = super.getView(context);

        View deleteBtn = v.findViewById(R.id.brick_else_if_delete_button);
        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(click -> {
                if (parentIfBrick != null && branchOwner != null) {
                    showDeleteConfirmationDialog(context, v);
                }
            });
        }
        return v;
    }

    private void showDeleteConfirmationDialog(Context context, View view) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_delete_else_if_title)
                .setMessage(R.string.dialog_delete_else_if_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (parentIfBrick != null && branchOwner != null) {
                        parentIfBrick.removeElseIfBranch(branchOwner);
                        refreshScriptList(view);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void setFormulaWithBrickField(FormulaField formulaField, Formula formula) {
        super.setFormulaWithBrickField(formulaField, formula);
        if (branchOwner != null && formula != null) {
            branchOwner.setCondition(formula);
        }
    }

    @Override
    public Formula getFormulaWithBrickField(FormulaField formulaField) {
        if (branchOwner != null && branchOwner.getCondition() != null) {
            return branchOwner.getCondition();
        }
        return super.getFormulaWithBrickField(formulaField);
    }

    @Override
    public boolean isCommentedOut() {
        return parentIfBrick != null && parentIfBrick.isCommentedOut();
    }

    @Override
    public boolean consistsOfMultipleParts() {
        return true;
    }

    @Override
    public List<Brick> getAllParts() {
        return parentIfBrick != null ? parentIfBrick.getAllParts() : super.getAllParts();
    }

    @Override
    public void addToFlatList(List<Brick> bricks) {
        if (parentIfBrick != null) {
            parentIfBrick.addToFlatList(bricks);
        }
    }

    @Override
    public List<Brick> getDragAndDropTargetList() {
        return branchOwner != null ? branchOwner.getBranchBricks() : null;
    }

    @Override
    public int getPositionInDragAndDropTargetList() {
        return -1;
    }

    @Override
    public int getViewResource() {
        return R.layout.brick_if_else_if_separator;
    }

    @Override
    public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
    }

    @Override
    public UUID getBrickID() {
        return parentIfBrick != null ? parentIfBrick.getBrickID() : super.getBrickID();
    }
}
