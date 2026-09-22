package sk.ziacik.nearly.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sk.ziacik.nearly.mobile.ui.theme.NearlyCoral
import sk.ziacik.nearly.mobile.ui.theme.NearlySurface
import sk.ziacik.nearly.mobile.ui.theme.NearlySurfaceHigh
import sk.ziacik.nearly.shared.CueMode

@Composable
fun CueSelector(
	selected: CueMode,
	onCue: (CueMode) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(10.dp),
	) {
		CueMode.entries.forEach { mode ->
			val isSelected = selected == mode
			val shape = RoundedCornerShape(20.dp)
			Column(
				modifier = Modifier
					.weight(1f)
					.height(92.dp)
					.background(if (isSelected) NearlySurfaceHigh else NearlySurface, shape)
					.border(1.5.dp, if (isSelected) NearlyCoral else Color.Transparent, shape)
					.clickable { onCue(mode) }
					.padding(horizontal = 8.dp, vertical = 12.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
			) {
				NearlyIcon(
					type = mode.iconType,
					modifier = Modifier.size(28.dp),
					color = if (isSelected) NearlyCoral else MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Text(
					text = mode.label,
					style = MaterialTheme.typography.labelLarge,
					color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
