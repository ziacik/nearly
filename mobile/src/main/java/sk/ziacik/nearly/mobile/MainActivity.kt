package sk.ziacik.nearly.mobile

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

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			val findViewModel: MobileFindViewModel = viewModel()
			val state by findViewModel.state.collectAsState()
			val nearly = application as NearlyMobileApplication
			val glowActive by nearly.cueController.glowActive.collectAsState()
			val permissionLauncher = rememberLauncherForActivityResult(
				ActivityResultContracts.RequestMultiplePermissions(),
			) {
				findViewModel.start()
			}

			DisposableEffect(glowActive) {
				if (glowActive) {
					window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				} else {
					window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				}
				onDispose {
					window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
				}
			}

			fun requestOrStart() {
				val missing = bluetoothPermissionState(this).missingRuntimePermissions
				if (missing.isEmpty()) findViewModel.start() else permissionLauncher.launch(missing.toTypedArray())
			}

			if (glowActive) {
				GlowScreen(
					onFound = {
						nearly.applicationScope.launch { nearly.targetController.stopLocally() }
					},
				)
			} else {
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
