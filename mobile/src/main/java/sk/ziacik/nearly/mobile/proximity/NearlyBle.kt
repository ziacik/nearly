package sk.ziacik.nearly.mobile.proximity

import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.util.UUID

internal val NEARLY_SERVICE_UUID = ParcelUuid(UUID.fromString("8a8f4bd0-1b9e-4f47-9b7a-8f5729b3d341"))

internal fun sessionTokenBytes(sessionToken: Int): ByteArray =
	ByteBuffer.allocate(Int.SIZE_BYTES).putInt(sessionToken).array()
