package sk.ziacik.nearly.wear.search

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
import sk.ziacik.nearly.wear.proximity.BleAdvertiseSession
import sk.ziacik.nearly.wear.proximity.BleAdvertiser

class WearFindCoordinatorTest {
	@Test
	fun `watch advertises and uses phone rssi samples for guidance`() = runTest {
		val transport = FakeTransport()
		val advertiser = FakeAdvertiser()
		val events = MutableSharedFlow<FindCommand>(extraBufferCapacity = 8)
		val haptics = FakeHaptics()
		val coordinator = coordinator(transport, advertiser, events, haptics)

		coordinator.start()
		runCurrent()
		assertEquals(listOf(7), advertiser.startedTokens)
		assertEquals(
			listOf(FindCommand.StartFind(7, CueMode.BOTH), FindCommand.StartProximity(7)),
			transport.commands,
		)

		events.emit(FindCommand.ProximitySample(7, -80))
		runCurrent()
		assertEquals(ProximityLevel.COLD, coordinator.state.value.proximityLevel)
		assertTrue(haptics.tickCount > 0)

		coordinator.stop()
		assertEquals(1, advertiser.stopCount)
		assertEquals(FindCommand.StopProximity(7), transport.commands[2])
		assertEquals(FindCommand.StopFind(7), transport.commands[3])
		assertFalse(coordinator.state.value.searching)
	}

	@Test
	fun `phone permission failure replaces endless searching with actionable error`() = runTest {
		val events = MutableSharedFlow<FindCommand>(extraBufferCapacity = 8)
		val coordinator = coordinator(FakeTransport(), FakeAdvertiser(), events, FakeHaptics())
		coordinator.start()
		runCurrent()

		events.emit(FindCommand.ProximityUnavailable(7, FindError.PEER_PERMISSION_MISSING))
		runCurrent()

		assertTrue(coordinator.state.value.searching)
		assertFalse(coordinator.state.value.proximityAvailable)
		assertEquals(FindError.PEER_PERMISSION_MISSING, coordinator.state.value.error)
	}

	@Test
	fun `samples for another session are ignored`() = runTest {
		val events = MutableSharedFlow<FindCommand>(extraBufferCapacity = 8)
		val coordinator = coordinator(FakeTransport(), FakeAdvertiser(), events, FakeHaptics())
		coordinator.start()
		runCurrent()

		events.emit(FindCommand.ProximitySample(8, -45))
		runCurrent()

		assertEquals(null, coordinator.state.value.proximityLevel)
	}

	@Test
	fun `missing peer prevents search`() = runTest {
		val transport = FakeTransport(sendResult = false)
		val advertiser = FakeAdvertiser()
		val coordinator = coordinator(
			transport,
			advertiser,
			MutableSharedFlow(extraBufferCapacity = 8),
			FakeHaptics(),
		)
		coordinator.start()
		assertEquals(FindError.PEER_NOT_CONNECTED, coordinator.state.value.error)
		assertFalse(coordinator.state.value.searching)
		assertEquals(emptyList<Int>(), advertiser.startedTokens)
	}

	@Test
	fun `unsupported proximity keeps silent find alive`() = runTest {
		val transport = FakeTransport()
		val advertiser = FakeAdvertiser(startResult = Result.failure(UnsupportedOperationException()))
		val coordinator = coordinator(
			transport,
			advertiser,
			MutableSharedFlow(extraBufferCapacity = 8),
			FakeHaptics(),
		)
		coordinator.start()
		assertTrue(coordinator.state.value.searching)
		assertFalse(coordinator.state.value.proximityAvailable)
		assertEquals(FindError.CAPABILITY_UNAVAILABLE, coordinator.state.value.error)
		assertEquals(listOf(FindCommand.StartFind(7, CueMode.BOTH)), transport.commands)
	}

	@Test
	fun `search times out at exactly two minutes and cleans up`() = runTest {
		val transport = FakeTransport()
		val advertiser = FakeAdvertiser()
		val coordinator = coordinator(
			transport,
			advertiser,
			MutableSharedFlow(extraBufferCapacity = 8),
			FakeHaptics(),
		)
		coordinator.start()
		advanceTimeBy(SEARCH_TIMEOUT_MS)
		runCurrent()
		assertFalse(coordinator.state.value.searching)
		assertEquals(FindError.TIMED_OUT, coordinator.state.value.error)
		assertEquals(1, advertiser.stopCount)
		assertTrue(transport.commands.contains(FindCommand.StopFind(7)))
	}

	private fun kotlinx.coroutines.test.TestScope.coordinator(
		transport: FakeTransport,
		advertiser: FakeAdvertiser,
		events: MutableSharedFlow<FindCommand>,
		haptics: FakeHaptics,
	) = WearFindCoordinator(
		scope = this,
		transport = transport,
		advertiseSession = BleAdvertiseSession(advertiser),
		proximityEvents = events,
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

	private class FakeAdvertiser(
		private val startResult: Result<Unit> = Result.success(Unit),
	) : BleAdvertiser {
		override val isSupported: Boolean = startResult.isSuccess
		val startedTokens = mutableListOf<Int>()
		var stopCount = 0

		override suspend fun start(sessionToken: Int): Result<Unit> {
			startedTokens += sessionToken
			return startResult
		}

		override suspend fun stop() {
			stopCount++
		}
	}

	private class FakeHaptics : GuidanceHaptics {
		var tickCount = 0
		override fun tick() { tickCount++ }
		override fun stop() = Unit
	}
}
