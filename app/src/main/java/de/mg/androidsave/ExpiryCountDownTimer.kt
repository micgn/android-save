package de.mg.androidsave

import android.os.CountDownTimer
import android.widget.TextView
import de.mg.androidsave.model.InMemoryState

object ExpiryCountDownTimer {

    fun schedule(textView: TextView) {

        object : CountDownTimer(9999999999 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingMillis =
                    if (InMemoryState.startMillis == null)
                        0
                    else
                        maxOf(
                            InMemoryState.startMillis!! + Config.TIMEOUT_SECONDS * 1000L - System.currentTimeMillis(),
                            0
                        ) / 1000

                if (remainingMillis != 0L) {
                    val min = remainingMillis / 60
                    val secs = remainingMillis - (remainingMillis / 60) * 60
                    val secsLeadingZeros = if (secs < 10) "0$secs" else "$secs"
                    textView.text = "$min:$secsLeadingZeros"
                } else {
                    textView.text = "---"
                }
            }

            override fun onFinish() {
            }
        }.start()
    }
}