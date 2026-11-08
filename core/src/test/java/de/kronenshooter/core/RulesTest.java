package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

/** Regeln einer Partie: Leben, Punkte, Unverwundbarkeit, Ende, Pause, Dauerfeuer, Krone. */
public class RulesTest {
    /** Partie ohne automatische Gegner; Schiff in der Mitte. */
    static Game quiet() {
        Game g = new Game(new Arena(true), 7);
        g.spawner.enabled = false;
        return g;
    }

    static void run(Game g, double seconds) {
        int n = (int) Math.round(seconds / Tuning.STEP);
        for (int i = 0; i < n; i++) g.step();
    }

    @Test
    public void exitCostsExactlyOneLife() {
        Game g = quiet();
        g.enemies.add(EnemyType.SMALL, 200, 700, 200, false, 0); // nicht in der Schusslinie (x = 500)
        run(g, 5);
        assertEquals(2, g.lives());
        assertEquals(1, g.lostByExit);
        assertEquals(0, g.enemies.count());
        run(g, 5);
        assertEquals(2, g.lives());
    }

    @Test
    public void exitHappensWhenLeavingTheCircleNotAtY1000() {
        Game g = quiet();
        int j = g.enemies.add(EnemyType.SMALL, 160, 700, 100, false, 0);
        while (g.enemies.alive[j]) g.step();
        double y = g.enemies.y[j];
        assertTrue("verschwindet am Rand der Bahn deutlich vor y = 1000: " + y, y < 920);
        assertEquals(2, g.lives());
    }

    @Test
    public void collisionCostsOneLifeDestroysEnemyNoPoints() {
        Game g = quiet();
        // auf Höhe des Schiffs: das eben abgefeuerte Geschoss liegt darüber und trifft ihn nicht
        g.enemies.add(EnemyType.SMALL, 500, Tuning.SHIP_Y, 0, false, 0);
        g.step();
        assertEquals(2, g.lives());
        assertEquals(1, g.lostByCollision);
        assertEquals(0, g.enemies.count());
        assertEquals(0, g.score());
        assertEquals(Tuning.INVULNERABLE_S, g.invulnerable, 1e-9);
        run(g, 5);
        assertEquals("explodierter Gegner kostet beim Verlassen kein zweites Leben", 2, g.lives());
    }

    @Test
    public void largeEnemyRammingAlsoCostsOnlyOneLife() {
        Game g = quiet();
        g.enemies.add(EnemyType.LARGE, 500, Tuning.SHIP_Y - 60, 50, false, 0);
        run(g, 10);
        assertEquals(2, g.lives());
        assertEquals(0, g.enemies.count());
    }

    @Test
    public void groupHittingShipCostsOneLife() {
        Game g = quiet();
        g.enemies.add(EnemyType.SMALL, 470, Tuning.SHIP_Y - 30, 0, false, 0);
        g.enemies.add(EnemyType.SMALL, 530, Tuning.SHIP_Y - 30, 0, false, 0);
        g.step();
        assertEquals(2, g.lives());
        assertEquals(0, g.enemies.count());
        // während der Unverwundbarkeit: Gegner explodiert, kein Leben
        run(g, 0.5);
        g.enemies.add(EnemyType.SMALL, 500, Tuning.SHIP_Y, 0, false, 0);
        g.step();
        assertEquals(2, g.lives());
        assertEquals(0, g.enemies.count());
        // danach wieder
        run(g, 0.6);
        assertEquals(0, g.invulnerable, 1e-9);
        g.enemies.add(EnemyType.SMALL, 500, Tuning.SHIP_Y, 0, false, 0);
        g.step();
        assertEquals(1, g.lives());
    }

    @Test
    public void exitDuringInvulnerabilityStillCostsALife() {
        Game g = quiet();
        g.enemies.add(EnemyType.SMALL, 500, Tuning.SHIP_Y, 0, false, 0);
        g.enemies.add(EnemyType.SMALL, 200, 1000, 0, false, 0);
        g.step();
        assertEquals(1, g.lives());
        assertEquals(1, g.lostByExit);
        assertEquals(1, g.lostByCollision);
    }

