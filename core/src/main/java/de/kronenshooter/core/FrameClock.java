package de.kronenshooter.core;

/**
 * Fester Zeitschritt: wandelt die Zeitpunkte der gezeichneten Bilder in eine Anzahl Logikschritte zu je STEP.
 * Bei 30 Bildern/s sind das 2 Schritte je Bild, bei 60 einer; das Spiel verhält sich bei jeder Bildrate gleich.
 * Lücken über MAX_FRAME_S (Ruckler) werden gekappt, damit nichts springt oder durch einen Gegner hindurchfliegt.
 */
public final class FrameClock {
    public static final double MAX_FRAME_S = 0.1;
    private static final double EPS = 1e-9;

    private long lastNs;
    private boolean started;
    private double acc;

    /** Nächstes Bild zum Zeitpunkt nowNs; Ergebnis: auszuführende Schritte (beim ersten Bild 0). */
    public int advance(long nowNs) {
        if (!started) {
            started = true;
            lastNs = nowNs;
            return 0;
        }
        double dt = (nowNs - lastNs) / 1e9;
        lastNs = nowNs;
        if (dt < 0) dt = 0;
        if (dt > MAX_FRAME_S) dt = MAX_FRAME_S;
        acc += dt;
        int n = (int) ((acc + EPS) / Tuning.STEP);
        acc = Math.max(0, acc - n * Tuning.STEP);
        return n;
    }

    /** Nach einer Pause neu beginnen (die Pausenzeit zählt nicht). */
    public void reset() {
        started = false;
        acc = 0;
    }
}
