package org.catrobat.catroid.stage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import org.catrobat.catroid.common.ScreenValues;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.UserVariable;
import org.catrobat.catroid.utils.ShowTextUtils;
import org.catrobat.catroid.utils.ShowTextUtils.AndroidStringProvider;

import java.util.List;
import java.util.Locale;

public class ShowTextActor extends Actor {

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

    @Override
    public void draw(Batch batch, float parentAlpha) {
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

    private void generateTexture(String textToDraw) {
        if (cachedTexture != null) {
            cachedTexture.dispose();
            cachedTexture = null;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(ShowTextUtils.sanitizeTextSize(textSize));
        if (typeface != null) paint.setTypeface(typeface);

        if (ShowTextUtils.isValidColorString(colorStr)) {
            int[] rgb = ShowTextUtils.calculateColorRGBs(colorStr.toUpperCase(Locale.getDefault()));
            paint.setColor((0xFF000000) | (rgb[0] << 16) | (rgb[1] << 8) | (rgb[2]));
        } else {
            paint.setColor(Color.BLACK);
        }

        String[] lines = textToDraw.split("\n");
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float totalHeight = lineHeight * lines.length;

        float maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, paint.measureText(line));
        }

        if (maxWidth <= 0 || totalHeight <= 0) return;

        Bitmap bitmap = Bitmap.createBitmap((int) Math.ceil(maxWidth), (int) Math.ceil(totalHeight), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float y = -fm.ascent;
        for (String line : lines) {
            float xOffset = 0;
            float lineWidth = paint.measureText(line);
            if (alignment == ShowTextUtils.ALIGNMENT_STYLE_CENTERED) xOffset = (maxWidth - lineWidth) / 2f;
            else if (alignment == ShowTextUtils.ALIGNMENT_STYLE_RIGHT) xOffset = maxWidth - lineWidth;

            canvas.drawText(line, xOffset, y, paint);
            y += lineHeight;
        }

        cachedTexture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, cachedTexture.getTextureObjectHandle());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();

        drawX = 0;
        if (alignment == ShowTextUtils.ALIGNMENT_STYLE_CENTERED) drawX = -maxWidth / 2f;
        else if (alignment == ShowTextUtils.ALIGNMENT_STYLE_RIGHT) drawX = -maxWidth;
        drawY = -totalHeight / 2f;
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
