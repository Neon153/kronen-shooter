package de.kronenshooter.wear;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * Grundlage aller Seiten: Innenabstände für runde Bildschirme und Drehen der Krone (wie in substanz-tracker).
 * Jede Seite ist eine eigene Activity, damit Wischen nach rechts (Standard auf Wear OS) genau eine Ebene zurückgeht.
 */
abstract class WearActivity extends Activity {
    float density;
    /** Scrollt mit der Krone, falls die Seite eine Liste hat. */
    ScrollView scroll;
    private float scrollFactor;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        density = getResources().getDisplayMetrics().density;
        scrollFactor = ViewConfiguration.get(this).getScaledVerticalScrollFactor();
    }

    /**
     * Innenabstände als Anteil der Bildschirmgröße: auf runden Uhren braucht der Inhalt oben, unten und seitlich
     * Abstand zum Rand. Eckige Uhren bekommen kleine feste Abstände.
     */
    void roundPadding(View content, double top, double bottom, double side) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w = dm.widthPixels, h = dm.heightPixels;
        if (getResources().getConfiguration().isScreenRound()) {
            content.setPadding((int) Math.round(w * side), (int) Math.round(h * top), (int) Math.round(w * side),
                    (int) Math.round(h * bottom));
        } else {
            int p = Math.round(8 * density);
            content.setPadding(p, p, p, 2 * p);
        }
    }

    /* Krone */

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            // Uhrzeigersinn positiv (wie scrollBy: Inhalt wandert nach oben)
            float px = -ev.getAxisValue(MotionEvent.AXIS_SCROLL) * scrollFactor;
            if (onRotary(px)) return true;
        }
        return super.dispatchGenericMotionEvent(ev);
    }

    /** Krone um px gedreht. Standard: Liste scrollen. true = verarbeitet. */
    boolean onRotary(float px) {
        if (scroll == null) return false;
        scroll.scrollBy(0, Math.round(px));
        return true;
    }
}
