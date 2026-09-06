package sk.ziacik.nearly.mobile

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sk.ziacik.nearly.mobile.cue.MobileCueController
import sk.ziacik.nearly.mobile.proximity.AndroidBleAdvertiser
import sk.ziacik.nearly.mobile.proximity.BleAdvertiseSession
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
			cueController = cueController,
			advertiseSession = BleAdvertiseSession(AndroidBleAdvertiser(this)),
		)
	}
}
