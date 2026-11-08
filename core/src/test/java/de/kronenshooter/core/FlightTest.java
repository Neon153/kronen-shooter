package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

/** Beschleunigung im Flug und Gegnermenge. */
public class FlightTest {
    @Test
    public void speedGrowsWithHeight() {
        assertEquals(1, Game.flightFactor(-200), 0);
        assertEquals(1, Game.flightFactor(0), 0);
        assertEquals(1 + 0.8 * Tuning.FLIGHT_ACCEL, Game.flightFactor(Tuning.SHIP_Y), 1e-12);
        assertEquals(1 + Tuning.FLIGHT_ACCEL, Game.flightFactor(1000), 1e-12);
        assertEquals(1 + Tuning.FLIGHT_ACCEL, Game.flightFactor(1300), 1e-12);
        for (double y = -100; y < 1100; y += 10) assertTrue(Game.flightFactor(y + 10) >= Game.flightFactor(y));
    }

    @Test
    public void enemyAcceleratesDuringFlight() {
        Game g = RulesTest.quiet();
        int j = g.enemies.add(EnemyType.SMALL, 200, 0, 150, false, 0); // neben der Schusslinie
        double y0 = g.enemies.y[j];
        g.step();
        double early = g.enemies.y[j] - y0;
        while (g.enemies.y[j] < 700) g.step();
        double y1 = g.enemies.y[j];
        g.step();
        double late = g.enemies.y[j] - y1;
        assertEquals(early * Game.flightFactor(y1) / Game.flightFactor(y0), late, late * 0.01);
        assertTrue(late > 1.4 * early);
    }

    @Test
    public void flightTimeMatchesSimulation() {
        // x = 200: neben der Schusslinie; dort ist der Gegner ab y ≈ 900 hinter dem Kreisrand verschwunden
        double[][] cases = {{150, -120, 700}, {40, 60, 680}, {220, 0, 850}, {100, -50, -10}};
        for (double[] c : cases) {
            Game g = RulesTest.quiet();
            int j = g.enemies.add(EnemyType.SMALL, 200, c[1], c[0], false, 0);
            int steps = 0;
            while (g.enemies.alive[j] && g.enemies.y[j] < c[2]) {
                g.step();
                steps++;
            }
            double predicted = Game.flightTime(c[0], c[1], c[2]);
            assertEquals("v0 = " + c[0] + " von " + c[1] + " bis " + c[2], predicted, steps * Tuning.STEP,
                    0.01 * predicted + 2 * Tuning.STEP);
        }
        // unterhalb von 1000 mit festem Endtempo v0 · (1 + a)
        assertEquals(100 / (100 * (1 + Tuning.FLIGHT_ACCEL)), Game.flightTime(100, 1000, 1100), 1e-12);
        double split = Game.flightTime(80, -100, 400) + Game.flightTime(80, 400, 1200);
        assertEquals(Game.flightTime(80, -100, 1200), split, 1e-9);
        assertEquals(0, Game.flightTime(100, 500, 400), 0);
        assertEquals(0, Game.flightTime(0, 0, 400), 0);
    }

    @Test
    public void timeToShipAboutAsBeforeAcceleration() {
        // Die Grundtempi sind so gesenkt, dass ein Gegner vom oberen Rand bis zum Schiff etwa so lange braucht wie
        // vorher mit festem Tempo (klein 160–220, mittel 110–140, groß 40–50 E/s): Abweichung höchstens 3 %.
        double[][] before = {{160, 220}, {110, 140}, {40, 50}};
        EnemyType[] types = EnemyType.values();
        for (int k = 0; k < types.length; k++) {
            EnemyType t = types[k];
            double now = Game.flightTime((t.vMin + t.vMax) / 2, 0, Tuning.SHIP_Y);
            double old = Tuning.SHIP_Y / ((before[k][0] + before[k][1]) / 2);
            assertEquals(t.name(), old, now, old * 0.03);
        }
    }

    @Test
    public void spawnLevelChangesAmountOfEnemies() {
        int previous = -1;
        for (int level = 0; level < Tuning.SPAWN_LEVELS.length; level++) {
            Game g = new Game(new Arena(true), 77, level);
            g.lives = Integer.MAX_VALUE / 2;
            RulesTest.run(g, 120);
            assertEquals(level, g.spawnLevel());
            assertEquals(Tuning.SPAWN_LEVELS[level], g.spawner.density, 0);
            assertTrue("Stufe " + level + ": " + g.spawner.spawned, g.spawner.spawned > previous);
            previous = g.spawner.spawned;
        }
        assertEquals(Tuning.SPAWN_DEFAULT, new Game(new Arena(true), 1).spawnLevel());
        assertEquals(0, new Game(new Arena(true), 1, -5).spawnLevel());
        assertEquals(Tuning.SPAWN_LEVELS.length - 1, new Game(new Arena(true), 1, 99).spawnLevel());
    }

    @Test
    public void spawnLevelScalesHpCap() {
        for (double t = 0; t < 400; t += 7) {
            for (int level = 0; level < Tuning.SPAWN_LEVELS.length; level++) {
                Spawner s = new Spawner(new Arena(true), new java.util.Random(level * 1000 + (long) t),
                        Tuning.SPAWN_LEVELS[level]);
                Enemies e = new Enemies();
                for (int w = 0; w < 6; w++) s.wave(t, e);
                assertTrue(e.hpTotal() <= Difficulty.hpCap(t) * Tuning.SPAWN_LEVELS[level] + 1e-9);
            }
        }
    }
}
