package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

/** Zeichenreihenfolge und Lage der Anzeigen auf dem runden Display. */
public class RendererTest {
    private static Game scene() {
        Game g = RulesTest.quiet();
        g.enemies.add(EnemyType.SMALL, 300, 400, 0, false, 0);
        g.enemies.add(EnemyType.MEDIUM, 500, 450, 0, false, 0);
        g.enemies.add(EnemyType.LARGE, 650, 250, 0, false, 0);
        RulesTest.run(g, 0.4);
        g.score = 12345;
        return g;
    }

    @Test
    public void hudInsideCircle() {
        Game g = scene();
        RecordingPainter p = new RecordingPainter();
        new GameRenderer().draw(g, p);
        RecordingPainter.Op score = p.text("12345");
        assertNotNull(score);
        assertTrue("Punkte im Kreis", RecordingPainter.insideCircle(score));
        // auch 6-stellig
        g.score = 999999;
        p = new RecordingPainter();
        new GameRenderer().draw(g, p);
        assertTrue(RecordingPainter.insideCircle(p.text("999999")));
        int lives = 0;
        for (RecordingPainter.Op o : p.ops) {
            if ("sprite".equals(o.kind) && o.id == Sprites.LIFE) {
                lives++;
                assertTrue("Leben im Kreis", RecordingPainter.insideCircle(o));
                assertTrue("Leben unter dem Schiff", o.y0 > Tuning.SHIP_Y + Tuning.SHIP_H / 2);
            }
        }
        assertEquals(3, lives);
    }

    @Test
    public void shipAtLaneEdgesInsideCircle() {
        for (double dx : new double[] {-1e6, 1e6}) {
            Game g = RulesTest.quiet();
            g.moveShip(dx);
            RecordingPainter p = new RecordingPainter();
            new GameRenderer().draw(g, p);
            assertTrue(RecordingPainter.insideCircle(p.ops.get(p.indexOf("sprite", Sprites.SHIP))));
        }
    }

    @Test
    public void drawOrder() {
        Game g = scene();
        RecordingPainter p = new RecordingPainter();
        new GameRenderer().draw(g, p);
        int score = p.ops.indexOf(p.text("12345"));
        int firstEnemy = Math.min(p.indexOf("sprite", Sprites.SMALL), p.indexOf("sprite", Sprites.MEDIUM));
        int lastEnemy = Math.max(p.lastIndexOf("sprite", Sprites.LARGE), p.lastIndexOf("sprite", Sprites.MEDIUM));
        int ship = p.indexOf("sprite", Sprites.SHIP);
        assertTrue("Punkte unter den Gegnern", score < firstEnemy);
        assertTrue("Schiff über den Gegnern", ship > lastEnemy);
    }

    @Test
    public void hitEnemyFlashesAndShowsHpBar() {
        Game g = RulesTest.quiet();
        int j = g.enemies.add(EnemyType.MEDIUM, 500, 400, 0, false, 0);
        while (g.hits == 0) g.step();
        RecordingPainter p = new RecordingPainter();
        new GameRenderer().draw(g, p);
        RecordingPainter.Op enemy = p.ops.get(p.indexOf("sprite", Sprites.MEDIUM));
        assertTrue(enemy.flag);
        int bar = 0;
        for (RecordingPainter.Op o : p.ops) if ("rect".equals(o.kind) && o.color == Palette.MEDIUM) bar++;
        assertEquals(1, bar);
        assertEquals(4, g.enemies.hp[j]);
    }

    @Test
    public void scoreTextIsCachedWhileUnchanged() {
        Game g = scene();
        GameRenderer r = new GameRenderer();
        RecordingPainter a = new RecordingPainter(), b = new RecordingPainter();
        r.draw(g, a);
        r.draw(g, b);
        assertSame(a.text("12345").text, b.text("12345").text);
    }

    @Test
    public void pauseOverlayInsideCircle() {
        Game g = scene();
        g.pause();
        RecordingPainter p = new RecordingPainter();
        new GameRenderer().draw(g, p);
        for (String s : new String[] {Texts.PAUSE, Texts.TAP_RESUME, Texts.SWIPE_MENU}) {
            assertNotNull(s, p.text(s));
            assertTrue(s, RecordingPainter.insideCircle(p.text(s)));
        }
        for (RecordingPainter.Op o : p.ops) {
            if ("rect".equals(o.kind) && o.color == Palette.CARD) assertTrue(RecordingPainter.insideCircle(o));
        }
    }

    @Test
    public void gameOverShowsResult() {
        Game g = scene();
        g.lives = 0;
        g.step();
        assertEquals(Game.State.OVER, g.state());
        GameRenderer r = new GameRenderer();
        r.setResult(12345, true);
        RecordingPainter p = new RecordingPainter();
        r.draw(g, p);
        for (String s : new String[] {Texts.GAME_OVER, "12345", Texts.NEW_RECORD, Texts.TAP_AGAIN, Texts.SWIPE_MENU}) {
            assertNotNull(s, p.text(s));
            assertTrue(s, RecordingPainter.insideCircle(p.text(s)));
        }
        assertEquals("kein Schiff nach dem Ende", -1, p.indexOf("sprite", Sprites.SHIP));
        r.setResult(20000, false);
        p = new RecordingPainter();
        r.draw(g, p);
        assertNotNull(p.text("Rekord 20000"));
        assertNull(p.text(Texts.NEW_RECORD));
    }

    @Test
    public void shipBlinksWhileInvulnerable() {
        Game g = RulesTest.quiet();
        g.enemies.add(EnemyType.SMALL, 500, Tuning.SHIP_Y, 0, false, 0);
        g.step();
        int visible = 0, hidden = 0;
        while (g.invulnerable > 0) {
            if (GameRenderer.shipVisible(g)) visible++;
            else hidden++;
            g.step();
        }
        assertTrue(visible > 10 && hidden > 10);
        assertTrue(GameRenderer.shipVisible(g));
    }
}
