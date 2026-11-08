# Planung und Entscheidungen

Festgehalten aus der Planung vor Version 1.0 (8. November 2026), mit den Abweichungen, die sich bei der Umsetzung
ergeben haben. Details zu Regeln und Werten in [spiel.md](spiel.md), zur App in [uhr.md](uhr.md).

## Vorgabe

Space-Invaders-artiges Spiel für die Pixel Watch 5:
- Die Krone bewegt das Schiff nach links und rechts, geschossen wird dauerhaft.
- Gegner kommen in zufälliger Menge: einfache (ein Treffer), mittlere (mehrere) und große (sehr viele).
- Jeder abgeschossene Gegner erhöht die Punkte.
- Drei Leben. Eines geht verloren, wenn ein Gegner unten hinausfliegt oder durch das Schiff fliegt.
- Kein Gewinnen, nur ein möglichst hoher Punktestand.

Aufbau wie `Neon153/substanz-tracker`: Java, möglichst nur Android-Framework, auf Akku optimiert, Installation über
Debugging per WLAN aus Android Studio.

## Probleme der Vorgabe und Lösungen

| Problem | Lösung |
|---|---|
| Auf dem runden Display ist die Bahn des Schiffs durch den Kreis begrenzt. Gegner außerhalb wären nicht zu treffen, kosteten aber ein Leben | Gegner erscheinen nur innerhalb der Bahn (`Arena.spawnMin/Max`), geprüft in `SpawnerTest` |
| „Nur verlieren“ funktioniert nicht mit reinem Zufall, ein guter Spieler spielte endlos | Schwierigkeit steigt mit der Spielzeit, nach 5 min langsam weiter ohne Obergrenze |
| Ein Gegner, der das Schiff rammt, flöge danach unten hinaus: zwei Leben | er explodiert bei der Kollision |
| Eine Gruppe könnte alle Leben auf einmal nehmen | 1 s unverwundbar nach einer Kollision |
| Große Gegner müssen bei fester Feuerrate schaffbar sein | Balancing-Bericht mit Bot-Spieler und Prüfung „großer Gegner allein immer zu schaffen“ |
| „Hinausfliegen“ auf einem runden Display | zählt, sobald der Gegner ganz außerhalb des sichtbaren Kreises ist, nicht erst bei y = 1000 |
| Stillstehen ist im Spiel normal, der Bildschirm ginge aus | `FLAG_KEEP_SCREEN_ON` nur, solange gespielt wird |
| Handgelenk runter, Benachrichtigung, Krone drücken | sofort Pause, weiter nur durch Tippen |
| Versehentliches Wischen beendet die Partie | 1.0: Wischen schließt nur in Pause und am Ende. Seit 1.1 aufgehoben, Zurück muss immer funktionieren (siehe unten) |
| Urheberrecht | eigene Sprites, nicht „Space Invaders“ als Name |

## Entscheidungen

| Frage | Entscheidung |
|---|---|
| Repo- und App-Name | `Neon153/kronen-shooter`, „Kronen-Shooter“ |
| Punkte | gewichtet: klein 1, mittel 3, groß 10 (Variante B) |
| Unverwundbarkeit nach Kollision | ja, 1 s |
| Bildrate | Start mit 30 Bildern/s, endgültig nach Messung an der Uhr (im Menü umschaltbar) |
| Bibliotheken | keine; ohne Kachel ist auch AndroidX nicht nötig, die APK baut damit auch ohne Maven |
| Ton | keiner |

## Abweichungen bei der Umsetzung

