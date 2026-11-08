package de.kronenshooter.core;

import static de.kronenshooter.core.Tuning.CENTER;
import static de.kronenshooter.core.Tuning.RADIUS;
import static de.kronenshooter.core.Tuning.SIZE;

/**
 * Geometrie des sichtbaren Spielfelds. Auf runden Uhren ist das der Kreis mit Mittelpunkt (500, 500) und Radius 500,
 * auf eckigen das Quadrat 1000 × 1000. Daraus folgen die Bahn des Schiffs (auf runden Uhren durch die Kreissehne auf
 * Höhe des Schiffs begrenzt), der Bereich für neue Gegner und wann ein Gegner unten hinausgeflogen ist.
 */
public final class Arena {
    public final boolean round;
    /** Bahn des Schiffs (Mittelpunkt). */
    public final double laneMin, laneMax;

    public Arena(boolean round) {
        this.round = round;
        // Sehne an der Unterkante des Schiffs: darunter wird der Kreis schmaler, die unteren Ecken begrenzen
        double half = halfChord(Tuning.SHIP_Y + Tuning.SHIP_H / 2);
        double inset = Tuning.SHIP_W / 2 + Tuning.LANE_MARGIN;
        laneMin = CENTER - half + inset;
        laneMax = CENTER + half - inset;
    }

    /** Halbe Breite des sichtbaren Bereichs auf Höhe y (0 außerhalb). */
    public double halfChord(double y) {
        if (!round) return y >= 0 && y <= SIZE ? SIZE / 2 : 0;
        double dy = y - CENTER;
        double q = RADIUS * RADIUS - dy * dy;
        return q > 0 ? Math.sqrt(q) : 0;
    }

    /** Oberer Rand des sichtbaren Bereichs bei x; NaN, wenn x ganz außerhalb liegt. */
    public double topAt(double x) {
        if (!round) return x >= 0 && x <= SIZE ? 0 : Double.NaN;
        double dx = x - CENTER;
        double q = RADIUS * RADIUS - dx * dx;
        return q >= 0 ? CENTER - Math.sqrt(q) : Double.NaN;
    }

    /** Unterer Rand des sichtbaren Bereichs bei x; NaN, wenn x ganz außerhalb liegt. */
    public double bottomAt(double x) {
        if (!round) return x >= 0 && x <= SIZE ? SIZE : Double.NaN;
        double dx = x - CENTER;
        double q = RADIUS * RADIUS - dx * dx;
        return q >= 0 ? CENTER + Math.sqrt(q) : Double.NaN;
    }

    /** Höchster oberer Rand über der Spanne [x0, x1] (an der Stelle, die dem Mittelpunkt am nächsten liegt). */
    public double minTop(double x0, double x1) {
        return topAt(nearestToCenter(x0, x1));
    }

    /** Tiefster unterer Rand über der Spanne [x0, x1]. */
    public double maxBottom(double x0, double x1) {
        return bottomAt(nearestToCenter(x0, x1));
    }

    private static double nearestToCenter(double x0, double x1) {
        return Math.max(x0, Math.min(CENTER, x1));
    }

    /**
     * Rechteck (Mittelpunkt, Breite, Höhe) ganz unterhalb des sichtbaren Bereichs: die Oberkante liegt überall in
     * seiner Breite unter dem unteren Rand. Auf runden Uhren ist ein Gegner am Rand der Bahn so schon bei einer
     * Oberkante von y ≈ 900 verschwunden und zählt genau dann als hinausgeflogen, nicht erst bei y = 1000.
     */
    public boolean goneBelow(double cx, double cy, double w, double h) {
        double bottom = maxBottom(cx - w / 2, cx + w / 2);
        return bottom != bottom || cy - h / 2 >= bottom;
    }

    /** Irgendein Teil des Rechtecks ist sichtbar. */
    public boolean visible(double cx, double cy, double w, double h) {
        double x0 = cx - w / 2, x1 = cx + w / 2, y0 = cy - h / 2, y1 = cy + h / 2;
        if (!round) return x1 > 0 && x0 < SIZE && y1 > 0 && y0 < SIZE;
        double nx = Math.max(x0, Math.min(CENTER, x1)) - CENTER;
        double ny = Math.max(y0, Math.min(CENTER, y1)) - CENTER;
        return nx * nx + ny * ny < RADIUS * RADIUS;
    }

    public double clampShip(double x) {
        return Math.max(laneMin, Math.min(laneMax, x));
    }

    /**
     * Kleinster Mittelpunkt eines neuen Gegners der Breite w: innerhalb der Bahn des Schiffs (sonst könnte man ihn
     * nicht treffen) und auf Höhe des Schiffs ganz sichtbar.
     */
    public double spawnMin(double w) {
        return Math.max(laneMin, CENTER - halfChord(Tuning.SHIP_Y) + w / 2);
    }

    /** Größter Mittelpunkt eines neuen Gegners der Breite w, siehe spawnMin. */
    public double spawnMax(double w) {
        return Math.min(laneMax, CENTER + halfChord(Tuning.SHIP_Y) - w / 2);
    }
}
