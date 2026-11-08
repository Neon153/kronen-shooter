package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArenaTest {
    private static final double EPS = 1e-9;
    private final Arena round = new Arena(true), square = new Arena(false);

    @Test
    public void laneIsChordAtShipBottomMinusShipAndMargin() {
        // Sehne an der Unterkante des Schiffs (y = 832): sqrt(500² - 332²) = 373,87 -> [126,13, 873,87];
        // minus 35 (halbes Schiff) und 10 (Rand) -> Bahn [171,13, 828,87]
        assertEquals(400, round.halfChord(Tuning.SHIP_Y), EPS);
        double half = Math.sqrt(500 * 500 - 332 * 332);
        assertEquals(500 - half + 45, round.laneMin, EPS);
        assertEquals(500 + half - 45, round.laneMax, EPS);
        assertEquals(171.13, round.laneMin, 0.01);
        assertEquals(45, square.laneMin, EPS);
        assertEquals(955, square.laneMax, EPS);
    }

    @Test
    public void shipAtLaneEdgeIsInsideCircle() {
        for (double x : new double[] {round.laneMin, round.laneMax}) {
            for (double dx : new double[] {-Tuning.SHIP_W / 2, Tuning.SHIP_W / 2}) {
                for (double dy : new double[] {-Tuning.SHIP_H / 2, Tuning.SHIP_H / 2}) {
                    double px = x + dx - 500, py = Tuning.SHIP_Y + dy - 500;
                    assertTrue("Ecke des Schiffs außerhalb", px * px + py * py < 500 * 500);
                }
            }
        }
    }

    @Test
    public void edgesOfCircle() {
        assertEquals(0, round.topAt(500), EPS);
        assertEquals(1000, round.bottomAt(500), EPS);
        assertEquals(500, round.topAt(0), EPS);
        assertTrue(Double.isNaN(round.topAt(-1)));
        // x = 150: dx = 350, sqrt(500² - 350²) = 357,07
        assertEquals(500 + Math.sqrt(500 * 500 - 350 * 350), round.bottomAt(150), EPS);
        assertEquals(round.topAt(300), round.topAt(700), EPS);
        assertEquals(0, square.topAt(10), EPS);
        assertEquals(1000, square.bottomAt(990), EPS);
    }

    @Test
    public void minTopAndMaxBottomUseNearestPointToCenter() {
        assertEquals(round.topAt(400), round.minTop(300, 400), EPS);
        assertEquals(round.topAt(600), round.minTop(600, 700), EPS);
        assertEquals(0, round.minTop(400, 600), EPS);
        assertEquals(1000, round.maxBottom(450, 550), EPS);
        assertEquals(round.bottomAt(200), round.maxBottom(100, 200), EPS);
    }

    @Test
    public void goneBelowAtLaneEdgeLongBeforeY1000() {
        EnemyType t = EnemyType.SMALL;
        double x = 150, edge = round.maxBottom(x - t.w / 2, x + t.w / 2);
        assertTrue(edge < 900);
        assertFalse(round.goneBelow(x, edge + t.h / 2 - 0.01, t.w, t.h));
        assertTrue(round.goneBelow(x, edge + t.h / 2, t.w, t.h));
        // in der Mitte erst unterhalb von 1000
        assertFalse(round.goneBelow(500, 1000 + t.h / 2 - 0.01, t.w, t.h));
        assertTrue(round.goneBelow(500, 1000 + t.h / 2, t.w, t.h));
        assertTrue(square.goneBelow(150, 1000 + t.h / 2, t.w, t.h));
        assertFalse(square.goneBelow(150, 990, t.w, t.h));
    }

    @Test
    public void visibleMatchesCircle() {
        assertTrue(round.visible(500, 500, 10, 10));
        assertFalse(round.visible(30, 30, 20, 20)); // Ecke außerhalb des Kreises
        assertTrue(square.visible(30, 30, 20, 20));
        assertFalse(round.visible(500, -20, 50, 39));
        assertTrue(round.visible(500, -19, 50, 39));
    }

    @Test
    public void spawnRangeReachableAndFullyVisibleAtShipHeight() {
        for (EnemyType t : EnemyType.values()) {
            double lo = round.spawnMin(t.w), hi = round.spawnMax(t.w);
            assertTrue(lo < hi);
            assertTrue("erreichbar", lo >= round.laneMin - EPS && hi <= round.laneMax + EPS);
            double half = round.halfChord(Tuning.SHIP_Y);
            assertTrue("auf Höhe des Schiffs ganz sichtbar", lo - t.w / 2 >= 500 - half - EPS);
            assertTrue(hi + t.w / 2 <= 500 + half + EPS);
        }
        assertEquals(190, round.spawnMin(EnemyType.LARGE.w), EPS);
        assertEquals(810, round.spawnMax(EnemyType.LARGE.w), EPS);
    }

    @Test
    public void clampShip() {
        assertEquals(round.laneMin, round.clampShip(-1e9), EPS);
        assertEquals(round.laneMax, round.clampShip(1e9), EPS);
        assertEquals(321, round.clampShip(321), EPS);
    }
}
