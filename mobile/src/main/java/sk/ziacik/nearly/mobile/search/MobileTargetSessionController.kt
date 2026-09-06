package sk.ziacik.nearly.mobile.search

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sk.ziacik.nearly.mobile.cue.CueController
import sk.ziacik.nearly.mobile.proximity.BleAdvertiseSession
import sk.ziacik.nearly.shared.FindCommand

class MobileTargetSessionController(
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

			is FindCommand.StopFind -> if (command.sessionToken == activeToken) {
				stopCurrent()
			}
		}
	}

	private suspend fun stopCurrent() {
		val token = activeToken ?: return
		advertiseSession.stop(token)
		cueController.stop()
		activeToken = null
	}
}
