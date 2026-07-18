package org.catrobat.catroid.sensing;

import android.graphics.PointF;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.danvexteam.lunoscript_annotations.LunoClass;

import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.content.Look;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Scene;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.utils.NativeLookOptimizer;

import java.util.ArrayList;
import java.util.List;

@LunoClass
public final class CollisionDetection {


	private static final Vector2 intersectV1 = new Vector2();
	private static final Vector2 intersectV2 = new Vector2();
	private static final Vector2 edgeFirstPoint = new Vector2();
	private static final Vector2 edgeSecondPoint = new Vector2();
	private static final Vector2 fingerStart = new Vector2();
	private static final Vector2 fingerEnd = new Vector2();
	private static final Vector2 fingerCenter = new Vector2();

	private static final Circle fingerCollisionCircle = new Circle();

	private CollisionDetection() {
	}

    public static boolean checkCollisionBetweenLooks(Look firstLook, Look secondLook) {
        if (firstLook == null || secondLook == null ||
                !firstLook.isVisible() || !firstLook.isLookVisible() ||
                !secondLook.isVisible() || !secondLook.isLookVisible()) {
            return false;
        }

        Rectangle firstHitbox = firstLook.getHitbox();
        Rectangle secondHitbox = secondLook.getHitbox();
        if (firstHitbox == null || secondHitbox == null || !firstHitbox.overlaps(secondHitbox)) {
            return false;
        }

        Polygon[] firstPolygons = firstLook.getCurrentCollisionPolygon();
        Polygon[] secondPolygons = secondLook.getCurrentCollisionPolygon();

        if (firstPolygons == null || secondPolygons == null || firstPolygons.length == 0 || secondPolygons.length == 0) {
            return false;
        }

        if (NativeLookOptimizer.isWorking) {
            float[][] firstPreparedPolys = new float[firstPolygons.length][];
            for (int i = 0; i < firstPolygons.length; i++) {
                firstPreparedPolys[i] = (firstPolygons[i] != null) ? firstPolygons[i].getTransformedVertices() : new float[0];
            }

            float[][] secondPreparedPolys = new float[secondPolygons.length][];
            for (int i = 0; i < secondPolygons.length; i++) {
                secondPreparedPolys[i] = (secondPolygons[i] != null) ? secondPolygons[i].getTransformedVertices() : new float[0];
            }

            return NativeLookOptimizer.checkSingleCollision(firstPreparedPolys, secondPreparedPolys);
        } else {
            return checkCollisionBetweenPolygonArrays(firstPolygons, secondPolygons);
        }
    }


	private static boolean checkCollisionBetweenPolygonArrays(Polygon[] first, Polygon[] second) {

		Rectangle[] firstBoxes = createBoundingBoxesOfCollisionPolygons(first);
		Rectangle[] secondBoxes = createBoundingBoxesOfCollisionPolygons(second);


		for (int firstIndex = 0; firstIndex < first.length; firstIndex++) {
			for (int secondIndex = 0; secondIndex < second.length; secondIndex++) {

				if (firstBoxes[firstIndex].overlaps(secondBoxes[secondIndex])) {

					if (intersectPolygons(first[firstIndex], second[secondIndex])) {
						return true;
					}
				}
			}
		}



		if (checkContainment(first, second, firstBoxes, secondBoxes)) {
			return true;
		}

		return false;
	}



	private static boolean checkCollisionBetweenPolygonArraysSAT(Polygon[] first, Polygon[] second, Rectangle[] firstBoxes, Rectangle[] secondBoxes) {
		if (first == null || second == null) return false;

		for (int firstIndex = 0; firstIndex < first.length; firstIndex++) {
			Polygon firstPolygon = first[firstIndex];

			if (firstPolygon == null || firstPolygon.getTransformedVertices() == null || firstPolygon.getTransformedVertices().length < 6) {
				continue;
			}

			for (int secondIndex = 0; secondIndex < second.length; secondIndex++) {
				Polygon secondPolygon = second[secondIndex];
				if (secondPolygon == null || secondPolygon.getTransformedVertices() == null || secondPolygon.getTransformedVertices().length < 6) {
					continue;
				}


				if (firstBoxes[firstIndex] != null && secondBoxes[secondIndex] != null &&
						firstBoxes[firstIndex].overlaps(secondBoxes[secondIndex]))
				{


					if (Intersector.overlapConvexPolygons(firstPolygon, secondPolygon)) {
						return true;
					}
				}
			}
		}

		return false;
	}


