package de.kronenshooter.core;

import java.util.Random;

/**
 * Erzeugt die Gegner in Wellen. Zufällig sind der Abstand der Wellen, die Anzahl (1 … waveMax), die Mischung der
 * Arten, die Position und das Tempo; die Erwartungswerte steigen mit der Spielzeit (Difficulty). Die Gegnermenge
 * (density) teilt den Abstand der Wellen und vervielfacht die Obergrenze der Trefferpunkte. Fairness:
 * <ul>
 * <li>jeder Gegner liegt in der Bahn des Schiffs (spawnMin/spawnMax), ist also erreichbar;</li>
 * <li>neue Gegner kommen ganz oberhalb des sichtbaren Bereichs herein und überlappen keinen vorhandenen;</li>
 * <li>die Summe der Trefferpunkte im Spiel bleibt unter hpCap × density, große Gegner höchstens maxLarge
 * gleichzeitig.</li>
 * </ul>
 */
final class Spawner {
    static final int TRIES = 12;
    static final double MARGIN = 12;

    private final Arena arena;
    private final Random rnd;
    /** Gegnermenge: Faktor auf die Rate der Wellen und die Obergrenze der Trefferpunkte (Tuning.SPAWN_LEVELS). */
    final double density;
    double nextWave = Tuning.FIRST_WAVE_S;
    boolean enabled = true;
    /** Statistik: erzeugte Wellen und Gegner. */
    int waves, spawned;

    Spawner(Arena arena, Random rnd) {
        this(arena, rnd, 1);
    }

    Spawner(Arena arena, Random rnd, double density) {
        this.arena = arena;
        this.rnd = rnd;
        this.density = density;
    }

    void update(double t, Enemies e) {
        if (!enabled || t < nextWave) return;
        wave(t, e);
        double jitter = 1 - Tuning.GAP_JITTER + 2 * Tuning.GAP_JITTER * rnd.nextDouble();
        nextWave = t + Difficulty.gap(t) * jitter / density;
    }

    void wave(double t, Enemies e) {
        waves++;
        int n = 1 + rnd.nextInt(Difficulty.waveMax(t));
        int hp = e.hpTotal();
        double cap = Difficulty.hpCap(t) * density;
        double speed = Difficulty.speed(t);
        for (int k = 0; k < n; k++) {
            EnemyType type = pick(t, e);
            if (hp + type.hp > cap) {
                if (hp + EnemyType.SMALL.hp > cap) return;
                type = EnemyType.SMALL;
            }
            boolean zig = type == EnemyType.SMALL && rnd.nextDouble() < Difficulty.zigzagChance(t);
            double amp = zig ? Tuning.ZIGZAG_AMP : 0;
            double lo = arena.spawnMin(type.w) + amp, hi = arena.spawnMax(type.w) - amp;
            double stagger = k == 0 ? 0 : k * Tuning.WAVE_STAGGER * (0.5 + 0.5 * rnd.nextDouble());
            for (int tryNo = 0; tryNo < TRIES; tryNo++) {
                double x = lo + (hi - lo) * rnd.nextDouble();
                // ganz oberhalb des sichtbaren Bereichs über die ganze Breite, die der Gegner beim Pendeln überstreicht
                double y = arena.minTop(x - type.w / 2 - amp, x + type.w / 2 + amp) - type.h / 2 - 2 - stagger;
                if (e.overlaps(x, y, type.w + 2 * amp, type.h, MARGIN)) continue;
                double v = (type.vMin + (type.vMax - type.vMin) * rnd.nextDouble()) * speed;
                if (e.add(type, x, y, v, zig, rnd.nextDouble() * 2 * Math.PI) >= 0) {
                    hp += type.hp;
                    spawned++;
                }
                break;
            }
        }
    }

    private EnemyType pick(double t, Enemies e) {
        double wl = e.count(EnemyType.LARGE) < Difficulty.maxLarge(t) ? Difficulty.weightLarge(t) : 0;
        double wm = Difficulty.weightMedium(t);
        double r = rnd.nextDouble();
        if (r < wl) return EnemyType.LARGE;
        if (r < wl + wm) return EnemyType.MEDIUM;
        return EnemyType.SMALL;
    }
}
