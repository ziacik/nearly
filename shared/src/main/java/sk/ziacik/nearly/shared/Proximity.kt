package sk.ziacik.nearly.shared

enum class ProximityLevel {
	COLD,
	WARMER,
	HOT,
	VERY_CLOSE,
}

data class ProximityConfig(
	val alpha: Double = 0.25,
	val warmerAt: Int = -78,
	val hotAt: Int = -66,
	val veryCloseAt: Int = -55,
)

class RssiSmoother(
	private val alpha: Double,
) {
	private var value: Double? = null

	fun add(rssi: Int): Double {
		val previous = value
		val next = if (previous == null) {
			rssi.toDouble()
		} else {
			alpha * rssi + (1.0 - alpha) * previous
		}
		value = next
		return next
	}
}

fun proximityLevel(
	rssi: Double,
	config: ProximityConfig = ProximityConfig(),
): ProximityLevel = when {
	rssi >= config.veryCloseAt -> ProximityLevel.VERY_CLOSE
	rssi >= config.hotAt -> ProximityLevel.HOT
	rssi >= config.warmerAt -> ProximityLevel.WARMER
	else -> ProximityLevel.COLD
}

fun hapticIntervalMs(level: ProximityLevel): Long = when (level) {
	ProximityLevel.COLD -> 1_400L
	ProximityLevel.WARMER -> 800L
	ProximityLevel.HOT -> 400L
	ProximityLevel.VERY_CLOSE -> 180L
}
