package sk.ziacik.nearly.wear.data

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.launch
import sk.ziacik.nearly.shared.FindProtocol
import sk.ziacik.nearly.wear.NearlyWearApplication

class NearlyWearableListenerService : WearableListenerService() {
	override fun onMessageReceived(messageEvent: MessageEvent) {
		val command = FindProtocol.decode(messageEvent.path, messageEvent.data) ?: return
		val nearly = application as? NearlyWearApplication ?: return
		nearly.applicationScope.launch {
			nearly.targetController.handle(command)
		}
	}
}
