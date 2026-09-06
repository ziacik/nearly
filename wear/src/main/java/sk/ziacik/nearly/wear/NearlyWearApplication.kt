package sk.ziacik.nearly.wear

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import sk.ziacik.nearly.wear.cue.WearCueController
import sk.ziacik.nearly.wear.data.FindCommandInbox
import sk.ziacik.nearly.wear.proximity.AndroidBleAdvertiser
import sk.ziacik.nearly.wear.proximity.BleAdvertiseSession
import sk.ziacik.nearly.wear.search.WearTargetSessionController

class NearlyWearApplication : Application() {
	val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	val proximityInbox = FindCommandInbox()
	lateinit var cueController: WearCueController
		private set
	lateinit var advertiseSession: BleAdvertiseSession
		private set
	lateinit var targetController: WearTargetSessionController
		private set

	override fun onCreate() {
		super.onCreate()
		cueController = WearCueController(this)
		advertiseSession = BleAdvertiseSession(AndroidBleAdvertiser(this))
		targetController = WearTargetSessionController(
			cueController = cueController,
			advertiseSession = advertiseSession,
		)
	}
}
