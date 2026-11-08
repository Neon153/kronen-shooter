package de.kronenshooter.wear;

import android.content.Context;
import android.content.SharedPreferences;

import de.kronenshooter.core.CrownInput;
import de.kronenshooter.core.Tuning;

/**
 * Rekorde und Einstellungen (SharedPreferences "kronen_shooter"). Der Rekord gilt je Gegnermenge ("best_0" …
 * "best_3"), weil Partien mit mehr Gegnern kürzer sind und anders punkten. Version 1.0 hatte einen einzigen Rekord
 * ("best", immer „mittel“); er wird beim ersten Start als Rekord für „mittel“ übernommen.
 */
final class Store {
    static final int FPS_LOW = 30, FPS_HIGH = 60;
    private static final String PREFS = "kronen_shooter";
    private static final String BEST = "best_", BEST_1_0 = "best", SPAWN = "spawn", SENSITIVITY = "sensitivity",
            INVERT = "invert", VIBRATE = "vibrate", FPS = "fps";

    private final SharedPreferences p;

    Store(Context c) {
        p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (p.contains(BEST_1_0)) {
            SharedPreferences.Editor e = p.edit().remove(BEST_1_0);
            String key = BEST + Tuning.SPAWN_DEFAULT;
            if (!p.contains(key)) e.putInt(key, p.getInt(BEST_1_0, 0));
            e.commit();
        }
    }

    private static int level(int spawnLevel) {
        return Math.max(0, Math.min(Tuning.SPAWN_LEVELS.length - 1, spawnLevel));
    }

    /** Rekord für eine Gegnermenge. */
    int best(int spawnLevel) {
        return p.getInt(BEST + level(spawnLevel), 0);
    }

    /** Synchron (commit): ein neuer Rekord soll auch ein sofortiges Beenden der App überstehen. */
    boolean saveBest(int spawnLevel, int score) {
        return p.edit().putInt(BEST + level(spawnLevel), score).commit();
    }

    /** Gegnermenge 0 … 3 (wenig, mittel, viel, sehr viel). */
    int spawnLevel() {
        return level(p.getInt(SPAWN, Tuning.SPAWN_DEFAULT));
    }

    void setSpawnLevel(int spawnLevel) {
        p.edit().putInt(SPAWN, level(spawnLevel)).apply();
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
