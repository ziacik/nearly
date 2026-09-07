package sk.ziacik.nearly.mobile.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.ziacik.nearly.mobile.cue.CueController
import sk.ziacik.nearly.mobile.cue.GlowLauncher
import sk.ziacik.nearly.mobile.data.PeerTransport
import sk.ziacik.nearly.mobile.proximity.BleScanner
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.shared.FindError

class MobileTargetSessionController(
	private val scope: CoroutineScope,
	private val cueController: CueController,
	private val scanner: BleScanner,
	private val transport: PeerTransport,
	private val glowLauncher: GlowLauncher,
	private val canScan: () -> Boolean = { true },
) {
	private val mutex = Mutex()
	private var proximityJob: Job? = null
	private val mutableProximityError = MutableStateFlow<FindError?>(null)
	var activeToken: Int? = null
		private set

	val proximityError: StateFlow<FindError?> = mutableProximityError.asStateFlow()

	suspend fun handle(command: FindCommand) = mutex.withLock {
		when (command) {
			is FindCommand.StartFind -> {
				if (activeToken == command.sessionToken) return@withLock
				stopCurrent()
				activeToken = command.sessionToken
				mutableProximityError.value = null
				setCue(command.cueMode)
			}

			is FindCommand.SetCue -> if (command.sessionToken == activeToken) {
				setCue(command.cueMode)
			}

			is FindCommand.StartProximity -> if (command.sessionToken == activeToken) {
				startProximity(command.sessionToken)
			}

			is FindCommand.StopProximity -> if (command.sessionToken == activeToken) {
				stopProximity()
			}

			is FindCommand.ProximitySample,
			is FindCommand.ProximityUnavailable -> Unit

			is FindCommand.StopFind -> if (command.sessionToken == activeToken) {
				stopCurrent()
			}
		}
	}

	suspend fun retryProximity() = mutex.withLock {
		val token = activeToken ?: return@withLock
		stopProximity()
		mutableProximityError.value = null
		startProximity(token)
	}

	suspend fun stopLocally() = mutex.withLock {
		stopCurrent()
	}

	private fun setCue(mode: CueMode) {
		cueController.set(mode)
		if (mode == CueMode.GLOW || mode == CueMode.BOTH) {
			glowLauncher.show()
		}
	}

	private fun startProximity(sessionToken: Int) {
		if (proximityJob?.isActive == true) return
		if (!canScan()) {
			proximityJob = scope.launch {
				reportProximityUnavailable(
					sessionToken = sessionToken,
					localError = FindError.PERMISSION_MISSING,
					peerError = FindError.PEER_PERMISSION_MISSING,
				)
			}
			return
		}
		if (!scanner.isSupported) {
			proximityJob = scope.launch {
				reportProximityUnavailable(
					sessionToken = sessionToken,
					localError = FindError.CAPABILITY_UNAVAILABLE,
					peerError = FindError.CAPABILITY_UNAVAILABLE,
				)
			}
			return
		}
		proximityJob = scope.launch {
			scanner.scan(sessionToken)
				.catch { error ->
					val permissionMissing = error is SecurityException
					reportProximityUnavailable(
						sessionToken = sessionToken,
						localError = if (permissionMissing) FindError.PERMISSION_MISSING else FindError.CAPABILITY_UNAVAILABLE,
						peerError = if (permissionMissing) FindError.PEER_PERMISSION_MISSING else FindError.CAPABILITY_UNAVAILABLE,
					)
				}
				.collect { rssi ->
					if (activeToken != sessionToken) return@collect
					mutableProximityError.value = null
					runCatching {
						transport.send(FindCommand.ProximitySample(sessionToken, rssi))
					}
				}
		}
	}

	private suspend fun reportProximityUnavailable(
		sessionToken: Int,
		localError: FindError,
		peerError: FindError,
	) {
		if (activeToken != sessionToken) return
		mutableProximityError.value = localError
		runCatching {
			transport.send(FindCommand.ProximityUnavailable(sessionToken, peerError))
		}
	}

	private suspend fun stopProximity() {
		val job = proximityJob ?: return
		proximityJob = null
		job.cancel()
		scanner.stop()
	}

	private suspend fun stopCurrent() {
		if (activeToken == null) return
		stopProximity()
		cueController.stop()
		mutableProximityError.value = null
		activeToken = null
	}
}
