package de.kronenshooter.core;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Balancing: simuliert viele Partien mit dem Bot (BOT_SPEED) und einem Spieler, der die Krone nie dreht, und gibt
 * Spieldauer, Punkte und Ursachen der verlorenen Leben aus. Prüft:
 * <ul>
 * <li>jede Partie endet (die Schwierigkeit steigt ohne Obergrenze);</li>
 * <li>der Anfang ist fair: kein Bot verliert in den ersten 90 s alle Leben;</li>
 * <li>die mittlere Spieldauer liegt im Ziel: Bot 2–6 min, menschenähnlicher Bot (halbes Tempo, 0,3 s
 * Reaktionszeit) 2–4 min. Ein echter Mensch entscheidet schlechter als jeder Bot und ist eher kürzer dabei;</li>
 * <li>ein großer Gegner ist während der ganzen Steigerung allein immer abschießbar, auch am Rand der Bahn.</li>
 * </ul>
 */
public class BalanceReport {
    static final int GAMES = 200;
    static final double BOT_SPEED = 1200, HUMAN_REACTION_S = 0.3, CAP_S = 30 * 60;

    static final class Stats {
        final double[] duration = new double[GAMES];
        final int[] score = new int[GAMES];
        long exits, collisions, kills, shots, hits;
        final long[] killsByType = new long[3];

        double median(double[] v) {
            double[] s = v.clone();
            Arrays.sort(s);
            return (s[s.length / 2 - 1] + s[s.length / 2]) / 2;
        }

        double medianScore() {
            double[] d = new double[score.length];
            for (int i = 0; i < d.length; i++) d[i] = score[i];
            return median(d);
        }

        double min(double[] v) {
            return Arrays.stream(v).min().orElse(0);
        }

        double max(double[] v) {
            return Arrays.stream(v).max().orElse(0);
        }
    }

    static Stats play(Bot proto) {
        Stats s = new Stats();
        for (int i = 0; i < GAMES; i++) {
            Bot bot = proto == null ? null : proto.fresh();
            Game g = new Game(new Arena(true), 1000 + i);
            while (g.state() == Game.State.PLAYING && g.time() < CAP_S) {
                if (bot != null) bot.control(g);
                g.step();
            }
            s.duration[i] = g.time();
            s.score[i] = g.score();
            s.exits += g.lostByExit;
            s.collisions += g.lostByCollision;
            s.kills += g.kills;
            s.shots += g.shots;
            s.hits += g.hits;
            for (int k = 0; k < 3; k++) s.killsByType[k] += g.killsByType[k];
        }
        return s;
    }

    private static void print(String name, Stats s) {
        System.out.printf("%-14s Dauer Median %5.0f s (min %4.0f, max %5.0f) | Punkte Median %6.0f | "
                        + "Leben verloren: %d hinausgeflogen, %d Kollision | Abschüsse klein/mittel/groß %d/%d/%d | "
                        + "Trefferquote %.0f %%%n",
                name, s.median(s.duration), s.min(s.duration), s.max(s.duration), s.medianScore(), s.exits,
                s.collisions, s.killsByType[0], s.killsByType[1], s.killsByType[2], 100.0 * s.hits / s.shots);
    }

    @Test
    public void report() {
        Stats bot = play(new Bot(BOT_SPEED));
        Stats slow = play(new Bot(BOT_SPEED / 2));
        Stats human = play(new Bot(BOT_SPEED / 2, HUMAN_REACTION_S));
        Stats idle = play(null);
        System.out.println("BalanceReport (" + GAMES + " Partien je Spieler, runde Uhr):");
        print("Bot 1200 E/s", bot);
        print("Bot 600 E/s", slow);
        print("wie Mensch", human);
        print("ohne Krone", idle);

        for (Stats s : new Stats[] {bot, slow, human, idle}) {
            assertTrue("Partie endet nicht", s.max(s.duration) < CAP_S);
        }
        for (Stats s : new Stats[] {bot, slow, human}) assertTrue("unfairer Anfang", s.min(s.duration) > 90);
        assertTrue("Bot zu schnell vorbei", bot.median(bot.duration) > 120);
        assertTrue("Bot zu lange", bot.median(bot.duration) < 360);
        assertTrue("Mensch zu schnell vorbei", human.median(human.duration) > 120);
        assertTrue("Mensch zu lange", human.median(human.duration) < 240);
        assertTrue(slow.median(slow.duration) < bot.median(bot.duration));
        assertTrue("ohne Steuern kein sinnvolles Spiel", idle.median(idle.duration) < 60);
        assertTrue("große Gegner kommen vor", bot.killsByType[2] > 0);
    }

    /**
     * Ein großer Gegner allein: ab dem Moment, in dem seine Unterkante sichtbar wird (am Rand der Bahn am spätesten),
     * bis zur Berührung mit dem Schiff muss Zeit für alle Schüsse plus Flugzeit bleiben, mit dem höchsten Tempo
     * während der Steigerung.
     */
    @Test
    public void largeEnemyAloneIsAlwaysKillableDuringRamp() {
        Arena a = new Arena(true);
        EnemyType t = EnemyType.LARGE;
        double x = a.spawnMin(t.w);
        double visibleBottom = a.minTop(x - t.w / 2, x + t.w / 2);
        double contact = Tuning.SHIP_Y - Tuning.SHIP_H / 2;
        double flight = (Tuning.SHIP_Y - visibleBottom) / Tuning.BULLET_SPEED;
        double need = t.hp / Tuning.FIRE_RATE + flight;
        double fastest = t.vMax * Difficulty.speed(Tuning.RAMP_S);
        double available = (contact - visibleBottom) / fastest;
        System.out.printf("Großer Gegner am Rand der Bahn: %.1f s Zeit, %.1f s nötig (Tempo %.0f E/s)%n", available,
                need, fastest);
        assertTrue(available > need);
    }
}
