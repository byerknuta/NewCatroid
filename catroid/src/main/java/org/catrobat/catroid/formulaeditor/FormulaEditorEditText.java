/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2023 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.formulaeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.danvexteam.lunoscript_annotations.LunoClass;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.formulaeditor.InternFormula.TokenSelectionType;
import org.catrobat.catroid.ui.fragment.FormulaEditorFragment;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressLint("AppCompatCustomView")
@LunoClass
public class FormulaEditorEditText extends EditText implements OnTouchListener {

	private static final String TAG = "FormulaEditorEditText";

	private static final BackgroundColorSpan COLOR_ERROR = new BackgroundColorSpan(0xFFF00000);
	private static final BackgroundColorSpan COLOR_HIGHLIGHT = new BackgroundColorSpan(0xFF33B5E5);
	private FormulaEditorHistory history = null;
	FormulaEditorFragment formulaEditorFragment = null;
	private int absoluteCursorPosition = 0;
	private InternFormula internFormula;
	private Context context;
	private final Paint paint = new Paint();

    private final Set<Integer> foldedBracketOccurrences = new HashSet<>();

    private PopupWindow foldButtonPopup = null;
    private Button foldButton = null;

	final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onDoubleTap(MotionEvent event) {
			internFormula.setCursorAndSelection(absoluteCursorPosition, true);
			history.updateCurrentSelection(internFormula.getSelection());
			highlightSelection();
			return true;
		}

