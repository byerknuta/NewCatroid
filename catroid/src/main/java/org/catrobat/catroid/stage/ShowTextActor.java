package org.catrobat.catroid.stage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import org.catrobat.catroid.CatroidApplication;
import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.utils.ShowTextUtils;
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider;

import java.util.List;
import java.util.Locale;

public class ShowTextActor extends Actor {

    private static int maxGpuTextureSize = 0;

    private float textSize;
    private float xPosition;
    private float yPosition;
    private String colorStr;
    private UserVariable variableToShow;
    private String variableNameToCompare;
    private String rawText;
    private int alignment;
    private Sprite sprite;
    private AndroidStringProvider androidStringProvider;
    private Typeface typeface;
    private boolean isText;

    private Texture cachedTexture;
    private String lastRenderedText = null;
    private float lastTextSize = -1f;
    private String lastColor = null;
    private Typeface lastTypeface = null;
    private boolean needsTextureUpdate = true;
    private float drawX, drawY;

    private boolean drawOnlyInBuffer = false;

    public ShowTextActor(Boolean isText, UserVariable userVariable, String rawText, float xPosition, float yPosition, float relativeSize,
                         String color, Sprite sprite, int alignment, AndroidStringProvider androidStringProvider) {
        this.isText = isText;
        this.variableToShow = userVariable;
        this.variableNameToCompare = userVariable != null ? userVariable.getName() : rawText;
        this.rawText = rawText;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.textSize = ShowTextUtils.DEFAULT_TEXT_SIZE * relativeSize;
        this.colorStr = color;
        this.sprite = sprite;
        this.alignment = alignment;
        this.androidStringProvider = androidStringProvider;
    }

