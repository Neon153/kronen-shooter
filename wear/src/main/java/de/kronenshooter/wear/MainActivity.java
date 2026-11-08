package de.kronenshooter.wear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import de.kronenshooter.core.Tuning;

/**
 * Menü: Rekord für die eingestellte Gegnermenge, Start und Einstellungen (Gegnermenge, Empfindlichkeit und Richtung
 * der Krone, Vibration, Bildrate) sowie die Messseite für die Krone. Einstellungen wirken ab dem nächsten Spiel.
 */
public final class MainActivity extends WearActivity implements View.OnClickListener {
    private Store store;
    private TextView best, spawn, sensitivity, direction, vibration, fps;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        store = new Store(this);
        scroll = findViewById(R.id.scroll);
        roundPadding(findViewById(R.id.content), 0.14, 0.22, 0.13);
        best = findViewById(R.id.best);
        spawn = findViewById(R.id.spawn);
        sensitivity = findViewById(R.id.sensitivity);
        direction = findViewById(R.id.direction);
        vibration = findViewById(R.id.vibration);
        fps = findViewById(R.id.fps);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.crownTest).setOnClickListener(this);
        spawn.setOnClickListener(this);
        sensitivity.setOnClickListener(this);
        direction.setOnClickListener(this);
        vibration.setOnClickListener(this);
        fps.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh(); // Rekord nach einer Partie
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start) {
            startActivity(new Intent(this, GameActivity.class));
            return;
        }
        if (id == R.id.crownTest) {
            startActivity(new Intent(this, CrownTestActivity.class));
            return;
        }
        if (id == R.id.spawn) store.setSpawnLevel((store.spawnLevel() + 1) % Tuning.SPAWN_LEVELS.length);
        else if (id == R.id.sensitivity) store.setSensitivity((store.sensitivity() + 1) % 3);
        else if (id == R.id.direction) store.setInvert(!store.invert());
        else if (id == R.id.vibration) store.setVibrate(!store.vibrate());
        else if (id == R.id.fps) store.setFps(store.fps() == Store.FPS_LOW ? Store.FPS_HIGH : Store.FPS_LOW);
        refresh();
    }

    private void refresh() {
        int level = store.spawnLevel();
        String levelName = spawnName(this, level);
        int b = store.best(level);
        best.setText(b > 0 ? getString(R.string.best, levelName, b) : getString(R.string.best_none, levelName));
        spawn.setText(getString(R.string.spawn, levelName));
        sensitivity.setText(getString(R.string.sensitivity, sensitivityName(this, store.sensitivity())));
        direction.setText(getString(R.string.direction,
                getString(store.invert() ? R.string.direction_inverted : R.string.direction_normal)));
        vibration.setText(getString(R.string.vibration, getString(store.vibrate() ? R.string.on : R.string.off)));
        fps.setText(getString(R.string.fps, store.fps()));
    }

    static String spawnName(android.content.Context c, int level) {
        int[] names = {R.string.spawn_0, R.string.spawn_1, R.string.spawn_2, R.string.spawn_3};
        return c.getString(names[Math.max(0, Math.min(names.length - 1, level))]);
    }

    static String sensitivityName(android.content.Context c, int level) {
        int[] names = {R.string.sensitivity_0, R.string.sensitivity_1, R.string.sensitivity_2};
        return c.getString(names[Math.max(0, Math.min(2, level))]);
    }
}
