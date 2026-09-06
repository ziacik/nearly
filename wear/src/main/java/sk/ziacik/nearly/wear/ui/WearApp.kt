package sk.ziacik.nearly.wear.ui

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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.shared.FindError
import sk.ziacik.nearly.shared.FindUiState
import sk.ziacik.nearly.shared.ProximityLevel

@Composable
fun WearApp(
	state: FindUiState,
	onFind: () -> Unit = {},
	onStop: () -> Unit = {},
	onCue: (CueMode) -> Unit = {},
	onGrantPermission: () -> Unit = {},
) {
	MaterialTheme {
		AppScaffold {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 14.dp, vertical = 24.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
			) {
				if (!state.searching) {
					Text("Nearly", style = MaterialTheme.typography.titleLarge)
					Spacer(Modifier.height(10.dp))
					state.error?.let {
						Text(errorText(it, false), style = MaterialTheme.typography.bodySmall)
						Spacer(Modifier.height(6.dp))
					}
					Button(
						onClick = onFind,
						modifier = Modifier.fillMaxWidth(),
						label = { Text("Find phone") },
					)
					if (state.error == FindError.PERMISSION_MISSING) {
						Spacer(Modifier.height(6.dp))
						Button(
							onClick = onGrantPermission,
							modifier = Modifier.fillMaxWidth(),
							label = { Text("Grant permission") },
						)
					}
					return@Column
				}

				Text(
					state.proximityLevel?.label ?: if (state.proximityAvailable) "Searching…" else "Hot/cold unavailable",
					style = MaterialTheme.typography.titleLarge,
				)
				if (!state.proximityAvailable) {
					Text("Hot/cold unavailable", style = MaterialTheme.typography.bodySmall)
				}
				state.error?.takeIf { it != FindError.TIMED_OUT }?.let {
					Text(errorText(it, true), style = MaterialTheme.typography.bodySmall)
				}
				Spacer(Modifier.height(8.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceEvenly,
				) {
					CueMode.entries.forEach { mode ->
						TextButton(
							onClick = { onCue(mode) },
							modifier = Modifier.size(48.dp),
						) {
							Text(mode.shortLabel)
						}
					}
				}
				Spacer(Modifier.height(6.dp))
				Button(
					onClick = onStop,
					modifier = Modifier.fillMaxWidth(),
					label = { Text("Found it") },
				)
			}
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

private val CueMode.shortLabel: String
	get() = when (this) {
		CueMode.GLOW -> "G"
		CueMode.VIBRATE -> "V"
		CueMode.BOTH -> "B"
	}

private fun errorText(error: FindError, searching: Boolean): String = when (error) {
	FindError.PEER_NOT_CONNECTED -> if (searching) "Phone connection lost" else "Phone not nearby"
	FindError.BLUETOOTH_OFF -> "Bluetooth needed for hot/cold"
	FindError.CAPABILITY_UNAVAILABLE -> "Hot/cold unsupported"
	FindError.PERMISSION_MISSING -> "Permission needed for hot/cold"
	FindError.TIMED_OUT -> "Search timed out"
}