| Geplant | Umgesetzt | Grund |
|---|---|---|
| Bahn x ∈ [150, 850] (Sehne auf Höhe der Schiffsmitte) | [171, 829] (Sehne an der Unterkante) | Am Rand ragten die unteren Ecken des Schiffs aus dem Kreis (`ArenaTest`) |
| 6 Schuss/s, Steigerung über 3 min, Wellenabstand 2,0 → 0,7 s, bis 6 Gegner je Welle, große ab 45 s | 7 Schuss/s, 5 min, 2,4 → 1,0 s, bis 5 Gegner, große ab 60 s | Mit den Startwerten hielt der Bot im Median nur 110 s durch, Ziel waren 2–4 min für Menschen. Varianten in [spiel.md](spiel.md#balancing) |
| Treffer-Blitz für alle Gegner | nur mittlere; Funken bei jedem Treffer | große Gegner hätten bei 7 Treffern/s dauernd weiß geflackert (Optik, OLED-Strom) |
| Mittlerer Gegner mit Hörnern oben | Delta-Jäger | erinnerte zu sehr an Arcade-Aliens |
| Ein Zufallsgenerator | zwei: Gegner (`rnd`) und Optik (`fx`) | Funken veränderten sonst die Gegnerfolge |
| Messanzeige für die Krone im Spiel | eigene Seite „Krone messen“ im Menü | stört das Spiel nicht, bleibt für spätere Uhren nutzbar |

## Version 1.1: Wünsche nach dem ersten Test

| Wunsch | Umsetzung |
|---|---|
| Gegnermenge einstellbar: wenig, mittel, viel, sehr viel | Faktor 0,6 / 1,0 / 1,5 / 2,2 auf die Rate der Wellen und die Obergrenze der Trefferpunkte. Größe der Wellen, Mischung und Tempo bleiben. **Rekord je Stufe**, weil Partien mit mehr Gegnern kürzer sind und anders punkten; der Rekord aus 1.0 gilt als „mittel“ |
| Gegner sollen über die Zeit im Flug schneller werden | Verstanden als: jeder Gegner beschleunigt auf dem Weg nach unten (oben Grundtempo, am Schiff ×1,48, unten ×1,6). Die Grundtempi sind um 18 % gesenkt, damit die Zeit bis zum Schiff gleich bleibt. Dass alle Gegner mit der Spielzeit schneller werden, gab es schon seit 1.0 (×1,0 → ×1,6 in 5 min, danach +0,1 je Minute) und bleibt |
| Wischen vom linken Rand nach rechts ist Zurück und muss auch im Spiel funktionieren | Sperre aus 1.0 entfernt. Zurück im Spiel beendet die Partie, der Rekord wird in `onPause` gesichert |

## Version 1.2: Leben einstellbar

| Wunsch | Umsetzung |
|---|---|
| Anzahl Leben 1 bis 5, am besten mit Schieberegler | `SeekBar` mit 5 Stufen und Strichen im Menü (Standard 3). Ein normaler SeekBar hätte beim Ziehen nach rechts das Zurück-Wischen ausgelöst und die App geschlossen; `StepSlider` meldet deshalb waagerechte Scrollbarkeit |
| (nicht gefordert) | Rekord je Kombination aus Gegnermenge und Leben, ältere Rekorde gelten als „3 Leben“. Mit 5 Leben dauern Partien länger, ein gemeinsamer Rekord wäre nicht vergleichbar |
| Erkenntnis | Die Lebenszahl verlängert Partien des Bots nur um rund 20 % (1 → 5 Leben), weil die Schwierigkeitswand am Ende alle Leben schnell nimmt. Bei Menschen mit frühen Fehlern wirkt sie stärker; sonst müsste die Wand flacher werden |

## Offen (an der Uhr)

1. Krone messen und `CrownInput.GAIN` setzen ([uhr.md](uhr.md#krone-einstellen)).
2. Bildrate und Akku bei 30 und 60 Bildern/s messen, Standard festlegen ([uhr.md](uhr.md#energie)).
3. Spielgefühl: Dauer einer Partie, Fairness am Anfang, Lesbarkeit der Punkte.
4. Prüfen, dass Zurück-Wischen mitten im Spiel zuverlässig ins Menü führt und nicht versehentlich ausgelöst wird.
5. Spielgefühl der Beschleunigung und der vier Gegnermengen.
