package sk.ziacik.nearly.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sk.ziacik.nearly.mobile.ui.theme.NearlyCoral
import sk.ziacik.nearly.mobile.ui.theme.NearlyTextSecondary
import sk.ziacik.nearly.mobile.ui.theme.NearlyTheme

@Composable
fun TargetPermissionScreen(
	onGrantPermission: () -> Unit = {},
	onFound: () -> Unit = {},
) {
	NearlyTheme {
		Surface(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(32.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
			) {
				NearlyBrandMark(Modifier.size(72.dp))
				Spacer(Modifier.height(24.dp))
				Text(
					text = "Nearby devices permission needed",
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.SemiBold,
					textAlign = TextAlign.Center,
				)
				Spacer(Modifier.height(10.dp))
				Text(
					text = "Nearly needs it to measure how close your watch is.",
					style = MaterialTheme.typography.bodyLarge,
					color = NearlyTextSecondary,
					textAlign = TextAlign.Center,
				)
				Spacer(Modifier.height(24.dp))
				Button(
					onClick = onGrantPermission,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(containerColor = NearlyCoral),
				) {
					Text("Allow Nearby devices", fontWeight = FontWeight.SemiBold)
				}
				TextButton(onClick = onFound) {
					Text("Found it")
				}
			}
		}
	}
}
