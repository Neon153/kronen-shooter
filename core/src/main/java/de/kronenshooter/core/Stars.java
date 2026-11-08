package de.kronenshooter.core;

import java.util.Random;

/** Wenige gedimmte Sterne, die langsam nach unten ziehen (drei Helligkeiten, schneller = heller). */
public final class Stars {
    final double[] x = new double[Tuning.STARS], y = new double[Tuning.STARS], speed = new double[Tuning.STARS];
    final int[] tone = new int[Tuning.STARS];

    Stars(Random rnd) {
        for (int i = 0; i < x.length; i++) {
            x[i] = rnd.nextDouble() * Tuning.SIZE;
            y[i] = rnd.nextDouble() * Tuning.SIZE;
            tone[i] = rnd.nextInt(3);
            speed[i] = 12 + 14 * tone[i];
        }
    }

    void update(double dt) {
        for (int i = 0; i < y.length; i++) {
            y[i] += speed[i] * dt;
            if (y[i] > Tuning.SIZE) y[i] -= Tuning.SIZE;
        }
    }
}
