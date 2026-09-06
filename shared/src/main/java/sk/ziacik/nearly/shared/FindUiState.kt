package sk.ziacik.nearly.shared

data class FindUiState(
	val searching: Boolean = false,
	val proximityLevel: ProximityLevel? = null,
	val smoothedRssi: Double? = null,
	val cueMode: CueMode = CueMode.BOTH,
	val proximityAvailable: Boolean = true,
	val error: FindError? = null,
)

enum class FindError {
	PEER_NOT_CONNECTED,
	BLUETOOTH_OFF,
	CAPABILITY_UNAVAILABLE,
	PERMISSION_MISSING,
	TIMED_OUT,
}
