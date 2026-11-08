package de.kronenshooter.core;

/**
 * Die drei Gegnerarten. Größe in E (passend zum Seitenverhältnis des Sprites), Trefferpunkte, Grundgeschwindigkeit
 * (zufällig zwischen vMin und vMax, mal Tempofaktor der Schwierigkeit), Punkte beim Abschuss, Partikel der Explosion.
 */
public enum EnemyType {
    SMALL(50, 39, 1, 160, 220, 1, 8),
    MEDIUM(80, 49, 5, 110, 140, 3, 12),
    LARGE(180, 115, 40, 40, 50, 10, 28);

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
