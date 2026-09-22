package sk.ziacik.nearly.wear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import sk.ziacik.nearly.wear.ui.theme.NearlyWearCoral
import sk.ziacik.nearly.wear.ui.theme.NearlyWearLavender
import sk.ziacik.nearly.wear.ui.theme.NearlyWearPeach
import sk.ziacik.nearly.wear.ui.theme.NearlyWearRose

@Composable
fun NearlyBrandMark(modifier: Modifier = Modifier) {
	Canvas(modifier = modifier) {
		val blobWidth = size.width * 0.58f
		val blobHeight = size.height * 0.78f
		val top = size.height * 0.11f
		val radius = blobWidth * 0.48f

		drawRoundRect(
			brush = Brush.linearGradient(listOf(NearlyWearCoral, NearlyWearPeach)),
			topLeft = Offset(size.width * 0.04f, top),
			size = Size(blobWidth, blobHeight),
			cornerRadius = CornerRadius(radius, radius),
		)
		drawRoundRect(
			brush = Brush.linearGradient(listOf(NearlyWearLavender, NearlyWearRose)),
			topLeft = Offset(size.width * 0.38f, top),
			size = Size(blobWidth, blobHeight),
			cornerRadius = CornerRadius(radius, radius),
		)
	}
}
