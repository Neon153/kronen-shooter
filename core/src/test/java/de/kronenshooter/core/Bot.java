package de.kronenshooter.core;

import static de.kronenshooter.core.Tuning.*;

/**
 * Einfacher Spieler für den BalanceReport, Tempo begrenzt wie ein Mensch an der Krone (speed).
 * <ul>
 * <li>Frist eines Gegners: spätester Zeitpunkt, zu dem das Schiff unter ihm stehen muss = Zeit bis zur
 * Berührungslinie − fehlende Schüsse / Feuerrate − Flugzeit der Geschosse.</li>
 * <li>Er wählt unter den noch rettbaren Gegnern (Frist − Weg / Tempo &gt; −0,3 s) den mit der frühesten Frist und
 * bleibt bei ihm, bis genug Geschosse unterwegs sind; danach zieht er weiter, ohne auf die Explosion zu warten.
 * Der Weg zählt beim Vergleich bewusst nicht, sonst pendelt er zwischen zwei ähnlich dringenden Zielen hin und
 * her und bleibt stehen.</li>
 * </ul>
 */
final class Bot {
    private final double speed, reaction;
    private final int reactionSteps;
    private int current = -1, wait;

    /** speed in E/s; die ganze Bahn (rund 660 E) in 0,55 s entspricht etwa 1200 E/s. */
    Bot(double speed) {
        this(speed, 0);
    }

    /** reaction in s: neue Zielwahl nur in diesem Abstand (wie ein Mensch, der nicht jedes Bild neu entscheidet). */
    Bot(double speed, double reaction) {
        this.speed = speed;
        this.reaction = reaction;
        reactionSteps = (int) Math.round(reaction / STEP);
    }

    /** Neuer Bot mit denselben Eigenschaften (ohne gemerktes Ziel), für die nächste Partie. */
    Bot fresh() {
        return new Bot(speed, reaction);
    }

    void control(Game g) {
        Enemies e = g.enemies;
        if (wait > 0) {
            wait--;
            if (current >= 0 && e.alive[current]) move(g, e.x[current]);
            return;
        }
        wait = reactionSteps;
        int best = -1;
        double bestKey = Double.POSITIVE_INFINITY, keepKey = Double.NaN;
        for (int j = 0; j < e.alive.length; j++) {
            double key = deadline(g, j);
            if (key != key) continue;
            if (j == current) keepKey = key;
            if (key < bestKey) {
                bestKey = key;
                best = j;
            }
        }
        // Ziel behalten, solange kein anderes deutlich dringender ist
        if (keepKey == keepKey && keepKey < bestKey + 0.4) best = current;
        current = best;
        move(g, best >= 0 ? e.x[best] : g.shipX);
    }

    private void move(Game g, double target) {
        double max = speed * STEP;
        g.moveShip(Math.max(-max, Math.min(max, target - g.shipX)));
    }

    /** Frist in s, oder NaN, wenn der Gegner erledigt, vorbei oder nicht mehr zu retten ist. */
    private double deadline(Game g, int j) {
        Enemies e = g.enemies;
        Bullets b = g.bullets;
        if (!e.alive[j]) return Double.NaN;
        EnemyType t = e.type[j];
        if (e.y[j] - t.h / 2 > SHIP_Y) return Double.NaN;
        int pending = 0;
        for (int i = 0; i < b.alive.length; i++) {
            if (b.alive[i] && b.y[i] > e.y[j] && Math.abs(b.x[i] - e.x[j]) < (t.w + BULLET_W) / 2) pending++;
        }
        int missing = e.hp[j] - pending;
        if (missing <= 0) return Double.NaN;
        // Zeit, bis die Unterkante die Oberkante des Schiffs erreicht (mit Beschleunigung im Flug)
        double reach = Game.flightTime(e.vy[j], e.y[j], SHIP_Y - SHIP_H / 2 - t.h / 2);
        double key = reach - missing / FIRE_RATE - Math.max(0, SHIP_Y - e.y[j]) / BULLET_SPEED;
        double travel = Math.abs(e.x[j] - g.shipX) / speed;
        return key - travel > -0.3 ? key : Double.NaN;
    }
}
