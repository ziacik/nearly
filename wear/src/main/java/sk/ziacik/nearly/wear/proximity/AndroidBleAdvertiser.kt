package sk.ziacik.nearly.wear.proximity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BleAdvertiseException(val errorCode: Int) : IllegalStateException("BLE advertise failed: $errorCode")

class AndroidBleAdvertiser(context: Context) : BleAdvertiser {
	private val bluetoothManager = context.applicationContext.getSystemService(BluetoothManager::class.java)
	private var activeAdvertiser: android.bluetooth.le.BluetoothLeAdvertiser? = null
	private var activeCallback: AdvertiseCallback? = null

	@get:SuppressLint("MissingPermission")
	override val isSupported: Boolean
		get() = runCatching {
			val adapter = bluetoothManager.adapter ?: return@runCatching false
			adapter.isEnabled && adapter.bluetoothLeAdvertiser != null
		}.getOrDefault(false)

	@SuppressLint("MissingPermission")
	override suspend fun start(sessionToken: Int): Result<Unit> = runCatching {
		stop()
		val adapter = bluetoothManager.adapter ?: error("Bluetooth adapter is unavailable")
		check(adapter.isEnabled) { "Bluetooth is disabled" }
		val advertiser = adapter.bluetoothLeAdvertiser ?: error("BLE advertising is unavailable")
		val data = AdvertiseData.Builder()
			.addServiceData(NEARLY_SERVICE_UUID, sessionTokenBytes(sessionToken))
			.setIncludeDeviceName(false)
			.setIncludeTxPowerLevel(false)
			.build()
		val settings = AdvertiseSettings.Builder()
			.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
			.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
			.setConnectable(false)
			.build()

		suspendCancellableCoroutine { continuation ->
			val callback = object : AdvertiseCallback() {
				override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
					activeAdvertiser = advertiser
					activeCallback = this
					if (continuation.isActive) continuation.resume(Unit)
				}

				override fun onStartFailure(errorCode: Int) {
					activeAdvertiser = null
					activeCallback = null
					if (continuation.isActive) continuation.resumeWithException(BleAdvertiseException(errorCode))
				}
			}

			continuation.invokeOnCancellation {
				runCatching { advertiser.stopAdvertising(callback) }
				if (activeCallback === callback) {
					activeAdvertiser = null
					activeCallback = null
				}
			}

			advertiser.startAdvertising(settings, data, callback)
		}
	}

	@SuppressLint("MissingPermission")
	override suspend fun stop() {
		val advertiser = activeAdvertiser
		val callback = activeCallback
		activeAdvertiser = null
		activeCallback = null
		if (advertiser != null && callback != null) {
			runCatching { advertiser.stopAdvertising(callback) }
		}
	}
}
