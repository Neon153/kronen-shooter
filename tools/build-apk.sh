#!/usr/bin/env bash
# Baut die Uhr-APK ohne Gradle und ohne Googles Maven-Repository:  ./tools/build-apk.sh
# In Android Studio wird stattdessen ganz normal mit Gradle gebaut (Run-Konfiguration "wear").
#
# Werkzeuge aus den Ubuntu-Paketen: aapt (enthält aapt2), dalvik-exchange (dx), zipalign, apksigner, ein JDK.
# Ablauf wie bei Gradle: aapt2 compile/link (Ressourcen) -> javac (-Xlint:all -Werror, core und wear) -> dx ->
# zipalign -> apksigner (Signaturschema v2 und v3).
#
# ANDROID_JAR      android.jar zum Kompilieren, API 35 wie compileSdk.
#                  Standard: $ANDROID_HOME/platforms/android-35/android.jar
# ANDROID_RES_JAR  android.jar für aapt2, API 34. Das aapt2 aus Ubuntu (Android 14) liest die Ressourcentabelle von
#                  API 35 nicht; die Ressourcen nutzen nichts Neueres. Standard: …/platforms/android-34/android.jar
# MIN_JAR          optional: android.jar API 30 (minSdk). Zusätzlich dagegen kompilieren; jede Meldung ist ein
#                  Aufruf oberhalb von minSdk und muss hinter einer Versionsabfrage liegen.
set -euo pipefail
cd "$(dirname "$0")/.."

# Werte aus wear/build.gradle.kts, damit beide Wege dieselbe APK-Version erzeugen
GRADLE=wear/build.gradle.kts
PKG=$(sed -n 's/.*namespace = "\(.*\)".*/\1/p' "$GRADLE")
APP_ID=$(sed -n 's/.*applicationId = "\(.*\)".*/\1/p' "$GRADLE")
MIN_SDK=$(sed -n 's/.*minSdk = \([0-9]*\).*/\1/p' "$GRADLE")
TARGET_SDK=$(sed -n 's/.*targetSdk = \([0-9]*\).*/\1/p' "$GRADLE")
VERSION_CODE=$(sed -n 's/.*versionCode = \([0-9]*\).*/\1/p' "$GRADLE")
VERSION_NAME=$(sed -n 's/.*versionName = "\(.*\)".*/\1/p' "$GRADLE")

AJ=${ANDROID_JAR:-${ANDROID_HOME:-}/platforms/android-35/android.jar}
AJ_RES=${ANDROID_RES_JAR:-${ANDROID_HOME:-}/platforms/android-34/android.jar}
[ -f "$AJ" ] || { echo "android.jar (API 35) nicht gefunden: ANDROID_JAR setzen" >&2; exit 1; }
[ -f "$AJ_RES" ] || { echo "android.jar (API 34) für aapt2 nicht gefunden: ANDROID_RES_JAR setzen" >&2; exit 1; }
OUT=build/local-wear
APK=build/KronenShooter.apk
rm -rf "$OUT" && mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/flat"

# Ressourcen. Der Paketname kommt bei Gradle aus "namespace", die applicationId ist kürzer (de.kronenshooter).
sed "s#<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">#<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$PKG\">#" \
  wear/src/main/AndroidManifest.xml > "$OUT/AndroidManifest.xml"
aapt2 compile --dir wear/src/main/res -o "$OUT/flat/res.zip"
aapt2 link -o "$OUT/app.unaligned.apk" -I "$AJ_RES" --manifest "$OUT/AndroidManifest.xml" \
  --java "$OUT/gen" --custom-package "$PKG" --rename-manifest-package "$APP_ID" \
  --min-sdk-version "$MIN_SDK" --target-sdk-version "$TARGET_SDK" \
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" "$OUT/flat/res.zip"

# Code: Spiellogik (core) und Uhr-App (wear). Java 8 als Zielversion, weil dx nichts Neueres versteht.
SRC=$(find core/src/main/java wear/src/main/java "$OUT/gen" -name '*.java')
javac -encoding UTF-8 -source 8 -target 8 -bootclasspath "$AJ" -Xlint:all,-options -Werror -d "$OUT/classes" $SRC
echo "Kompiliert gegen API 35 ohne Warnungen (-Werror)."

if [ -n "${MIN_JAR:-}" ]; then
  echo "Aufrufe oberhalb von minSdk $MIN_SDK (müssen hinter einer Versionsabfrage liegen):"
  mkdir -p "$OUT/min"
  LOW=$(javac -encoding UTF-8 -nowarn -source 8 -target 8 -bootclasspath "$MIN_JAR" -d "$OUT/min" $SRC 2>&1 \
    | grep -oE '[A-Za-z]+\.java:[0-9]+: error: .*' || true)
  echo "${LOW:-  keine}"
fi

dalvik-exchange --dex --min-sdk-version=26 --output="$OUT/classes.dex" "$OUT/classes"
(cd "$OUT" && aapt add app.unaligned.apk classes.dex > /dev/null)

zipalign -f -p 4 "$OUT/app.unaligned.apk" "$OUT/app.aligned.apk"
mkdir -p build
apksigner sign --ks keystore/kronenshooter.jks --ks-pass pass:kronenshooter --key-pass pass:kronenshooter \
  --v2-signing-enabled true --v3-signing-enabled true --out "$APK" "$OUT/app.aligned.apk"
apksigner verify --print-certs "$APK" | head -2
echo "APK: $APK ($(du -h "$APK" | cut -f1), Version $VERSION_NAME / $VERSION_CODE)"
