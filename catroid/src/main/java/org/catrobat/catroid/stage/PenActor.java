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

package org.catrobat.catroid.stage;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.content.PenConfiguration;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.XmlHeader;

public class PenActor extends Actor {

    private static class PenCommand {
        int type; // 0=Line, 1=Tri, 2=Circle, 3=Rect, 4=Arc, 5=Clear
        float[] p = new float[7];
        Color color = new Color();
        boolean fill;
        int blendMode;
        float alpha;
    }

    private FrameBuffer buffer;
    private Batch bufferBatch;
    private OrthographicCamera camera;
    private Float screenRatio;
    private TextureRegion bufferRegion;
    private ShapeRenderer shapeRenderer;

    private boolean autoRedraw = true;
    private float penAlpha = 1f;
    private int currentBlendMode = 0;
    private boolean needsFlush = false;

    private final Array<PenCommand> commandQueue = new Array<>(false, 1000);

    public PenActor() {
        XmlHeader header = ProjectManager.getInstance().getCurrentProject().getXmlHeader();
        int width = header.getVirtualScreenWidth();
        int height = header.getVirtualScreenHeight();

        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        bufferBatch = new SpriteBatch();
        camera = new OrthographicCamera(width, height);
        camera.update();
        bufferBatch.setProjectionMatrix(camera.combined);

        bufferRegion = new TextureRegion(buffer.getColorBufferTexture());
        bufferRegion.flip(false, true);

        screenRatio = calculateScreenRatio();
        reset();
    }

    public boolean isAutoRedraw() { return autoRedraw; }
    public void setAutoRedraw(boolean autoRedraw) { this.autoRedraw = autoRedraw; }
    public void setPenAlpha(float alpha) { this.penAlpha = Math.max(0f, Math.min(1f, alpha)); }
    public void setBlendMode(int mode) { this.currentBlendMode = mode; }
    public void flush() { this.needsFlush = true; }

    public static void applyBlendMode(int mode) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        switch (mode) {
            case 1: // Glow
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                break;
            case 2: // Eraser
                Gdx.gl.glBlendFunc(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case 0: // Normal
            default:
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                break;
        }
    }

    private void addCommand(int type, Color color, boolean fill, float... params) {
        PenCommand cmd = new PenCommand();
        cmd.type = type;
        cmd.color.set(color);
        cmd.fill = fill;
        cmd.blendMode = currentBlendMode;
        cmd.alpha = penAlpha;
        System.arraycopy(params, 0, cmd.p, 0, params.length);

        commandQueue.add(cmd);
    }

    public void drawDirectLine(float x1, float y1, float x2, float y2, float thickness, Color color) {
        addCommand(0, color, true, x1, y1, x2, y2, thickness);
    }

    public void drawDirectTriangle(float x1, float y1, float x2, float y2, float x3, float y3, boolean fill, Color color) {
        addCommand(1, color, fill, x1, y1, x2, y2, x3, y3);
    }

    public void drawDirectCircle(float x, float y, float radius, boolean fill, Color color) {
        addCommand(2, color, fill, x, y, radius);
    }

    public void drawDirectRect(float x, float y, float w, float h, boolean fill, Color color) {
        addCommand(3, color, fill, x, y, w, h);
    }

    public void drawDirectCircleOrArc(float x, float y, float radius, float startAngle, float degrees, boolean fill, Color color) {
        addCommand(4, color, fill, x, y, radius, startAngle, degrees);
    }

