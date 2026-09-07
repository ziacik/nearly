package sk.ziacik.nearly.mobile.search

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

class MobileTargetSessionController(
	private val scope: CoroutineScope,
	private val cueController: CueController,
	private val scanner: BleScanner,
	private val transport: PeerTransport,
	private val glowLauncher: GlowLauncher,
) {
	private val mutex = Mutex()
	private var proximityJob: Job? = null
	var activeToken: Int? = null
		private set

	suspend fun handle(command: FindCommand) = mutex.withLock {
		when (command) {
			is FindCommand.StartFind -> {
				if (activeToken == command.sessionToken) return@withLock
				stopCurrent()
				activeToken = command.sessionToken
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

			is FindCommand.ProximitySample -> Unit

			is FindCommand.StopFind -> if (command.sessionToken == activeToken) {
				stopCurrent()
			}
		}
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
		if (proximityJob?.isActive == true || !scanner.isSupported) return
		proximityJob = scope.launch {
			scanner.scan(sessionToken)
				.catch { }
				.collect { rssi ->
					runCatching {
						transport.send(FindCommand.ProximitySample(sessionToken, rssi))
					}
				}
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
		activeToken = null
	}
}
