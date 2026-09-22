package sk.ziacik.nearly.wear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import sk.ziacik.nearly.wear.ui.theme.NearlyWearText

internal enum class NearlyIconType {
	GLOW,
	VIBRATE,
	BOTH,
	FOUND,
}

@Composable
internal fun NearlyIcon(
	type: NearlyIconType,
	modifier: Modifier = Modifier,
	color: Color = NearlyWearText,
) {
	Canvas(modifier = modifier) {
		val stroke = size.minDimension * 0.09f
		val center = Offset(size.width / 2f, size.height / 2f)

		fun glow(origin: Offset, scale: Float = 1f) {
			val radius = size.minDimension * 0.17f * scale
			drawCircle(color, radius, origin)
			repeat(8) { index ->
				val angle = Math.toRadians((index * 45).toDouble())
				val inner = radius * 1.55f
				val outer = radius * 2.2f
				drawLine(
					color,
					Offset(origin.x + kotlin.math.cos(angle).toFloat() * inner, origin.y + kotlin.math.sin(angle).toFloat() * inner),
					Offset(origin.x + kotlin.math.cos(angle).toFloat() * outer, origin.y + kotlin.math.sin(angle).toFloat() * outer),
					stroke * 0.7f,
					StrokeCap.Round,
				)
			}
		}

		fun vibrate() {
			repeat(3) { index ->
				val x = size.width * (0.28f + index * 0.22f)
				val path = Path().apply {
					moveTo(x - size.width * 0.06f, size.height * 0.25f)
					lineTo(x + size.width * 0.04f, size.height * 0.40f)
					lineTo(x - size.width * 0.04f, size.height * 0.56f)
					lineTo(x + size.width * 0.06f, size.height * 0.73f)
				}
				drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round))
			}
		}

		when (type) {
			NearlyIconType.GLOW -> glow(center)
			NearlyIconType.VIBRATE -> vibrate()
			NearlyIconType.BOTH -> {
				glow(Offset(size.width * 0.30f, center.y), 0.7f)
				val path = Path().apply {
					moveTo(size.width * 0.60f, size.height * 0.28f)
					lineTo(size.width * 0.73f, size.height * 0.44f)
					lineTo(size.width * 0.63f, size.height * 0.58f)
					lineTo(size.width * 0.76f, size.height * 0.72f)
				}
				drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round))
			}
			NearlyIconType.FOUND -> {
				val path = Path().apply {
					moveTo(size.width * 0.20f, size.height * 0.52f)
					lineTo(size.width * 0.43f, size.height * 0.72f)
					lineTo(size.width * 0.82f, size.height * 0.30f)
				}
				drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round))
			}
		}
	}
}
