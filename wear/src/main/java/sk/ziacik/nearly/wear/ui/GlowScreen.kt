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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text

@Composable
fun GlowScreen(onFound: () -> Unit) {
	val transition = rememberInfiniteTransition(label = "glow")
	val alpha by transition.animateFloat(
		initialValue = 0.72f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
		label = "glow-alpha",
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White.copy(alpha = alpha)),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Button(onClick = onFound, label = { Text("Found it") })
	}
}
