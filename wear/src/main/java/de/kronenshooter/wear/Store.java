package de.kronenshooter.wear;

import android.content.Context;
import android.content.SharedPreferences;

import de.kronenshooter.core.CrownInput;

/** Rekord und Einstellungen (SharedPreferences "kronen_shooter"). */
final class Store {
    static final int FPS_LOW = 30, FPS_HIGH = 60;
    private static final String PREFS = "kronen_shooter";
    private static final String BEST = "best", SENSITIVITY = "sensitivity", INVERT = "invert", VIBRATE = "vibrate",
            FPS = "fps";

    private final SharedPreferences p;

    Store(Context c) {
        p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    int best() {
        return p.getInt(BEST, 0);
    }

    /** Synchron (commit): ein neuer Rekord soll auch ein sofortiges Beenden der App überstehen. */
    boolean saveBest(int score) {
        return p.edit().putInt(BEST, score).commit();
    }

    /** Empfindlichkeit der Krone 0 … 2 (langsam, mittel, schnell). */
    int sensitivity() {
        return p.getInt(SENSITIVITY, CrownInput.DEFAULT_LEVEL);
    }

    void setSensitivity(int level) {
        p.edit().putInt(SENSITIVITY, level).apply();
    }

    boolean invert() {
        return p.getBoolean(INVERT, false);
    }

    void setInvert(boolean v) {
        p.edit().putBoolean(INVERT, v).apply();
    }

    boolean vibrate() {
        return p.getBoolean(VIBRATE, true);
    }

    void setVibrate(boolean v) {
        p.edit().putBoolean(VIBRATE, v).apply();
    }

    /** Bildrate 30 (Standard, spart Akku) oder 60. */
    int fps() {
        return p.getInt(FPS, FPS_LOW) == FPS_HIGH ? FPS_HIGH : FPS_LOW;
    }

    void setFps(int fps) {
        p.edit().putInt(FPS, fps).apply();
    }

    CrownInput crown() {
        return new CrownInput(sensitivity(), invert());
    }
}
