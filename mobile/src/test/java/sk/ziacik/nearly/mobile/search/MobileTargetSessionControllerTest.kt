package sk.ziacik.nearly.mobile.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sk.ziacik.nearly.mobile.cue.CueController
import sk.ziacik.nearly.mobile.cue.GlowLauncher
import sk.ziacik.nearly.mobile.data.PeerTransport
import sk.ziacik.nearly.mobile.proximity.BleScanner
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand

class MobileTargetSessionControllerTest {
	@Test
	fun `duplicate commands are idempotent`() = runTest {
		val cue = FakeCueController()
		val scanner = FakeScanner()
		val glow = FakeGlowLauncher()
		val controller = controller(cue, scanner, FakeTransport(), glow)

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartProximity(7))
		controller.handle(FindCommand.StartProximity(7))
		runCurrent()
		controller.handle(FindCommand.StopProximity(7))
		controller.handle(FindCommand.StopProximity(7))

		assertEquals(listOf(CueMode.BOTH), cue.modes)
		assertEquals(listOf(7), scanner.startedTokens)
		assertEquals(1, scanner.stopCount)
		assertEquals(1, glow.showCount)
	}

	@Test
	fun `phone scans and forwards rssi samples to the watch`() = runTest {
		val scanner = FakeScanner()
		val transport = FakeTransport()
		val controller = controller(FakeCueController(), scanner, transport, FakeGlowLauncher())

		controller.handle(FindCommand.StartFind(7, CueMode.VIBRATE))
		controller.handle(FindCommand.StartProximity(7))
		runCurrent()
		scanner.samples.emit(-73)
		runCurrent()

		assertEquals(listOf(FindCommand.ProximitySample(7, -73)), transport.commands)

		controller.handle(FindCommand.StopProximity(7))
	}

	@Test
	fun `stale commands are ignored and current cue can change`() = runTest {
		val cue = FakeCueController()
		val glow = FakeGlowLauncher()
		val controller = controller(cue, FakeScanner(), FakeTransport(), glow)

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.SetCue(8, CueMode.GLOW))
		controller.handle(FindCommand.StopFind(8))
		controller.handle(FindCommand.SetCue(7, CueMode.VIBRATE))

		assertEquals(listOf(CueMode.BOTH, CueMode.VIBRATE), cue.modes)
		assertEquals(1, glow.showCount)
		assertEquals(7, controller.activeToken)
	}

	@Test
	fun `glow and both present the phone while vibrate does not`() = runTest {
		val glow = FakeGlowLauncher()
		val controller = controller(FakeCueController(), FakeScanner(), FakeTransport(), glow)

		controller.handle(FindCommand.StartFind(7, CueMode.VIBRATE))
		controller.handle(FindCommand.SetCue(7, CueMode.GLOW))
		controller.handle(FindCommand.SetCue(7, CueMode.BOTH))
		controller.handle(FindCommand.SetCue(7, CueMode.VIBRATE))

		assertEquals(2, glow.showCount)
	}

	@Test
	fun `new token replaces old session and stop cleans up`() = runTest {
		val cue = FakeCueController()
		val scanner = FakeScanner()
		val controller = controller(cue, scanner, FakeTransport(), FakeGlowLauncher())

		controller.handle(FindCommand.StartFind(7, CueMode.BOTH))
		controller.handle(FindCommand.StartProximity(7))
		runCurrent()
		controller.handle(FindCommand.StartFind(8, CueMode.GLOW))
		controller.handle(FindCommand.StopFind(8))

		assertEquals(listOf(CueMode.BOTH, CueMode.GLOW), cue.modes)
		assertEquals(2, cue.stopCount)
		assertEquals(1, scanner.stopCount)
		assertNull(controller.activeToken)
	}

	private fun kotlinx.coroutines.test.TestScope.controller(
		cue: FakeCueController,
		scanner: FakeScanner,
		transport: FakeTransport,
		glow: FakeGlowLauncher,
	) = MobileTargetSessionController(
		scope = this,
		cueController = cue,
		scanner = scanner,
		transport = transport,
		glowLauncher = glow,
	)

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

	private class FakeScanner : BleScanner {
		override val isSupported = true
		val samples = MutableSharedFlow<Int>(extraBufferCapacity = 8)
		val startedTokens = mutableListOf<Int>()
		var stopCount = 0

		override fun scan(sessionToken: Int): Flow<Int> {
			startedTokens += sessionToken
			return samples
		}

		override suspend fun stop() {
			stopCount++
		}
	}

	private class FakeTransport : PeerTransport {
		val commands = mutableListOf<FindCommand>()

		override suspend fun send(command: FindCommand): Boolean {
			commands += command
			return true
		}
	}

	private class FakeGlowLauncher : GlowLauncher {
		var showCount = 0

		override fun show() {
			showCount++
		}
	}
}
