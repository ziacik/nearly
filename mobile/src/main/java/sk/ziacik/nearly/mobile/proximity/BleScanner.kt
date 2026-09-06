package sk.ziacik.nearly.mobile.proximity

import kotlinx.coroutines.flow.Flow

interface BleScanner {
	val isSupported: Boolean
	fun scan(sessionToken: Int): Flow<Int>
	suspend fun stop()
}
