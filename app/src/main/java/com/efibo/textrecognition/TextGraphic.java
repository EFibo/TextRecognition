package com.efibo.textrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import com.efibo.textrecognition.GraphicOverlay.Graphic;
import com.google.mlkit.vision.text.Text;

public class TextGraphic extends Graphic {
    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Element element;

    TextGraphic(GraphicOverlay overlay, Text.Element element) {
        super(overlay);
        this.element = element;

        rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.0f);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(54.0f);
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Log.d("TextGraphic", "on draw text graphic");
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text");
        }
        RectF rect = new RectF(element.getBoundingBox());
        canvas.drawRect(rect, rectPaint);
        canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);
    }
}
