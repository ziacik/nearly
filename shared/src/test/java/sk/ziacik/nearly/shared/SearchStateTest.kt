package sk.ziacik.nearly.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchStateTest {
	@Test
	fun `search timeout is two minutes`() {
		assertEquals(120_000L, SEARCH_TIMEOUT_MS)
	}

	@Test
	fun `timeout changes an active search to timed out`() {
		val searching = SearchState.Searching(
			level = ProximityLevel.WARMER,
			rssi = -72.0,
			cueMode = CueMode.VIBRATE,
			remainingMs = 10L,
		)

		assertEquals(SearchState.TimedOut, searching.onTimeout())
	}
}
