import java.util.Properties // Import necesario para leer el archivo local.properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "mx.itson.cheemstour"

    buildFeatures {
        buildConfig = true
    }

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "mx.itson.cheemstour"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        // 1. Instanciamos un objeto Properties
        val properties = Properties()

        // 2. Apuntamos al archivo local.properties que está en la raíz del proyecto
        val localPropertiesFile = rootProject.file("local.properties")

        // 3. Verificamos si el archivo existe y lo cargamos en memoria
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // 4. Creamos una variable dinámica (Placeholder) para el AndroidManifest.xml.
        // Buscará 'MAPS_API_KEY' en el local.properties. Si no lo encuentra, asignará un string vacío.
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY") ?: ""

        buildConfigField(
            "String",
            "OPENWEATHER_API_KEY",
            "\"${properties.getProperty("OPENWEATHER_API_KEY") ?: ""}\""
        )




    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.gson.converter)
    implementation(libs.retrofit)
    implementation(libs.google.maps)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}