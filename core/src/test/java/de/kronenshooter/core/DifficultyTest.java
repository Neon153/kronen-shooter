package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class DifficultyTest {
    private static final double EPS = 1e-9;

    @Test
    public void endpoints() {
        double end = Tuning.RAMP_S;
        assertEquals(Tuning.GAP_START, Difficulty.gap(0), EPS);
        assertEquals(Tuning.GAP_END, Difficulty.gap(end), EPS);
        assertEquals(Tuning.GAP_END, Difficulty.gap(end + 900), EPS);
        assertEquals(2, Difficulty.waveMax(0));
        assertEquals(5, Difficulty.waveMax(end));
        assertEquals(Tuning.SPEED_START, Difficulty.speed(0), EPS);
        assertEquals(Tuning.SPEED_END, Difficulty.speed(end), EPS);
        assertEquals(Tuning.SPEED_END + 5 * Tuning.SPEED_PER_MIN_AFTER, Difficulty.speed(end + 5 * 60), EPS);
        assertEquals(0, Difficulty.weightLarge(Tuning.LARGE_FROM_S - 0.1), EPS);
        assertEquals(Tuning.W_LARGE_END, Difficulty.weightLarge(end), EPS);
        assertEquals(0, Difficulty.maxLarge(Tuning.LARGE_FROM_S - 0.1));
        assertEquals(1, Difficulty.maxLarge(Tuning.LARGE_FROM_S));
        assertEquals(2, Difficulty.maxLarge(Tuning.TWO_LARGE_FROM_S));
        assertEquals(0, Difficulty.zigzagChance(Tuning.ZIGZAG_FROM_S - 0.1), EPS);
        assertEquals(Tuning.ZIGZAG_MAX, Difficulty.zigzagChance(end), EPS);
        assertEquals(Tuning.HP_CAP_START, Difficulty.hpCap(0), EPS);
        assertEquals(Tuning.HP_CAP_END + 2 * Tuning.HP_CAP_PER_MIN_AFTER, Difficulty.hpCap(end + 120), EPS);
    }

    @Test
    public void monotonic() {
        for (double t = 0; t < 1200; t += 0.5) {
            double u = t + 0.5;
            assertTrue(Difficulty.gap(u) <= Difficulty.gap(t) + EPS);
            assertTrue(Difficulty.waveMax(u) >= Difficulty.waveMax(t));
            assertTrue(Difficulty.speed(u) >= Difficulty.speed(t) - EPS);
            assertTrue(Difficulty.weightMedium(u) >= Difficulty.weightMedium(t) - EPS);
            assertTrue(Difficulty.weightLarge(u) >= Difficulty.weightLarge(t) - EPS);
            assertTrue(Difficulty.hpCap(u) >= Difficulty.hpCap(t) - EPS);
            assertTrue(Difficulty.zigzagChance(u) >= Difficulty.zigzagChance(t) - EPS);
        }
    }

    @Test
    public void speedKeepsGrowingSoEveryGameEnds() {
        assertTrue(Difficulty.speed(3600) > 6);
    }

    @Test
    public void largeFitsUnderHpCapWhenAllowed() {
        for (double t = Tuning.LARGE_FROM_S; t < 600; t += 1) {
            assertTrue(Difficulty.hpCap(t) >= EnemyType.LARGE.hp);
        }
    }
}
