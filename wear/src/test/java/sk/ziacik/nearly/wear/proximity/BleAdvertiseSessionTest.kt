package sk.ziacik.nearly.wear.proximity

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BleAdvertiseSessionTest {
	@Test
	fun `duplicate start for same token advertises once`() = runTest {
		val advertiser = FakeBleAdvertiser()
		val session = BleAdvertiseSession(advertiser)

		session.start(7).getOrThrow()
		session.start(7).getOrThrow()

		assertEquals(listOf(7), advertiser.startedTokens)
		assertEquals(7, session.activeToken)
	}

	@Test
	fun `duplicate and stale stops are harmless`() = runTest {
		val advertiser = FakeBleAdvertiser()
		val session = BleAdvertiseSession(advertiser)
		session.start(7).getOrThrow()

		session.stop(8)
		assertEquals(0, advertiser.stopCount)
		session.stop(7)
		session.stop(7)

		assertEquals(1, advertiser.stopCount)
		assertNull(session.activeToken)
	}

	@Test
	fun `session token bytes are big endian`() {
		assertArrayEquals(byteArrayOf(1, 2, 3, 4), sessionTokenBytes(0x01020304))
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
