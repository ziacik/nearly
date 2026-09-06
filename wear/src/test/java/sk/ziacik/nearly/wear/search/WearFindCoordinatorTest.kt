package sk.ziacik.nearly.wear.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.shared.FindError
import sk.ziacik.nearly.shared.ProximityLevel
import sk.ziacik.nearly.shared.SEARCH_TIMEOUT_MS
import sk.ziacik.nearly.wear.cue.GuidanceHaptics
import sk.ziacik.nearly.wear.data.PeerTransport
import sk.ziacik.nearly.wear.permissions.BluetoothPermissionState
import sk.ziacik.nearly.wear.proximity.BleScanner

class WearFindCoordinatorTest {
	@Test
	fun `start scan and stop follow expected command order`() = runTest {
		val transport = FakeTransport()
		val scanner = FakeScanner()
		val haptics = FakeHaptics()
		val coordinator = coordinator(transport, scanner, haptics)

		coordinator.start()
		runCurrent()
		assertEquals(
			listOf(FindCommand.StartFind(7, CueMode.BOTH), FindCommand.StartProximity(7)),
			transport.commands,
		)

		scanner.samples.emit(-80)
		runCurrent()
		assertEquals(ProximityLevel.COLD, coordinator.state.value.proximityLevel)
		assertTrue(haptics.tickCount > 0)

		coordinator.stop()
		assertEquals(FindCommand.StopProximity(7), transport.commands[2])
		assertEquals(FindCommand.StopFind(7), transport.commands[3])
		assertFalse(coordinator.state.value.searching)
	}

	@Test
	fun `missing peer prevents search`() = runTest {
		val transport = FakeTransport(sendResult = false)
		val coordinator = coordinator(transport, FakeScanner(), FakeHaptics())
		coordinator.start()
		assertEquals(FindError.PEER_NOT_CONNECTED, coordinator.state.value.error)
		assertFalse(coordinator.state.value.searching)
	}

	@Test
	fun `unsupported proximity keeps silent find alive`() = runTest {
		val transport = FakeTransport()
		val scanner = FakeScanner(isSupported = false)
		val coordinator = coordinator(transport, scanner, FakeHaptics())
		coordinator.start()
		assertTrue(coordinator.state.value.searching)
		assertFalse(coordinator.state.value.proximityAvailable)
		assertEquals(FindError.CAPABILITY_UNAVAILABLE, coordinator.state.value.error)
		assertEquals(listOf(FindCommand.StartFind(7, CueMode.BOTH)), transport.commands)
	}

	@Test
	fun `search times out at exactly two minutes and cleans up`() = runTest {
		val transport = FakeTransport()
		val coordinator = coordinator(transport, FakeScanner(), FakeHaptics())
		coordinator.start()
		advanceTimeBy(SEARCH_TIMEOUT_MS)
		runCurrent()
		assertFalse(coordinator.state.value.searching)
		assertEquals(FindError.TIMED_OUT, coordinator.state.value.error)
		assertTrue(transport.commands.contains(FindCommand.StopFind(7)))
	}

	private fun kotlinx.coroutines.test.TestScope.coordinator(
		transport: FakeTransport,
		scanner: FakeScanner,
		haptics: FakeHaptics,
	) = WearFindCoordinator(
		scope = this,
		transport = transport,
		scanner = scanner,
		guidanceHaptics = haptics,
		permissionState = { BluetoothPermissionState(true, true, true, emptyList()) },
		bluetoothEnabled = { true },
		tokenProvider = { 7 },
	)

	private class FakeTransport(private val sendResult: Boolean = true) : PeerTransport {
		val commands = mutableListOf<FindCommand>()
		override suspend fun send(command: FindCommand): Boolean {
			commands += command
			return sendResult
		}
	}

	private class FakeScanner(override val isSupported: Boolean = true) : BleScanner {
		val samples = MutableSharedFlow<Int>(extraBufferCapacity = 8)
		override fun scan(sessionToken: Int): Flow<Int> = samples
		override suspend fun stop() = Unit
	}

	private class FakeHaptics : GuidanceHaptics {
		var tickCount = 0
		override fun tick() { tickCount++ }
		override fun stop() = Unit
	}
}
