package sk.ziacik.nearly.shared

const val SEARCH_TIMEOUT_MS = 120_000L

sealed interface SearchState {
	data object Idle : SearchState

	data class Searching(
		val level: ProximityLevel?,
		val rssi: Double?,
		val cueMode: CueMode,
		val remainingMs: Long,
	) : SearchState

	data object Unsupported : SearchState

	data class Error(
		val message: String,
	) : SearchState

	data object TimedOut : SearchState
}

fun SearchState.onTimeout(): SearchState = when (this) {
	is SearchState.Searching -> SearchState.TimedOut
	else -> this
}
