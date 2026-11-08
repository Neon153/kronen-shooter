package de.kronenshooter.core;

/** Fester Speicher für die Geschosse des Schiffs (Mittelpunkt x, y). Bei 6 Schuss/s sind höchstens 4 unterwegs. */
public final class Bullets {
    final double[] x = new double[Tuning.MAX_BULLETS], y = new double[Tuning.MAX_BULLETS];
    final boolean[] alive = new boolean[Tuning.MAX_BULLETS];

    /** Neues Geschoss; Ergebnis Index oder -1, wenn der Speicher voll ist. */
    int add(double bx, double by) {
        for (int i = 0; i < alive.length; i++) {
            if (!alive[i]) {
                alive[i] = true;
                x[i] = bx;
                y[i] = by;
                return i;
            }
        }
        return -1;
    }

    public int count() {
        int n = 0;
        for (boolean a : alive) if (a) n++;
        return n;
    }
}
