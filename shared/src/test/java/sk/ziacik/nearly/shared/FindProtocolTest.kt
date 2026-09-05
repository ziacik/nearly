package sk.ziacik.nearly.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindProtocolTest {
	@Test
	fun `commands round trip through wire protocol`() {
		val commands = listOf(
			FindCommand.StartFind(sessionToken = 42, cueMode = CueMode.BOTH),
			FindCommand.StopFind(sessionToken = 42),
			FindCommand.StartProximity(sessionToken = 42),
			FindCommand.StopProximity(sessionToken = 42),
		)

		commands.forEach { command ->
			val message = FindProtocol.encode(command)

			assertEquals(command, FindProtocol.decode(message.path, message.payload))
		}
	}

	@Test
	fun `malformed payload is ignored`() {
		assertNull(FindProtocol.decode(FindProtocol.START_FIND_PATH, byteArrayOf(1)))
	}
}
