package sk.ziacik.nearly.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindError
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.ProximityLevel
import sk.ziacik.nearly.wear.ui.theme.NearlyWearBackground
import sk.ziacik.nearly.wear.ui.theme.NearlyWearCoral
import sk.ziacik.nearly.wear.ui.theme.NearlyWearText
import sk.ziacik.nearly.wear.ui.theme.NearlyWearTextSecondary
import sk.ziacik.nearly.wear.ui.theme.NearlyWearTheme

@Composable
fun WearApp(
	state: FindUiState,
	onFind: () -> Unit = {},
	onStop: () -> Unit = {},
	onCue: (CueMode) -> Unit = {},
	onGrantPermission: () -> Unit = {},
) {
	NearlyWearTheme {
		AppScaffold {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(NearlyWearBackground),
			) {
				if (state.searching) {
					SearchingContent(state, onStop, onCue)
				} else {
					IdleContent(state, onFind, onGrantPermission)
				}
			}
		}
	}
}

@Composable
private fun IdleContent(
	state: FindUiState,
	onFind: () -> Unit,
	onGrantPermission: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 18.dp, vertical = 18.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		NearlyBrandMark(Modifier.size(44.dp))
		Text("Nearly", style = MaterialTheme.typography.titleMedium, color = NearlyWearText)
		Text(
			text = "Find phone",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.SemiBold,
			color = NearlyWearText,
		)
		state.error?.let {
			Text(
				text = errorText(it, searching = false),
				style = MaterialTheme.typography.labelSmall,
				color = NearlyWearTextSecondary,
				textAlign = TextAlign.Center,
			)
		}
		Spacer(Modifier.height(6.dp))
		Button(
			onClick = onFind,
			modifier = Modifier.fillMaxWidth(),
			label = { Text("Start", fontWeight = FontWeight.SemiBold) },
		)
		if (state.error == FindError.PERMISSION_MISSING) {
			Spacer(Modifier.height(4.dp))
			Button(
				onClick = onGrantPermission,
				modifier = Modifier.fillMaxWidth(),
				label = { Text("Permission") },
			)
		}
	}
}

@Composable
private fun SearchingContent(
	state: FindUiState,
	onStop: () -> Unit,
	onCue: (CueMode) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = WearSearchLayout.horizontalPaddingDp.dp)
			.padding(
				top = WearSearchLayout.topPaddingDp.dp,
				bottom = WearSearchLayout.bottomPaddingDp.dp,
			),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		WearProximityIndicator(
			level = state.proximityLevel,
			searching = state.searching,
			modifier = Modifier.size(WearSearchLayout.proximitySizeDp.dp),
		)
		Text(
			text = state.proximityLevel?.label ?: "Searching…",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			color = state.proximityLevel?.accentColor ?: NearlyWearText,
		)
		if (!state.proximityAvailable) {
			Text(
				text = "Hot/cold unavailable",
				style = MaterialTheme.typography.labelSmall,
				color = NearlyWearTextSecondary,
			)
		}
		state.error?.takeIf { it != FindError.TIMED_OUT }?.let {
			Text(
				text = errorText(it, searching = true),
				style = MaterialTheme.typography.labelSmall,
				color = NearlyWearTextSecondary,
				textAlign = TextAlign.Center,
			)
		}
		Spacer(Modifier.height(WearSearchLayout.sectionGapDp.dp))
		WearCueSelector(
			selected = state.cueMode,
			onCue = onCue,
			modifier = Modifier.fillMaxWidth(),
		)
		Spacer(Modifier.height(WearSearchLayout.sectionGapDp.dp))
		Button(
			onClick = onStop,
			modifier = Modifier
				.fillMaxWidth(WearSearchLayout.foundButtonWidthFraction)
				.height(WearSearchLayout.foundButtonHeightDp.dp),
			label = {
				Row(verticalAlignment = Alignment.CenterVertically) {
					NearlyIcon(NearlyIconType.FOUND, Modifier.size(16.dp), NearlyWearText)
					Spacer(Modifier.size(4.dp))
					Text("Found it")
				}
			},
		)
	}
}

private val ProximityLevel.label: String
	get() = when (this) {
		ProximityLevel.COLD -> "Cold"
		ProximityLevel.WARMER -> "Warmer"
		ProximityLevel.HOT -> "Hot"
		ProximityLevel.VERY_CLOSE -> "Very close"
	}

private val ProximityLevel.accentColor
	get() = when (this) {
		ProximityLevel.COLD -> sk.ziacik.nearly.wear.ui.theme.NearlyWearLavender
		ProximityLevel.WARMER -> sk.ziacik.nearly.wear.ui.theme.NearlyWearRose
		ProximityLevel.HOT -> NearlyWearCoral
		ProximityLevel.VERY_CLOSE -> sk.ziacik.nearly.wear.ui.theme.NearlyWearAmber
	}

private fun errorText(error: FindError, searching: Boolean): String = when (error) {
	FindError.PEER_NOT_CONNECTED -> if (searching) "Phone connection lost" else "Phone not nearby"
	FindError.BLUETOOTH_OFF -> "Bluetooth needed for hot/cold"
	FindError.CAPABILITY_UNAVAILABLE -> "Hot/cold unsupported"
	FindError.PERMISSION_MISSING -> "Permission needed for hot/cold"
	FindError.TIMED_OUT -> "Search timed out"
}
