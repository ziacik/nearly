package sk.ziacik.nearly.mobile

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import sk.ziacik.nearly.mobile.permissions.bluetoothPermissionState
import sk.ziacik.nearly.mobile.ui.GlowScreen
import sk.ziacik.nearly.mobile.ui.MobileApp
import sk.ziacik.nearly.mobile.ui.MobileFindViewModel
import sk.ziacik.nearly.mobile.ui.TargetPermissionScreen
import sk.ziacik.nearly.shared.FindError

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val nearly = application as NearlyMobileApplication
		val defaultBrightness = window.attributes.screenBrightness
		applyGlowWindow(nearly.cueController.glowActive.value, defaultBrightness)

		setContent {
			val findViewModel: MobileFindViewModel = viewModel()
			val state by findViewModel.state.collectAsState()
			val glowActive by nearly.cueController.glowActive.collectAsState()
			val targetError by nearly.targetController.proximityError.collectAsState()
			val permissionLauncher = rememberLauncherForActivityResult(
				ActivityResultContracts.RequestMultiplePermissions(),
			) {
				findViewModel.start()
			}
			val targetPermissionLauncher = rememberLauncherForActivityResult(
				ActivityResultContracts.RequestMultiplePermissions(),
			) {
				nearly.applicationScope.launch { nearly.targetController.retryProximity() }
			}

			DisposableEffect(glowActive) {
				applyGlowWindow(glowActive, defaultBrightness)
				onDispose {
					if (glowActive) applyGlowWindow(false, defaultBrightness)
				}
			}

			fun requestOrStart() {
				val missing = bluetoothPermissionState(this).missingRuntimePermissions
				if (missing.isEmpty()) findViewModel.start() else permissionLauncher.launch(missing.toTypedArray())
			}

			fun requestTargetPermission() {
				val missing = bluetoothPermissionState(this).missingRuntimePermissions
				if (missing.isEmpty()) {
					nearly.applicationScope.launch { nearly.targetController.retryProximity() }
				} else {
					targetPermissionLauncher.launch(missing.toTypedArray())
				}
			}

			fun stopTarget() {
				nearly.applicationScope.launch { nearly.targetController.stopLocally() }
			}

			when {
				glowActive && targetError == FindError.PERMISSION_MISSING -> {
					TargetPermissionScreen(
						onGrantPermission = ::requestTargetPermission,
						onFound = ::stopTarget,
					)
				}

				glowActive -> {
					GlowScreen(onFound = ::stopTarget)
				}

				else -> {
					MobileApp(
						state = state,
						onFind = ::requestOrStart,
						onStop = findViewModel::stop,
						onCue = findViewModel::setCue,
						onGrantPermission = ::requestOrStart,
					)
				}
			}
		}
	}

	private fun applyGlowWindow(active: Boolean, defaultBrightness: Float) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setShowWhenLocked(active)
			setTurnScreenOn(active)
		} else {
			val legacyFlags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
			if (active) window.addFlags(legacyFlags) else window.clearFlags(legacyFlags)
		}

		if (active) {
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		} else {
			window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		}

		val attributes = window.attributes
		attributes.screenBrightness = if (active) 1f else defaultBrightness
		window.attributes = attributes
	}
}
