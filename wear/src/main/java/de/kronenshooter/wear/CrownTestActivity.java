package de.kronenshooter.wear;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import de.kronenshooter.core.Arena;
import de.kronenshooter.core.CrownInput;

/**
 * Messseite für die Krone: summiert die Drehung in dp (wie sie im Spiel ankommt) und zeigt, wie viele Umdrehungen
 * die ganze Bahn des Schiffs bei der eingestellten Empfindlichkeit braucht. Damit lassen sich die Werte in
 * CrownInput.GAIN an der Uhr bestimmen (docs/uhr.md). Antippen setzt zurück.
 */
public final class CrownTestActivity extends WearActivity {
    private TextView total, detail, lane;
    private double sumDp, lastDp;
    private int events, level;
    private double laneWidth;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_crown_test);
        View content = findViewById(R.id.content);
        roundPadding(content, 0.12, 0.12, 0.12);
        total = findViewById(R.id.total);
        detail = findViewById(R.id.detail);
        lane = findViewById(R.id.lane);
        Arena a = new Arena(getResources().getConfiguration().isScreenRound());
        laneWidth = a.laneMax - a.laneMin;
        level = new Store(this).sensitivity();
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sumDp = 0;
                lastDp = 0;
                events = 0;
                show();
            }
        });
        show();
    }

    @Override
    boolean onRotary(float px) {
        lastDp = px / density;
        sumDp += lastDp;
        events++;
        show();
        return true;
    }

    private void show() {
        total.setText(getString(R.string.crown_total, String.format(Locale.GERMANY, "%.0f", sumDp)));
        detail.setText(getString(R.string.crown_events, events, String.format(Locale.GERMANY, "%.1f", lastDp)));
        double perTurn = Math.abs(sumDp) * CrownInput.GAIN[level];
        String turns = perTurn > 1 ? String.format(Locale.GERMANY, "%.2f", laneWidth / perTurn) : "–";
        lane.setText(getString(R.string.crown_lane, MainActivity.sensitivityName(this, level), turns));
    }
}
