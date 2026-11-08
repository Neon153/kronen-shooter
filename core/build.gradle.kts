// Spiellogik ohne Android: Regeln, Gegner, Kollisionen, Schwierigkeit, Zeichenreihenfolge.
// Wird von der Uhr-App (:wear) genutzt; die Tests laufen auf dem Rechner ohne Gerät.
plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    // Vorschaubilder (PreviewReport) entstehen ohne Bildschirm
    systemProperty("java.awt.headless", "true")
}
