package sk.ziacik.nearly.wear.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import sk.ziacik.nearly.shared.ProximityLevel
import sk.ziacik.nearly.wear.ui.theme.NearlyWearAmber
import sk.ziacik.nearly.wear.ui.theme.NearlyWearCoral
import sk.ziacik.nearly.wear.ui.theme.NearlyWearLavender
import sk.ziacik.nearly.wear.ui.theme.NearlyWearPeach
import sk.ziacik.nearly.wear.ui.theme.NearlyWearRose
import sk.ziacik.nearly.wear.ui.theme.NearlyWearSurfaceHigh

@Composable
fun WearProximityIndicator(
	level: ProximityLevel?,
	searching: Boolean,
	modifier: Modifier = Modifier,
) {
	val transition = rememberInfiniteTransition(label = "wear-proximity")
	val pulse by transition.animateFloat(
		initialValue = 0.985f,
		targetValue = 1.025f,
		animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
		label = "wear-proximity-pulse",
	)
	val color = level.proximityColor

	Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
		Canvas(
			modifier = Modifier
				.matchParentSize()
				.graphicsLayer(
					scaleX = if (searching) pulse else 1f,
					scaleY = if (searching) pulse else 1f,
				),
		) {
			val radius = size.minDimension / 2f
			drawCircle(color.copy(alpha = 0.10f), radius * 0.95f)
			drawCircle(color.copy(alpha = 0.16f), radius * 0.70f)
			drawCircle(NearlyWearSurfaceHigh, radius * 0.38f)
			drawCircle(color.copy(alpha = 0.95f), radius * 0.86f, style = Stroke(radius * 0.08f))
			drawCircle(color.copy(alpha = 0.38f), radius * 0.58f, style = Stroke(radius * 0.035f))
		}
		NearlyBrandMark(Modifier.size(34.dp))
	}
}

private val ProximityLevel?.proximityColor: Color
	get() = when (this) {
		ProximityLevel.COLD -> NearlyWearLavender
		ProximityLevel.WARMER -> NearlyWearRose
		ProximityLevel.HOT -> NearlyWearCoral
		ProximityLevel.VERY_CLOSE -> NearlyWearAmber
		null -> NearlyWearPeach
	}
