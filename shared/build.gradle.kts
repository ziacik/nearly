plugins {
	alias(libs.plugins.android.library)
}

android {
	namespace = "sk.ziacik.nearly.shared"
	compileSdk = 36

	defaultConfig {
		minSdk = 26
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

dependencies {
	testImplementation(libs.junit)
}
