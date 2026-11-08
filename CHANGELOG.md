# Changelog

## 2026-11-08 – 1.0

- Erste Version der Uhr-App für Wear OS 3 und neuer (gebaut für die Pixel Watch 5), läuft ohne Handy.
- Schiff mit der Krone steuern (drei Empfindlichkeiten, Richtung umkehrbar), Dauerfeuer mit 7 Schuss/s.
- Drei Gegnerarten mit 1, 5 und 40 Trefferpunkten, Punkte 1 / 3 / 10 je Abschuss.
- Wellen mit zufälliger Anzahl, Mischung und Position. Fair: jeder Gegner in Reichweite, von oberhalb, ohne
  Überlappung, mit Obergrenze der Trefferpunkte. Die Schwierigkeit steigt über 5 Minuten, danach langsam ohne Ende.
- 3 Leben: Hinausfliegen (sobald ganz außerhalb des sichtbaren Kreises) und Kollision kosten je eines. Nach einer
  Kollision 1 s unverwundbar.
- Rekord mit „Neuer Rekord!“, automatische Pause beim Verlassen, Vibration bei Lebensverlust und am Ende.
- Menü mit Einstellungen und Messseite „Krone messen“.
- Akku: Takt nur beim Spielen, 30 Bilder/s (umschaltbar auf 60) bei fester Logik mit 60 Hz, keine Objekte pro
  Bild, schwarzer Hintergrund, Bildschirm nur beim Spielen wach.
- Eigene Pixel-Sprites; das App-Symbol ist aus dem Schiff-Sprite erzeugt.
- Spiellogik in `core/` (reines Java) mit 58 Tests, Balancing-Bericht mit Bot-Spielern und Vorschaubildern.
- `tools/build-apk.sh` baut die APK ohne Gradle und ohne Maven, `tools/test.sh` die Tests ohne Gradle.
- Noch nicht auf einer Uhr getestet.
