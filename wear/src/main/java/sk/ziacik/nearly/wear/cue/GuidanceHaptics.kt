package sk.ziacik.nearly.wear.cue

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

interface GuidanceHaptics {
	fun tick()
	fun stop()
}

class AndroidGuidanceHaptics(context: Context) : GuidanceHaptics {
	private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		context.getSystemService(VibratorManager::class.java).defaultVibrator
	} else {
		@Suppress("DEPRECATION")
		(context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
	}

	override fun tick() {
		vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
	}

	override fun stop() {
		vibrator.cancel()
	}
}
