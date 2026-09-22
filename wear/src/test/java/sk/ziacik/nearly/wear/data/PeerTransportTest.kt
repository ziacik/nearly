package sk.ziacik.nearly.wear.data

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindCommand

class PeerTransportTest {
	@Test
	fun `selects a connected peer even when it is not directly nearby`() {
		val peers = listOf(
			PeerCandidate(id = "network-peer", isNearby = false),
			PeerCandidate(id = "bluetooth-peer", isNearby = true),
		)

		assertEquals("network-peer", selectConnectedNodeId(peers))
	}

	@Test
	fun `returns null when there are no connected peers`() {
		assertNull(selectConnectedNodeId(emptyList()))
	}

	@Test
	fun `command inbox delivers incoming commands`() = runTest {
		val inbox = FindCommandInbox()
		val received = async(start = CoroutineStart.UNDISPATCHED) {
			inbox.commands.first()
		}
		val command = FindCommand.StartFind(7, CueMode.VIBRATE)

		assertTrue(inbox.offer(command))
		assertEquals(command, received.await())
	}
}