        @Override
        public boolean onSingleTapUp(MotionEvent motion) {
            Layout layout = getLayout();
            if (layout != null) {
                int x = (int) motion.getX() - getPaddingLeft() + getScrollX();
                int y = (int) motion.getY() - getPaddingTop() + getScrollY();

                if (y < 0) {
                    y = 0;
                }
                if (y >= layout.getHeight()) {
                    y = Math.max(0, layout.getHeight() - 1);
                }

                int line = layout.getLineForVertical(y);
                int tempCursorPosition = layout.getOffsetForHorizontal(line, x);

                if (tempCursorPosition < 0) {
                    tempCursorPosition = 0;
                }
                if (tempCursorPosition > length()) {
                    tempCursorPosition = length();
                }

                if (!isDoNotMoveCursorOnTab()) {
                    absoluteCursorPosition = tempCursorPosition;
                }
                absoluteCursorPosition = Math.min(absoluteCursorPosition, length());
                setSelection(absoluteCursorPosition);
                postInvalidate();

                internFormula.setCursorAndSelection(absoluteCursorPosition, false);

                highlightSelection();
                history.updateCurrentSelection(internFormula.getSelection());
                history.updateCurrentCursor(absoluteCursorPosition);

                formulaEditorFragment.refreshFormulaPreviewString(internFormula.getExternFormulaString());
                formulaEditorFragment.updateButtonsOnKeyboardAndInvalidateOptionsMenu();
            }
            return true;
        }
	});

	public FormulaEditorEditText(Context context) {
		super(context);
		this.context = context;
	}

	public FormulaEditorEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public void addTokensToActiveFormula(List<InternToken> tokensToAdd) {
		if (internFormula == null || tokensToAdd == null || tokensToAdd.isEmpty()) {
			Log.w(TAG, "addTokensToActiveFormula: internFormula is null or tokensToAdd is null/empty");
			return;
		}

		internFormula.insertTokens(context, tokensToAdd);

		pushToHistoryAndRefreshPreviewString();
		if (formulaEditorFragment != null) {
			formulaEditorFragment.updateButtonsOnKeyboardAndInvalidateOptionsMenu();
		}


	}

	@SuppressLint("ClickableViewAccessibility")
	public void init(FormulaEditorFragment formulaEditorFragment) {
		this.formulaEditorFragment = formulaEditorFragment;
		this.setOnTouchListener(this);
		this.setLongClickable(false);
		this.setSelectAllOnFocus(false);
		this.setCursorVisible(false);
        setTextColor(Color.WHITE);
        setHintTextColor(0xFFB0BEC5);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(0xFF002B4D);
        backgroundDrawable.setCornerRadius(24);
        backgroundDrawable.setStroke(3, 0xFF51658E);

        setBackground(backgroundDrawable);

        setPadding(36, 28, 36, 28);

        cursorAnimation.run();
	}

	public List<InternToken> getSelectedTokens() {
		return internFormula.getSelectedTokenForCopy();
	}

	private void pushToHistoryAndRefreshPreviewString() {
		history.push(new UndoState(internFormula.getInternFormulaState(),
				formulaEditorFragment.getCurrentBrickField()));
		String resultingText = updateTextAndCursorFromInternFormula();
		setSelection(absoluteCursorPosition);
		formulaEditorFragment.refreshFormulaPreviewString(resultingText);
	}

	public void addTokens(List<InternToken> tokens) {
		internFormula.addTokens(context, tokens);
		pushToHistoryAndRefreshPreviewString();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

    public void enterNewFormula(UndoState state) {
        foldedBracketOccurrences.clear();
        hideFoldPopup();

        internFormula = state.internFormulaState.createInternFormulaFromState();
        internFormula.generateExternFormulaStringAndInternExternMapping(context);

        updateTextAndCursorFromInternFormula();

        internFormula.selectWholeFormula();
        highlightSelection();

        if (history == null) {
            history = new FormulaEditorHistory(state);
        } else {
            history.updateCurrentState(state);
        }
    }

	public void updateVariableReferences(String oldName, String newName) {
		if (internFormula == null) {
			return;
		}
		internFormula.updateVariableReferences(oldName, newName, this.context);
		history.push(new UndoState(internFormula.getInternFormulaState(),
				formulaEditorFragment.getCurrentBrickField()));
		Map<Brick.FormulaField, InternFormulaState> initialState = history.initialStates;
		for (Map.Entry<Brick.FormulaField, InternFormulaState> state : initialState.entrySet()) {
			state.getValue().updateUserDataTokens(InternTokenType.USER_VARIABLE, oldName, newName);
		}
		String resultingText = updateTextAndCursorFromInternFormula();
		setSelection(absoluteCursorPosition);
		formulaEditorFragment.refreshFormulaPreviewString(resultingText);
	}

	public void updateListReferences(String oldName, String newName) {
		if (internFormula == null) {
			return;
		}
		internFormula.updateListReferences(oldName, newName, this.context);
		history.push(new UndoState(internFormula.getInternFormulaState(),
				formulaEditorFragment.getCurrentBrickField()));
		Map<Brick.FormulaField, InternFormulaState> initialState = history.initialStates;
		for (Map.Entry<Brick.FormulaField, InternFormulaState> state : initialState.entrySet()) {
			state.getValue().updateUserDataTokens(InternTokenType.USER_LIST, oldName, newName);
		}
		String resultingText = updateTextAndCursorFromInternFormula();
		setSelection(absoluteCursorPosition);
		formulaEditorFragment.refreshFormulaPreviewString(resultingText);
	}

	private final Runnable cursorAnimation = new Runnable() {
		@Override
		public void run() {
            int textColor = getCurrentTextColor();
            paint.setColor((paint.getColor() == 0x00000000) ? textColor : 0x00000000);
            invalidate();
            postDelayed(cursorAnimation, 500);
		}
	};

    public void syncCursorPosition() {
        absoluteCursorPosition = getSelectionStart();
        absoluteCursorPosition = Math.min(absoluteCursorPosition, length());

        if (internFormula != null) {
            internFormula.setCursorAndSelection(absoluteCursorPosition, false);
        }

        highlightSelection();

        if (history != null && internFormula != null) {
            history.updateCurrentSelection(internFormula.getSelection());
            history.updateCurrentCursor(absoluteCursorPosition);
        }

        if (formulaEditorFragment != null && internFormula != null) {
            formulaEditorFragment.refreshFormulaPreviewString(internFormula.getExternFormulaString());
            formulaEditorFragment.updateButtonsOnKeyboardAndInvalidateOptionsMenu();
        }

        postInvalidate();

        checkForBracketNearCursor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        absoluteCursorPosition = Math.min(Math.max(0, absoluteCursorPosition), length());
        paint.setStrokeWidth(3);

        Layout layout = getLayout();
        if (layout != null && length() > 0 && absoluteCursorPosition <= length()) {
            int line = layout.getLineForOffset(absoluteCursorPosition);
            float xCoordinate = layout.getPrimaryHorizontal(absoluteCursorPosition) + getPaddingLeft();
            float startYCoordinate = layout.getLineBaseline(line) + layout.getLineAscent(line) + getPaddingTop();
            float endYCoordinate = layout.getLineBaseline(line) + layout.getLineAscent(line) + getTextSize() + getPaddingTop();
            endYCoordinate += line == 0 ? 5 : 0;

            canvas.drawLine(xCoordinate, startYCoordinate, xCoordinate, endYCoordinate, paint);
        }
    }

    public void highlightSelection() {
        Spannable highlightSpan = this.getText();
        highlightSpan.removeSpan(COLOR_HIGHLIGHT);
        highlightSpan.removeSpan(COLOR_ERROR);

        if (internFormula == null) {
            return;
        }

        int selectionStartIndex = internFormula.getExternSelectionStartIndex();
        int selectionEndIndex = internFormula.getExternSelectionEndIndex();
        TokenSelectionType selectionType = internFormula.getExternSelectionType();

        if (selectionStartIndex < 0 || selectionEndIndex < 0
                || selectionStartIndex >= selectionEndIndex
                || selectionEndIndex > highlightSpan.length()) {
            return;
        }

        if (selectionType == TokenSelectionType.USER_SELECTION) {
            highlightSpan.setSpan(COLOR_HIGHLIGHT, selectionStartIndex, selectionEndIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            highlightSpan.setSpan(COLOR_ERROR, selectionStartIndex, selectionEndIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

	public void setParseErrorCursorAndSelection() {
		internFormula.selectParseErrorTokenAndSetCursor();
		highlightSelection();
		setSelection(absoluteCursorPosition);
	}

	public void handleKeyEvent(int resource, String name) {
		internFormula.handleKeyInput(resource, context, name);
		pushToHistoryAndRefreshPreviewString();
	}

	public String getStringFromInternFormula() {
		return internFormula.getExternFormulaString();
	}

	public String getSelectedTextFromInternFormula() {
		return internFormula.getSelectedText();
	}

	public boolean isSelectedTokenFirstParamOfRegularExpression() {
		return internFormula.isSelectedTokenFirstParamOfRegularExpression();
	}

	public void overrideSelectedText(String string) {
		internFormula.overrideSelectedText(string, context);
		pushToHistoryAndRefreshPreviewString();
	}

	public boolean hasChanges() {
		return history != null && history.hasUnsavedChanges();
	}

	public void formulaSaved() {
		history.changesSaved();
	}

	public void endEdit() {
		history.clear();
	}

	public void quickSelect() {
		internFormula.selectWholeFormula();
		highlightSelection();
	}

	public boolean undo() {
		if (!history.undoIsPossible()) {
			return false;
		}
		UndoState previousState = history.backward();
		if (previousState != null) {

			internFormula = previousState.internFormulaState.createInternFormulaFromState();
			internFormula.generateExternFormulaStringAndInternExternMapping(context);
			internFormula.updateInternCursorPosition();
			updateTextAndCursorFromInternFormula();
		}

		formulaEditorFragment.refreshFormulaPreviewString(internFormula.getExternFormulaString());
		return true;
	}

	public boolean redo() {
		if (!history.redoIsPossible()) {
			return false;
		}
		UndoState nextStep = history.forward();
		if (nextStep != null) {

			internFormula = nextStep.internFormulaState.createInternFormulaFromState();
			internFormula.generateExternFormulaStringAndInternExternMapping(context);
			internFormula.updateInternCursorPosition();
			updateTextAndCursorFromInternFormula();
		}
		formulaEditorFragment.refreshFormulaPreviewString(internFormula.getExternFormulaString());
		return true;
	}

	private String updateTextAndCursorFromInternFormula() {
		String newExternFormulaString = internFormula.getExternFormulaString();
		setText(
				FormulaSpannableStringBuilder.buildSpannableFormulaString(context,
						newExternFormulaString, getTextSize()));
        applyFoldedSpans();
		absoluteCursorPosition = internFormula.getExternCursorPosition();
		if (absoluteCursorPosition > length()) {
			absoluteCursorPosition = length();
		}

		highlightSelection();

		return newExternFormulaString;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motion) {
		return gestureDetector.onTouchEvent(motion);
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return false;
	}

	public InternFormulaParser getFormulaParser() {
		return internFormula.getInternFormulaParser();
	}

	public boolean isDoNotMoveCursorOnTab() {
		return false;
	}

	public FormulaEditorHistory getHistory() {
		return history;
	}

	public boolean isThereSomethingToDelete() {
		if (internFormula == null) {
			return false;
		}
		return internFormula.isThereSomethingToDelete();
	}

	public int getIndexOfCorrespondingRegularExpression() {
		return internFormula.getIndexOfCorrespondingRegularExpression();
	}

	public void setSelectionToFirstParamOfRegularExpressionAtInternalIndex(int indexOfRegularExpression) {
		internFormula.setSelectionToFirstParamOfRegularExpressionAtInternalIndex(indexOfRegularExpression);
		highlightSelection();
	}

	public InternFormula getInternFormula() {
		return internFormula;
	}

    private void showFoldPopup(final int bracketIndex, boolean isFolded, float x, float y) {
        if (foldButtonPopup == null) {
            foldButton = new Button(getContext());
            foldButton.setTextSize(12);
            foldButton.setPadding(24, 12, 24, 12);
            foldButton.setAllCaps(false);

            int backgroundColor = getContext().getResources().getColor(R.color.button_background);
            int strokeColor = getContext().getResources().getColor(R.color.accent);

            GradientDrawable gd = new GradientDrawable();
            gd.setColor(backgroundColor);
            gd.setCornerRadius(16);
            foldButton.setBackground(gd);
            foldButton.setTextColor(Color.WHITE);

            foldButtonPopup = new PopupWindow(foldButton,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            foldButtonPopup.setFocusable(false);
            foldButtonPopup.setOutsideTouchable(true);
        }

        String buttonText = isFolded
                ? getContext().getString(R.string.formula_unfold)
                : getContext().getString(R.string.formula_fold);

        foldButton.setText(buttonText);
        foldButton.setOnClickListener(v -> {
            toggleFold(bracketIndex);
            foldButtonPopup.dismiss();
        });

        if (getWindowToken() != null) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            int popupX = location[0] + (int) x - 100;
            int popupY = location[1] + (int) y - 140;

            foldButtonPopup.showAtLocation(this, Gravity.NO_GRAVITY, popupX, popupY);
        }
    }

    private void checkForBracketNearCursor() {
        String text = getText().toString();
        int cursor = getSelectionStart();
        int bracketIndex = -1;

        if (cursor > 0 && text.charAt(cursor - 1) == '(') {
            bracketIndex = cursor - 1;
        } else if (cursor < text.length() && text.charAt(cursor) == '(') {
            bracketIndex = cursor;
        } else if (cursor > 0 && text.charAt(cursor - 1) == ')') {
            bracketIndex = cursor - 1;
        } else if (cursor < text.length() && text.charAt(cursor) == ')') {
            bracketIndex = cursor;
        }

        if (bracketIndex != -1) {
            int matchingIndex = findMatchingBracket(text, bracketIndex);
            if (matchingIndex != -1) {
                int openingIndex = (text.charAt(bracketIndex) == '(') ? bracketIndex : matchingIndex;
                int occurrence = getOpeningBracketOccurrenceIndex(text, openingIndex);
                boolean isFolded = foldedBracketOccurrences.contains(occurrence);

                Layout layout = getLayout();
                if (layout != null) {
                    int line = layout.getLineForOffset(bracketIndex);
                    float x = layout.getPrimaryHorizontal(bracketIndex) + getPaddingLeft();
                    float y = layout.getLineBaseline(line) + layout.getLineAscent(line);

                    showFoldPopup(bracketIndex, isFolded, x, y);
                }
                return;
            }
        }

        hideFoldPopup();
    }

    public static int findMatchingBracket(String text, int index) {
        if (index < 0 || index >= text.length()) return -1;
        char c = text.charAt(index);
        if (c == '(') {
            int depth = 0;
            for (int i = index; i < text.length(); i++) {
                if (text.charAt(i) == '(') depth++;
                else if (text.charAt(i) == ')') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        } else if (c == ')') {
            int depth = 0;
            for (int i = index; i >= 0; i--) {
                if (text.charAt(i) == ')') depth++;
                else if (text.charAt(i) == '(') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private int getOpeningBracketOccurrenceIndex(String text, int openingBracketIndex) {
        int occurrence = 0;
        for (int i = 0; i < openingBracketIndex; i++) {
            if (text.charAt(i) == '(') {
                occurrence++;
            }
        }
        return occurrence;
    }

    public void applyFoldedSpans() {
        Spannable spannable = getText();
        String text = spannable.toString();

        FoldedFormulaSpan[] oldSpans = spannable.getSpans(0, spannable.length(), FoldedFormulaSpan.class);
        for (FoldedFormulaSpan span : oldSpans) {
            spannable.removeSpan(span);
        }

        int occurrence = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '(') {
                if (foldedBracketOccurrences.contains(occurrence)) {
                    int closingIndex = findMatchingBracket(text, i);
                    if (closingIndex != -1 && closingIndex > i + 1) {
                        spannable.setSpan(
                                new FoldedFormulaSpan(getTextSize()),
                                i + 1,
                                closingIndex,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                }
                occurrence++;
            }
        }
    }

    private void toggleFold(int bracketIndex) {
        String text = getText().toString();
        if (bracketIndex < 0 || bracketIndex >= text.length()) return;

        char c = text.charAt(bracketIndex);
        int openingIndex = (c == '(') ? bracketIndex : findMatchingBracket(text, bracketIndex);
        if (openingIndex == -1) return;

        int occurrence = getOpeningBracketOccurrenceIndex(text, openingIndex);
        if (foldedBracketOccurrences.contains(occurrence)) {
            foldedBracketOccurrences.remove(occurrence);
        } else {
            foldedBracketOccurrences.add(occurrence);
        }

        updateTextAndCursorFromInternFormula();
    }

    private void hideFoldPopup() {
        if (foldButtonPopup != null && foldButtonPopup.isShowing()) {
            foldButtonPopup.dismiss();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        hideFoldPopup();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        absoluteCursorPosition = selStart;

        post(new Runnable() {
            @Override
            public void run() {
                checkForBracketNearCursor();
            }
        });
    }
}
