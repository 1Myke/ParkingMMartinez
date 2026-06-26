plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    //id("org.sonarqube")
    jacoco
}

val appVersionCode = 3
val appVersionName = "v1.1.0"
val targetSdkVersion = 36

// Versiones de paquetes
val lifecycleVersion = "2.6.2"
val navigationVersion = "2.8.0"
val gsonVersion = "2.11.0"
val mockitoVersion = "5.11.0"
val archVersion = "2.2.0"
val coroutinesVersion = "1.7.3"
val appcompatVersion = "1.7.0"
val coilVersion = "2.6.0"


android {
    namespace = "com.lksnext.ParkingMMartinez"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.lksnext.ParkingMMartinez"
        minSdk = 26
        targetSdk = targetSdkVersion
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

sonar {
    properties {
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/coverage/test/debug/report.xml")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.navigation:navigation-compose:$navigationVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
    implementation("com.google.firebase:firebase-messaging")
    //implementation("io.coil-kt.coil:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("androidx.arch.core:core-testing:$archVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}