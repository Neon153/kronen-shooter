package de.kronenshooter.core;

/**
 * Farben als ARGB (wie android.graphics.Color). Hintergrund schwarz (OLED: schwarze Pixel bleiben aus), helle Farben
 * nur auf kleinen Flächen. Grundtöne wie das dunkle Design von substanz-tracker.
 */
public final class Palette {
    private Palette() {
    }

    public static final int BG = 0xFF000000;
    public static final int TEXT = 0xFFECEEF3;
    public static final int MUTED = 0xFF9BA1AD;
    public static final int SCORE = 0xFFB4BAC6;
    public static final int CARD = 0xFF1C1F26;
    public static final int ACCENT = 0xFF6F9CFF;
    public static final int DANGER = 0xFFFF6B63;
    public static final int BULLET = 0xFFFFF27A;
    public static final int HP_BG = 0xFF2B2F38;
    public static final int[] STAR = {0xFF2A2E36, 0xFF3F4550, 0xFF656C78};

    public static final int SHIP = 0xFF5CD6FF;
    public static final int SMALL = 0xFF6BE675;
    public static final int MEDIUM = 0xFFFFB347;
    public static final int LARGE = 0xFFE0457B;

    /** Leitfarbe einer Gegnerart (Explosion, Lebensbalken). */
    public static int enemy(EnemyType t) {
        switch (t) {
            case SMALL:
                return SMALL;
            case MEDIUM:
                return MEDIUM;
            default:
                return LARGE;
        }
    }
}
