package de.mg.androidsave.scheduled

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.mg.androidsave.model.InMemoryState


class CleanupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        InMemoryState.setPassword(null)
    }
}