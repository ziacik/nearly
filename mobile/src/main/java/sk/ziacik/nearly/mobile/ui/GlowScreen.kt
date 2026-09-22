package sk.ziacik.nearly.mobile.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlowScreen(onFound: () -> Unit) {
	val transition = rememberInfiniteTransition(label = "glow")
	val pulse by transition.animateFloat(
		initialValue = 0.78f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(tween(1450), RepeatMode.Reverse),
		label = "glow-pulse",
	)
	val center = Color(0xFFFFF8E7)
	val warm = Color(0xFFFFD49A)
	val edge = Color(0xFFFF8A72)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(
				Brush.radialGradient(
					colors = listOf(
						center,
						warm.copy(alpha = 0.88f + pulse * 0.12f),
						edge.copy(alpha = 0.72f + pulse * 0.20f),
					),
				),
			)
			.padding(28.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Bottom,
	) {
		Text(
			text = "Here I am",
			fontSize = 34.sp,
			fontWeight = FontWeight.Bold,
			color = Color(0xFF2D1815),
		)
		Spacer(Modifier.height(12.dp))
		Button(
			onClick = onFound,
			colors = ButtonDefaults.buttonColors(
				containerColor = Color(0xDD23191A),
				contentColor = Color.White,
			),
		) {
			Text("Found it")
		}
	}
}
