package de.kronenshooter.core;

/**
 * Alle Spielwerte an einer Stelle. Einheiten: E (Spielfeld 1000 × 1000 E, auf runden Uhren ist der sichtbare Bereich
 * der Kreis mit 1000 E Durchmesser), Sekunden, E/s. y wächst nach unten wie auf dem Bildschirm. Die Werte sind mit
 * dem BalanceReport abgestimmt; Herleitung und Begründung in docs/spiel.md.
 */
public final class Tuning {
    private Tuning() {
    }

    /* Spielfeld */
    public static final double SIZE = 1000, CENTER = 500, RADIUS = 500;
    /** Fester Zeitschritt der Spiellogik (unabhängig von der Bildrate). */
    public static final double STEP = 1.0 / 60;

    /* Schiff */
    public static final double SHIP_Y = 800, SHIP_W = 70, SHIP_H = 64;
    /** Abstand der unteren Ecken des Schiffs zum sichtbaren Rand. */
    public static final double LANE_MARGIN = 10;
    /** Anteil von Breite und Höhe, der bei Kollisionen Schiff–Gegner zählt (fairer als das volle Sprite). */
    public static final double SHIP_HITBOX = 0.8, ENEMY_HITBOX = 0.85;
    /** Unverwundbarkeit nach einer Kollision; gilt nicht für Gegner, die unten hinausfliegen. */
    public static final double INVULNERABLE_S = 1.0;
    public static final int LIVES = 3;

    /* Dauerfeuer */
    public static final double FIRE_RATE = 7, BULLET_SPEED = 1400, BULLET_W = 8, BULLET_H = 24;

    /* Feste Speichergrößen: während des Spiels wird nichts neu angelegt */
    public static final int MAX_BULLETS = 8, MAX_ENEMIES = 32, MAX_PARTICLES = 192, STARS = 32;

    /* Schwierigkeit (Difficulty): steigt bis RAMP_S auf das Grundniveau, danach langsam ohne Obergrenze */
    public static final double RAMP_S = 300;
    public static final double FIRST_WAVE_S = 1.5;
    /** Abstand der Wellen in s, zufällig ± GAP_JITTER (Anteil). */
    public static final double GAP_START = 2.4, GAP_END = 1.0, GAP_JITTER = 0.25;
    /** Höchstzahl Gegner je Welle (die Zahl ist zufällig 1 … Höchstzahl). */
    public static final double WAVE_MAX_START = 2, WAVE_MAX_END = 5;
    /** Gegner einer Welle kommen versetzt: je weiterer Gegner bis zu so viel E weiter oben. */
    public static final double WAVE_STAGGER = 110;
    /** Tempofaktor auf die Grundgeschwindigkeit der Gegner. */
    public static final double SPEED_START = 1.0, SPEED_END = 1.6, SPEED_PER_MIN_AFTER = 0.1;
    /** Anteil mittlerer und großer Gegner an einer Welle. */
    public static final double W_MEDIUM_START = 0.2, W_MEDIUM_END = 0.32, W_LARGE_END = 0.06;
    /** Große Gegner ab LARGE_FROM_S höchstens einer gleichzeitig, ab TWO_LARGE_FROM_S zwei. */
    public static final double LARGE_FROM_S = 60, TWO_LARGE_FROM_S = 180;
    /** Obergrenze der Trefferpunkte aller Gegner im Spiel, gegen zufällige Spitzen, die nicht zu schaffen sind. */
    public static final double HP_CAP_START = 45, HP_CAP_END = 90, HP_CAP_PER_MIN_AFTER = 15;
    /** Kleine Gegner pendeln ab ZIGZAG_FROM_S seitlich, bis zu diesem Anteil am Ende der Steigerung. */
    public static final double ZIGZAG_FROM_S = 60, ZIGZAG_MAX = 0.5, ZIGZAG_AMP = 40, ZIGZAG_OMEGA = 2.4;

    /*
     * Gegnermenge (Einstellung im Menü: wenig, mittel, viel, sehr viel). Faktor auf die Rate der Wellen und auf die
     * Obergrenze der Trefferpunkte; ohne die zweite würde die Grenze eine höhere Menge wieder abschneiden.
     */
    public static final double[] SPAWN_LEVELS = {0.6, 1.0, 1.5, 2.2};
    public static final int SPAWN_DEFAULT = 1;

    /*
     * Beschleunigung im Flug: Tempo = Grundtempo × (1 + FLIGHT_ACCEL × y / 1000), y auf [0, 1000] begrenzt. Oben
     * fliegen die Gegner mit dem Grundtempo, auf Höhe des Schiffs 1,48-mal, unten 1,6-mal so schnell. Die
     * Grundtempi in EnemyType sind so gesenkt, dass die Flugzeit bis zum Schiff etwa gleich bleibt wie ohne.
     */
    public static final double FLIGHT_ACCEL = 0.6;

    /* Rückmeldung */
    public static final double FLASH_S = 0.06;
}
