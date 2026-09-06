package sk.ziacik.nearly.mobile.search

import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.ziacik.nearly.mobile.cue.GuidanceHaptics
import sk.ziacik.nearly.mobile.data.PeerTransport
import sk.ziacik.nearly.mobile.permissions.BluetoothPermissionState
import sk.ziacik.nearly.mobile.proximity.BleScanner
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.shared.FindError
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.RssiSmoother
import sk.ziacik.nearly.shared.SEARCH_TIMEOUT_MS
import sk.ziacik.nearly.shared.hapticIntervalMs
import sk.ziacik.nearly.shared.proximityLevel

class MobileFindCoordinator(
	private val scope: CoroutineScope,
	private val transport: PeerTransport,
	private val scanner: BleScanner,
	private val guidanceHaptics: GuidanceHaptics,
	private val permissionState: () -> BluetoothPermissionState,
	private val bluetoothEnabled: () -> Boolean,
	private val tokenProvider: () -> Int = { SecureRandom().nextInt() },
) {
	private val mutex = Mutex()
	private val mutableState = MutableStateFlow(FindUiState())
	private var scannerJob: Job? = null
	private var timeoutJob: Job? = null
	private var guidanceJob: Job? = null
	private var guidanceLevel: sk.ziacik.nearly.shared.ProximityLevel? = null
	@Volatile private var activeToken: Int? = null

	val state: StateFlow<FindUiState> = mutableState.asStateFlow()

	suspend fun start() {
		val token = mutex.withLock {
			if (activeToken != null) return@withLock null
			val nextToken = tokenProvider()
			val sent = runCatching {
				transport.send(FindCommand.StartFind(nextToken, CueMode.BOTH))
			}.getOrDefault(false)
			if (!sent) {
				mutableState.value = FindUiState(error = FindError.PEER_NOT_CONNECTED)
				return@withLock null
			}
			activeToken = nextToken
			mutableState.value = FindUiState(searching = true)
			nextToken
		} ?: return

		startProximity(token)
		timeoutJob = scope.launch {
			delay(SEARCH_TIMEOUT_MS)
			stopInternal(timedOut = true, cancelTimeout = false)
		}
	}

	suspend fun setCue(mode: CueMode) {
		val token = activeToken ?: return
		mutableState.update { it.copy(cueMode = mode) }
		runCatching { transport.send(FindCommand.SetCue(token, mode)) }
	}

	suspend fun stop() {
		stopInternal(timedOut = false, cancelTimeout = true)
	}

	private suspend fun startProximity(token: Int) {
		val permissions = permissionState()
		if (!permissions.canScan) {
			markProximityUnavailable(token, FindError.PERMISSION_MISSING)
			return
		}
		if (!bluetoothEnabled()) {
			markProximityUnavailable(token, FindError.BLUETOOTH_OFF)
			return
		}
		if (!scanner.isSupported) {
			markProximityUnavailable(token, FindError.CAPABILITY_UNAVAILABLE)
			return
		}

		val proximitySent = runCatching {
			transport.send(FindCommand.StartProximity(token))
		}.getOrDefault(false)
		if (!proximitySent) {
			markProximityUnavailable(token, FindError.PEER_NOT_CONNECTED)
			return
		}

		val smoother = RssiSmoother()
		scannerJob = scope.launch {
			scanner.scan(token)
				.catch {
					markProximityUnavailable(token, FindError.CAPABILITY_UNAVAILABLE)
				}
				.collect { sample ->
					if (activeToken != token) return@collect
					val smoothed = smoother.add(sample)
					val level = proximityLevel(smoothed)
					mutableState.update {
						it.copy(
							proximityLevel = level,
							smoothedRssi = smoothed,
							proximityAvailable = true,
							error = null,
						)
					}
					restartGuidance(level, token)
				}
		}
	}

	private fun restartGuidance(level: sk.ziacik.nearly.shared.ProximityLevel, token: Int) {
		if (guidanceLevel == level) return
		guidanceLevel = level
		guidanceJob?.cancel()
		guidanceJob = scope.launch {
			while (isActive && activeToken == token) {
				guidanceHaptics.tick()
				delay(hapticIntervalMs(level))
			}
		}
	}

	private fun markProximityUnavailable(token: Int, error: FindError) {
		if (activeToken != token) return
		guidanceJob?.cancel()
		guidanceLevel = null
		guidanceHaptics.stop()
		mutableState.update {
			it.copy(
				proximityLevel = null,
				smoothedRssi = null,
				proximityAvailable = false,
				error = error,
			)
		}
	}

	private suspend fun stopInternal(timedOut: Boolean, cancelTimeout: Boolean) {
		val token = mutex.withLock {
			val current = activeToken ?: return@withLock null
			activeToken = null
			mutableState.value = FindUiState(error = if (timedOut) FindError.TIMED_OUT else null)
			current
		} ?: return

		if (cancelTimeout) timeoutJob?.cancel()
		timeoutJob = null
		scannerJob?.cancel()
		scannerJob = null
		guidanceJob?.cancel()
		guidanceJob = null
		guidanceLevel = null
		runCatching { scanner.stop() }
		guidanceHaptics.stop()
		runCatching { transport.send(FindCommand.StopProximity(token)) }
		runCatching { transport.send(FindCommand.StopFind(token)) }
	}
}
