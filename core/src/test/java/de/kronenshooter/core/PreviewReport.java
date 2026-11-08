package de.kronenshooter.core;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Vorschaubilder ohne Uhr: rendert typische Szenen über GameRenderer mit AwtPainter als PNG nach build/preview/
 * (450 px, etwa die Auflösung der Pixel Watch 5). Dient zur Sichtprüfung von Layout und Sprites.
 */
public class PreviewReport {
    static final int PX = 450;

    private static File out(String name) {
        File dir = new File(System.getProperty("preview.dir", "build/preview"));
        if (!dir.isDirectory() && !dir.mkdirs()) throw new IllegalStateException("kein Ordner " + dir);
        return new File(dir, name);
    }

    private static void save(Game g, GameRenderer r, String name) throws IOException {
        AwtPainter p = new AwtPainter(PX);
        r.draw(g, p);
        p.maskRound();
        File f = out(name);
        assertTrue(ImageIO.write(p.image, "png", f));
        System.out.println("Vorschau: " + f.getPath());
    }

    /** Szene mitten im Spiel: alle drei Gegnerarten, ein getroffener großer Gegner, Explosion, Geschosse. */
    static Game midGame() {
        Game g = RulesTest.quiet();
        g.moveShip(-60);
        g.enemies.add(EnemyType.LARGE, 440, 260, 0, false, 0);
        g.enemies.add(EnemyType.SMALL, 300, 420, 0, false, 0);
        g.enemies.add(EnemyType.SMALL, 690, 360, 0, false, 0);
        g.enemies.add(EnemyType.MEDIUM, 620, 540, 0, false, 0);
        g.enemies.add(EnemyType.SMALL, 240, 140, 0, false, 0);
        RulesTest.run(g, 1.3);
        g.particles.burst(560, 470, Palette.SMALL, 8, g.fx);
        g.particles.update(0.12);
        g.score = 1287;
        g.lives = 2;
        return g;
    }

    @Test
    public void render() throws IOException {
        Game g = midGame();
        save(g, new GameRenderer(), "spiel.png");

        g.pause();
        save(g, new GameRenderer(), "pause.png");

        Game over = midGame();
        over.lives = 0;
        over.step();
        GameRenderer r = new GameRenderer();
        r.setResult(over.score(), true);
        save(over, r, "ende.png");

        // die drei Gegnerarten nebeneinander, Schiff in der Mitte (noch kein Schuss abgegeben)
        Game s = RulesTest.quiet();
        s.enemies.add(EnemyType.SMALL, 300, 330, 0, false, 0);
        s.enemies.add(EnemyType.MEDIUM, 690, 330, 0, false, 0);
        s.enemies.add(EnemyType.LARGE, 500, 560, 0, false, 0);
        save(s, new GameRenderer(), "gegner.png");
    }
}
