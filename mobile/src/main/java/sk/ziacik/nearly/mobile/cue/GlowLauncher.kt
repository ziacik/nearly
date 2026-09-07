package sk.ziacik.nearly.mobile.cue

import android.content.Context
import android.content.Intent
import sk.ziacik.nearly.mobile.MainActivity

fun interface GlowLauncher {
	fun show()
}

class AndroidGlowLauncher(context: Context) : GlowLauncher {
	private val applicationContext = context.applicationContext

	override fun show() {
		val intent = Intent(applicationContext, MainActivity::class.java)
			.addFlags(
				Intent.FLAG_ACTIVITY_NEW_TASK or
					Intent.FLAG_ACTIVITY_SINGLE_TOP or
					Intent.FLAG_ACTIVITY_CLEAR_TOP,
			)
		runCatching { applicationContext.startActivity(intent) }
	}
}
