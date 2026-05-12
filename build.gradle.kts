// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "7.3.0.8198"
}
sonar {
    properties {
        property("sonar.projectKey", "1Myke_ParkingMMartinez")
        property("sonar.organization", "1myke")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}