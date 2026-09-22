package sk.ziacik.nearly.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val NearlyBackground = Color(0xFF111218)
val NearlySurface = Color(0xFF1B1D26)
val NearlySurfaceHigh = Color(0xFF252834)
val NearlyTextPrimary = Color(0xFFF7F2EE)
val NearlyTextSecondary = Color(0xFFB8B2B3)
val NearlyCoral = Color(0xFFFF6E68)
val NearlyPeach = Color(0xFFFF9C72)
val NearlyAmber = Color(0xFFFFBD63)
val NearlyRose = Color(0xFFE96A8D)
val NearlyLavender = Color(0xFF8D77D9)

private val NearlyColorScheme = darkColorScheme(
	primary = NearlyCoral,
	onPrimary = Color(0xFF321112),
	primaryContainer = Color(0xFF4B2528),
	onPrimaryContainer = NearlyTextPrimary,
	secondary = NearlyLavender,
	onSecondary = Color(0xFF1D1534),
	background = NearlyBackground,
	onBackground = NearlyTextPrimary,
	surface = NearlySurface,
	onSurface = NearlyTextPrimary,
	surfaceVariant = NearlySurfaceHigh,
	onSurfaceVariant = NearlyTextSecondary,
	error = Color(0xFFFFB4AB),
)

private val NearlyShapes = Shapes(
	small = RoundedCornerShape(14.dp),
	medium = RoundedCornerShape(22.dp),
	large = RoundedCornerShape(30.dp),
)

@Composable
fun NearlyTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		colorScheme = NearlyColorScheme,
		shapes = NearlyShapes,
		content = content,
	)
}
