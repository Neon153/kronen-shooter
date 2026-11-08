package de.kronenshooter.core;

/** Texte, die im Spielfeld gezeichnet werden (Menü-Texte liegen in den Android-Ressourcen). */
public final class Texts {
    private Texts() {
    }

    public static final String PAUSE = "Pause";
    public static final String TAP_RESUME = "Tippen: weiter";
    public static final String SWIPE_MENU = "Wischen: Menü";
    public static final String GAME_OVER = "Vorbei";
    public static final String TAP_AGAIN = "Tippen: nochmal";
    public static final String NEW_RECORD = "Neuer Rekord!";

    public static String record(int best) {
        return "Rekord " + best;
    }
}
