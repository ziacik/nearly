package sk.ziacik.nearly.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import sk.ziacik.nearly.shared.CueMode
import sk.ziacik.nearly.wear.ui.theme.NearlyWearCoral
import sk.ziacik.nearly.wear.ui.theme.NearlyWearSurface
import sk.ziacik.nearly.wear.ui.theme.NearlyWearSurfaceHigh
import sk.ziacik.nearly.wear.ui.theme.NearlyWearText
import sk.ziacik.nearly.wear.ui.theme.NearlyWearTextSecondary

@Composable
fun WearCueSelector(
	selected: CueMode,
	onCue: (CueMode) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		CueMode.entries.forEach { mode ->
			val selectedMode = mode == selected
			val shape = RoundedCornerShape(14.dp)
			Column(
				modifier = Modifier
					.weight(1f)
					.height(46.dp)
					.background(if (selectedMode) NearlyWearSurfaceHigh else NearlyWearSurface, shape)
					.border(1.dp, if (selectedMode) NearlyWearCoral else Color.Transparent, shape)
					.clickable { onCue(mode) }
					.padding(vertical = 4.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
			) {
				NearlyIcon(
					type = mode.iconType,
					modifier = Modifier.size(18.dp),
					color = if (selectedMode) NearlyWearCoral else NearlyWearTextSecondary,
				)
				Text(
					text = mode.label,
					style = MaterialTheme.typography.labelSmall,
					color = if (selectedMode) NearlyWearText else NearlyWearTextSecondary,
				)
			}
		}
	}
}

private val CueMode.iconType: NearlyIconType
	get() = when (this) {
		CueMode.GLOW -> NearlyIconType.GLOW
		CueMode.VIBRATE -> NearlyIconType.VIBRATE
		CueMode.BOTH -> NearlyIconType.BOTH
	}

private val CueMode.label: String
	get() = when (this) {
		CueMode.GLOW -> "Glow"
		CueMode.VIBRATE -> "Vibrate"
		CueMode.BOTH -> "Both"
	}
