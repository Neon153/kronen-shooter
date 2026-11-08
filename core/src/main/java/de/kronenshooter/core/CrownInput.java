package de.kronenshooter.core;

/**
 * Drehung der Krone (in dp, Uhrzeigersinn positiv) in eine Verschiebung des Schiffs (in E). Die Krone setzt die
 * Position wie ein Drehregler, ohne Trägheit. Drei Empfindlichkeiten; die Werte sind Startwerte und werden mit der
 * Messseite „Krone messen“ an der Uhr bestimmt (docs/uhr.md).
 */
public final class CrownInput {
    /** E pro dp Drehung: langsam, mittel, schnell. */
    public static final double[] GAIN = {1.2, 2.0, 3.4};
    public static final int DEFAULT_LEVEL = 1;

    private final double gain;

    /** level 0 … 2 (wird begrenzt); invert kehrt die Richtung um (Uhrzeigersinn = links). */
    public CrownInput(int level, boolean invert) {
        double g = GAIN[Math.max(0, Math.min(GAIN.length - 1, level))];
        gain = invert ? -g : g;
    }

    public double dx(double dp) {
        return dp == dp ? dp * gain : 0;
    }
}
