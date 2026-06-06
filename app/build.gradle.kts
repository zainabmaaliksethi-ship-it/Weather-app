plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.splashscreen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.splashscreen"
        minSdk = 26
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Auth
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth")

    // Google Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp (for logging, optional but helpful)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