    @Test
    public void killPointsAreWeighted() {
        Game g = quiet();
        int j = g.enemies.add(EnemyType.MEDIUM, 500, 400, 0, false, 0);
        run(g, 2.5);
        assertFalse(g.enemies.alive[j]);
        assertEquals(5, g.hits);
        assertEquals(3, g.score());
        g.enemies.add(EnemyType.SMALL, 500, 400, 0, false, 0);
        run(g, 1);
        assertEquals(4, g.score());
        g.enemies.add(EnemyType.LARGE, 500, 300, 0, false, 0);
        run(g, 10);
        assertEquals(14, g.score());
        assertArrayEquals(new int[] {1, 1, 1}, g.killsByType);
    }

    @Test
    public void hitFlashesMediumAndDecrementsHp() {
        Game g = quiet();
        int j = g.enemies.add(EnemyType.MEDIUM, 500, 300, 0, false, 0);
        while (g.hits == 0) g.step();
        assertEquals(4, g.enemies.hp[j]);
        assertTrue(g.enemies.flash[j] > 0);
        assertTrue("Funken am Einschlag", g.particles.count() > 0);
        run(g, Tuning.FLASH_S + 0.02);
        assertEquals(0, g.enemies.flash[j], 1e-9);
    }

    @Test
    public void largeEnemyDoesNotFlash() {
        Game g = quiet();
        int j = g.enemies.add(EnemyType.LARGE, 500, 300, 0, false, 0);
        run(g, 3);
        assertTrue(g.hits > 10);
        assertEquals(EnemyType.LARGE.hp - g.hits, g.enemies.hp[j]);
        assertEquals(0, g.enemies.flash[j], 0);
    }

    @Test
    public void gameOverAtZeroLivesThenFrozen() {
        Game g = quiet();
        for (int k = 0; k < 3; k++) g.enemies.add(EnemyType.SMALL, 200 + 40 * k, 990 + 30 * k, 0, false, 0);
        g.step();
        assertEquals(Game.State.OVER, g.state());
        assertEquals(0, g.lives());
        double t = g.time();
        g.step();
        g.moveShip(100);
        assertEquals(t, g.time(), 0);
        assertEquals(500, g.shipX(), 0);
    }

    @Test
    public void livesNeverNegative() {
        Game g = quiet();
        for (int k = 0; k < 6; k++) g.enemies.add(EnemyType.SMALL, 170 + 60 * k, 1100, 0, false, 0);
        g.step();
        assertEquals(0, g.lives());
        assertEquals(6, g.lifeLostCount());
    }

    @Test
    public void continuousFireAtFireRate() {
        Game g = quiet();
        run(g, 10);
        assertEquals(10 * Tuning.FIRE_RATE, g.shots, 1);
        assertTrue(g.bullets.count() <= 5);
    }

    @Test
    public void pauseFreezesEverythingIncludingCrown() {
        Game g = quiet();
        g.enemies.add(EnemyType.SMALL, 200, 300, 200, false, 0);
        run(g, 1);
        g.pause();
        assertEquals(Game.State.PAUSED, g.state());
        double t = g.time(), y = g.enemies.y[0];
        run(g, 3);
        g.moveShip(200);
        assertEquals(t, g.time(), 0);
        assertEquals(y, g.enemies.y[0], 0);
        assertEquals(500, g.shipX(), 0);
        g.resume();
        g.step();
        assertTrue(g.time() > t);
    }

    @Test
    public void crownMovesShipAndClamps() {
        Game g = quiet();
        g.moveShip(new CrownInput(1, false).dx(50));
        assertEquals(600, g.shipX(), 1e-9);
        g.moveShip(1e6);
        assertEquals(g.arena.laneMax, g.shipX(), 0);
        g.moveShip(-1e6);
        assertEquals(g.arena.laneMin, g.shipX(), 0);
        g.moveShip(Double.NaN);
        assertEquals(g.arena.laneMin, g.shipX(), 0);
    }

    @Test
    public void sameSeedSameGame() {
        Game a = new Game(new Arena(true), 42), b = new Game(new Arena(true), 42);
        for (int i = 0; i < 60 * 90; i++) {
            double dx = 300 * Math.sin(i * 0.01) * Tuning.STEP;
            a.moveShip(dx);
            b.moveShip(dx);
            a.step();
            b.step();
        }
        assertEquals(a.score(), b.score());
        assertEquals(a.lives(), b.lives());
        assertEquals(a.spawner.spawned, b.spawner.spawned);
        assertArrayEquals(a.enemies.y, b.enemies.y, 0);
    }
}
