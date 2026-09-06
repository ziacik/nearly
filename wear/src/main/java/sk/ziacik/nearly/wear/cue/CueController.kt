package sk.ziacik.nearly.wear.cue

import kotlinx.coroutines.flow.StateFlow
import sk.ziacik.nearly.shared.CueMode

interface CueController {
	val glowActive: StateFlow<Boolean>
	fun set(mode: CueMode)
	fun stop()
}
