package sk.ziacik.nearly.wear.search

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.wear.cue.CueController
import sk.ziacik.nearly.wear.proximity.BleAdvertiseSession

class WearTargetSessionController(
	private val cueController: CueController,
	private val advertiseSession: BleAdvertiseSession,
) {
	private val mutex = Mutex()
	var activeToken: Int? = null
		private set

	suspend fun handle(command: FindCommand) = mutex.withLock {
		when (command) {
			is FindCommand.StartFind -> {
				if (activeToken == command.sessionToken) return@withLock
				stopCurrent()
				activeToken = command.sessionToken
				cueController.set(command.cueMode)
			}

			is FindCommand.SetCue -> if (command.sessionToken == activeToken) {
				cueController.set(command.cueMode)
			}

			is FindCommand.StartProximity -> if (command.sessionToken == activeToken) {
				advertiseSession.start(command.sessionToken)
			}

			is FindCommand.StopProximity -> if (command.sessionToken == activeToken) {
				advertiseSession.stop(command.sessionToken)
			}

			is FindCommand.ProximitySample,
			is FindCommand.ProximityUnavailable -> Unit

			is FindCommand.StopFind -> if (command.sessionToken == activeToken) {
				stopCurrent()
			}
		}
	}

	suspend fun stopLocally() = mutex.withLock {
		stopCurrent()
	}

	private suspend fun stopCurrent() {
		val token = activeToken ?: return
		advertiseSession.stop(token)
		cueController.stop()
		activeToken = null
	}
}
