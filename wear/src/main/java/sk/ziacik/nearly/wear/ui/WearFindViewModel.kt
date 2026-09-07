package sk.ziacik.nearly.wear.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.wear.NearlyWearApplication
import sk.ziacik.nearly.wear.cue.AndroidGuidanceHaptics
import sk.ziacik.nearly.wear.data.WearPeerTransport
import sk.ziacik.nearly.wear.permissions.bluetoothPermissionState
import sk.ziacik.nearly.wear.permissions.isBluetoothEnabled
import sk.ziacik.nearly.wear.search.WearFindCoordinator

class WearFindViewModel(application: Application) : AndroidViewModel(application) {
	private val nearly = application as NearlyWearApplication
	private val coordinator = WearFindCoordinator(
		scope = viewModelScope,
		transport = WearPeerTransport(application),
		advertiseSession = nearly.advertiseSession,
		proximityEvents = nearly.proximityInbox.commands,
		guidanceHaptics = AndroidGuidanceHaptics(application),
		permissionState = { bluetoothPermissionState(application) },
		bluetoothEnabled = { isBluetoothEnabled(application) },
	)

	val state = coordinator.state

	fun start() {
		viewModelScope.launch { coordinator.start() }
	}

	fun stop() {
		viewModelScope.launch { coordinator.stop() }
	}

	fun setCue(mode: CueMode) {
		viewModelScope.launch { coordinator.setCue(mode) }
	}

	override fun onCleared() {
		viewModelScope.launch { coordinator.stop() }
		super.onCleared()
	}
}
