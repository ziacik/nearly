package sk.ziacik.nearly.wear.proximity

import kotlinx.coroutines.flow.Flow

interface BleScanner {
	val isSupported: Boolean
	fun scan(sessionToken: Int): Flow<Int>
	suspend fun stop()
}
