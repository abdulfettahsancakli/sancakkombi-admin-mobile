import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

val localProperties = Properties().apply {
  val propertiesFile = rootProject.file("local.properties")
  if (propertiesFile.exists()) propertiesFile.inputStream().use(::load)
}
val mapsApiKey = providers.gradleProperty("MAPS_API_KEY").orNull
  ?: System.getenv("MAPS_API_KEY")
  ?: localProperties.getProperty("MAPS_API_KEY").orEmpty()
val escapedMapsApiKey = mapsApiKey.replace("\\", "\\\\").replace("\"", "\\\"")
val appVersionCode = 20
val appVersionName = "1.19"

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.sancakkombi.admin"
    minSdk = 24
    targetSdk = 36
    versionCode = appVersionCode
    versionName = appVersionName

    manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    buildConfigField("String", "MAPS_API_KEY", "\"$escapedMapsApiKey\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
      buildConfigField("String", "API_BASE_URL", "\"https://www.sancakkombi.com/\"")
    }
    debug {
      buildConfigField("String", "API_BASE_URL", "\"https://www.sancakkombi.com/\"")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

val copyVersionedDebugApk by tasks.registering(Copy::class) {
  from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
  into(rootProject.layout.projectDirectory.dir("apk-releases"))
  rename("app-debug.apk", "SancakKombi-v$appVersionName-build$appVersionCode-debug.apk")
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
  finalizedBy(copyVersionedDebugApk)
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.google.code.scanner)
  implementation(libs.maps.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Conscrypt resolves its Windows native library name using the default locale.
// Turkish lower-casing turns "windows" into "wındows" during Robolectric tests.
tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
  systemProperty("user.language", "en")
  systemProperty("user.country", "US")
}
