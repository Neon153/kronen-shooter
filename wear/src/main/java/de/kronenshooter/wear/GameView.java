package de.kronenshooter.wear;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import de.kronenshooter.core.Arena;
import de.kronenshooter.core.CrownInput;
import de.kronenshooter.core.FrameClock;
import de.kronenshooter.core.Game;
import de.kronenshooter.core.GameRenderer;

/**
 * Spielfeld. Takt über den Choreographer, aber nur solange gespielt wird und die Activity im Vordergrund ist
 * (zwischen onResume und onPause): in Pause und nach dem Ende kein einziges Bild mehr.
 * <ul>
 * <li>Je Bild: Zeit an FrameClock, die nötigen Logikschritte (fest 1/60 s), dann einmal zeichnen. Bilder kommen
 * höchstens mit der eingestellten Bildrate (30 oder 60/s); Vsyncs dazwischen werden ausgelassen.</li>
 * <li>Die Krone verschiebt das Schiff sofort; sichtbar wird es mit dem nächsten Bild.</li>
 * <li>Tippen: in Pause weiter, nach dem Ende (frühestens nach 0,8 s, gegen versehentliches Tippen) neue Partie.
 * Während des Spiels wird Tippen ignoriert (die Hand an der Krone streift leicht den Rand).</li>
 * <li>Wischen nach rechts (Zurück, Standard auf Wear OS) schließt das Spiel immer, auch mitten in einer Partie.
 * Die Ansicht meldet deshalb keine waagerechte Scrollbarkeit. Der Rekord wird dabei in onPause gesichert.</li>
 * </ul>
 */
final class GameView extends View implements Choreographer.FrameCallback {
    interface Listener {
        /** Takt läuft (true) oder steht (false): Bildschirm wach halten oder nicht. */
        void onRunningChanged(boolean running);

        void onLifeLost();

        void onGameOver(int score);

        void onNewGame();
    }

    static final long RESTART_DELAY_MS = 800;
    /** Toleranz beim Auslassen von Vsyncs: ein Bild darf so viel früher kommen als 1/Bildrate. */
    private static final long FRAME_TOLERANCE_NS = 4_000_000L;

    private final boolean round;
    private final int spawnLevel, startLives;
    private final CanvasPainter painter = new CanvasPainter();
    private final GameRenderer renderer = new GameRenderer();
    private final FrameClock clock = new FrameClock();
    private Listener listener;
    private CrownInput crown = new CrownInput(CrownInput.DEFAULT_LEVEL, false);
    private Game game;
    private long minFrameNs, lastFrameNs, overAtMs;
    private boolean resumed, running;
    private int seenLifeLost;

    /** spawnLevel: Gegnermenge 0 … 3 (wenig, mittel, viel, sehr viel); startLives: 1 … 5. */
    GameView(Context context, boolean round, int spawnLevel, int startLives) {
        super(context);
        this.round = round;
        this.spawnLevel = spawnLevel;
        this.startLives = startLives;
        game = new Game(new Arena(round), System.nanoTime(), spawnLevel, startLives);
        setFps(Store.FPS_LOW);
    }

    void setListener(Listener l) {
        listener = l;
    }

    void setCrown(CrownInput c) {
        crown = c;
    }

    void setFps(int fps) {
        minFrameNs = 1_000_000_000L / fps - FRAME_TOLERANCE_NS;
    }

    Game game() {
        return game;
    }

    void newGame() {
        game = new Game(new Arena(round), System.nanoTime(), spawnLevel, startLives);
        renderer.clearResult();
        seenLifeLost = 0;
        if (listener != null) listener.onNewGame();
        updateLoop();
        invalidate();
    }

    void showResult(int best, boolean newRecord) {
        renderer.setResult(best, newRecord);
        invalidate();
    }

    void onResumed() {
        resumed = true;
        updateLoop();
        invalidate();
    }

    /** Activity verliert den Vordergrund: Spiel pausiert, Takt steht. */
    void onPaused() {
        resumed = false;
        game.pause();
        updateLoop();
        invalidate();
    }

    /** Krone um dp gedreht (Uhrzeigersinn positiv). */
    void crown(double dp) {
        game.moveShip(crown.dx(dp));
    }

    private void updateLoop() {
        boolean want = resumed && game.state() == Game.State.PLAYING;
        if (want == running) return;
        running = want;
        Choreographer ch = Choreographer.getInstance();
        if (running) {
            clock.reset();
            lastFrameNs = 0;
            ch.postFrameCallback(this);
        } else {
            ch.removeFrameCallback(this);
        }
        if (listener != null) listener.onRunningChanged(running);
    }

    @Override
    public void doFrame(long nowNs) {
        if (!running) return;
        if (lastFrameNs != 0 && nowNs - lastFrameNs < minFrameNs) {
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }
        lastFrameNs = nowNs;
        int steps = clock.advance(nowNs);
        for (int i = 0; i < steps; i++) game.step();
        invalidate();
        if (game.state() == Game.State.OVER) {
            overAtMs = SystemClock.uptimeMillis();
            seenLifeLost = game.lifeLostCount();
            updateLoop();
            if (listener != null) listener.onGameOver(game.score());
            return;
        }
        if (game.lifeLostCount() != seenLifeLost) {
            seenLifeLost = game.lifeLostCount();
            if (listener != null) listener.onLifeLost();
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        painter.setSize(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        painter.begin(canvas);
        renderer.draw(game, painter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_UP) performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        Game.State s = game.state();
        if (s == Game.State.PAUSED) {
            game.resume();
            updateLoop();
            invalidate();
        } else if (s == Game.State.OVER && SystemClock.uptimeMillis() - overAtMs > RESTART_DELAY_MS) {
            newGame();
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(this);
        running = false;
        super.onDetachedFromWindow();
    }
}
