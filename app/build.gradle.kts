plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.yash.productivityapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yash.productivityapp"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Firebase dependencies
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-auth:22.1.1") // Firebase Auth
    implementation("com.google.firebase:firebase-storage:20.2.1") // Firebase Storage

}

// Apply the Google Services plugin after dependencies
apply(plugin = "com.google.gms.google-services")