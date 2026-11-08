package de.kronenshooter.core;

/**
 * Zeichen-Schnittstelle, Koordinaten in E (Spielfeld 1000 × 1000). Auf der Uhr zeichnet CanvasPainter auf einen
 * Canvas, im Test protokolliert RecordingPainter jeden Aufruf, für Vorschaubilder zeichnet AwtPainter in ein PNG.
 */
public interface Painter {
    /** Sprite (Sprites.SHIP …) mit Mittelpunkt (cx, cy) in seiner logischen Größe; flash = weiß (Treffer). */
    void sprite(int id, double cx, double cy, boolean flash);

    /** Gefülltes Rechteck. */
    void rect(double x0, double y0, double x1, double y1, int argb);

    /** Text, waagerecht zentriert auf cx, Grundlinie bei baseline, Schriftgröße size (E). */
    void text(String s, double cx, double baseline, double size, int argb, boolean bold);
}
