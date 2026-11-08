package de.kronenshooter.core;

import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/** Fairness der Gegner: erreichbar, von oben, ohne Überlappung, Grenzen für große Gegner und Trefferpunkte. */
public class SpawnerTest {
    private static final double EPS = 1e-9;

    @Test
    public void everyWaveIsFair() {
        Arena arena = new Arena(true);
        int checked = 0;
        for (long seed = 1; seed <= 40; seed++) {
            Random rnd = new Random(seed);
            for (double t = 0; t <= 480; t += 3) {
                Spawner s = new Spawner(arena, rnd);
                Enemies e = new Enemies();
                for (int w = 0; w < 3; w++) s.wave(t, e);
                for (int i = 0; i < e.alive.length; i++) {
                    if (!e.alive[i]) continue;
                    checked++;
                    EnemyType ty = e.type[i];
                    double amp = e.zigzag[i] ? Tuning.ZIGZAG_AMP : 0;
                    assertTrue("erreichbar", e.baseX[i] - amp >= arena.spawnMin(ty.w) - EPS);
                    assertTrue("erreichbar", e.baseX[i] + amp <= arena.spawnMax(ty.w) + EPS);
                    double top = arena.minTop(e.baseX[i] - ty.w / 2 - amp, e.baseX[i] + ty.w / 2 + amp);
                    assertTrue("kommt von oberhalb des sichtbaren Bereichs", e.y[i] + ty.h / 2 <= top + EPS);
                    assertTrue(e.vy[i] >= ty.vMin * Difficulty.speed(t) - EPS);
                    assertTrue(e.vy[i] <= ty.vMax * Difficulty.speed(t) + EPS);
                    assertTrue(!e.zigzag[i] || ty == EnemyType.SMALL);
                    for (int k = i + 1; k < e.alive.length; k++) {
                        if (!e.alive[k]) continue;
                        EnemyType tk = e.type[k];
                        boolean apartX = Math.abs(e.x[i] - e.x[k]) >= (ty.w + tk.w) / 2;
                        boolean apartY = Math.abs(e.y[i] - e.y[k]) >= (ty.h + tk.h) / 2;
                        assertTrue("überlappen", apartX || apartY);
                    }
                }
                assertTrue(e.count(EnemyType.LARGE) <= Difficulty.maxLarge(t));
                assertTrue("Trefferpunkte über der Grenze", e.hpTotal() <= Difficulty.hpCap(t) + EPS);
                if (t < Tuning.LARGE_FROM_S) assertEquals(0, e.count(EnemyType.LARGE));
                if (t < Tuning.ZIGZAG_FROM_S) for (boolean z : e.zigzag) assertFalse(z);
            }
        }
        assertTrue(checked > 10000);
    }

    @Test
    public void enemiesStayInLaneDuringAGame() {
        Game g = new Game(new Arena(true), 3);
        g.lives = Integer.MAX_VALUE / 2; // nicht vorzeitig enden
        int seen = 0;
        for (int i = 0; i < 60 * 300; i++) {
            g.step();
            Enemies e = g.enemies;
            for (int j = 0; j < e.alive.length; j++) {
                if (!e.alive[j]) continue;
                seen++;
                EnemyType t = e.type[j];
                assertTrue(e.x[j] >= g.arena.spawnMin(t.w) - EPS && e.x[j] <= g.arena.spawnMax(t.w) + EPS);
            }
        }
        assertTrue(seen > 0);
    }

    @Test
    public void allTypesAppearAndLargeOnlyLater() {
        Game g = new Game(new Arena(true), 11);
        g.lives = Integer.MAX_VALUE / 2;
        double firstLarge = -1, firstMedium = -1;
        for (int i = 0; i < 60 * 240 && firstLarge < 0; i++) {
            g.step();
            if (firstMedium < 0 && g.enemies.count(EnemyType.MEDIUM) > 0) firstMedium = g.time();
            if (firstLarge < 0 && g.enemies.count(EnemyType.LARGE) > 0) firstLarge = g.time();
        }
        assertTrue(firstMedium > 0 && firstMedium < 30);
        assertTrue("großer Gegner erst ab 45 s: " + firstLarge, firstLarge >= Tuning.LARGE_FROM_S);
    }

    @Test
    public void firstWaveAfterLeadIn() {
        Game g = new Game(new Arena(true), 5);
        while (g.time() < Tuning.FIRST_WAVE_S - 0.05) {
            g.step();
            assertEquals(0, g.enemies.count());
        }
        RulesTest.run(g, 0.1);
        assertTrue(g.enemies.count() > 0);
    }

    @Test
    public void squareArenaWorksToo() {
        Arena a = new Arena(false);
        Spawner s = new Spawner(a, new Random(1));
        Enemies e = new Enemies();
        s.wave(200, e);
        for (int i = 0; i < e.alive.length; i++) {
            if (!e.alive[i]) continue;
            assertTrue(e.y[i] + e.type[i].h / 2 <= 0);
            assertTrue(e.x[i] >= a.laneMin && e.x[i] <= a.laneMax);
        }
    }
}
