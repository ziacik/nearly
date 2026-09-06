package sk.ziacik.nearly.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class ProximityTest {
	@Test
	fun `EMA smooths a sudden RSSI jump and can reset`() {
		val smoother = RssiSmoother(alpha = 0.25)

		assertEquals(-80.0, smoother.add(-80), 0.001)
		assertEquals(-75.0, smoother.add(-60), 0.001)
		smoother.reset()
		assertEquals(-40.0, smoother.add(-40), 0.001)
	}

	@Test
	fun `RSSI boundaries map to exact proximity levels`() {
		assertEquals(ProximityLevel.COLD, proximityLevel(-80.0))
		assertEquals(ProximityLevel.WARMER, proximityLevel(-75.0))
		assertEquals(ProximityLevel.HOT, proximityLevel(-62.0))
		assertEquals(ProximityLevel.VERY_CLOSE, proximityLevel(-50.0))
	}

	@Test
	fun `haptic intervals match product behavior`() {
		assertEquals(2_500L, hapticIntervalMs(ProximityLevel.COLD))
		assertEquals(1_500L, hapticIntervalMs(ProximityLevel.WARMER))
		assertEquals(750L, hapticIntervalMs(ProximityLevel.HOT))
		assertEquals(300L, hapticIntervalMs(ProximityLevel.VERY_CLOSE))
	}
}
