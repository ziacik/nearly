package sk.ziacik.nearly.mobile.proximity

interface BleAdvertiser {
	val isSupported: Boolean
	suspend fun start(sessionToken: Int): Result<Unit>
	suspend fun stop()
}

class BleAdvertiseSession(
	private val advertiser: BleAdvertiser,
) {
	var activeToken: Int? = null
		private set

	suspend fun start(sessionToken: Int): Result<Unit> {
		if (activeToken == sessionToken) return Result.success(Unit)
		if (!advertiser.isSupported) {
			return Result.failure(UnsupportedOperationException("BLE advertising is unavailable"))
		}

		if (activeToken != null) advertiser.stop()
		activeToken = null
		return advertiser.start(sessionToken).onSuccess {
			activeToken = sessionToken
		}
	}

	suspend fun stop(sessionToken: Int? = activeToken) {
		if (sessionToken == null || sessionToken != activeToken) return
		advertiser.stop()
		activeToken = null
	}
}
