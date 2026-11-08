plugins {
    id("com.android.application")
}

// Uhr-App für Wear OS (z. B. Pixel Watch 5). Läuft eigenständig ohne Handy, nutzt nur das Android-Framework.
android {
    namespace = "de.kronenshooter.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.kronenshooter"
        minSdk = 30 // Wear OS 3
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    // Schlüssel liegt im Repository (nur private Nutzung, siehe docs/entwicklung.md)
    signingConfigs {
        create("shared") {
            storeFile = file("../keystore/kronenshooter.jks")
            storePassword = "kronenshooter"
            keyAlias = "kronenshooter"
            keyPassword = "kronenshooter"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Einzige Abhängigkeit: die eigene Spiellogik. Keine AndroidX-Bibliotheken.
    implementation(project(":core"))
}