    public void clearWithColor(Color color, float alpha) {
        PenCommand cmd = new PenCommand();
        cmd.type = 5;
        cmd.color.set(color);
        cmd.alpha = Math.max(0f, Math.min(1f, alpha));
        if (commandQueue.size < 5000) commandQueue.add(cmd);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        StageListener listener = StageActivity.getActiveStageListener();
        if (listener == null || buffer == null) return;

        boolean processNow = autoRedraw || needsFlush;

        boolean hasLines = false;
        for (Sprite sprite : listener.getSpritesFromStage()) {
            if (sprite != null && sprite.penConfiguration != null && sprite.penConfiguration.hasLinesToDraw()) {
                hasLines = true; break;
            }
        }

        if (processNow && (commandQueue.size > 0 || hasLines)) {
            batch.end();
            buffer.begin();

            if (shapeRenderer == null) {
                shapeRenderer = new ShapeRenderer();
            }

            if (commandQueue.size > 0) {
                int currentBlend = -1;
                boolean srActive = false;
                int currentType = -1;

                for (PenCommand cmd : commandQueue) {
                    if (cmd.type == 5) {
                        if (srActive) { shapeRenderer.end(); srActive = false; }
                        if (cmd.alpha >= 0.999f) {
                            Gdx.gl20.glClearColor(cmd.color.r, cmd.color.g, cmd.color.b, cmd.color.a);
                            Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
                        } else {
                            Gdx.gl.glEnable(GL20.GL_BLEND);
                            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                            shapeRenderer.setProjectionMatrix(camera.combined);
                            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                            shapeRenderer.setColor(cmd.color.r, cmd.color.g, cmd.color.b, cmd.alpha);
                            shapeRenderer.rect(-buffer.getWidth() / 2f, -buffer.getHeight() / 2f, buffer.getWidth(), buffer.getHeight());
                            shapeRenderer.end();
                        }
                        currentBlend = -1;
                        continue;
                    }

                    if (cmd.blendMode != currentBlend) {
                        if (srActive) { shapeRenderer.end(); srActive = false; }
                        applyBlendMode(cmd.blendMode);
                        currentBlend = cmd.blendMode;
                    }

                    int neededType = cmd.fill ? 0 : 1;
                    if (srActive && currentType != neededType) {
                        shapeRenderer.end(); srActive = false;
                    }

                    if (!srActive) {
                        shapeRenderer.setProjectionMatrix(camera.combined);
                        shapeRenderer.begin(neededType == 0 ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
                        srActive = true;
                        currentType = neededType;
                    }

                    shapeRenderer.setColor(cmd.color.r, cmd.color.g, cmd.color.b, cmd.color.a * cmd.alpha);

                    switch (cmd.type) {
                        case 0: // Line
                            float size = cmd.p[4] * screenRatio;
                            shapeRenderer.circle(cmd.p[0], cmd.p[1], size / 2f);
                            shapeRenderer.rectLine(cmd.p[0], cmd.p[1], cmd.p[2], cmd.p[3], size);
                            shapeRenderer.circle(cmd.p[2], cmd.p[3], size / 2f);
                            break;
                        case 1: // Triangle
                            shapeRenderer.triangle(cmd.p[0], cmd.p[1], cmd.p[2], cmd.p[3], cmd.p[4], cmd.p[5]);
                            break;
                        case 2: // Circle
                            shapeRenderer.circle(cmd.p[0], cmd.p[1], cmd.p[2]);
                            break;
                        case 3: // Rect
                            shapeRenderer.rect(cmd.p[0] - cmd.p[2]/2f, cmd.p[1] - cmd.p[3]/2f, cmd.p[2], cmd.p[3]);
                            break;
                        case 4: // Arc
                            if (cmd.p[4] >= 360f) shapeRenderer.circle(cmd.p[0], cmd.p[1], cmd.p[2]);
                            else shapeRenderer.arc(cmd.p[0], cmd.p[1], cmd.p[2], cmd.p[3], cmd.p[4]);
                            break;
                    }
                }
                if (srActive) {
                    shapeRenderer.end();
                }
                commandQueue.clear();
            }

            if (hasLines) {
                applyBlendMode(currentBlendMode);
                for (Sprite sprite : listener.getSpritesFromStage()) {
                    if (sprite != null && sprite.penConfiguration != null) {
                        sprite.penConfiguration.drawLinesForSprite(screenRatio, camera, penAlpha, currentBlendMode);
                    }
                }
            }

            buffer.end();
            needsFlush = false;

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            batch.begin();
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(bufferRegion, -buffer.getWidth() / 2f, -buffer.getHeight() / 2f, buffer.getWidth(), buffer.getHeight());
    }

    public void reset() {
        if (buffer == null) return;
        commandQueue.clear();
        buffer.begin();
        Gdx.gl20.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        buffer.end();
    }

    public void stampToFrameBuffer() {
        if (buffer == null) return;
        StageListener listener = StageActivity.getActiveStageListener();
        if (listener == null) return;

        buffer.begin();
        bufferBatch.begin();
        for (Sprite sprite : listener.getSpritesFromStage()) {
            if (sprite != null && sprite.penConfiguration != null && sprite.penConfiguration.hasStamp()) {
                sprite.look.draw(bufferBatch, 1.0f);
                sprite.penConfiguration.setStamp(false);
            }
        }
        bufferBatch.end();
        buffer.end();
    }

    public void dispose() {
        commandQueue.clear();
        if (buffer != null) { buffer.dispose(); buffer = null; }
        if (bufferBatch != null) { bufferBatch.dispose(); bufferBatch = null; }
        if (shapeRenderer != null) { shapeRenderer.dispose(); shapeRenderer = null; }
    }

    private float calculateScreenRatio() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float deviceDiagonalPixel = (float) Math.sqrt(Math.pow(metrics.widthPixels, 2) + Math.pow(metrics.heightPixels, 2));
        XmlHeader header = ProjectManager.getInstance().getCurrentProject().getXmlHeader();
        float creatorDiagonalPixel = (float) Math.sqrt(Math.pow(header.getVirtualScreenWidth(), 2) + Math.pow(header.getVirtualScreenHeight(), 2));
        return creatorDiagonalPixel / deviceDiagonalPixel;
    }
}
