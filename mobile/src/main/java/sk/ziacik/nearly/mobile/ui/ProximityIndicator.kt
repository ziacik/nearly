package sk.ziacik.nearly.mobile.ui

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import sk.ziacik.nearly.mobile.ui.theme.NearlyAmber
import sk.ziacik.nearly.mobile.ui.theme.NearlyCoral
import sk.ziacik.nearly.mobile.ui.theme.NearlyLavender
import sk.ziacik.nearly.mobile.ui.theme.NearlyPeach
import sk.ziacik.nearly.mobile.ui.theme.NearlyRose
import sk.ziacik.nearly.mobile.ui.theme.NearlySurfaceHigh
import sk.ziacik.nearly.shared.ProximityLevel

@Composable
fun ProximityIndicator(
	level: ProximityLevel?,
	searching: Boolean,
	modifier: Modifier = Modifier,
) {
	val transition = rememberInfiniteTransition(label = "proximity")
	val pulse by transition.animateFloat(
		initialValue = 0.985f,
		targetValue = 1.025f,
		animationSpec = infiniteRepeatable(tween(1250), RepeatMode.Reverse),
		label = "proximity-pulse",
	)
	val color = level.proximityColor
	val scale = if (searching) pulse else 1f

	Box(
		modifier = modifier.aspectRatio(1f),
		contentAlignment = Alignment.Center,
	) {
		Canvas(
			modifier = Modifier
				.matchParentSize()
				.graphicsLayer(scaleX = scale, scaleY = scale),
		) {
			val radius = size.minDimension / 2f
			drawCircle(color.copy(alpha = 0.08f), radius = radius * 0.96f)
			drawCircle(color.copy(alpha = 0.12f), radius = radius * 0.76f)
			drawCircle(color.copy(alpha = 0.18f), radius = radius * 0.55f)
			drawCircle(
				color = NearlySurfaceHigh,
				radius = radius * 0.34f,
			)
			drawCircle(
				color = color.copy(alpha = 0.92f),
				radius = radius * 0.90f,
				style = Stroke(width = radius * 0.075f, cap = StrokeCap.Round),
			)
			drawCircle(
				color = color.copy(alpha = 0.42f),
				radius = radius * 0.56f,
				style = Stroke(width = radius * 0.035f),
			)
		}
		NearlyBrandMark(Modifier.size(64.dp))
	}
}

private val ProximityLevel?.proximityColor: Color
	get() = when (this) {
		ProximityLevel.COLD -> NearlyLavender
		ProximityLevel.WARMER -> NearlyRose
		ProximityLevel.HOT -> NearlyCoral
		ProximityLevel.VERY_CLOSE -> NearlyAmber
		null -> NearlyPeach
	}
