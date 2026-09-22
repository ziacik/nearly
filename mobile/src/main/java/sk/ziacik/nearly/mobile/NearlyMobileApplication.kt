package sk.ziacik.nearly.mobile

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sk.ziacik.nearly.mobile.cue.AndroidGlowLauncher
import sk.ziacik.nearly.mobile.cue.MobileCueController
import sk.ziacik.nearly.mobile.data.WearPeerTransport
import sk.ziacik.nearly.mobile.permissions.bluetoothPermissionState
import sk.ziacik.nearly.mobile.proximity.AndroidBleScanner
import sk.ziacik.nearly.mobile.search.MobileTargetSessionController

class NearlyMobileApplication : Application() {
	val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	lateinit var cueController: MobileCueController
		private set
	lateinit var targetController: MobileTargetSessionController
		private set

	override fun onCreate() {
		super.onCreate()
		cueController = MobileCueController(this)
		targetController = MobileTargetSessionController(
			scope = applicationScope,
			cueController = cueController,
			scanner = AndroidBleScanner(this),
			transport = WearPeerTransport(this),
			glowLauncher = AndroidGlowLauncher(this),
			canScan = { bluetoothPermissionState(this).canScan },
		)
	}
}
