package sk.ziacik.nearly.mobile.cue

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import sk.ziacik.nearly.shared.CueMode

class MobileCueController(context: Context) : CueController {
	private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		context.getSystemService(VibratorManager::class.java).defaultVibrator
	} else {
		@Suppress("DEPRECATION")
		(context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
	}
	private val mutableGlowActive = MutableStateFlow(false)
	private var currentMode: CueMode? = null

	override val glowActive = mutableGlowActive.asStateFlow()

	override fun set(mode: CueMode) {
		if (currentMode == mode) return
		currentMode = mode
		mutableGlowActive.value = mode == CueMode.GLOW || mode == CueMode.BOTH

		if (mode == CueMode.VIBRATE || mode == CueMode.BOTH) {
			vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 180L, 820L), 0))
		} else {
			vibrator.cancel()
		}
	}

	override fun stop() {
		currentMode = null
		mutableGlowActive.value = false
		vibrator.cancel()
	}
}
