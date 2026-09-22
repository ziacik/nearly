package sk.ziacik.nearly.shared

enum class CueMode {
	GLOW,
	VIBRATE,
	BOTH,
}

sealed interface FindCommand {
	val sessionToken: Int

	data class StartFind(
		override val sessionToken: Int,
		val cueMode: CueMode,
	) : FindCommand

	data class StopFind(
		override val sessionToken: Int,
	) : FindCommand

	data class StartProximity(
		override val sessionToken: Int,
	) : FindCommand

	data class StopProximity(
		override val sessionToken: Int,
	) : FindCommand

	data class ProximitySample(
		override val sessionToken: Int,
		val rssi: Int,
	) : FindCommand

	data class ProximityUnavailable(
		override val sessionToken: Int,
		val error: FindError,
	) : FindCommand

	data class SetCue(
		override val sessionToken: Int,
		val cueMode: CueMode,
	) : FindCommand
}
