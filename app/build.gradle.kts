import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    id("com.google.devtools.ksp")

}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { input ->
            load(input)
        }
    }
}

android {
    namespace = "com.example.waywayapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.waywayapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "ADMIN_EMAILS",
            "\"${localProperties.getProperty("ADMIN_EMAILS", "")}\""
        )
        buildConfigField(
            "String",
            "FIRESTORE_DATABASE_ID",
            "\"${localProperties.getProperty("FIRESTORE_DATABASE_ID", "waywayapp")}\""
        )
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")
        manifestPlaceholders["FACEBOOK_APP_ID"] =
            localProperties.getProperty("FACEBOOK_APP_ID", "")
        manifestPlaceholders["FACEBOOK_CLIENT_TOKEN"] =
            localProperties.getProperty("FACEBOOK_CLIENT_TOKEN", "")
        resValue(
            "string",
            "google_web_client_id",
            localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")
        )
        resValue(
            "string",
            "facebook_app_id",
            localProperties.getProperty("FACEBOOK_APP_ID", "")
        )
        resValue(
            "string",
            "facebook_client_token",
            localProperties.getProperty("FACEBOOK_CLIENT_TOKEN", "")
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
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.coroutines)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.facebook.login)

    // Maps & Location
    implementation("com.google.maps.android:maps-compose:6.1.2")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.play.services.location)
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    // Retrofit (for OSRM)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    val roomVersion = "2.7.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
