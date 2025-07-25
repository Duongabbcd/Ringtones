plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

android {
    namespace = "com.ezt.ringify.ringtonewallpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ezt.ringify.ringtonewallpaper"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
        viewBinding { enable = true }
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    base{
        archivesName.set("Ringtones_${defaultConfig.versionName}")
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.livedata.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":carouselview"))
    implementation(project(":cropper"))

    implementation ("com.auth0:java-jwt:4.4.0")

    implementation("com.google.dagger:hilt-android:2.55")
    kapt("com.google.dagger:hilt-compiler:2.55")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // For Gson converter
    implementation("com.squareup.okhttp3:okhttp:4.9.0") // OkHttp for networking

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation ("com.airbnb.android:lottie:3.4.0")

    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-ui:1.7.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.7.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    implementation("com.google.android.material:material:1.10.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation("me.tankery.lib:circularSeekBar:1.4.2")
    implementation("io.coil-kt:coil:2.4.0")

    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    kapt("androidx.room:room-compiler:2.7.1")

    //advertisement
    implementation("com.github.thienlp201097:DktechLib:2.1.5")
    implementation("com.facebook.android:facebook-android-sdk:18.0.2")
    implementation("com.google.android.gms:play-services-ads:24.1.0")
    implementation("com.github.thienlp201097:smart-app-rate:1.0.7")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.ads.mediation:pangle:7.2.0.6.0")
    implementation("com.google.ads.mediation:applovin:13.0.1.0")
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:vungle:7.4.2.0")
    implementation("com.google.ads.mediation:mintegral:16.9.71.0")
}