package de.kronenshooter.core;

import static de.kronenshooter.core.Tuning.*;

/**
 * Schwierigkeit als Funktion der Spielzeit t (s). Bis RAMP_S steigen alle Werte linear vom Start- auf den Endwert
 * (Anteil ramp = t / RAMP_S), danach steigen Tempo und Obergrenze der Trefferpunkte langsam weiter, ohne Obergrenze:
 * Es gibt kein Gewinnen, jede Partie endet irgendwann.
 */
public final class Difficulty {
    private Difficulty() {
    }

    static double ramp(double t) {
        return Math.max(0, Math.min(1, t / RAMP_S));
    }

    /** Minuten nach dem Ende der Steigerung. */
    static double over(double t) {
        return Math.max(0, t - RAMP_S) / 60;
    }

    private static double lerp(double a, double b, double f) {
        return a + (b - a) * f;
    }

    /** Mittlerer Abstand der Wellen in s. */
    public static double gap(double t) {
        return lerp(GAP_START, GAP_END, ramp(t));
    }

    /** Höchstzahl Gegner einer Welle. */
    public static int waveMax(double t) {
        return (int) Math.round(lerp(WAVE_MAX_START, WAVE_MAX_END, ramp(t)));
    }

    /** Faktor auf die Grundgeschwindigkeit der Gegner. */
    public static double speed(double t) {
        return lerp(SPEED_START, SPEED_END, ramp(t)) + SPEED_PER_MIN_AFTER * over(t);
    }

    /** Anteil mittlerer Gegner. */
    public static double weightMedium(double t) {
        return lerp(W_MEDIUM_START, W_MEDIUM_END, ramp(t));
    }

    /** Anteil großer Gegner (0 vor LARGE_FROM_S). */
    public static double weightLarge(double t) {
        return t < LARGE_FROM_S ? 0 : lerp(0, W_LARGE_END, ramp(t));
    }

    /** Höchstzahl großer Gegner gleichzeitig. */
    public static int maxLarge(double t) {
        return t < LARGE_FROM_S ? 0 : t < TWO_LARGE_FROM_S ? 1 : 2;
    }

    /** Obergrenze der Trefferpunkte aller Gegner im Spiel. */
    public static double hpCap(double t) {
        return lerp(HP_CAP_START, HP_CAP_END, ramp(t)) + HP_CAP_PER_MIN_AFTER * over(t);
    }

    /** Anteil pendelnder kleiner Gegner. */
    public static double zigzagChance(double t) {
        if (t < ZIGZAG_FROM_S) return 0;
        return ZIGZAG_MAX * Math.min(1, (t - ZIGZAG_FROM_S) / (RAMP_S - ZIGZAG_FROM_S));
    }
}
