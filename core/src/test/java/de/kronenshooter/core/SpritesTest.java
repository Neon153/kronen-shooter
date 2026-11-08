package de.kronenshooter.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpritesTest {
    @Test
    public void rastersAreRectangularAndUseKnownColors() {
        for (int id = 0; id < Sprites.COUNT; id++) {
            int cols = Sprites.cols(id);
            int filled = 0;
            for (int r = 0; r < Sprites.rowCount(id); r++) {
                String row = Sprites.ROWS[id == Sprites.LIFE ? Sprites.SHIP : id][r];
                assertEquals("Sprite " + id + " Zeile " + r, cols, row.length());
                for (int c = 0; c < cols; c++) {
                    char ch = row.charAt(c);
                    if (ch == '.') continue;
                    assertTrue("unbekannte Farbe '" + ch + "' in Sprite " + id, Sprites.KEYS[id].indexOf(ch) >= 0);
                    assertNotEquals(0, Sprites.pixel(id, c, r));
                    filled++;
                }
            }
            assertEquals(Sprites.KEYS[id].length(), Sprites.COLORS[id].length);
            assertTrue(filled > 0);
        }
    }

    @Test
    public void logicalSizeMatchesRasterAspect() {
        for (int id = 0; id < Sprites.COUNT; id++) {
            double raster = (double) Sprites.cols(id) / Sprites.rowCount(id);
            double logical = Sprites.w(id) / Sprites.h(id);
            assertEquals("Seitenverhältnis Sprite " + id, raster, logical, raster * 0.05);
        }
    }

    @Test
    public void enemySpritesMatchTypes() {
        for (EnemyType t : EnemyType.values()) {
            int id = Sprites.of(t);
            assertEquals(t.w, Sprites.w(id), 0);
            assertEquals(t.h, Sprites.h(id), 0);
        }
        assertEquals(Tuning.SHIP_W, Sprites.w(Sprites.SHIP), 0);
        assertEquals(Tuning.SHIP_H, Sprites.h(Sprites.SHIP), 0);
    }

    @Test
    public void transparentPixelIsZero() {
        assertEquals(0, Sprites.pixel(Sprites.SHIP, 0, 0));
        assertEquals(0xFFFFFFFF, Sprites.pixel(Sprites.SHIP, 5, 0));
    }
}
