package de.kronenshooter.core;

import java.util.Random;

/** Fester Speicher für die Funken der Explosionen. Ist er voll, entfallen weitere Funken (nur Optik). */
public final class Particles {
    final double[] x = new double[Tuning.MAX_PARTICLES], y = new double[Tuning.MAX_PARTICLES];
    final double[] vx = new double[Tuning.MAX_PARTICLES], vy = new double[Tuning.MAX_PARTICLES];
    final double[] life = new double[Tuning.MAX_PARTICLES], maxLife = new double[Tuning.MAX_PARTICLES];
    final int[] color = new int[Tuning.MAX_PARTICLES];

    /** n Funken ab (px, py) in zufällige Richtungen, 150–400 E/s, 0,35–0,6 s Lebensdauer. */
    void burst(double px, double py, int argb, int n, Random rnd) {
        int i = 0;
        for (int k = 0; k < n; k++) {
            while (i < life.length && life[i] > 0) i++;
            if (i == life.length) return;
            double a = rnd.nextDouble() * 2 * Math.PI, v = 150 + 250 * rnd.nextDouble();
            x[i] = px;
            y[i] = py;
            vx[i] = Math.cos(a) * v;
            vy[i] = Math.sin(a) * v;
            maxLife[i] = 0.35 + 0.25 * rnd.nextDouble();
            life[i] = maxLife[i];
            color[i] = argb;
        }
    }

    void update(double dt) {
        for (int i = 0; i < life.length; i++) {
            if (life[i] <= 0) continue;
            life[i] -= dt;
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
        }
    }

    public int count() {
        int n = 0;
        for (double l : life) if (l > 0) n++;
        return n;
    }
}
