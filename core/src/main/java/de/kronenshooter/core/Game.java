package de.kronenshooter.core;

import java.util.Random;

import static de.kronenshooter.core.Tuning.*;

/**
 * Eine Partie. Läuft in festen Schritten (step, je STEP Sekunden); die Krone verschiebt das Schiff sofort
 * (moveShip). Regeln:
 * <ul>
 * <li>Gegner fliegen senkrecht nach unten und werden dabei schneller (flightFactor, Tuning.FLIGHT_ACCEL).</li>
 * <li>Das Schiff feuert ununterbrochen (FIRE_RATE). Ein Treffer kostet den Gegner einen Trefferpunkt; bei 0 ist er
 * abgeschossen und bringt seine Punkte (1 / 3 / 10).</li>
 * <li>Ein Leben geht verloren, wenn ein Gegner den sichtbaren Bereich unten verlässt (Arena.goneBelow) oder das
 * Schiff berührt. Ein Gegner, der das Schiff berührt, explodiert (ohne Punkte); danach ist das Schiff INVULNERABLE_S
 * lang unverwundbar gegen weitere Berührungen. Hinausfliegende Gegner kosten immer ein Leben. Kein Gegner kostet
 * mehr als ein Leben.</li>
 * <li>Bei 0 Leben ist die Partie vorbei (State.OVER).</li>
 * </ul>
 * Zufall nur aus Random mit festem Seed: gleiche Eingaben ergeben die gleiche Partie (Tests, BalanceReport). Die
 * Gegner haben einen eigenen Zufallsgenerator (rnd), Funken und Sterne einen zweiten (fx), damit Optik den Ablauf
 * nicht verändert.
 */
public final class Game {
    public enum State { PLAYING, PAUSED, OVER }

    public final Arena arena;
    final Random rnd, fx;
    final Bullets bullets = new Bullets();
    final Enemies enemies = new Enemies();
    final Particles particles = new Particles();
    final Stars stars;
    final Spawner spawner;

    State state = State.PLAYING;
    double time, shipX = CENTER, invulnerable, fireTimer;
    int score, lives = LIVES;
    /** Zähler, die nur steigen: für Vibration (App) und Statistik (BalanceReport). */
    int shots, hits, kills, lifeLost, lostByExit, lostByCollision;
    final int[] killsByType = new int[EnemyType.values().length];

    final int spawnLevel;

    /** Partie mit der Gegnermenge „mittel“. */
    public Game(Arena arena, long seed) {
        this(arena, seed, SPAWN_DEFAULT);
    }

    /** spawnLevel: Gegnermenge 0 … 3 (wenig, mittel, viel, sehr viel), siehe Tuning.SPAWN_LEVELS. */
    public Game(Arena arena, long seed, int spawnLevel) {
        this.arena = arena;
        this.spawnLevel = Math.max(0, Math.min(SPAWN_LEVELS.length - 1, spawnLevel));
        rnd = new Random(seed);
        fx = new Random(seed ^ 0x5DEECE66DL);
        stars = new Stars(fx);
        spawner = new Spawner(arena, rnd, SPAWN_LEVELS[this.spawnLevel]);
    }

    public int spawnLevel() {
        return spawnLevel;
    }

    /** Faktor auf das Grundtempo eines Gegners auf Höhe y (Mittelpunkt): Beschleunigung im Flug. */
    public static double flightFactor(double y) {
        return 1 + FLIGHT_ACCEL * Math.max(0, Math.min(SIZE, y)) / SIZE;
    }

    /**
     * Flugzeit in s eines Gegners mit Grundtempo v0 von Höhe y0 bis y1 (Mittelpunkt). Oberhalb von 0 fliegt er mit
     * v0, zwischen 0 und 1000 wächst das Tempo linear mit y (dy/dt = v0 · (1 + a·y/1000), integriert:
     * t = 1000 / (a · v0) · ln((1 + a·y1/1000) / (1 + a·y0/1000))), unterhalb von 1000 mit v0 · (1 + a).
     */
    public static double flightTime(double v0, double y0, double y1) {
        if (!(v0 > 0) || y1 <= y0) return 0;
        double t = 0;
        if (y0 < 0) {
            t += (Math.min(0, y1) - y0) / v0;
            y0 = 0;
        }
        double mid = Math.min(y1, SIZE);
        if (mid > y0) {
            t += FLIGHT_ACCEL == 0 ? (mid - y0) / v0
                    : SIZE / (FLIGHT_ACCEL * v0)
                            * Math.log((1 + FLIGHT_ACCEL * mid / SIZE) / (1 + FLIGHT_ACCEL * y0 / SIZE));
        }
        if (y1 > SIZE) t += (y1 - Math.max(y0, SIZE)) / (v0 * (1 + FLIGHT_ACCEL));
        return t;
    }

    public State state() {
        return state;
    }

    public int score() {
        return score;
    }

    public int lives() {
        return lives;
    }

    /** Spielzeit in s (ohne Pausen). */
    public double time() {
        return time;
    }

    public double shipX() {
        return shipX;
    }

    /** Wie oft bisher ein Leben verloren ging (steigt nur). */
    public int lifeLostCount() {
        return lifeLost;
    }

    /** Krone: Schiff um dx (E) verschieben, an den Rändern der Bahn begrenzt. Nur während des Spiels. */
    public void moveShip(double dx) {
        if (state != State.PLAYING || dx != dx) return;
        shipX = arena.clampShip(shipX + dx);
    }

    public void pause() {
        if (state == State.PLAYING) state = State.PAUSED;
    }

    public void resume() {
        if (state == State.PAUSED) state = State.PLAYING;
    }

