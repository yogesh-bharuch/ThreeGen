plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // id("com.google.devtools.ksp") // Add this line
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)


    // Kotlin serialization plugin for type safe routes and navigation arguments
    //alias(libs.plugins.kotlin.serialization)
    kotlin("plugin.serialization") version "2.0.21"
    // for firebase
    //id("com.google.gms.google-services")

}

android {
    namespace = "com.example.threegen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.threegen"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // live data
    implementation(libs.androidx.runtime.livedata)

    //room
    implementation(libs.androidx.room.runtime)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.room.compiler)


    // See Add the KSP plugin to your project
    ksp(libs.androidx.room.room.compiler)


    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    // coil
    implementation(libs.coil.compose.v260)

    // Material Icons Extended library
    implementation(libs.androidx.material.icons.extended)

    //Material3 Icons
    implementation(libs.material3)


    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    //implementation(libs.firebase.database)

    // For example, declare the dependencies for Firebase Authentication and Cloud Firestore
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.firestore)

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)


}