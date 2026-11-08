package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

/** Geschoss gegen Gegner: kein Durchspringen, nächster Gegner zuerst, ein Geschoss = ein Treffer. */
public class CollisionTest {
    @Test
    public void bulletNeverTunnelsThroughSmallestEnemy() {
        // Ein Schritt bewegt das Geschoss um 23,3 E, der flachste Gegner ist 39 E hoch; geprüft wird für jede Lage
        // im Raster von 1,7 E, dass kein Geschoss an einem lebenden Gegner vorbeikommt.
        for (double y = 100; y < 700; y += 1.7) {
            Game g = RulesTest.quiet();
            int j = g.enemies.add(EnemyType.SMALL, 500, y, 0, false, 0);
            double top = y - EnemyType.SMALL.h / 2;
            for (int s = 0; s < 60 && g.enemies.alive[j]; s++) {
                g.step();
                for (int i = 0; i < g.bullets.alive.length; i++) {
                    if (g.bullets.alive[i] && g.enemies.alive[j]) {
                        assertTrue("Geschoss übersprungen bei y = " + y, g.bullets.y[i] + Tuning.BULLET_H / 2 > top);
                    }
                }
            }
            assertFalse("nicht getroffen bei y = " + y, g.enemies.alive[j]);
            assertEquals(1, g.hits);
        }
    }

    @Test
    public void bulletHitsTheLowerEnemyFirst() {
        Game g = RulesTest.quiet();
        int upper = g.enemies.add(EnemyType.SMALL, 500, 200, 0, false, 0);
        int lower = g.enemies.add(EnemyType.SMALL, 500, 420, 0, false, 0);
        while (g.hits == 0) g.step();
        assertFalse(g.enemies.alive[lower]);
        assertTrue(g.enemies.alive[upper]);
    }

    @Test
    public void oneBulletOneHit() {
        Game g = RulesTest.quiet();
        int j = g.enemies.add(EnemyType.LARGE, 500, 300, 0, false, 0);
        RulesTest.run(g, 3);
        int alive = g.bullets.count();
        assertEquals(g.shots - alive - 0, g.hits, 1); // höchstens ein Geschoss noch unterwegs zwischen Schiff und Ziel
        assertEquals(EnemyType.LARGE.hp - g.hits, g.enemies.hp[j]);
    }

    @Test
    public void bulletsBesideEnemyMiss() {
        Game g = RulesTest.quiet();
        // Rand des Gegners 0,5 E neben dem Rand des Geschosses
        double x = 500 + Tuning.BULLET_W / 2 + EnemyType.SMALL.w / 2 + 0.5;
        g.enemies.add(EnemyType.SMALL, x, 400, 0, false, 0);
        RulesTest.run(g, 2);
        assertEquals(0, g.hits);
    }

    @Test
    public void bulletsVanishAboveVisibleArea() {
        Game g = RulesTest.quiet();
        g.moveShip(-1000); // Rand der Bahn: dort beginnt der sichtbare Bereich erst tiefer
        // Ein Geschoss verschwindet, sobald es ganz oberhalb des sichtbaren Bereichs ist; im letzten Schritt
        // überstreicht es noch höchstens 12 + 23,3 + 12 = 47,3 E darüber. Ein Gegner, dessen Unterkante weiter oben
        // liegt, wird nie getroffen.
        double x = g.shipX(), edge = g.arena.topAt(x);
        double bottom = edge - 2 * Tuning.BULLET_H - 1;
        g.enemies.add(EnemyType.SMALL, x, bottom - EnemyType.SMALL.h / 2, 0, false, 0);
        RulesTest.run(g, 2);
        assertEquals(0, g.hits);
        assertTrue(g.shots > 5);
    }

    @Test
    public void shipHitboxIsSmallerThanSprite() {
        Game g = RulesTest.quiet();
        // Sprites berühren sich knapp, Trefferflächen nicht
        double x = 500 + Tuning.SHIP_W / 2 + EnemyType.SMALL.w / 2 - 4;
        g.enemies.add(EnemyType.SMALL, x, Tuning.SHIP_Y, 0, false, 0);
        g.step();
        assertEquals(3, g.lives());
    }
}
