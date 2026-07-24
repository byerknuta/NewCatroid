package org.catrobat.catroid.physics.shapebuilder;

import android.graphics.BitmapFactory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.physics.PhysicsWorldConverter;

import java.util.ArrayList;
import java.util.List;

public final class PhysicsShapeBuilderStrategyConcave implements PhysicsShapeBuilderStrategy {

    private LookData currentLookData;

    public void setLookData(LookData lookData) {
        this.currentLookData = lookData;
    }

    @Override
    public Shape[] build(Pixmap pixmap, float scale) {
        if (pixmap == null) {
            return null;
        }

        int imgWidth = pixmap.getWidth();
        int imgHeight = pixmap.getHeight();

        try {
            com.badlogic.gdx.math.Polygon[] colPolys = null;

            if (currentLookData != null && currentLookData.getCollisionInformation() != null) {
                currentLookData.getCollisionInformation().loadCollisionPolygon();
                colPolys = currentLookData.getCollisionInformation().collisionPolygons;
            }

            if (colPolys != null && colPolys.length > 0) {
                List<Shape> shapes = new ArrayList<>();

                int rawImgWidth = imgWidth;
                int rawImgHeight = imgHeight;

                if (currentLookData != null && currentLookData.getFile() != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(currentLookData.getFile().getAbsolutePath(), options);
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        rawImgWidth = options.outWidth;
                        rawImgHeight = options.outHeight;
                    }
                }

                float centerX = rawImgWidth / 2.0f;
                float centerY = rawImgHeight / 2.0f;

                float scaleFactorX = (float) imgWidth / rawImgWidth;
                float scaleFactorY = (float) imgHeight / rawImgHeight;

                for (com.badlogic.gdx.math.Polygon poly : colPolys) {
                    float[] verts = poly.getVertices();
                    int numPoints = verts.length / 2;

                    if (numPoints >= 3 && numPoints <= 8) {
                        Vector2[] boxCorners = new Vector2[numPoints];
                        for (int i = 0; i < numPoints; i++) {
                            float px = (verts[i * 2] - centerX) * scaleFactorX;
                            float py = (verts[i * 2 + 1] - centerY) * scaleFactorY;

                            boxCorners[i] = PhysicsWorldConverter.convertCatroidToBox2dVector(new Vector2(px, py));
                        }

                        if (calculateSignedArea(boxCorners) < 0) {
                            reverseArray(boxCorners);
                        }

                        float area = Math.abs(calculateSignedArea(boxCorners));
                        if (area > 0.00001f) {
                            PolygonShape polygon = new PolygonShape();
                            polygon.set(boxCorners);
                            polygon.setRadius(0.0001f);
                            shapes.add(polygon);
                        }
                    }
                }

                if (!shapes.isEmpty()) {
                    return shapes.toArray(new Shape[0]);
                }
            }
        } catch (Exception e) {
        }

        return new PhysicsShapeBuilderStrategyFastHull().build(pixmap, scale);
    }

    private float calculateSignedArea(Vector2[] points) {
        float area = 0f;
        int n = points.length;
        for (int i = 0; i < n; i++) {
            Vector2 p1 = points[i];
            Vector2 p2 = points[(i + 1) % n];
            area += p1.x * p2.y - p2.x * p1.y;
        }
        return area * 0.5f;
    }

    private void reverseArray(Vector2[] points) {
        int n = points.length;
        for (int i = 0; i < n / 2; i++) {
            Vector2 temp = points[i];
            points[i] = points[n - 1 - i];
            points[n - 1 - i] = temp;
        }
    }
}
