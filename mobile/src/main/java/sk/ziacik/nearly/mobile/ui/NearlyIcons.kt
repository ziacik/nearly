package sk.ziacik.nearly.mobile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

internal enum class NearlyIconType {
	GLOW,
	VIBRATE,
	BOTH,
	SEARCH,
	FOUND,
}

@Composable
internal fun NearlyIcon(
	type: NearlyIconType,
	modifier: Modifier = Modifier,
	color: Color = MaterialTheme.colorScheme.onSurface,
) {
	Canvas(modifier = modifier) {
		val stroke = size.minDimension * 0.09f
		val center = Offset(size.width / 2f, size.height / 2f)

		fun drawGlow(origin: Offset, scale: Float = 1f) {
			val radius = size.minDimension * 0.17f * scale
			drawCircle(color = color, radius = radius, center = origin)
			repeat(8) { index ->
				val angle = Math.toRadians((index * 45).toDouble())
				val inner = radius * 1.55f
				val outer = radius * 2.25f
				drawLine(
					color = color,
					start = Offset(
						x = origin.x + kotlin.math.cos(angle).toFloat() * inner,
						y = origin.y + kotlin.math.sin(angle).toFloat() * inner,
					),
					end = Offset(
						x = origin.x + kotlin.math.cos(angle).toFloat() * outer,
						y = origin.y + kotlin.math.sin(angle).toFloat() * outer,
					),
					strokeWidth = stroke * 0.72f,
					cap = StrokeCap.Round,
				)
			}
		}

		fun drawVibrate(offsetX: Float = 0f, scale: Float = 1f) {
			repeat(3) { index ->
				val x = size.width * (0.28f + index * 0.22f) * scale + offsetX
				val path = Path().apply {
					moveTo(x - size.width * 0.06f * scale, size.height * 0.25f)
					lineTo(x + size.width * 0.04f * scale, size.height * 0.40f)
					lineTo(x - size.width * 0.04f * scale, size.height * 0.56f)
					lineTo(x + size.width * 0.06f * scale, size.height * 0.73f)
				}
				drawPath(path = path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
			}
		}

		when (type) {
			NearlyIconType.GLOW -> drawGlow(center)
			NearlyIconType.VIBRATE -> drawVibrate()
			NearlyIconType.BOTH -> {
				drawGlow(Offset(size.width * 0.30f, center.y), 0.72f)
				val path = Path().apply {
					moveTo(size.width * 0.60f, size.height * 0.28f)
					lineTo(size.width * 0.72f, size.height * 0.43f)
					lineTo(size.width * 0.62f, size.height * 0.58f)
					lineTo(size.width * 0.76f, size.height * 0.72f)
				}
				drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round))
			}
			NearlyIconType.SEARCH -> {
				drawCircle(color = color, radius = size.minDimension * 0.26f, center = Offset(size.width * 0.44f, size.height * 0.43f), style = Stroke(stroke))
				drawLine(color, Offset(size.width * 0.61f, size.height * 0.62f), Offset(size.width * 0.82f, size.height * 0.83f), stroke, StrokeCap.Round)
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
