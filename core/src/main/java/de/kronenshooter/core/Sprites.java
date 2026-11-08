package de.kronenshooter.core;

/**
 * Eigene Pixel-Sprites (keine Vorlage aus bestehenden Spielen). Jedes Sprite ist ein Raster aus Zeichen: '.' ist
 * durchsichtig, jedes andere Zeichen steht für eine Farbe aus KEYS/COLORS. Gezeichnet wird in der logischen Größe
 * W × H (in E), die zum Seitenverhältnis des Rasters passt; die App rechnet das Raster einmal in ein Bitmap um.
 */
public final class Sprites {
    private Sprites() {
    }

    public static final int SHIP = 0, SMALL = 1, MEDIUM = 2, LARGE = 3, LIFE = 4, COUNT = 5;

    static final String[][] ROWS = {
            { // Schiff: Pfeil mit Cockpit, Flügeln und zwei Triebwerken
                    ".....w.....",
                    "....waw....",
                    "....aca....",
                    "...aacaa...",
                    "...aaaaa...",
                    ".b.aaaaa.b.",
                    ".baaaaaaab.",
                    "bbaaabaaabb",
                    "bb.aa.aa.bb",
                    "....r.r....",
            },
            { // klein: Drohne, Spitze nach unten
                    "a.......a",
                    "aa.....aa",
                    "aaaaaaaaa",
                    ".abbbbba.",
                    "..aaeaa..",
                    "...aaa...",
                    "....a....",
            },
            { // mittel: Delta-Jäger mit zwei Triebwerken, Spitze nach unten
                    "bb.aaaaaaa.bb",
                    "bbaaaaaaaaabb",
                    ".aaaaaaaaaaa.",
                    "..aaaaeaaaa..",
                    "...aaaaaaa...",
                    "....aaaaa....",
                    ".....aaa.....",
                    "......a......",
            },
            { // groß: sechseckiger Rumpf mit Kern
                    "......aaaaaaaaaa......",
                    "....aaabbbbbbbbaaa....",
                    "..aaabbaaaaaaaabbaaa..",
                    ".aabbaaccaaaaccaabbaa.",
                    "aabaaaccccaaccccaaabaa",
                    "abaaaaaccaeeaccaaaaaba",
                    "abaaaaaaaeeeeaaaaaaaba",
                    "abaaaaaaaeeeeaaaaaaaba",
                    "abaaaaaccaeeaccaaaaaba",
                    "aabaaaccccaaccccaaabaa",
                    ".aabbaaccaaaaccaabbaa.",
                    "..aaabbaaaaaaaabbaaa..",
                    "....aaabbbbbbbbaaa....",
                    "......aaaaaaaaaa......",
            },
            null, // LIFE: wie SHIP, kleiner
    };

    static final String[] KEYS = {"wabcr", "abe", "abe", "abce", "wabcr"};
    static final int[][] COLORS = {
            {0xFFFFFFFF, Palette.SHIP, 0xFF2F6FED, 0xFF2F8FD8, 0xFFFF9F43},
            {Palette.SMALL, 0xFF2E8B57, 0xFFFFF27A},
            {Palette.MEDIUM, 0xFFC2410C, 0xFFFFFFFF},
            {0xFFB3306A, 0xFF7A1D45, 0xFFFF8FB8, 0xFFFFE066},
            {0xFFFFFFFF, Palette.SHIP, 0xFF2F6FED, 0xFF2F8FD8, 0xFFFF9F43},
    };

    /** Logische Größe in E. */
    static final double[] W = {Tuning.SHIP_W, EnemyType.SMALL.w, EnemyType.MEDIUM.w, EnemyType.LARGE.w, 33};
    static final double[] H = {Tuning.SHIP_H, EnemyType.SMALL.h, EnemyType.MEDIUM.h, EnemyType.LARGE.h, 30};

    public static int of(EnemyType t) {
        switch (t) {
            case SMALL:
                return SMALL;
            case MEDIUM:
                return MEDIUM;
            default:
                return LARGE;
        }
    }

    private static String[] rows(int id) {
        return ROWS[id == LIFE ? SHIP : id];
    }

    public static int cols(int id) {
        return rows(id)[0].length();
    }

    public static int rowCount(int id) {
        return rows(id).length;
    }

    public static double w(int id) {
        return W[id];
    }

    public static double h(int id) {
        return H[id];
    }

    /** Farbe des Rasterpunkts (Spalte c, Zeile r) als ARGB, 0 = durchsichtig. */
    public static int pixel(int id, int c, int r) {
        char ch = rows(id)[r].charAt(c);
        if (ch == '.') return 0;
        int k = KEYS[id].indexOf(ch);
        return k < 0 ? 0 : COLORS[id][k];
    }
}