    /** Ein Logikschritt von STEP Sekunden. */
    public void step() {
        if (state != State.PLAYING) return;
        double dt = STEP;
        time += dt;
        if (invulnerable > 0) invulnerable = Math.max(0, invulnerable - dt);
        stars.update(dt);
        particles.update(dt);
        moveEnemies(dt);
        fire(dt);
        moveBullets(dt);
        shipCollisions();
        exits();
        spawner.update(time, enemies);
        if (lives <= 0) state = State.OVER;
    }

    private void moveEnemies(double dt) {
        Enemies e = enemies;
        for (int i = 0; i < e.alive.length; i++) {
            if (!e.alive[i]) continue;
            e.y[i] += e.vy[i] * flightFactor(e.y[i]) * dt;
            if (e.zigzag[i]) e.x[i] = e.baseX[i] + ZIGZAG_AMP * Math.sin(ZIGZAG_OMEGA * time + e.phase[i]);
            if (e.flash[i] > 0) e.flash[i] = Math.max(0, e.flash[i] - dt);
        }
    }

    private void fire(double dt) {
        fireTimer -= dt;
        while (fireTimer <= 1e-9) {
            if (bullets.add(shipX, SHIP_Y - SHIP_H / 2 - BULLET_H / 2) >= 0) shots++;
            fireTimer += 1 / FIRE_RATE;
        }
    }

    /**
     * Geschosse bewegen und treffen. Geprüft wird die ganze in diesem Schritt überstrichene Strecke (alte bis neue
     * Position), damit ein schnelles Geschoss nicht durch einen flachen Gegner hindurchspringt. Getroffen wird der
     * Gegner mit der tiefsten Unterkante, also der, den das Geschoss zuerst erreicht.
     */
    private void moveBullets(double dt) {
        Bullets b = bullets;
        Enemies e = enemies;
        for (int i = 0; i < b.alive.length; i++) {
            if (!b.alive[i]) continue;
            double oldY = b.y[i];
            b.y[i] -= BULLET_SPEED * dt;
            double top = b.y[i] - BULLET_H / 2, bottom = oldY + BULLET_H / 2;
            double bx0 = b.x[i] - BULLET_W / 2, bx1 = b.x[i] + BULLET_W / 2;
            int hit = -1;
            double hitBottom = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < e.alive.length; j++) {
                if (!e.alive[j]) continue;
                EnemyType t = e.type[j];
                double ex0 = e.x[j] - t.w / 2, ex1 = e.x[j] + t.w / 2;
                double ey0 = e.y[j] - t.h / 2, ey1 = e.y[j] + t.h / 2;
                if (bx1 > ex0 && bx0 < ex1 && bottom > ey0 && top < ey1 && ey1 > hitBottom) {
                    hit = j;
                    hitBottom = ey1;
                }
            }
            if (hit >= 0) {
                b.alive[i] = false;
                damage(hit, b.x[i], hitBottom);
                continue;
            }
            // ganz oberhalb des sichtbaren Bereichs: weg (was man nicht sieht, kann man nicht treffen)
            double edge = arena.topAt(b.x[i]);
            if (edge != edge || b.y[i] + BULLET_H / 2 < edge) b.alive[i] = false;
        }
    }

    /**
     * Treffer an der Stelle (hx, hy). Kleine Funken in Geschossfarbe zeigen jeden Treffer; mittlere Gegner blitzen
     * zusätzlich weiß auf. Große nicht: bei 7 Treffern/s würden sie dauernd flackern, und eine große weiße Fläche
     * kostet auf OLED unnötig Strom.
     */
    private void damage(int j, double hx, double hy) {
        Enemies e = enemies;
        EnemyType t = e.type[j];
        hits++;
        e.hp[j]--;
        if (e.hp[j] > 0) {
            if (t != EnemyType.LARGE) e.flash[j] = FLASH_S;
            particles.burst(hx, hy, Palette.BULLET, 2, fx);
            return;
        }
        e.alive[j] = false;
        score += t.points;
        kills++;
        killsByType[t.ordinal()]++;
        particles.burst(e.x[j], e.y[j], Palette.enemy(t), t.particles, fx);
    }

    private void shipCollisions() {
        Enemies e = enemies;
        double sw = SHIP_W * SHIP_HITBOX / 2, sh = SHIP_H * SHIP_HITBOX / 2;
        for (int j = 0; j < e.alive.length; j++) {
            if (!e.alive[j]) continue;
            EnemyType t = e.type[j];
            double ew = t.w * ENEMY_HITBOX / 2, eh = t.h * ENEMY_HITBOX / 2;
            if (Math.abs(e.x[j] - shipX) >= sw + ew || Math.abs(e.y[j] - SHIP_Y) >= sh + eh) continue;
            e.alive[j] = false;
            particles.burst(e.x[j], e.y[j], Palette.enemy(t), t.particles, fx);
            if (invulnerable > 0) continue;
            loseLife();
            lostByCollision++;
            invulnerable = INVULNERABLE_S;
        }
    }

    private void exits() {
        Enemies e = enemies;
        for (int j = 0; j < e.alive.length; j++) {
            if (!e.alive[j]) continue;
            EnemyType t = e.type[j];
            if (!arena.goneBelow(e.x[j], e.y[j], t.w, t.h)) continue;
            e.alive[j] = false;
            double edge = arena.bottomAt(e.x[j]);
            particles.burst(e.x[j], (edge == edge ? edge : SIZE) - 16, Palette.DANGER, 10, fx);
            loseLife();
            lostByExit++;
        }
    }

    private void loseLife() {
        if (lives > 0) lives--;
        lifeLost++;
    }
}
