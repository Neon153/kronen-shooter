# Entwicklung

## In Android Studio bauen

1. *File → Open* und den Repository-Ordner wählen (den Ordner mit `settings.gradle.kts`), *Trust Project*.
2. Gradle-Sync abwarten. Er lädt Gradle 8.11.1 (Wrapper) und das Android-Gradle-Plugin 8.7.3, wie bei
   substanz-tracker. Fehlt die Android-Plattform 35, bietet Android Studio die Installation an. Ein vorgeschlagenes
   Upgrade des Plugins kann angenommen werden. Das Projekt hat zwei Module: `core` (Spiellogik, reines Java) und
   `wear` (Uhr).
3. Starten: oben die Run-Konfiguration **wear** und die Uhr (oder einen Wear-OS-Emulator) wählen, *Run ▶*. Die Uhr
   wird per WLAN verbunden, siehe [uhr.md](uhr.md#auf-die-uhr-bringen).
4. APK: *Build → Generate App Bundles or APKs → Generate APKs*. Ergebnis:
   `wear/build/outputs/apk/debug/wear-debug.apk`.

Gradle-JDK: das in Android Studio enthaltene JDK (17 oder neuer). Quell- und Zielversion Java 11. Die Quellen
verzichten auf Lambdas und auf Sprachmittel nach Java 8, damit sie auch mit `javac -source 8` und `dx` bauen
(`tools/build-apk.sh`).

| Meldung | Ursache / Abhilfe |
|---|---|
| `Plugin [id: 'com.android.application'] was not found` | kein Internet beim Sync, Proxy prüfen |
| `Unsupported Java` | *Settings → Build Tools → Gradle → Gradle JDK* auf das enthaltene JDK stellen |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | installierte App mit anderer Signatur deinstallieren (löscht den Rekord) |

## Tests

Rechtsklick auf `core/src/test/java` → *Run Tests*, `./gradlew :core:test` oder ohne Gradle `./tools/test.sh`
(alle Klassen) bzw. `./tools/test.sh RulesTest BalanceReport` (einzelne). `tools/test.sh` braucht JUnit 4.13 und
Hamcrest 1.3, unter Ubuntu aus den Paketen `junit4` und `libhamcrest-java`. Es prüft dabei auch, dass `core` mit
`-source 8` und `-Xlint:all -Werror` kompiliert.

| Test | prüft |
|---|---|
| `ArenaTest` | Bahn aus der Sehne an der Unterkante des Schiffs, Schiff am Rand ganz im Kreis, Kreisränder, „ganz unten hinaus“ am Rand der Bahn vor y = 1000, Sichtbarkeit, Bereich für neue Gegner (erreichbar, auf Höhe des Schiffs ganz sichtbar), eckige Uhren |
| `RulesTest` | Leben: 1 bis 5 zu Beginn (begrenzt), Hinausfliegen und Kollision kosten genau eins, Rammen durch große Gegner, Gruppe = ein Leben, Unverwundbarkeit, Hinausfliegen trotz Unverwundbarkeit, Ende bei 0 Leben, nie negativ; Punkte 1 / 3 / 10, Trefferpunkte, Blitz nur bei mittleren; Dauerfeuer; Pause friert alles ein; Krone mit Begrenzung; gleicher Seed = gleiche Partie |
| `CollisionTest` | kein Durchspringen (Raster 1,7 E), unterer Gegner zuerst, ein Geschoss = ein Treffer, knapp daneben = kein Treffer, keine Treffer oberhalb des sichtbaren Bereichs, kleinere Trefferfläche bei Kollisionen |
| `SpawnerTest` | über 10.000 Gegner aus 40 Seeds: in der Bahn (auch pendelnd), von oberhalb, keine Überlappung, Tempo im Bereich, Grenze der Trefferpunkte, große erst ab 1 min und höchstens maxLarge; während einer Partie bleiben alle Gegner in der Bahn; erste Welle nach 1,5 s; eckige Uhren |
| `FlightTest` | Beschleunigung im Flug: Faktor nach Höhe, Gegner wird im Flug schneller, `flightTime` stimmt mit der Simulation überein (1 %), Zeit bis zum Schiff wie vor der Beschleunigung (3 %); Gegnermenge: mehr Stufe = mehr Gegner, Grenze der Trefferpunkte skaliert, Begrenzung der Stufe |
| `DifficultyTest` | Start- und Endwerte, alle Werte monoton, Tempo steigt ohne Ende, großer Gegner passt unter die Grenze |
| `InputTest` | Krone: drei Stufen, Richtung, Begrenzung, NaN; FrameClock: 30 und 60 Bilder/s ergeben 60 Schritte/s, Lücken gekappt, Neubeginn nach Pause |
| `SpritesTest` | Raster rechteckig, nur bekannte Farben, Seitenverhältnis passt zur logischen Größe, Größen wie `EnemyType` |
| `RendererTest` | Punkte, Leben (auch 5, mittig), Schiff am Rand, Pause- und End-Feld ganz im Kreis; Reihenfolge (Punkte unter den Gegnern, Schiff darüber); Blitz und Lebensbalken; Punktetext nur bei Änderung neu; Ergebnis am Ende; Blinken bei Unverwundbarkeit |
| `BalanceReport` | 4 × 200 Partien mit Bot-Spielern, Spieldauer und Punkte, Ursachen der Lebensverluste; je Gegnermenge und je Lebenszahl 200 Partien „wie Mensch“; Ziele siehe [spiel.md](spiel.md#balancing); großer Gegner allein immer zu schaffen |
| `PreviewReport` | Vorschaubilder (siehe unten) |

## Vorschaubilder

`PreviewReport` zeichnet typische Szenen über denselben `GameRenderer` mit `java.awt` als PNG (450 px, runde
Maske): `spiel.png`, `pause.png`, `ende.png`, `gegner.png`. Ziel: `build/preview/` bei `tools/test.sh`,
`core/build/preview/` bei Gradle. Die Bilder in `docs/img/` stammen daraus; nach Änderungen an Sprites oder Anzeige
neu erzeugen und kopieren. Schriften und Kantenglättung weichen leicht von der Uhr ab.

## APK ohne Gradle bauen

`tools/build-apk.sh` baut die Uhr-APK mit Werkzeugen aus den Ubuntu-Paketen `aapt` (enthält `aapt2`),
`dalvik-exchange`, `zipalign` und `apksigner`, in derselben Reihenfolge wie Gradle:
1. aapt2 compile/link (Ressourcen);
2. javac mit `-Xlint:all -Werror`, `core` und `wear`;
3. dx;
4. zipalign;
5. apksigner.

Gedacht für Umgebungen ohne Zugriff auf Googles Maven-Repository. Da die App keine Bibliotheken nutzt, entsteht
dieselbe App wie mit Gradle. Paketname, minSdk, targetSdk und Version liest das Skript aus
`wear/build.gradle.kts`.

```sh
export ANDROID_JAR=/pfad/zu/android-35/android.jar ANDROID_RES_JAR=/pfad/zu/android-34/android.jar
MIN_JAR=/pfad/zu/android-30/android.jar ./tools/build-apk.sh    # Ergebnis: build/KronenShooter.apk
```

- `ANDROID_JAR`: Plattform zum Kompilieren, API 35 wie `compileSdk`. Standard:
  `$ANDROID_HOME/platforms/android-35/android.jar`.
- `ANDROID_RES_JAR`: Plattform für aapt2, API 34. Das aapt2 aus Ubuntu (Android 14) kann die Ressourcentabelle von
  API 35 nicht lesen; die Ressourcen nutzen nichts Neueres.
- `MIN_JAR` (optional): zusätzlich gegen minSdk 30 kompilieren, als Ersatz für die Lint-Prüfung „NewApi“. Jede
  Meldung ist ein Aufruf oberhalb von minSdk und muss hinter einer Versionsabfrage liegen. Stand 1.2: nur
  `View.setRequestedFrameRate` in `GameActivity` (API 35), abgesichert.
- Ohne Android-SDK: die `android.jar` gibt es z. B. im GitHub-Repository `Sable/android-platforms`
  (Ordner `android-30`, `android-34`, `android-35`).

## Werte ändern

Spielwerte stehen in `Tuning` und `EnemyType`. Nach einer Änderung `./tools/test.sh BalanceReport` laufen lassen.
Der Bericht zeigt Spieldauer und Punkte und schlägt fehl, wenn das Spiel zu leicht, zu schwer oder am Anfang unfair
wird. Danach die Tabellen in [spiel.md](spiel.md) nachziehen.

## Signatur

Debug- und Release-Builds werden mit `keystore/kronenshooter.jks` signiert (Alias und Passwort `kronenshooter`,
konfiguriert in `wear/build.gradle.kts` und `tools/build-apk.sh`). Gradle- und Skript-Builds tragen dieselbe
Signatur und lassen sich übereinander installieren, ohne den Rekord zu verlieren. Der Schlüssel liegt offen im
Repository. Das ist nur für private Nutzung vertretbar; vor einer Veröffentlichung einen neuen Schlüssel erzeugen
und außerhalb des Repositorys aufbewahren.

## Version

`versionCode`/`versionName` in `wear/build.gradle.kts` (3 / 1.2). `tools/build-apk.sh` liest sie von dort.
