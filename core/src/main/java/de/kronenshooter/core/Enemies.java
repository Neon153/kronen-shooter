package de.kronenshooter.core;

/**
 * Fester Speicher für die Gegner. Je Gegner: Art, Mittelpunkt, Geschwindigkeit nach unten, verbleibende
 * Trefferpunkte, Restzeit des Treffer-Blitzes und für pendelnde Gegner Grundposition und Phase.
 */
public final class Enemies {
    final EnemyType[] type = new EnemyType[Tuning.MAX_ENEMIES];
    final double[] x = new double[Tuning.MAX_ENEMIES], y = new double[Tuning.MAX_ENEMIES];
    final double[] vy = new double[Tuning.MAX_ENEMIES], flash = new double[Tuning.MAX_ENEMIES];
    final double[] baseX = new double[Tuning.MAX_ENEMIES], phase = new double[Tuning.MAX_ENEMIES];
    final int[] hp = new int[Tuning.MAX_ENEMIES];
    final boolean[] alive = new boolean[Tuning.MAX_ENEMIES], zigzag = new boolean[Tuning.MAX_ENEMIES];

    /** Neuer Gegner; Ergebnis Index oder -1, wenn der Speicher voll ist. */
    int add(EnemyType t, double ex, double ey, double speed, boolean zig, double ph) {
        for (int i = 0; i < alive.length; i++) {
            if (!alive[i]) {
                alive[i] = true;
                type[i] = t;
                x[i] = ex;
                baseX[i] = ex;
                y[i] = ey;
                vy[i] = speed;
                hp[i] = t.hp;
                flash[i] = 0;
                zigzag[i] = zig;
                phase[i] = ph;
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

    public int count(EnemyType t) {
        int n = 0;
        for (int i = 0; i < alive.length; i++) if (alive[i] && type[i] == t) n++;
        return n;
    }

    /** Summe der verbleibenden Trefferpunkte aller Gegner im Spiel. */
    public int hpTotal() {
        int n = 0;
        for (int i = 0; i < alive.length; i++) if (alive[i]) n += hp[i];
        return n;
    }

    /** Überschneidet das Rechteck (Mittelpunkt, Größe, plus margin rundum) einen vorhandenen Gegner? */
    boolean overlaps(double cx, double cy, double w, double h, double margin) {
        for (int i = 0; i < alive.length; i++) {
            if (!alive[i]) continue;
            EnemyType t = type[i];
            if (Math.abs(x[i] - cx) < (t.w + w) / 2 + margin && Math.abs(y[i] - cy) < (t.h + h) / 2 + margin) {
                return true;
            }
        }
        return false;
    }
}
