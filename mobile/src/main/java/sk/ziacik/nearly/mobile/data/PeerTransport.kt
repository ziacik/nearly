package sk.ziacik.nearly.mobile.data

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import sk.ziacik.nearly.shared.FindCommand
import sk.ziacik.nearly.shared.FindProtocol

data class PeerCandidate(
	val id: String,
	val isNearby: Boolean,
)

fun selectNearbyNodeId(peers: List<PeerCandidate>): String? = peers.firstOrNull(PeerCandidate::isNearby)?.id

class FindCommandInbox {
	private val mutableCommands = MutableSharedFlow<FindCommand>(extraBufferCapacity = 16)
	val commands = mutableCommands.asSharedFlow()

	fun offer(command: FindCommand): Boolean = mutableCommands.tryEmit(command)
}

object MobileFindCommands {
	val inbox = FindCommandInbox()
}

class WearPeerTransport(context: Context) {
	private val nodeClient = Wearable.getNodeClient(context.applicationContext)
	private val messageClient = Wearable.getMessageClient(context.applicationContext)

	suspend fun send(command: FindCommand): Boolean {
		val peers = nodeClient.connectedNodes.await().map { node ->
			PeerCandidate(id = node.id, isNearby = node.isNearby)
		}
		val nodeId = selectNearbyNodeId(peers) ?: return false
		val message = FindProtocol.encode(command)
		messageClient.sendMessage(nodeId, message.path, message.payload).await()
		return true
	}
}
