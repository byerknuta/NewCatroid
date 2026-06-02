package org.catrobat.catroid.formulaeditor;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

public class FoldedFormulaSpan extends ReplacementSpan {
    private final float textSize;

    public FoldedFormulaSpan(float textSize) {
        this.textSize = textSize;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) paint.measureText(" ... ") + 16;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int originalColor = paint.getColor();
        float width = paint.measureText(" ... ");

        paint.setColor(0xFF003554);
        RectF rect = new RectF(x + 4, top + 2, x + width + 12, bottom - 2);
        canvas.drawRoundRect(rect, 8, 8, paint);

        paint.setColor(0xFFA8DFF4);
        canvas.drawText(" ... ", x + 8, y, paint);

        paint.setColor(originalColor);
    }
}
