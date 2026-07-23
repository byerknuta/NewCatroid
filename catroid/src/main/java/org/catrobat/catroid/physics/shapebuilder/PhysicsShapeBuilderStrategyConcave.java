package org.catrobat.catroid.physics.shapebuilder;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import org.catrobat.catroid.physics.PhysicsWorldConverter;

import java.util.ArrayList;
import java.util.List;

public final class PhysicsShapeBuilderStrategyConcave implements PhysicsShapeBuilderStrategy {

    private static final int ALPHA_THRESHOLD = 10;
    private static final int GRID_SIZE = 6;

    @Override
    public Shape[] build(Pixmap pixmap, float scale) {
        if (pixmap == null || pixmap.getWidth() < 1 || pixmap.getHeight() < 1) {
            return null;
        }

        int imgWidth = pixmap.getWidth();
        int imgHeight = pixmap.getHeight();

        int cols = (int) Math.ceil((double) imgWidth / GRID_SIZE);
        int rows = (int) Math.ceil((double) imgHeight / GRID_SIZE);

        boolean[][] grid = new boolean[cols][rows];

        for (int gx = 0; gx < cols; gx++) {
            for (int gy = 0; gy < rows; gy++) {
                grid[gx][gy] = cellHasOpaquePixels(pixmap, gx * GRID_SIZE, gy * GRID_SIZE, GRID_SIZE, imgWidth, imgHeight);
            }
        }

        List<RectBox> rects = new ArrayList<>();
        boolean[][] visited = new boolean[cols][rows];

        for (int gy = 0; gy < rows; gy++) {
            for (int gx = 0; gx < cols; gx++) {
                if (grid[gx][gy] && !visited[gx][gy]) {
                    int w = 1;
                    while (gx + w < cols && grid[gx + w][gy] && !visited[gx + w][gy]) {
                        w++;
                    }

                    int h = 1;
                    boolean canExpand = true;
                    while (gy + h < rows && canExpand) {
                        for (int k = 0; k < w; k++) {
                            if (!grid[gx + k][gy + h] || visited[gx + k][gy + h]) {
                                canExpand = false;
                                break;
                            }
                        }
                        if (canExpand) {
                            h++;
                        }
                    }

                    for (int dy = 0; dy < h; dy++) {
                        for (int dx = 0; dx < w; dx++) {
                            visited[gx + dx][gy + dy] = true;
                        }
                    }

                    float pxLeft = gx * GRID_SIZE;
                    float pxTop = gy * GRID_SIZE;
                    float pxRight = Math.min(imgWidth, (gx + w) * GRID_SIZE);
                    float pxBottom = Math.min(imgHeight, (gy + h) * GRID_SIZE);

                    rects.add(new RectBox(pxLeft, pxTop, pxRight - pxLeft, pxBottom - pxTop));
                }
            }
        }

        if (rects.isEmpty()) {
            return new PhysicsShapeBuilderStrategyFastHull().build(pixmap, scale);
        }

        float centerX = imgWidth / 2.0f;
        float centerY = imgHeight / 2.0f;

        List<Shape> shapes = new ArrayList<>();

        for (RectBox rect : rects) {
            float x1 = rect.x - centerX;
            float y1 = centerY - rect.y;

            float x2 = (rect.x + rect.width) - centerX;
            float y2 = centerY - (rect.y + rect.height);

            Vector2 b1 = PhysicsWorldConverter.convertCatroidToBox2dVector(new Vector2(x1, y1));
            Vector2 b2 = PhysicsWorldConverter.convertCatroidToBox2dVector(new Vector2(x2, y2));

            Vector2 boxCenterMeters = new Vector2((b1.x + b2.x) / 2.0f, (b1.y + b2.y) / 2.0f);
            float hx = Math.abs(b2.x - b1.x) / 2.0f;
            float hy = Math.abs(b1.y - b2.y) / 2.0f;

            if (hx > 0.0001f && hy > 0.0001f) {
                PolygonShape boxShape = new PolygonShape();
                boxShape.setAsBox(hx, hy, boxCenterMeters, 0f);
                shapes.add(boxShape);
            }
        }

        if (shapes.isEmpty()) {
            return new PhysicsShapeBuilderStrategyFastHull().build(pixmap, scale);
        }

        return shapes.toArray(new Shape[0]);
    }

    private boolean cellHasOpaquePixels(Pixmap pixmap, int startX, int startY, int size, int maxW, int maxH) {
        int endX = Math.min(maxW, startX + size);
        int endY = Math.min(maxH, startY + size);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if ((pixmap.getPixel(x, y) & 0xff) >= ALPHA_THRESHOLD) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class RectBox {
        float x, y, width, height;

        RectBox(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
