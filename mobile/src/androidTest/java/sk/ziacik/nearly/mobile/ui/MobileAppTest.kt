package sk.ziacik.nearly.mobile.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.ProximityLevel

class MobileAppTest {
	@get:Rule
	val composeRule = createComposeRule()

	@Test
	fun idleShowsBrandedFindWatchHero() {
		composeRule.setContent {
			MobileApp(state = FindUiState())
		}

		composeRule.onNodeWithText("Nearly").assertIsDisplayed()
		composeRule.onNodeWithText("Find your watch").assertIsDisplayed()
		composeRule.onNodeWithText("Start searching").assertIsDisplayed()
	}

	@Test
	fun searchShowsProximityAndCueControls() {
		composeRule.setContent {
			MobileApp(
				state = FindUiState(
					searching = true,
					proximityLevel = ProximityLevel.VERY_CLOSE,
				),
			)
		}

		composeRule.onNodeWithText("Finding your watch…").assertIsDisplayed()
		composeRule.onNodeWithText("Very close").assertIsDisplayed()
		composeRule.onNodeWithText("Glow").assertIsDisplayed()
		composeRule.onNodeWithText("Vibrate").assertIsDisplayed()
		composeRule.onNodeWithText("Both").assertIsDisplayed()
		composeRule.onNodeWithText("Found it").assertIsDisplayed()
	}

	@Test
	fun proximityFallbackKeepsSilentControls() {
		composeRule.setContent {
			MobileApp(
				state = FindUiState(
					searching = true,
					proximityAvailable = false,
				),
			)
		}

		composeRule.onNodeWithText("Hot/cold unavailable").assertIsDisplayed()
		composeRule.onNodeWithText("Glow").assertIsDisplayed()
		composeRule.onNodeWithText("Vibrate").assertIsDisplayed()
		composeRule.onNodeWithText("Both").assertIsDisplayed()
	}
}
