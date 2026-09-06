package sk.ziacik.nearly.wear.proximity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BleScanException(val errorCode: Int) : IllegalStateException("BLE scan failed: $errorCode")

class AndroidBleScanner(context: Context) : BleScanner {
	private val bluetoothManager = context.applicationContext.getSystemService(BluetoothManager::class.java)
	private val lock = Any()
	private var activeClose: (() -> Unit)? = null

	@get:SuppressLint("MissingPermission")
	override val isSupported: Boolean
		get() = runCatching {
			val adapter = bluetoothManager.adapter ?: return@runCatching false
			adapter.isEnabled && adapter.bluetoothLeScanner != null
		}.getOrDefault(false)

	@SuppressLint("MissingPermission")
	override fun scan(sessionToken: Int): Flow<Int> = callbackFlow {
		val adapter = bluetoothManager.adapter
		if (adapter == null) {
			close(IllegalStateException("Bluetooth adapter is unavailable"))
			return@callbackFlow
		}
		if (!adapter.isEnabled) {
			close(IllegalStateException("Bluetooth is disabled"))
			return@callbackFlow
		}
		val scanner = adapter.bluetoothLeScanner
		if (scanner == null) {
			close(UnsupportedOperationException("BLE scanning is unavailable"))
			return@callbackFlow
		}

		val callback = object : ScanCallback() {
			override fun onScanResult(callbackType: Int, result: ScanResult) {
				trySend(result.rssi)
			}

			override fun onBatchScanResults(results: MutableList<ScanResult>) {
				results.forEach { result -> trySend(result.rssi) }
			}

			override fun onScanFailed(errorCode: Int) {
				close(BleScanException(errorCode))
			}
		}
		val filter = ScanFilter.Builder()
			.setServiceData(NEARLY_SERVICE_UUID, sessionTokenBytes(sessionToken))
			.build()
		val settings = ScanSettings.Builder()
			.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
			.build()

		val closeFlow = { close(); Unit }
		val previous = synchronized(lock) {
			val old = activeClose
			activeClose = closeFlow
			old
		}
		previous?.invoke()

		try {
			scanner.startScan(listOf(filter), settings, callback)
		} catch (error: Throwable) {
			synchronized(lock) {
				if (activeClose === closeFlow) activeClose = null
			}
			close(error)
		}

		awaitClose {
			runCatching { scanner.stopScan(callback) }
			synchronized(lock) {
				if (activeClose === closeFlow) activeClose = null
			}
		}
	}

	override suspend fun stop() {
		val close = synchronized(lock) {
			activeClose.also { activeClose = null }
		}
		close?.invoke()
	}
}
