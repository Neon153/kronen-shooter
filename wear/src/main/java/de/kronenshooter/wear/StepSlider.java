package de.kronenshooter.wear;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Schieberegler (SeekBar) für die Uhr. Wear OS schließt eine Seite, wenn man nach rechts wischt (Zurück), außer die
 * Ansicht unter dem Finger meldet, dass sie waagerecht scrollen kann. Ein normaler SeekBar meldet das nicht: Ein Zug
 * nach rechts auf dem Regler würde sonst das Menü und damit die App schließen. Dieser Regler meldet deshalb immer
 * waagerechte Scrollbarkeit; Zurück-Wischen funktioniert überall sonst auf der Seite.
 */
public final class StepSlider extends SeekBar {
    public StepSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return true;
    }
}
