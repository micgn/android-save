package de.mg.androidsave

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executors
import kotlin.system.exitProcess

class InitialActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)
        authenticate(this)
    }

    private fun authenticate(activity: AppCompatActivity) {

        val prompt = BiometricPrompt(
            activity,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        exitApp()
                    } else {
                        if (Config.DEV_MODE) {
                            val intent = Intent(activity, MainActivity::class.java)
                            activity.startActivity(intent)
                        } else {
                            Dialogs.infoDialog(
                                "Fingerprint Authentication",
                                "An unrecoverable error occurred. Exiting...",
                                {
                                    exitApp()
                                },
                                activity
                            )
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val intent = Intent(activity, MainActivity::class.java)
                    activity.startActivity(intent)
                }

            })

        val promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Save")
            .setSubtitle("fingerprint needed")
            //.setDescription("This is the description")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)
    }
}
