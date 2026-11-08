#!/usr/bin/env bash
# Unit-Tests und Berichte von core ohne Gradle:  ./tools/test.sh [Testklasse …]
# Ohne Argument laufen alle Klassen *Test und *Report. Vorschaubilder landen in build/preview/.
#
# JUNIT_JAR     JUnit 4.13 (Ubuntu-Paket junit4: /usr/share/java/junit4.jar)
# HAMCREST_JAR  Hamcrest Core 1.3 (Ubuntu-Paket libhamcrest-java: /usr/share/java/hamcrest-core.jar)
set -euo pipefail
cd "$(dirname "$0")/.."
JUNIT=${JUNIT_JAR:-/usr/share/java/junit4.jar}
HAMCREST=${HAMCREST_JAR:-/usr/share/java/hamcrest-core.jar}
OUT=build/test-classes
rm -rf "$OUT" && mkdir -p "$OUT"

# core selbst muss Java 8 bleiben (dx in tools/build-apk.sh); das prüft der erste Aufruf
javac -encoding UTF-8 -source 8 -target 8 -Xlint:all,-options -Werror -d "$OUT" \
  $(find core/src/main/java -name '*.java')
javac -encoding UTF-8 --release 11 -Xlint:all -Werror -d "$OUT" -cp "$OUT:$JUNIT" \
  $(find core/src/test/java -name '*.java')

if [ $# -gt 0 ]; then
  CLASSES=()
  for c in "$@"; do [[ "$c" == *.* ]] && CLASSES+=("$c") || CLASSES+=("de.kronenshooter.core.$c"); done
else
  mapfile -t CLASSES < <(cd core/src/test/java && find . \( -name '*Test.java' -o -name '*Report.java' \) \
    | sed 's#^\./##; s#\.java$##; s#/#.#g' | sort)
fi
java -Djava.awt.headless=true -cp "$OUT:$JUNIT:$HAMCREST" org.junit.runner.JUnitCore "${CLASSES[@]}"
