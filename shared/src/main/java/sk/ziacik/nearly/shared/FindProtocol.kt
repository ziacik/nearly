package sk.ziacik.nearly.shared

import java.nio.ByteBuffer

object FindProtocol {
	const val START_FIND_PATH = "/nearly/find/start"
	const val STOP_FIND_PATH = "/nearly/find/stop"
	const val START_PROXIMITY_PATH = "/nearly/proximity/start"
	const val STOP_PROXIMITY_PATH = "/nearly/proximity/stop"

	data class Message(
		val path: String,
		val payload: ByteArray,
	)

	fun encode(command: FindCommand): Message = when (command) {
		is FindCommand.StartFind -> Message(
			path = START_FIND_PATH,
			payload = ByteBuffer.allocate(Int.SIZE_BYTES + 1)
				.putInt(command.sessionToken)
				.put(command.cueMode.ordinal.toByte())
				.array(),
		)

		is FindCommand.StopFind -> tokenMessage(STOP_FIND_PATH, command.sessionToken)
		is FindCommand.StartProximity -> tokenMessage(START_PROXIMITY_PATH, command.sessionToken)
		is FindCommand.StopProximity -> tokenMessage(STOP_PROXIMITY_PATH, command.sessionToken)
	}

	fun decode(path: String, payload: ByteArray): FindCommand? = when (path) {
		START_FIND_PATH -> decodeStartFind(payload)
		STOP_FIND_PATH -> decodeToken(payload)?.let(FindCommand::StopFind)
		START_PROXIMITY_PATH -> decodeToken(payload)?.let(FindCommand::StartProximity)
		STOP_PROXIMITY_PATH -> decodeToken(payload)?.let(FindCommand::StopProximity)
		else -> null
	}

	private fun tokenMessage(path: String, sessionToken: Int): Message = Message(
		path = path,
		payload = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(sessionToken).array(),
	)

	private fun decodeStartFind(payload: ByteArray): FindCommand.StartFind? {
		if (payload.size != Int.SIZE_BYTES + 1) return null
		val buffer = ByteBuffer.wrap(payload)
		val sessionToken = buffer.int
		val cueMode = CueMode.entries.getOrNull(buffer.get().toInt()) ?: return null
		return FindCommand.StartFind(sessionToken, cueMode)
	}

	private fun decodeToken(payload: ByteArray): Int? {
		if (payload.size != Int.SIZE_BYTES) return null
		return ByteBuffer.wrap(payload).int
	}
}
