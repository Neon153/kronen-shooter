package de.kronenshooter.wear;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import de.kronenshooter.core.Painter;
import de.kronenshooter.core.Sprites;
import de.kronenshooter.core.Tuning;

/**
 * Painter auf einen Canvas. Das Spielfeld (1000 × 1000 E) wird auf das Quadrat in der Mitte der Ansicht skaliert.
 * Die Sprites werden einmal als kleine Bitmaps im Raster angelegt (normal und weiß für den Treffer-Blitz) und beim
 * Zeichnen ohne Filter vergrößert: scharfe Pixel, ein drawBitmap je Objekt. Während des Spiels wird nichts neu
 * angelegt.
 */
final class CanvasPainter implements Painter {
    private final Bitmap[] normal = new Bitmap[Sprites.COUNT], white = new Bitmap[Sprites.COUNT];
    private final Paint fill = new Paint(), text = new Paint(Paint.ANTI_ALIAS_FLAG), sprite = new Paint();
    private final RectF dst = new RectF();
    private Canvas canvas;
    private float scale, ox, oy;

    CanvasPainter() {
        for (int id = 0; id < Sprites.COUNT; id++) {
            int cols = Sprites.cols(id), rows = Sprites.rowCount(id);
            normal[id] = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888);
            white[id] = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int argb = Sprites.pixel(id, c, r);
                    normal[id].setPixel(c, r, argb);
                    white[id].setPixel(c, r, argb == 0 ? 0 : 0xFFFFFFFF);
                }
            }
        }
        sprite.setFilterBitmap(false);
        text.setTextAlign(Paint.Align.CENTER);
        text.setFontFeatureSettings("tnum"); // Ziffern gleich breit: die Punktzahl springt nicht
    }

    /** Größe der Ansicht in Pixeln. */
    void setSize(int w, int h) {
        float side = Math.min(w, h);
        scale = side / (float) Tuning.SIZE;
        ox = (w - side) / 2f;
        oy = (h - side) / 2f;
    }

    void begin(Canvas c) {
        canvas = c;
    }

    private float x(double v) {
        return ox + (float) v * scale;
    }

    private float y(double v) {
        return oy + (float) v * scale;
    }

    @Override
    public void sprite(int id, double cx, double cy, boolean flash) {
        if (scale == 0) return;
        double hw = Sprites.w(id) / 2, hh = Sprites.h(id) / 2;
        dst.set(x(cx - hw), y(cy - hh), x(cx + hw), y(cy + hh));
        canvas.drawBitmap(flash ? white[id] : normal[id], null, dst, sprite);
    }

    @Override
    public void rect(double x0, double y0, double x1, double y1, int argb) {
        if (scale == 0) return;
        fill.setColor(argb);
        canvas.drawRect(x(x0), y(y0), x(x1), y(y1), fill);
    }

    @Override
    public void text(String s, double cx, double baseline, double size, int argb, boolean bold) {
        if (scale == 0) return;
        text.setColor(argb);
        text.setTextSize((float) size * scale);
        text.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        canvas.drawText(s, x(cx), y(baseline), text);
    }
}
