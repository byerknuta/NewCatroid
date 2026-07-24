package org.catrobat.catroid.utils;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;
import java.util.List;

public final class PolygonDecomposer {

    private PolygonDecomposer() {
    }

    public static Polygon[] decompose(List<Polygon> inputPolygons) {
        if (inputPolygons == null || inputPolygons.isEmpty()) {
            return new Polygon[0];
        }

        List<Polygon> cleanedInputs = new ArrayList<>();
        for (Polygon p : inputPolygons) {
            Polygon cleaned = cleanPolygon(p);
            if (cleaned != null && cleaned.getVertices().length >= 6) {
                cleanedInputs.add(cleaned);
            }
        }

        if (cleanedInputs.isEmpty()) {
            return new Polygon[0];
        }

        List<Polygon> outerPolygons = new ArrayList<>();
        List<List<Polygon>> holesForOuter = new ArrayList<>();

        for (Polygon poly : cleanedInputs) {
            int parentIndex = findParentPolygonIndex(poly, cleanedInputs);
            if (parentIndex == -1) {
                outerPolygons.add(poly);
                holesForOuter.add(new ArrayList<>());
            } else {
                Polygon parent = cleanedInputs.get(parentIndex);
                int outerIdx = outerPolygons.indexOf(parent);
                if (outerIdx != -1) {
                    holesForOuter.get(outerIdx).add(poly);
                } else {
                    outerPolygons.add(poly);
                    holesForOuter.add(new ArrayList<>());
                }
            }
        }

        List<Polygon> resultList = new ArrayList<>();

        for (int i = 0; i < outerPolygons.size(); i++) {
            Polygon outer = outerPolygons.get(i);
            List<Polygon> holes = holesForOuter.get(i);

            if (holes.isEmpty()) {
                resultList.addAll(triangulatePolygon(outer));
            } else {
                Polygon merged = outer;
                for (Polygon hole : holes) {
                    merged = mergeHoleIntoOuter(merged, hole);
                }
                merged = cleanPolygon(merged);
                if (merged != null && merged.getVertices().length >= 6) {
                    resultList.addAll(triangulatePolygon(merged));
                }
            }
        }

        if (resultList.isEmpty()) {
            for (Polygon p : cleanedInputs) {
                resultList.addAll(triangulatePolygon(p));
            }
        }

        return resultList.toArray(new Polygon[0]);
    }

    private static List<Polygon> triangulatePolygon(Polygon poly) {
        List<Polygon> tris = new ArrayList<>();
        if (poly == null) return tris;
        float[] verts = poly.getVertices();
        if (verts.length < 6) return tris;

        if (verts.length == 6) {
            tris.add(poly);
            return tris;
        }

        try {
            EarClippingTriangulator triangulator = new EarClippingTriangulator();
            ShortArray indices = triangulator.computeTriangles(verts);

            if (indices.size >= 3) {
                for (int t = 0; t < indices.size; t += 3) {
                    int i1 = indices.get(t) * 2;
                    int i2 = indices.get(t + 1) * 2;
                    int i3 = indices.get(t + 2) * 2;

                    float[] triVerts = {
                            verts[i1], verts[i1 + 1],
                            verts[i2], verts[i2 + 1],
                            verts[i3], verts[i3 + 1]
                    };

                    if (Math.abs(calculateSignedArea(triVerts)) > 0.0001f) {
                        tris.add(new Polygon(triVerts));
                    }
                }
                if (!tris.isEmpty()) {
                    return tris;
                }
            }
        } catch (Exception ignored) {
        }

        return triangulateByFan(poly);
    }

    private static List<Polygon> triangulateByFan(Polygon poly) {
        List<Polygon> tris = new ArrayList<>();
        float[] verts = poly.getVertices();
        int numPoints = verts.length / 2;
        if (numPoints < 3) return tris;

        float x0 = verts[0], y0 = verts[1];
        for (int i = 1; i < numPoints - 1; i++) {
            float[] tri = {
                    x0, y0,
                    verts[i * 2], verts[i * 2 + 1],
                    verts[(i + 1) * 2], verts[(i + 1) * 2 + 1]
            };
            if (Math.abs(calculateSignedArea(tri)) > 0.0001f) {
                tris.add(new Polygon(tri));
            }
        }
        return tris;
    }

