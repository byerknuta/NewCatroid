/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2022 The Catrobat Team
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

package org.catrobat.catroid.content;

import android.graphics.PointF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Queue;

import org.catrobat.catroid.common.BrickValues;
import org.catrobat.catroid.stage.StageActivity;

public class PenConfiguration {

    private Queue<Queue<PointF>> positions = new Queue<>();
    private boolean penDown = false;
    private double penSize = BrickValues.PEN_SIZE;
    private PenColor penColor = new PenColor(0, 0, 1, 1);
    private boolean stamp = false;
    private int queuesToFinish = 0;

    public PenConfiguration() {
    }

    public boolean hasLinesToDraw() {
        return currentQueueHasJobToHandle();
    }

    public void drawLinesForSprite(Float screenRatio, Camera camera) {
        if (!currentQueueHasJobToHandle()) {
            return;
        }

        ShapeRenderer renderer = StageActivity.getActiveStageListener().shapeRenderer;
        renderer.setProjectionMatrix(camera.combined);
        renderer.setColor(new Color(penColor.r, penColor.g, penColor.b, penColor.a));
        renderer.begin(ShapeRenderer.ShapeType.Filled);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        while (currentQueueHasJobToHandle()) {
            drawLine(screenRatio, renderer, camera);
            updateQueues();
        }

        renderer.end();
    }

    private boolean currentQueueHasJobToHandle() {
        if (positions.isEmpty()) {
            return false;
        }
        Queue<PointF> currentQueue = positions.first();
        if (currentQueue == null || currentQueue.isEmpty()) {
            positions.removeFirst();
            if (queuesToFinish > 0) {
                queuesToFinish--;
            }
            return !positions.isEmpty();
        }
        return currentQueue.size > 1 || (currentQueue.size == 1 && queuesToFinish > 0);
    }

    public void drawAllLines(ShapeRenderer renderer, Float screenRatio, Camera camera) {
        renderer.setColor(new Color(penColor.r, penColor.g, penColor.b, penColor.a));

        while (currentQueueHasJobToHandle()) {
            drawLine(screenRatio, renderer, camera);
            updateQueues();
        }
        renderer.flush();
    }

    private void drawLine(Float screenRatio, ShapeRenderer renderer, Camera camera) {
        if (positions.isEmpty()) {
            return;
        }
        Queue<PointF> currentQueue = positions.first();
        if (currentQueue == null || currentQueue.isEmpty()) {
            return;
        }

        Float calculatedPenSize = (float) this.penSize * screenRatio;

        if (currentQueue.size == 1) {
            PointF point = currentQueue.removeFirst();
            float x = point.x + camera.position.x;
            float y = point.y + camera.position.y;
            renderer.circle(x, y, calculatedPenSize / 2f);
        } else {
            PointF currentPosition = currentQueue.removeFirst();
            PointF nextPosition = currentQueue.first();

            float x1 = currentPosition.x + camera.position.x;
            float y1 = currentPosition.y + camera.position.y;
            float x2 = nextPosition.x + camera.position.x;
            float y2 = nextPosition.y + camera.position.y;

            if (x1 != x2 || y1 != y2) {
                renderer.circle(x1, y1, calculatedPenSize / 2f);
                renderer.rectLine(x1, y1, x2, y2, calculatedPenSize);
                renderer.circle(x2, y2, calculatedPenSize / 2f);
            }
        }
    }

    private void updateQueues() {
        if (!positions.isEmpty() && positions.first().isEmpty()) {
            positions.removeFirst();
            if (queuesToFinish > 0) {
                queuesToFinish--;
            }
        }
    }

    public void addQueue() {
        positions.addLast(new Queue<>());
    }

    public void addPosition(PointF position) {
        if (positions.isEmpty()) {
            addQueue();
        }
        positions.last().addLast(position);
    }

    public void incrementQueuesToFinish() {
        queuesToFinish++;
    }

    public void decrementQueuesToFinish() {
        if (queuesToFinish > 0) {
            queuesToFinish--;
        }
    }

    public void setPenDown(boolean penDown) {
        this.penDown = penDown;
    }

    public boolean isPenDown() {
        return penDown;
    }

    public void setPenSize(double penSize) {
        this.penSize = penSize;
    }

    public void setPenColor(PenColor penColor) {
        this.penColor = penColor;
    }

    public void setStamp(boolean stamp) {
        this.stamp = stamp;
    }

    public boolean hasStamp() {
        return stamp;
    }

    public Queue<Queue<PointF>> getPositions() {
        return positions;
    }
}
