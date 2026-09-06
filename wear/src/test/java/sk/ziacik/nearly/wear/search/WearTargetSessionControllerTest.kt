package sk.ziacik.nearly.wear.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.wear.cue.CueController
import sk.ziacik.nearly.wear.proximity.BleAdvertiseSession
import sk.ziacik.nearly.wear.proximity.BleAdvertiser

class WearTargetSessionControllerTest {
	@Test
	fun `duplicate commands are idempotent`() = runTest {
		val cue = FakeCueController()
		val advertiser = FakeBleAdvertiser()
		val controller = WearTargetSessionController(cue, BleAdvertiseSession(advertiser))

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartProximity(7))
		controller.handle(FindCommand.StartProximity(7))
		controller.handle(FindCommand.StopProximity(7))
		controller.handle(FindCommand.StopProximity(7))

		assertEquals(listOf(CueMode.BOTH), cue.modes)
		assertEquals(listOf(7), advertiser.startedTokens)
		assertEquals(1, advertiser.stopCount)
	}

	@Test
	fun `stale commands are ignored and current cue can change`() = runTest {
		val cue = FakeCueController()
		val advertiser = FakeBleAdvertiser()
		val controller = WearTargetSessionController(cue, BleAdvertiseSession(advertiser))

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.SetCue(8, CueMode.GLOW))
		controller.handle(FindCommand.StopFind(8))
		controller.handle(FindCommand.SetCue(7, CueMode.VIBRATE))

		assertEquals(listOf(CueMode.BOTH, CueMode.VIBRATE), cue.modes)
		assertEquals(7, controller.activeToken)
	}

	@Test
	fun `new token replaces old session and stop cleans up`() = runTest {
		val cue = FakeCueController()
		val advertiser = FakeBleAdvertiser()
		val controller = WearTargetSessionController(cue, BleAdvertiseSession(advertiser))

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartProximity(7))
		controller.handle(FindCommand.StartFind(8, CueMode.GLOW))
		controller.handle(FindCommand.StopFind(8))

		assertEquals(listOf(CueMode.BOTH, CueMode.GLOW), cue.modes)
		assertEquals(2, cue.stopCount)
		assertEquals(1, advertiser.stopCount)
		assertNull(controller.activeToken)
	}

	private class FakeCueController : CueController {
		override val glowActive = MutableStateFlow(false)
		val modes = mutableListOf<CueMode>()
		var stopCount = 0

		override fun set(mode: CueMode) {
			modes += mode
		}

		override fun stop() {
			stopCount++
		}
	}

	private class FakeBleAdvertiser : BleAdvertiser {
		override val isSupported = true
		val startedTokens = mutableListOf<Int>()
		var stopCount = 0

		override suspend fun start(sessionToken: Int): Result<Unit> {
			startedTokens += sessionToken
			return Result.success(Unit)
		}

		override suspend fun stop() {
			stopCount++
		}
	}
}
