package de.kronenshooter.wear;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

/**
 * Das Spiel im Vollbild. Lebenszyklus: onPause (Handgelenk runter, Benachrichtigung, Schnelleinstellungen, Druck auf
 * die Krone) pausiert sofort und stoppt den Takt; nach onResume steht „Pause – Tippen: weiter“ da. Der Bildschirm
 * bleibt nur wach, solange der Takt läuft (Stillstehen ist im Spiel normal, ohne das ginge der Bildschirm aus).
 * Rekord: gespeichert beim Ende und in onPause, falls überboten; „Neuer Rekord“ bezieht sich auf den Stand beim
 * Start der Partie.
 */
public final class GameActivity extends WearActivity implements GameView.Listener {
    private GameView view;
    private Store store;
    private Vibrator vibrator;
    private int bestAtStart;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new Store(this);
        bestAtStart = store.best();
        int fps = store.fps();
        view = new GameView(this, getResources().getConfiguration().isScreenRound());
        view.setCrown(store.crown());
        view.setFps(fps);
        view.setListener(this);
        setContentView(view);
        requestFrameRate(fps);
        if (store.vibrate()) {
            Vibrator v = getSystemService(Vibrator.class);
            vibrator = v != null && v.hasVibrator() ? v : null;
        }
    }

    /**
     * Wunsch an das System: Display mit der Bildrate des Spiels betreiben (LTPO-Display der Pixel Watch: 1–60 Hz).
     * Ob es folgt, entscheidet das System; das Spiel liefert ohnehin nur fps Bilder pro Sekunde.
     */
    private void requestFrameRate(int fps) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.preferredRefreshRate = fps;
        getWindow().setAttributes(lp);
        if (Build.VERSION.SDK_INT >= 35) view.setRequestedFrameRate(fps); // 35 = Android 15
    }

    @Override
    protected void onResume() {
        super.onResume();
        view.onResumed();
    }

    @Override
    protected void onPause() {
        view.onPaused();
        int score = view.game().score();
        if (score > store.best()) store.saveBest(score);
        super.onPause();
    }

    @Override
    boolean onRotary(float px) {
        view.crown(px / density);
        return true;
    }

    @Override
    public void onRunningChanged(boolean running) {
        if (running) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onLifeLost() {
        buzz(VibrationEffect.EFFECT_HEAVY_CLICK);
    }

    @Override
    public void onGameOver(int score) {
        int best = store.best();
        if (score > best) {
            store.saveBest(score);
            best = score;
        }
        view.showResult(best, score > bestAtStart);
        buzz(VibrationEffect.EFFECT_DOUBLE_CLICK);
    }

    @Override
    public void onNewGame() {
        bestAtStart = store.best();
    }

    private void buzz(int effect) {
        if (vibrator != null) vibrator.vibrate(VibrationEffect.createPredefined(effect));
    }
}
