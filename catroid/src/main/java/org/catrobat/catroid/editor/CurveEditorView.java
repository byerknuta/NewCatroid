package org.catrobat.catroid.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.catrobat.catroid.raptor.ParticleCurvePoint;

import java.util.Collections;
import java.util.List;

public class CurveEditorView extends View {

    private List<ParticleCurvePoint<Float>> points;
    private Paint linePaint, pointPaint, gridPaint, bgPaint, zeroLinePaint;
    private int selectedIndex = -1;
    private Runnable onUpdateListener;


    private float minVal = 0f;
    private float maxVal = 1f;

    public CurveEditorView(Context context) { super(context); init(); }
    public CurveEditorView(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5f);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(0x40FFFFFF);
        gridPaint.setStrokeWidth(2f);


        zeroLinePaint = new Paint();
        zeroLinePaint.setColor(0x80FF0000);
        zeroLinePaint.setStrokeWidth(3f);

        bgPaint = new Paint();
        bgPaint.setColor(0xFF202020);
    }


    public void setData(List<ParticleCurvePoint<Float>> points, float min, float max, Runnable onUpdate) {
        this.points = points;

        if (min >= max) max = min + 1f;
        this.minVal = min;
        this.maxVal = max;
        this.onUpdateListener = onUpdate;
        invalidate();
    }


    public void setRange(float min, float max) {
        if (min >= max) max = min + 1f;
        this.minVal = min;
        this.maxVal = max;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        float w = getWidth();
        float h = getHeight();


        if (minVal < 0 && maxVal > 0) {
            float zeroY = mapY(0);
            canvas.drawLine(0, zeroY, w, zeroY, zeroLinePaint);
        }


        canvas.drawLine(w/2, 0, w/2, h, gridPaint);

        if (points == null || points.isEmpty()) return;


        for (int i = 0; i < points.size() - 1; i++) {
            float x1 = mapX(points.get(i).time);
            float y1 = mapY(points.get(i).value);
            float x2 = mapX(points.get(i+1).time);
            float y2 = mapY(points.get(i+1).value);
            canvas.drawLine(x1, y1, x2, y2, linePaint);
        }


        for (int i = 0; i < points.size(); i++) {
            float x = mapX(points.get(i).time);
            float y = mapY(points.get(i).value);

            if (i == selectedIndex) {
                pointPaint.setColor(Color.YELLOW);
                canvas.drawCircle(x, y, 16, pointPaint);
            } else {
                pointPaint.setColor(Color.WHITE);
                canvas.drawCircle(x, y, 12, pointPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (points == null) return false;

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                float minDist = 80f;
                selectedIndex = -1;
                for (int i = 0; i < points.size(); i++) {
                    float px = mapX(points.get(i).time);
                    float py = mapY(points.get(i).value);
                    if (Math.hypot(touchX - px, touchY - py) < minDist) {
                        selectedIndex = i;
                    }
                }
                if (selectedIndex != -1) invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (selectedIndex != -1) {
                    getParent().requestDisallowInterceptTouchEvent(true);


                    float newTime = touchX / getWidth();

                    newTime = Math.max(0f, Math.min(1f, newTime));



                    float normalizedY = 1f - (touchY / getHeight());

                    float newVal = minVal + (normalizedY * (maxVal - minVal));


                    newVal = Math.max(minVal, Math.min(maxVal, newVal));

                    points.get(selectedIndex).time = newTime;
                    points.get(selectedIndex).value = newVal;
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                if (selectedIndex != -1) {
                    Collections.sort(points, (o1, o2) -> Float.compare(o1.time, o2.time));
                    selectedIndex = -1;
                    if (onUpdateListener != null) onUpdateListener.run();
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private float mapX(float time) {
        return time * getWidth();
    }

    private float mapY(float val) {

        float range = maxVal - minVal;
        if (Math.abs(range) < 0.0001f) range = 1f;


        float norm = (val - minVal) / range;


        return getHeight() * (1f - norm);
    }

    public boolean deleteSelectedPoint() {
        if (selectedIndex != -1 && selectedIndex < points.size()) {
            points.remove(selectedIndex);


            selectedIndex = -1;


            if (onUpdateListener != null) onUpdateListener.run();
            invalidate();
            return true;
        }
        return false;
    }


    public boolean hasSelection() {
        return selectedIndex != -1;
    }
}