	private static Rectangle[] createBoundingBoxesOfCollisionPolygons(Polygon[] polygons) {
		Rectangle[] boundingBoxes = new Rectangle[polygons.length];
		for (int i = 0; i < polygons.length; i++) {

			if (polygons[i] != null) {
				boundingBoxes[i] = polygons[i].getBoundingRectangle();
			} else {


				boundingBoxes[i] = new Rectangle();
			}
		}
		return boundingBoxes;
	}

	/**
	 * Checks if any edge of the first polygon intersects with the second polygon.
	 * Note: This checks intersection of first's edges against second polygon area.
	 * LibGDX's intersectSegmentPolygon is generally robust enough.
	 */
	public static boolean intersectPolygons(Polygon first, Polygon second) {
		if (first == null || second == null) return false;

		float[] firstVertices = first.getTransformedVertices();
		int firstLength = firstVertices.length;


		if (firstLength < 4) {
			return false;
		}


		Vector2 v1 = CollisionDetection.intersectV1;
		Vector2 v2 = CollisionDetection.intersectV2;

		for (int firstIndex = 0; firstIndex < firstLength; firstIndex += 2) {
			v1.x = firstVertices[firstIndex];
			v1.y = firstVertices[firstIndex + 1];

			v2.x = firstVertices[(firstIndex + 2) % firstLength];
			v2.y = firstVertices[(firstIndex + 3) % firstLength];


			if (Intersector.intersectSegmentPolygon(v1, v2, second)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Checks if any polygon from the 'inner' set is contained within any polygon
	 * from the 'outer' set. Uses bounding box checks for a quick exit.
	 * Checks containment by testing if the *first vertex* of an inner polygon
	 * lies within an outer polygon. This is a heuristic, more robust than the original,
	 * but still not perfectly accurate for all complex containment scenarios involving multiple polygons.
	 * It assumes that if containment exists, at least one vertex will be inside.
	 */
	private static boolean checkVertexContainment(Polygon[] inner, Polygon[] outer, Rectangle[] innerBoxes, Rectangle[] outerBoxes) {
		for (int i = 0; i < inner.length; i++) {
			Polygon innerPolygon = inner[i];
			if (innerPolygon == null) continue;

			float[] innerVertices = innerPolygon.getTransformedVertices();

			if (innerVertices.length < 2) {
				continue;
			}
			float testX = innerVertices[0];
			float testY = innerVertices[1];




			boolean potentiallyContained = false;
			for(Rectangle outerBox : outerBoxes) {
				if (outerBox != null && outerBox.contains(testX, testY)) {
					potentiallyContained = true;
					break;
				}
			}
			if (!potentiallyContained) continue;


			for (int j = 0; j < outer.length; j++) {
				Polygon outerPolygon = outer[j];
				if (outerPolygon == null) continue;


				if (innerBoxes[i] != null && outerBoxes[j] != null && innerBoxes[i].overlaps(outerBoxes[j])) {

					if (outerPolygon.contains(testX, testY)) {




						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * New containment check function replacing the old checkCollisionForPolygonsInPolygons.
	 * Checks both directions: first inside second, and second inside first.
	 */
	private static boolean checkContainment(Polygon[] first, Polygon[] second, Rectangle[] firstBoxes, Rectangle[] secondBoxes) {

		if (checkVertexContainment(first, second, firstBoxes, secondBoxes)) {
			return true;
		}

		if (checkVertexContainment(second, first, secondBoxes, firstBoxes)) {
			return true;
		}
		return false;
	}





	public static String getSecondSpriteNameFromCollisionFormulaString(String formula, Project currentProject) {
		if (currentProject == null || formula == null) return null;

		int indexOfSpriteInFormula = formula.length();
		String secondSpriteName = null;



		for (Scene scene : currentProject.getSceneList()) {
			if (scene == null) continue;
			for (Sprite sprite : scene.getSpriteList()) {
				if (sprite == null || sprite.getName() == null) continue;

				if (formula.endsWith(sprite.getName())) {
					int index = formula.lastIndexOf(sprite.getName());


					if (index >= 0 && index < indexOfSpriteInFormula) {
						indexOfSpriteInFormula = index;
						secondSpriteName = sprite.getName();
					}
				}
			}
		}

		return secondSpriteName;
	}

	public static boolean collidesWithEdge(Polygon[] currentCollisionPolygon, Rectangle screen) {
		if (currentCollisionPolygon == null || screen == null) return false;


		Vector2 firstPoint = CollisionDetection.edgeFirstPoint;
		Vector2 secondPoint = CollisionDetection.edgeSecondPoint;


		for (Polygon polygon : currentCollisionPolygon) {
			if (polygon == null) continue;
			float[] transformedVertices = polygon.getTransformedVertices();
			int len = transformedVertices.length;

			if (len < 4) continue;

			for (int i = 0; i < len; i += 2) {
				firstPoint.set(transformedVertices[i], transformedVertices[i + 1]);

				secondPoint.set(transformedVertices[(i + 2) % len], transformedVertices[(i + 3) % len]);


				boolean firstIn = screen.contains(firstPoint);
				boolean secondIn = screen.contains(secondPoint);

				if (firstIn != secondIn) {
					return true;
				}




				if (!firstIn && !secondIn) {
					if (Intersector.intersectSegmentRectangle(firstPoint, secondPoint, screen)) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public static double collidesWithFinger(Polygon[] currentCollisionPolygon, ArrayList<PointF> touchingPoints) {
		if (currentCollisionPolygon == null || touchingPoints == null || touchingPoints.isEmpty()) {
			return 0d;
		}


		Vector2 start = CollisionDetection.fingerStart;
		Vector2 end = CollisionDetection.fingerEnd;
		Vector2 center = CollisionDetection.fingerCenter;
		float touchRadius = Constants.COLLISION_WITH_FINGER_TOUCH_RADIUS;
		float touchRadiusSq = touchRadius * touchRadius;

		for (PointF point : touchingPoints) {
			if (point == null) continue;
			center.set(point.x, point.y);
			boolean intersected = false;
			int containedInCount = 0;

			for (Polygon polygon : currentCollisionPolygon) {
				if (polygon == null) continue;


				Rectangle boundingRectangle = polygon.getBoundingRectangle();


				fingerCollisionCircle.set(center.x, center.y, touchRadius);
				if (!Intersector.overlaps(fingerCollisionCircle, boundingRectangle)) {
					continue;
				}



				float[] vertices = polygon.getTransformedVertices();
				int numVertices = vertices.length / 2;
				if (numVertices < 2) continue;

				for (int i = 0; i < numVertices; i++) {
					int i2 = (i + 1) % numVertices;
					start.set(vertices[i * 2], vertices[i * 2 + 1]);
					end.set(vertices[i2 * 2], vertices[i2 * 2 + 1]);

					if (Intersector.intersectSegmentCircle(start, end, center, touchRadiusSq)) {
						intersected = true;
						break;
					}
				}

				if (intersected) {
					break;
				}



				if (polygon.contains(center.x, center.y)) {
					containedInCount++;
				}
			}

			if (intersected) {
				return 1d;
			}



			if (containedInCount % 2 != 0) {
				return 1d;
			}

		}

		return 0d;
	}

	public static List<Sprite[]> findAllCollisions(List<Sprite> sprites) {
		if (sprites == null || sprites.size() < 2) {
			return new ArrayList<>();
		}



		List<Sprite> activeSprites = new ArrayList<>();
		for (Sprite sprite : sprites) {
			if (sprite != null && sprite.look != null && sprite.look.isVisible() && sprite.look.isLookVisible()) {
				activeSprites.add(sprite);
			}
		}

		if (activeSprites.size() < 2) {
			return new ArrayList<>();
		}

		float[][][] allPolygons = new float[activeSprites.size()][][];
		for (int i = 0; i < activeSprites.size(); i++) {
			Polygon[] lookPolygons = activeSprites.get(i).look.getCurrentCollisionPolygon();
			if (lookPolygons == null) {
				allPolygons[i] = new float[0][];
				continue;
			}
			allPolygons[i] = new float[lookPolygons.length][];
			for (int j = 0; j < lookPolygons.length; j++) {
				if (lookPolygons[j] != null) {
					allPolygons[i][j] = lookPolygons[j].getTransformedVertices();
				} else {
					allPolygons[i][j] = new float[0];
				}
			}
		}

		int[] collidingPairs = NativeLookOptimizer.checkAllCollisions(allPolygons);


		List<Sprite[]> result = new ArrayList<>();
		if (collidingPairs != null) {
			for (int i = 0; i < collidingPairs.length; i += 2) {
				Sprite sprite1 = activeSprites.get(collidingPairs[i]);
				Sprite sprite2 = activeSprites.get(collidingPairs[i+1]);
				result.add(new Sprite[]{sprite1, sprite2});
			}
		}
		return result;
	}
}
