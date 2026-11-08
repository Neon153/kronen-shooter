package de.kronenshooter.wear;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import de.kronenshooter.core.CrownInput;
import de.kronenshooter.core.Tuning;

/**
 * Rekorde und Einstellungen (SharedPreferences "kronen_shooter"). Der Rekord gilt je Kombination aus Gegnermenge und
 * Leben ("best_&lt;Stufe&gt;_&lt;Leben&gt;", z. B. "best_1_3"), weil Partien mit mehr Gegnern oder weniger Leben
 * kürzer sind und anders punkten. Ältere Rekorde werden beim ersten Start übernommen, jeweils mit 3 Leben:
 * 1.0 "best" (immer „mittel“) und 1.1 "best_&lt;Stufe&gt;".
 */
final class Store {
    static final int FPS_LOW = 30, FPS_HIGH = 60;
    private static final String PREFS = "kronen_shooter";
    private static final String BEST = "best_", BEST_1_0 = "best", SPAWN = "spawn", LIVES = "lives",
            SENSITIVITY = "sensitivity", INVERT = "invert", VIBRATE = "vibrate", FPS = "fps";

    private final SharedPreferences p;

    Store(Context c) {
        p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        migrate();
    }

    /** Rekorde aus 1.0 ("best") und 1.1 ("best_&lt;Stufe&gt;") als Rekorde mit 3 Leben übernehmen. */
    private void migrate() {
        Map<String, ?> all = p.getAll();
        Map<String, Integer> moved = new HashMap<>();
        SharedPreferences.Editor e = null;
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String k = entry.getKey();
            String target;
            if (k.equals(BEST_1_0)) target = key(Tuning.SPAWN_DEFAULT, Tuning.LIVES);
            else if (k.matches("best_[0-9]")) target = key(k.charAt(BEST.length()) - '0', Tuning.LIVES);
            else continue;
            if (e == null) e = p.edit();
            e.remove(k);
            if (!(entry.getValue() instanceof Integer)) continue;
            int v = (Integer) entry.getValue();
            Integer before = moved.get(target);
            if (before == null) before = p.getInt(target, 0);
            moved.put(target, Math.max(before, v));
        }
        if (e == null) return;
        for (Map.Entry<String, Integer> m : moved.entrySet()) e.putInt(m.getKey(), m.getValue());
        e.commit();
    }

    private static int level(int spawnLevel) {
        return Math.max(0, Math.min(Tuning.SPAWN_LEVELS.length - 1, spawnLevel));
    }

    private static int clampLives(int lives) {
        return Math.max(Tuning.LIVES_MIN, Math.min(Tuning.LIVES_MAX, lives));
    }

    private static String key(int spawnLevel, int lives) {
        return BEST + level(spawnLevel) + "_" + clampLives(lives);
    }

    /** Rekord für eine Kombination aus Gegnermenge und Leben. */
    int best(int spawnLevel, int lives) {
        return p.getInt(key(spawnLevel, lives), 0);
    }

    /** Synchron (commit): ein neuer Rekord soll auch ein sofortiges Beenden der App überstehen. */
    boolean saveBest(int spawnLevel, int lives, int score) {
        return p.edit().putInt(key(spawnLevel, lives), score).commit();
    }

    /** Leben zu Beginn einer Partie, 1 … 5 (Standard 3). */
    int lives() {
        return clampLives(p.getInt(LIVES, Tuning.LIVES));
    }

    void setLives(int lives) {
        p.edit().putInt(LIVES, clampLives(lives)).apply();
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
