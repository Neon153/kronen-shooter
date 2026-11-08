package de.kronenshooter.core;

import java.util.ArrayList;
import java.util.List;

/** Painter für Tests: protokolliert jeden Aufruf. */
final class RecordingPainter implements Painter {
    static final class Op {
        final String kind;
        final int id, color;
        final double x0, y0, x1, y1, size;
        final String text;
        final boolean flag;

        Op(String kind, int id, double x0, double y0, double x1, double y1, double size, int color, String text,
                boolean flag) {
            this.kind = kind;
            this.id = id;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.size = size;
            this.color = color;
            this.text = text;
            this.flag = flag;
        }
    }

    final List<Op> ops = new ArrayList<>();

    @Override
    public void sprite(int id, double cx, double cy, boolean flash) {
        double w = Sprites.w(id) / 2, h = Sprites.h(id) / 2;
        ops.add(new Op("sprite", id, cx - w, cy - h, cx + w, cy + h, 0, 0, null, flash));
    }

    @Override
    public void rect(double x0, double y0, double x1, double y1, int argb) {
        ops.add(new Op("rect", -1, x0, y0, x1, y1, 0, argb, null, false));
    }

    @Override
    public void text(String s, double cx, double baseline, double size, int argb, boolean bold) {
        // grobe Textbox: Breite 0,6 × Größe je Zeichen, Höhe von Oberlänge (0,75 × Größe) bis Grundlinie
        double w = 0.6 * size * s.length() / 2;
        ops.add(new Op("text", -1, cx - w, baseline - 0.75 * size, cx + w, baseline, size, argb, s, bold));
    }

    Op text(String s) {
        for (Op o : ops) if ("text".equals(o.kind) && s.equals(o.text)) return o;
        return null;
    }

    int indexOf(String kind, int id) {
        for (int i = 0; i < ops.size(); i++) if (ops.get(i).kind.equals(kind) && ops.get(i).id == id) return i;
        return -1;
    }

    int lastIndexOf(String kind, int id) {
        for (int i = ops.size() - 1; i >= 0; i--) if (ops.get(i).kind.equals(kind) && ops.get(i).id == id) return i;
        return -1;
    }

    /** Alle vier Ecken im Kreis des runden Displays. */
    static boolean insideCircle(Op o) {
        return in(o.x0, o.y0) && in(o.x1, o.y0) && in(o.x0, o.y1) && in(o.x1, o.y1);
    }

    private static boolean in(double x, double y) {
        double dx = x - 500, dy = y - 500;
        return dx * dx + dy * dy < 500 * 500;
    }
}
