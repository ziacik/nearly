plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.compose.compiler)
}

android {
	namespace = "sk.ziacik.nearly.mobile"
	compileSdk = 36

	defaultConfig {
		applicationId = "sk.ziacik.nearly.mobile"
		minSdk = 26
		targetSdk = 36
		versionCode = 1
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
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui)
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
