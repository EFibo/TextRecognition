package com.efibo.textrecognition;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private int previewWidth;
    private float widthScaleFactor = 1.0f;
    private int previewHeigth;
    private float heigthScaleFactor = 1.0f;
    private int facing = CameraCharacteristics.LENS_FACING_BACK;
    private Set<Graphic> graphics = new HashSet<>();

    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal) {
            return horizontal * overlay.widthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * overlay.heigthScaleFactor;
        }

        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }

        public float translateX(float x) {
            if (overlay.facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return overlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    public void setCameraInfo(int previewWidth, int previewHeigth, int facing) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeigth = previewHeigth;
            this.facing = facing;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (lock) {
            if ((previewWidth != 0) && (previewHeigth != 0)) {
                widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
                heigthScaleFactor = (float) canvas.getHeight() / (float) previewHeigth;
            }
            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}