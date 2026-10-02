plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val appVersionName = providers.gradleProperty("releaseVersionName").orElse("2.1.1").get()
val appVersionCode =
    providers
        .gradleProperty("releaseVersionCode")
        .orElse("20101")
        .get()
        .toInt()

// Release signing comes from Gradle properties or environment variables (see README). Without them, release
// builds fall back to the debug key so local and CI builds keep working.
fun signingValue(name: String): String? =
    providers
        .gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orNull
        ?.takeIf { it.isNotBlank() }

val releaseKeystorePath = signingValue("RELEASE_KEYSTORE_PATH")

// Point a build at another server, e.g. `-PbaseUrl=http://10.0.2.2:3077` for a local Rails app on the emulator host.
val baseUrl = providers.gradleProperty("baseUrl").orElse("https://clusterheadachetracker.com").get()

android {
    namespace = "me.paolino.clusterheadachetracker"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "me.paolino.clusterheadachetracker"
        minSdk = 28
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters += listOf("en", "de", "it", "es")
        generateLocaleConfig = true
    }

    // The widget renders in the language picked in the web app, so every language must ship in every install.
    bundle {
        language {
            enableSplit = false
        }
    }

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = signingValue("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = signingValue("RELEASE_KEY_ALIAS")
                keyPassword = signingValue("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkDependencies = false
        disable += "GradleDependency"
        disable += "NewerVersionAvailable"
        disable += "AndroidGradlePluginVersion"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hotwire.core)
    implementation(libs.hotwire.navigation.fragments)
    implementation(libs.bridge.components)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
}