    private static Polygon cleanPolygon(Polygon poly) {
        if (poly == null) return null;
        float[] verts = poly.getVertices();
        if (verts.length < 6) return null;

        List<Float> cleanList = new ArrayList<>();
        int numPoints = verts.length / 2;

        for (int i = 0; i < numPoints; i++) {
            float x1 = verts[i * 2];
            float y1 = verts[i * 2 + 1];

            int nextIdx = (i + 1) % numPoints;
            float x2 = verts[nextIdx * 2];
            float y2 = verts[nextIdx * 2 + 1];

            float dx = x2 - x1;
            float dy = y2 - y1;
            if (Math.hypot(dx, dy) > 0.01f) {
                cleanList.add(x1);
                cleanList.add(y1);
            }
        }

        if (cleanList.size() < 6) return null;

        float[] cleanArray = new float[cleanList.size()];
        for (int i = 0; i < cleanList.size(); i++) {
            cleanArray[i] = cleanList.get(i);
        }

        if (calculateSignedArea(cleanArray) < 0) {
            reverseVertices(cleanArray);
        }

        return new Polygon(cleanArray);
    }

    private static float calculateSignedArea(float[] verts) {
        float area = 0f;
        int numPoints = verts.length / 2;
        for (int i = 0; i < numPoints; i++) {
            int j = (i + 1) % numPoints;
            area += verts[i * 2] * verts[j * 2 + 1];
            area -= verts[j * 2] * verts[i * 2 + 1];
        }
        return area * 0.5f;
    }

    private static void reverseVertices(float[] verts) {
        int numPoints = verts.length / 2;
        for (int i = 0; i < numPoints / 2; i++) {
            int opposite = numPoints - 1 - i;

            float tempX = verts[i * 2];
            float tempY = verts[i * 2 + 1];

            verts[i * 2] = verts[opposite * 2];
            verts[i * 2 + 1] = verts[opposite * 2 + 1];

            verts[opposite * 2] = tempX;
            verts[opposite * 2 + 1] = tempY;
        }
    }

    private static int findParentPolygonIndex(Polygon candidateHole, List<Polygon> allPolygons) {
        float[] verts = candidateHole.getVertices();
        if (verts.length < 2) return -1;

        float testX = verts[0];
        float testY = verts[1];

        int parentIdx = -1;

        for (int i = 0; i < allPolygons.size(); i++) {
            Polygon other = allPolygons.get(i);
            if (other == candidateHole) continue;

            if (other.contains(testX, testY)) {
                parentIdx = i;
            }
        }
        return parentIdx;
    }

    private static Polygon mergeHoleIntoOuter(Polygon outer, Polygon hole) {
        float[] outerVerts = outer.getVertices();
        float[] holeVerts = hole.getVertices();

        if (outerVerts.length < 6 || holeVerts.length < 6) return outer;

        int bestOuterIdx = 0;
        int bestHoleIdx = 0;
        float minDistSq = Float.MAX_VALUE;

        for (int o = 0; o < outerVerts.length; o += 2) {
            float ox = outerVerts[o];
            float oy = outerVerts[o + 1];

            for (int h = 0; h < holeVerts.length; h += 2) {
                float hx = holeVerts[h];
                float hy = holeVerts[h + 1];

                float dx = ox - hx;
                float dy = oy - hy;
                float distSq = dx * dx + dy * dy;

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    bestOuterIdx = o;
                    bestHoleIdx = h;
                }
            }
        }

        int outerNumPoints = outerVerts.length / 2;
        int holeNumPoints = holeVerts.length / 2;

        int oPointIdx = bestOuterIdx / 2;
        int hPointIdx = bestHoleIdx / 2;

        List<Float> mergedList = new ArrayList<>();

        for (int p = 0; p <= oPointIdx; p++) {
            mergedList.add(outerVerts[p * 2]);
            mergedList.add(outerVerts[p * 2 + 1]);
        }

        for (int p = 0; p < holeNumPoints; p++) {
            int idx = (hPointIdx + p) % holeNumPoints;
            mergedList.add(holeVerts[idx * 2]);
            mergedList.add(holeVerts[idx * 2 + 1]);
        }

        mergedList.add(holeVerts[hPointIdx * 2]);
        mergedList.add(holeVerts[hPointIdx * 2 + 1]);

        mergedList.add(outerVerts[oPointIdx * 2]);
        mergedList.add(outerVerts[oPointIdx * 2 + 1]);

        for (int p = oPointIdx + 1; p < outerNumPoints; p++) {
            mergedList.add(outerVerts[p * 2]);
            mergedList.add(outerVerts[p * 2 + 1]);
        }

        float[] mergedArray = new float[mergedList.size()];
        for (int i = 0; i < mergedList.size(); i++) {
            mergedArray[i] = mergedList.get(i);
        }

        return new Polygon(mergedArray);
    }

    public static Array<Polygon> decompose(Polygon concavePolygon) {
        List<Polygon> singleList = new ArrayList<>();
        singleList.add(concavePolygon);
        Polygon[] res = decompose(singleList);
        Array<Polygon> gdxArray = new Array<>();
        for (Polygon p : res) {
            gdxArray.add(p);
        }
        return gdxArray;
    }
}
