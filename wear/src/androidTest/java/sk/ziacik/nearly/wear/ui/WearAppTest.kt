package sk.ziacik.nearly.wear.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.ProximityLevel

class WearAppTest {
	@get:Rule
	val composeRule = createComposeRule()

	@Test
	fun idleShowsFindPhone() {
		composeRule.setContent {
			WearApp(state = FindUiState())
		}

		composeRule.onNodeWithText("Find phone").assertIsDisplayed()
	}

	@Test
	fun searchShowsProximityAndCueControls() {
		composeRule.setContent {
			WearApp(
				state = FindUiState(
					searching = true,
					proximityLevel = ProximityLevel.HOT,
				),
			)
		}

		composeRule.onNodeWithText("Hot").assertIsDisplayed()
		composeRule.onNodeWithText("Glow").assertIsDisplayed()
		composeRule.onNodeWithText("Vibrate").assertIsDisplayed()
		composeRule.onNodeWithText("Both").assertIsDisplayed()
		composeRule.onNodeWithText("Found it").assertIsDisplayed()
	}

	@Test
	fun proximityFallbackKeepsCueControls() {
		composeRule.setContent {
			WearApp(
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
