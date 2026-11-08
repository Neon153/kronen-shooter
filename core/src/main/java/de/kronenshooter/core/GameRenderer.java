package de.kronenshooter.core;

import static de.kronenshooter.core.Tuning.*;

/**
 * Zeichenreihenfolge des Spielfelds über Painter: Sterne, Punkte, Geschosse, Gegner (mit Lebensbalken), Funken,
 * Schiff, Leben, darüber in Pause und nach dem Ende ein Feld mit Texten. Alles in E; die Lage der Anzeigen ist so
 * gewählt, dass sie auf runden Uhren ganz im Kreis liegt (RendererTest).
 * Pro Bild entstehen keine neuen Objekte: der Punktetext wird nur bei Änderung neu formatiert.
 */
public final class GameRenderer {
    /** Punkte oben in der Mitte (dort ist der Kreis noch rund 650 E breit; die Gegner fliegen darüber). */
    public static final double SCORE_Y = 150, SCORE_SIZE = 56;
    /** Leben als kleine Schiffe unter dem Schiff. */
    public static final double LIVES_Y = 905, LIVES_GAP = 44;
    /** Feld für Pause und Ende. */
    public static final double BOX_X0 = 170, BOX_X1 = 830;

    private int shownScore = -1;
    private String scoreText = "0";
    private boolean result, newRecord;
    private String recordText = "";

    /** Ergebnis für die Anzeige nach dem Ende (Rekord nach dieser Partie, neuer Rekord ja/nein). */
    public void setResult(int best, boolean isNewRecord) {
        result = true;
        newRecord = isNewRecord;
        recordText = Texts.record(best);
    }

    public void clearResult() {
        result = false;
    }

    public void draw(Game g, Painter p) {
        Stars s = g.stars;
        for (int i = 0; i < s.x.length; i++) {
            double r = 1.5 + s.tone[i];
            p.rect(s.x[i] - r, s.y[i] - r, s.x[i] + r, s.y[i] + r, Palette.STAR[s.tone[i]]);
        }

        if (g.state != Game.State.OVER) p.text(scoreText(g), CENTER, SCORE_Y, SCORE_SIZE, Palette.SCORE, true);

        Bullets b = g.bullets;
        for (int i = 0; i < b.alive.length; i++) {
            if (!b.alive[i]) continue;
            p.rect(b.x[i] - BULLET_W / 2, b.y[i] - BULLET_H / 2, b.x[i] + BULLET_W / 2, b.y[i] + BULLET_H / 2,
                    Palette.BULLET);
        }

        Enemies e = g.enemies;
        for (int i = 0; i < e.alive.length; i++) {
            if (!e.alive[i]) continue;
            EnemyType t = e.type[i];
            p.sprite(Sprites.of(t), e.x[i], e.y[i], e.flash[i] > 0);
            if (e.hp[i] < t.hp) {
                double x0 = e.x[i] - t.w / 2, y0 = e.y[i] - t.h / 2 - 12, y1 = y0 + 6;
                p.rect(x0, y0, x0 + t.w, y1, Palette.HP_BG);
                p.rect(x0, y0, x0 + t.w * e.hp[i] / t.hp, y1, Palette.enemy(t));
            }
        }

        Particles pt = g.particles;
        for (int i = 0; i < pt.life.length; i++) {
            if (pt.life[i] <= 0) continue;
            double r = 2 + 4 * pt.life[i] / pt.maxLife[i];
            p.rect(pt.x[i] - r, pt.y[i] - r, pt.x[i] + r, pt.y[i] + r, pt.color[i]);
        }

        if (g.state != Game.State.OVER && shipVisible(g)) p.sprite(Sprites.SHIP, g.shipX, SHIP_Y, false);

        for (int i = 0; i < g.lives; i++) {
            p.sprite(Sprites.LIFE, CENTER + (i - (g.lives - 1) / 2.0) * LIVES_GAP, LIVES_Y, false);
        }

        if (g.state == Game.State.PAUSED) {
            p.rect(BOX_X0, 370, BOX_X1, 670, Palette.CARD);
            p.text(Texts.PAUSE, CENTER, 475, 72, Palette.TEXT, true);
            p.text(Texts.TAP_RESUME, CENTER, 565, 44, Palette.MUTED, false);
            p.text(Texts.SWIPE_MENU, CENTER, 630, 40, Palette.MUTED, false);
        } else if (g.state == Game.State.OVER) {
            p.rect(BOX_X0, 330, BOX_X1, 740, Palette.CARD);
            p.text(Texts.GAME_OVER, CENTER, 410, 64, Palette.DANGER, true);
            p.text(scoreText(g), CENTER, 525, 100, Palette.TEXT, true);
            if (result) {
                p.text(newRecord ? Texts.NEW_RECORD : recordText, CENTER, 595, 44,
                        newRecord ? Palette.ACCENT : Palette.MUTED, newRecord);
            }
            p.text(Texts.TAP_AGAIN, CENTER, 660, 38, Palette.MUTED, false);
            p.text(Texts.SWIPE_MENU, CENTER, 710, 38, Palette.MUTED, false);
        }
    }

    private String scoreText(Game g) {
        if (g.score != shownScore) {
            shownScore = g.score;
            scoreText = Integer.toString(g.score);
        }
        return scoreText;
    }

    /** Nach einer Kollision blinkt das Schiff, solange es unverwundbar ist (5 × pro Sekunde). */
    static boolean shipVisible(Game g) {
        return g.invulnerable <= 0 || ((int) (g.invulnerable * 10)) % 2 == 0;
    }
}
