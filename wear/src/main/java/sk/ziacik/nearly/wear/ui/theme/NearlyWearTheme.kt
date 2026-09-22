package sk.ziacik.nearly.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme

val NearlyWearBackground = Color(0xFF111218)
val NearlyWearSurface = Color(0xFF1B1D26)
val NearlyWearSurfaceHigh = Color(0xFF252834)
val NearlyWearText = Color(0xFFF7F2EE)
val NearlyWearTextSecondary = Color(0xFFB8B2B3)
val NearlyWearCoral = Color(0xFFFF6E68)
val NearlyWearPeach = Color(0xFFFF9C72)
val NearlyWearAmber = Color(0xFFFFBD63)
val NearlyWearRose = Color(0xFFE96A8D)
val NearlyWearLavender = Color(0xFF8D77D9)

@Composable
fun NearlyWearTheme(content: @Composable () -> Unit) {
	MaterialTheme(content = content)
}
