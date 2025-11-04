plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase / Google services
}

android {
    namespace = "com.example.fusion0_lottery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fusion0_lottery"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        // Java project (no Kotlin required)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Android UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.android.gms:play-services-base:18.5.0")

    // Firebase (use BOM to pin versions)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.storage)               // you already had this
    implementation("com.google.firebase:firebase-messaging") // <-- for push notifications

    // For showing an in-app pop-up from the FCM service
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // (Your QR deps)
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
