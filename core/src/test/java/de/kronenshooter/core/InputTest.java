package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

/** Krone (CrownInput) und fester Zeitschritt (FrameClock). */
public class InputTest {
    private static final long MS = 1_000_000L;

    @Test
    public void crownGainLevelsAndDirection() {
        assertEquals(1.2 * 10, new CrownInput(0, false).dx(10), 1e-9);
        assertEquals(2.0 * 10, new CrownInput(1, false).dx(10), 1e-9);
        assertEquals(3.4 * 10, new CrownInput(2, false).dx(10), 1e-9);
        assertEquals(-2.0 * 10, new CrownInput(1, true).dx(10), 1e-9);
        assertEquals(-2.0 * 5, new CrownInput(1, false).dx(-5), 1e-9);
        assertEquals(3.4, new CrownInput(99, false).dx(1), 1e-9);
        assertEquals(1.2, new CrownInput(-3, false).dx(1), 1e-9);
        assertEquals(0, new CrownInput(1, false).dx(Double.NaN), 0);
    }

    @Test
    public void firstFrameRunsNoStep() {
        assertEquals(0, new FrameClock().advance(123 * MS));
    }

    @Test
    public void thirtyFpsGivesTwoStepsPerFrame() {
        FrameClock c = new FrameClock();
        long t = 5_000 * MS;
        c.advance(t);
        int total = 0;
        for (int i = 1; i <= 30 * 10; i++) {
            long next = 5_000 * MS + Math.round(i * 1e9 / 30);
            int n = c.advance(next);
            assertTrue(n >= 1 && n <= 3);
            total += n;
        }
        assertEquals(600, total, 1);
    }

    @Test
    public void sixtyFpsGivesOneStepPerFrame() {
        FrameClock c = new FrameClock();
        c.advance(0);
        int total = 0;
        for (int i = 1; i <= 600; i++) total += c.advance(Math.round(i * 1e9 / 60));
        assertEquals(600, total, 1);
    }

    @Test
    public void gapsAreCappedAndResetSkipsPause() {
        FrameClock c = new FrameClock();
        c.advance(0);
        assertEquals((int) Math.round(FrameClock.MAX_FRAME_S / Tuning.STEP), c.advance(2_000 * MS));
        assertEquals(0, c.advance(1_000 * MS)); // Zeit rückwärts
        c.reset();
        assertEquals(0, c.advance(60_000 * MS));
        assertEquals(2, c.advance(60_000 * MS + 34 * MS));
    }
}
