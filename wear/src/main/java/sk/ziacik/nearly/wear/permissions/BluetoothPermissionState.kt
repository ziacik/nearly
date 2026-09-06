package sk.ziacik.nearly.wear.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

data class BluetoothPermissionState(
	val canScan: Boolean,
	val canAdvertise: Boolean,
	val canConnect: Boolean,
	val missingRuntimePermissions: List<String>,
)

fun bluetoothPermissionState(context: Context): BluetoothPermissionState {
	fun granted(permission: String) = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		val scan = granted(Manifest.permission.BLUETOOTH_SCAN)
		val advertise = granted(Manifest.permission.BLUETOOTH_ADVERTISE)
		val connect = granted(Manifest.permission.BLUETOOTH_CONNECT)
		BluetoothPermissionState(
			canScan = scan,
			canAdvertise = advertise,
			canConnect = connect,
			missingRuntimePermissions = buildList {
				if (!scan) add(Manifest.permission.BLUETOOTH_SCAN)
				if (!advertise) add(Manifest.permission.BLUETOOTH_ADVERTISE)
				if (!connect) add(Manifest.permission.BLUETOOTH_CONNECT)
			},
		)
	} else {
		val location = granted(Manifest.permission.ACCESS_FINE_LOCATION)
		BluetoothPermissionState(
			canScan = location,
			canAdvertise = true,
			canConnect = true,
			missingRuntimePermissions = if (location) emptyList() else listOf(Manifest.permission.ACCESS_FINE_LOCATION),
		)
	}
}