    private static int getMaxGpuTextureSize() {
        if (maxGpuTextureSize <= 0) {
            try {
                int[] params = new int[1];
                GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, params, 0);
                if (params[0] > 0) {
                    maxGpuTextureSize = params[0];
                } else {
                    maxGpuTextureSize = 8192;
                }
            } catch (Exception e) {
                maxGpuTextureSize = 8192;
            }
        }
        return maxGpuTextureSize;
    }

    public String getVariableNameToCompare() {
        return variableNameToCompare;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setFont(Typeface typeface) {
        if (this.typeface != typeface) {
            this.typeface = typeface;
            this.needsTextureUpdate = true;
        }
    }

    public void updateProperties(String newText, float x, float y, float relativeSize, String newColor, Typeface newFont) {
        this.xPosition = x;
        this.yPosition = y;

        float newSize = ShowTextUtils.DEFAULT_TEXT_SIZE * relativeSize;
        if (this.textSize != newSize || !this.colorStr.equals(newColor) || this.typeface != newFont || (isText && !this.rawText.equals(newText))) {
            this.rawText = newText;
            this.textSize = newSize;
            this.colorStr = newColor;
            this.typeface = newFont;
            this.needsTextureUpdate = true;
        }
    }

    private String getCurrentTextValue() {
        if (isText) return rawText;
        if (variableToShow != null && variableToShow.isDummy()) {
            return CatroidApplication.getAppContext().getString(R.string.no_variable_selected);
        }

        ProjectManager pm = ProjectManager.getInstance();
        UserVariable foundVar = findVariable(pm.getCurrentProject().getUserVariables());
        if (foundVar == null) foundVar = findVariable(pm.getCurrentProject().getMultiplayerVariables());
        if (foundVar == null) foundVar = findVariable(sprite.getUserVariables());

        if (foundVar != null && foundVar.getVisible()) {
            Object value = foundVar.getValue();
            if (value instanceof Boolean) return androidStringProvider.getTrueOrFalse((Boolean) value);
            String valStr = value.toString();
            return ShowTextUtils.isNumberAndInteger(valStr) ? ShowTextUtils.getStringAsInteger(valStr) : valStr;
        }
        return null;
    }

    private UserVariable findVariable(List<UserVariable> list) {
        if (list == null) return null;
        for (UserVariable v : list) {
            if (v.getName().equals(variableNameToCompare)) return v;
        }
        return null;
    }

    public boolean isDrawOnlyInBuffer() {
        return drawOnlyInBuffer;
    }

    public void setDrawOnlyInBuffer(boolean drawOnlyInBuffer) {
        this.drawOnlyInBuffer = drawOnlyInBuffer;
        this.needsTextureUpdate = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (drawOnlyInBuffer && !org.catrobat.catroid.content.RenderTextureManager.INSTANCE.isRenderingToBuffer()) {
            return;
        }

        String currentText = getCurrentTextValue();
        if (currentText == null || currentText.isEmpty()) return;

        if (needsTextureUpdate || !currentText.equals(lastRenderedText)) {
            generateTexture(currentText);
            lastRenderedText = currentText;
            needsTextureUpdate = false;
        }

        if (cachedTexture != null) {
            batch.setColor(1, 1, 1, parentAlpha);
            batch.draw(cachedTexture,
                    xPosition + drawX, yPosition + drawY,
                    -drawX, -drawY,
                    cachedTexture.getWidth(), cachedTexture.getHeight(),
                    1f, 1f, getRotation(),
                    0, 0, cachedTexture.getWidth(), cachedTexture.getHeight(),
                    false, false);
        }
    }

    private int parseColorWithAlpha(String colorStr) {
        if (colorStr == null || colorStr.trim().isEmpty()) {
            return Color.BLACK;
        }

        String cleanColor = colorStr.trim().toUpperCase(Locale.getDefault());

        if (!cleanColor.startsWith("#")) {
            if (cleanColor.matches("^[0-9A-FA-F]{6}$") || cleanColor.matches("^[0-9A-FA-F]{8}$")) {
                cleanColor = "#" + cleanColor;
            }
        }

        if (cleanColor.startsWith("#")) {
            String hex = cleanColor.substring(1);
            if (hex.length() == 6) {
                try {
                    return Color.parseColor(cleanColor);
                } catch (Exception ignored) {}
            } else if (hex.length() == 8) {
                try {
                    return Color.parseColor(cleanColor);
                } catch (IllegalArgumentException e) {
                    try {
                        String argb = "#" + hex.substring(6, 8) + hex.substring(0, 6);
                        return Color.parseColor(argb);
                    } catch (Exception ignored) {}
                }
            }
        }

        try {
            return Color.parseColor(cleanColor);
        } catch (Exception e) {
            if (ShowTextUtils.isValidColorString(colorStr)) {
                int[] rgb = ShowTextUtils.calculateColorRGBs(cleanColor);
                return (0xFF000000) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]);
            }
        }

        return Color.BLACK;
    }

    private void generateTexture(String textToDraw) {
        if (cachedTexture != null) {
            cachedTexture.dispose();
            cachedTexture = null;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float sanitizedSize = ShowTextUtils.sanitizeTextSize(textSize);
        paint.setTextSize(sanitizedSize);
        if (typeface != null) paint.setTypeface(typeface);

        paint.setColor(parseColorWithAlpha(colorStr));

        String[] lines = textToDraw.split("\n");
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float totalHeight = lineHeight * lines.length;

        float maxWidth = 0;
        Rect bounds = new Rect();

        for (String line : lines) {
            float measureW = paint.measureText(line);
            paint.getTextBounds(line, 0, line.length(), bounds);
            float boundsW = bounds.width();
            float lineW = Math.max(measureW, boundsW);
            maxWidth = Math.max(maxWidth, lineW);
        }

        if (maxWidth <= 0 || totalHeight <= 0) return;

        int paddingX = Math.max(32, (int) (sanitizedSize * 0.4f));
        int paddingY = Math.max(16, (int) (sanitizedSize * 0.2f));

        int maxAllowedSize = getMaxGpuTextureSize();
        int bitmapWidth = Math.min(maxAllowedSize, (int) Math.ceil(maxWidth) + (paddingX * 2));
        int bitmapHeight = Math.min(maxAllowedSize, (int) Math.ceil(totalHeight) + (paddingY * 2));

        try {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            float y = -fm.ascent + paddingY;
            for (String line : lines) {
                float xOffset = paddingX;
                float lineWidth = paint.measureText(line);

                if (alignment == ShowTextUtils.ALIGNMENT_STYLE_CENTERED) {
                    xOffset = (bitmapWidth - lineWidth) / 2f;
                } else if (alignment == ShowTextUtils.ALIGNMENT_STYLE_RIGHT) {
                    xOffset = bitmapWidth - paddingX - lineWidth;
                }

                if (xOffset < 0) {
                    xOffset = 0;
                }

                canvas.drawText(line, xOffset, y, paint);
                y += lineHeight;
            }

            cachedTexture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cachedTexture.getTextureObjectHandle());
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            bitmap.recycle();

            if (alignment == ShowTextUtils.ALIGNMENT_STYLE_CENTERED) {
                drawX = -bitmapWidth / 2f;
            } else if (alignment == ShowTextUtils.ALIGNMENT_STYLE_RIGHT) {
                drawX = -(bitmapWidth - paddingX);
            } else {
                drawX = -paddingX;
            }
            drawY = -bitmapHeight / 2f;

        } catch (Throwable t) {
            android.util.Log.e("ShowTextActor", "Failed to generate text texture safely: size="
                    + bitmapWidth + "x" + bitmapHeight, t);

            if (cachedTexture != null) {
                try {
                    cachedTexture.dispose();
                } catch (Exception ignored) {}
                cachedTexture = null;
            }
        }
    }

    @Override
    public boolean remove() {
        if (cachedTexture != null) {
            cachedTexture.dispose();
            cachedTexture = null;
        }
        return super.remove();
    }
}
