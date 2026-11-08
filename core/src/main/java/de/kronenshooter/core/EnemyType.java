package de.kronenshooter.core;

/**
 * Die drei Gegnerarten. Größe in E (passend zum Seitenverhältnis des Sprites), Trefferpunkte, Grundtempo am oberen
 * Rand (zufällig zwischen vMin und vMax, mal Tempofaktor der Schwierigkeit; im Flug schneller, siehe
 * Tuning.FLIGHT_ACCEL), Punkte beim Abschuss, Partikel der Explosion.
 */
public enum EnemyType {
    SMALL(50, 39, 1, 130, 180, 1, 8),
    MEDIUM(80, 49, 5, 90, 115, 3, 12),
    LARGE(180, 115, 40, 33, 41, 10, 28);

    public final double w, h;
    public final int hp;
    public final double vMin, vMax;
    public final int points, particles;

    EnemyType(double w, double h, int hp, double vMin, double vMax, int points, int particles) {
        this.w = w;
        this.h = h;
        this.hp = hp;
        this.vMin = vMin;
        this.vMax = vMax;
        this.points = points;
        this.particles = particles;
    }
}
