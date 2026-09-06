package sk.ziacik.nearly.mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sk.ziacik.nearly.mobile.cue.AndroidGuidanceHaptics
import sk.ziacik.nearly.mobile.data.WearPeerTransport
import sk.ziacik.nearly.mobile.permissions.bluetoothPermissionState
import sk.ziacik.nearly.mobile.permissions.isBluetoothEnabled
import sk.ziacik.nearly.mobile.proximity.AndroidBleScanner
import sk.ziacik.nearly.mobile.search.MobileFindCoordinator
import sk.ziacik.nearly.shared.CueMode

class MobileFindViewModel(application: Application) : AndroidViewModel(application) {
	private val coordinator = MobileFindCoordinator(
		scope = viewModelScope,
		transport = WearPeerTransport(application),
		scanner = AndroidBleScanner(application),
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
