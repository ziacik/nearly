package sk.ziacik.nearly.shared

import java.nio.ByteBuffer

object FindProtocol {
	const val START_FIND_PATH = "/nearly/find/start"
	const val STOP_FIND_PATH = "/nearly/find/stop"
	const val START_PROXIMITY_PATH = "/nearly/proximity/start"
	const val STOP_PROXIMITY_PATH = "/nearly/proximity/stop"
	const val SET_CUE_PATH = "/nearly/cue/set"

	data class Message(
		val path: String,
		val payload: ByteArray,
	)

	fun encode(command: FindCommand): Message = when (command) {
		is FindCommand.StartFind -> cueMessage(START_FIND_PATH, command.sessionToken, command.cueMode)
		is FindCommand.StopFind -> tokenMessage(STOP_FIND_PATH, command.sessionToken)
		is FindCommand.StartProximity -> tokenMessage(START_PROXIMITY_PATH, command.sessionToken)
		is FindCommand.StopProximity -> tokenMessage(STOP_PROXIMITY_PATH, command.sessionToken)
		is FindCommand.SetCue -> cueMessage(SET_CUE_PATH, command.sessionToken, command.cueMode)
	}

	fun decode(path: String, payload: ByteArray): FindCommand? = when (path) {
		START_FIND_PATH -> decodeCue(payload)?.let { (token, cue) -> FindCommand.StartFind(token, cue) }
		STOP_FIND_PATH -> decodeToken(payload)?.let(FindCommand::StopFind)
		START_PROXIMITY_PATH -> decodeToken(payload)?.let(FindCommand::StartProximity)
		STOP_PROXIMITY_PATH -> decodeToken(payload)?.let(FindCommand::StopProximity)
		SET_CUE_PATH -> decodeCue(payload)?.let { (token, cue) -> FindCommand.SetCue(token, cue) }
		else -> null
	}

	private fun tokenMessage(path: String, sessionToken: Int): Message = Message(
		path = path,
		payload = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(sessionToken).array(),
	)

	private fun cueMessage(path: String, sessionToken: Int, cueMode: CueMode): Message = Message(
		path = path,
		payload = ByteBuffer.allocate(Int.SIZE_BYTES + 1)
			.putInt(sessionToken)
			.put(cueMode.ordinal.toByte())
			.array(),
	)

	private fun decodeCue(payload: ByteArray): Pair<Int, CueMode>? {
		if (payload.size != Int.SIZE_BYTES + 1) return null
		val buffer = ByteBuffer.wrap(payload)
		val sessionToken = buffer.int
		val cueMode = CueMode.entries.getOrNull(buffer.get().toInt()) ?: return null
		return sessionToken to cueMode
	}

	private fun decodeToken(payload: ByteArray): Int? {
		if (payload.size != Int.SIZE_BYTES) return null
		return ByteBuffer.wrap(payload).int
	}
}
