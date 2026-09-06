package sk.ziacik.nearly.mobile.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
	MaterialTheme {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Text("Nearly", style = MaterialTheme.typography.headlineLarge)
			Spacer(Modifier.height(28.dp))

			if (!state.searching) {
				state.error?.let { error ->
					Text(errorText(error, searching = false))
					Spacer(Modifier.height(16.dp))
				}
				Button(onClick = onFind) {
					Text("Find watch")
				}
				if (state.error == FindError.PERMISSION_MISSING) {
					Spacer(Modifier.height(12.dp))
					Button(onClick = onGrantPermission) {
						Text("Grant permission")
					}
				}
				return@Column
			}

			Text(
				text = state.proximityLevel?.label ?: if (state.proximityAvailable) "Searching…" else "Hot/cold unavailable",
				fontSize = 36.sp,
			)
			Spacer(Modifier.height(16.dp))

			if (state.proximityAvailable) {
				SignalIndicator(state.proximityLevel)
			} else {
				Text("Hot/cold unavailable")
			}

			state.error?.takeIf { it != FindError.TIMED_OUT }?.let {
				Spacer(Modifier.height(8.dp))
				Text(errorText(it, searching = true), style = MaterialTheme.typography.bodyMedium)
			}

			Spacer(Modifier.height(28.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
			) {
				CueMode.entries.forEach { mode ->
					FilterChip(
						selected = state.cueMode == mode,
						onClick = { onCue(mode) },
						label = { Text(mode.label) },
					)
				}
			}

			Spacer(Modifier.height(24.dp))
			Button(onClick = onStop) {
				Text("Found it")
			}
		}
	}
}

@Composable
private fun SignalIndicator(level: ProximityLevel?) {
	val active = when (level) {
		ProximityLevel.COLD -> 1
		ProximityLevel.WARMER -> 2
		ProximityLevel.HOT -> 3
		ProximityLevel.VERY_CLOSE -> 4
		null -> 0
	}
	Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
		repeat(4) { index ->
			Box(
				modifier = Modifier
					.size(14.dp)
					.background(
						if (index < active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
						CircleShape,
					),
			)
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

private val CueMode.label: String
	get() = when (this) {
		CueMode.GLOW -> "Glow"
		CueMode.VIBRATE -> "Vibrate"
		CueMode.BOTH -> "Both"
	}

private fun errorText(error: FindError, searching: Boolean): String = when (error) {
	FindError.PEER_NOT_CONNECTED -> if (searching) "Watch connection lost" else "Watch not nearby"
	FindError.BLUETOOTH_OFF -> "Turn on Bluetooth for hot/cold"
	FindError.CAPABILITY_UNAVAILABLE -> "This device can't provide hot/cold proximity"
	FindError.PERMISSION_MISSING -> "Nearby permission is needed for hot/cold"
	FindError.TIMED_OUT -> "Search timed out"
}
