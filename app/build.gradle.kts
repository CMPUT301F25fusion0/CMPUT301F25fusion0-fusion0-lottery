plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")   // <-- added

    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("me.dm7.barcodescanner:zxing:1.9")
}
tasks.withType<Test>{
    useJUnitPlatform()
}