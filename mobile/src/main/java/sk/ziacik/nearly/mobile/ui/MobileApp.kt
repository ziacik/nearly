package sk.ziacik.nearly.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sk.ziacik.nearly.mobile.ui.theme.NearlyCoral
import sk.ziacik.nearly.mobile.ui.theme.NearlySurface
import sk.ziacik.nearly.mobile.ui.theme.NearlyTextSecondary
import sk.ziacik.nearly.mobile.ui.theme.NearlyTheme
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindError
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.ProximityLevel

@Composable
fun MobileApp(
	state: FindUiState,
	onFind: () -> Unit = {},
	onStop: () -> Unit = {},
	onCue: (CueMode) -> Unit = {},
	onGrantPermission: () -> Unit = {},
) {
	NearlyTheme {
		Surface(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 24.dp, vertical = 22.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				BrandHeader()
				Spacer(Modifier.height(24.dp))
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
private fun BrandHeader() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(10.dp),
	) {
		NearlyBrandMark(Modifier.size(34.dp))
		Text(
			text = "Nearly",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.SemiBold,
		)
	}
}

@Composable
private fun IdleContent(
	state: FindUiState,
	onFind: () -> Unit,
	onGrantPermission: () -> Unit,
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		Card(
			modifier = Modifier.fillMaxWidth(),
			colors = CardDefaults.cardColors(containerColor = NearlySurface),
			shape = MaterialTheme.shapes.large,
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
				) {
					Column(modifier = Modifier.weight(1f)) {
						Text("WATCH", style = MaterialTheme.typography.labelMedium, color = NearlyCoral)
						Text(
							text = "Find your watch",
							style = MaterialTheme.typography.headlineMedium,
							fontWeight = FontWeight.SemiBold,
						)
					}
					NearlyBrandMark(Modifier.size(76.dp))
				}
				Text(
					text = "Quietly find it nearby.",
					style = MaterialTheme.typography.bodyLarge,
					color = NearlyTextSecondary,
				)
				Button(
					onClick = onFind,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(containerColor = NearlyCoral),
				) {
					Text("Start searching", fontWeight = FontWeight.SemiBold)
				}
			}
		}

		state.error?.let { error ->
			Text(
				text = errorText(error, searching = false),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
			if (error == FindError.PERMISSION_MISSING) {
				Button(onClick = onGrantPermission) {
					Text("Grant permission")
				}
			}
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
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp),
	) {
		Text(
			text = "Finding your watch…",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.SemiBold,
		)
		Text(
			text = guidanceText(state.proximityLevel),
			style = MaterialTheme.typography.bodyMedium,
			color = NearlyTextSecondary,
		)

		ProximityIndicator(
			level = state.proximityLevel,
			searching = state.searching,
			modifier = Modifier.size(246.dp),
		)

		Text(
			text = state.proximityLevel?.label ?: "Searching…",
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.Bold,
		)
		if (!state.proximityAvailable) {
			Text(
				text = "Hot/cold unavailable",
				style = MaterialTheme.typography.bodyMedium,
				color = NearlyTextSecondary,
			)
		}
		state.error?.takeIf { it != FindError.TIMED_OUT }?.let {
			Text(
				text = errorText(it, searching = true),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}

		CueSelector(
			selected = state.cueMode,
			onCue = onCue,
		)
		Button(
			onClick = onStop,
			modifier = Modifier
				.fillMaxWidth()
				.height(52.dp),
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
		) {
			NearlyIcon(NearlyIconType.FOUND, Modifier.size(22.dp))
			Spacer(Modifier.size(8.dp))
			Text("Found it")
		}
	}
}

private val ProximityLevel.label: String
	get() = when (this) {
		ProximityLevel.COLD -> "Cold"
		ProximityLevel.WARMER -> "Warmer"
		ProximityLevel.HOT -> "Hot"
		ProximityLevel.VERY_CLOSE -> "Very close"
	}

private fun guidanceText(level: ProximityLevel?): String = when (level) {
	ProximityLevel.COLD -> "Keep moving around"
	ProximityLevel.WARMER -> "You're getting closer"
	ProximityLevel.HOT -> "Very close now"
	ProximityLevel.VERY_CLOSE -> "Right around here"
	null -> "Move around slowly"
}

private fun errorText(error: FindError, searching: Boolean): String = when (error) {
	FindError.PEER_NOT_CONNECTED -> if (searching) "Watch connection lost" else "Watch not nearby"
	FindError.BLUETOOTH_OFF -> "Turn on Bluetooth for hot/cold"
	FindError.CAPABILITY_UNAVAILABLE -> "This device can't provide hot/cold proximity"
	FindError.PERMISSION_MISSING -> "Nearby permission is needed for hot/cold"
	FindError.TIMED_OUT -> "Search timed out"
}
