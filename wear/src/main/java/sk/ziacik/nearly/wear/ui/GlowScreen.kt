package sk.ziacik.nearly.wear.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text

@Composable
fun GlowScreen(onFound: () -> Unit) {
	val transition = rememberInfiniteTransition(label = "wear-glow")
	val pulse by transition.animateFloat(
		initialValue = 0.78f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(tween(1350), RepeatMode.Reverse),
		label = "wear-glow-pulse",
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.radialGradient(
					colors = listOf(
						Color(0xFFFFF8E7),
						Color(0xFFFFD49A).copy(alpha = 0.88f + pulse * 0.12f),
						Color(0xFFFF8A72).copy(alpha = 0.72f + pulse * 0.20f),
					),
				),
			)
			.padding(18.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Text("Here I am", color = Color(0xFF2D1815), fontWeight = FontWeight.Bold)
		Button(
			onClick = onFound,
			label = { Text("Found it") },
		)
	}
}
