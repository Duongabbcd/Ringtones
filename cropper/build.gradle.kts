plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.licensee)
    alias(libs.plugins.paparazzi)
}

licensee {
    allow("Apache-2.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

android {
    namespace = "com.canhub.cropper"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Main dependencies
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.exifinterface)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.livedata.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Workaround: https://github.com/cashapp/paparazzi/issues/1231
plugins.withId("app.cash.paparazzi") {
    afterEvaluate {
        dependencies.constraints {
            add("testImplementation", "com.google.guava:guava") {
                attributes {
                    attribute(
                        TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                        objects.named(
                            TargetJvmEnvironment::class.java,
                            TargetJvmEnvironment.STANDARD_JVM
                        ),
                    )
                }
                because(
                    "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                            " See https://github.com/cashapp/paparazzi/issues/906."
                )
            }
        }
    }
}
