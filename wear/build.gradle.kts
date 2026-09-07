import java.util.Base64

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.compiler)
}

val nearlyDebugKeystore = rootProject.file(".gradle/nearly-debug.keystore")
if (!nearlyDebugKeystore.exists()) {
	nearlyDebugKeystore.parentFile.mkdirs()
	nearlyDebugKeystore.writeBytes(
		Base64.getDecoder().decode(rootProject.file("config/nearly-debug.keystore.b64").readText().trim()),
	)
}

android {
	namespace = "sk.ziacik.nearly.wear"
	compileSdk = 36

	signingConfigs {
		getByName("debug") {
			storeFile = nearlyDebugKeystore
			storePassword = "android"
			keyAlias = "androiddebugkey"
			keyPassword = "android"
			storeType = "PKCS12"
		}
	}

	defaultConfig {
		applicationId = "sk.ziacik.nearly"
		minSdk = 30
		targetSdk = 36
		versionCode = 20_001
		versionName = "0.1.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures {
		compose = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

dependencies {
	implementation(project(":shared"))
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.wear.compose.foundation)
	implementation(libs.androidx.wear.compose.material3)
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.play.services.wearable)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.play.services)

	testImplementation(libs.junit)
	testImplementation(libs.kotlinx.coroutines.test)

	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	androidTestImplementation(libs.androidx.test.runner)

	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}
