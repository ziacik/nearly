package sk.ziacik.nearly.wear.data

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import sk.ziacik.nearly.shared.FindProtocol

class NearlyWearableListenerService : WearableListenerService() {
	override fun onMessageReceived(messageEvent: MessageEvent) {
		val command = FindProtocol.decode(messageEvent.path, messageEvent.data) ?: return
		WearFindCommands.inbox.offer(command)
	}
}
