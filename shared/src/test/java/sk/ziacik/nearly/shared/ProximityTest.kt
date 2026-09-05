package sk.ziacik.nearly.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximityTest {
	private val config = ProximityConfig()

	@Test
	fun `EMA smooths a sudden RSSI jump`() {
		val smoother = RssiSmoother(alpha = 0.25)

		assertEquals(-80.0, smoother.add(-80), 0.001)
		assertEquals(-75.0, smoother.add(-60), 0.001)
	}

	@Test
	fun `RSSI maps to ordered proximity levels`() {
		assertEquals(ProximityLevel.COLD, proximityLevel(-90.0, config))
		assertEquals(ProximityLevel.WARMER, proximityLevel(-75.0, config))
		assertEquals(ProximityLevel.HOT, proximityLevel(-62.0, config))
		assertEquals(ProximityLevel.VERY_CLOSE, proximityLevel(-50.0, config))
	}

	@Test
	fun `haptics become faster as target gets closer`() {
		val intervals = ProximityLevel.entries.map(::hapticIntervalMs)

		assertTrue(intervals.zipWithNext().all { (farther, nearer) -> nearer < farther })
	}
}
