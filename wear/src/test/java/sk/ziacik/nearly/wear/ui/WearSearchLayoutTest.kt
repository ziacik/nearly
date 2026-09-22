package sk.ziacik.nearly.wear.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class WearSearchLayoutTest {
	@Test
	fun compactRoundWatchKeepsBottomActionInSafeArea() {
		assertTrue(WearSearchLayout.proximitySizeDp <= 70)
		assertTrue(WearSearchLayout.foundButtonWidthFraction <= 0.8f)
		assertTrue(WearSearchLayout.bottomPaddingDp >= 18)
	}
}
