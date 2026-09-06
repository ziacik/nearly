package sk.ziacik.nearly.shared

enum class ProximityLevel {
	COLD,
	WARMER,
	HOT,
	VERY_CLOSE,
}

data class ProximityConfig(
	val alpha: Double = 0.25,
	val warmerThresholdDbm: Double = -75.0,
	val hotThresholdDbm: Double = -62.0,
	val veryCloseThresholdDbm: Double = -50.0,
)

class RssiSmoother(
	private val alpha: Double = 0.25,
) {
	private var value: Double? = null

	fun add(sampleDbm: Int): Double {
		val previous = value
		val next = if (previous == null) {
			sampleDbm.toDouble()
		} else {
			alpha * sampleDbm + (1.0 - alpha) * previous
		}
		value = next
		return next
	}

	fun reset() {
		value = null
	}
}

fun proximityLevel(
	rssiDbm: Double,
	config: ProximityConfig = ProximityConfig(),
): ProximityLevel = when {
	rssiDbm >= config.veryCloseThresholdDbm -> ProximityLevel.VERY_CLOSE
	rssiDbm >= config.hotThresholdDbm -> ProximityLevel.HOT
	rssiDbm >= config.warmerThresholdDbm -> ProximityLevel.WARMER
	else -> ProximityLevel.COLD
}

fun hapticIntervalMs(level: ProximityLevel): Long = when (level) {
	ProximityLevel.COLD -> 2_500L
	ProximityLevel.WARMER -> 1_500L
	ProximityLevel.HOT -> 750L
	ProximityLevel.VERY_CLOSE -> 300L
}
