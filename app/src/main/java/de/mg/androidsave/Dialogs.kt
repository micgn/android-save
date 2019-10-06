package de.mg.androidsave

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import de.mg.androidsave.model.InMemoryState
import de.mg.androidsave.scheduled.CleanupScheduler
import de.mg.androidsave.server.CallServerWorkManager
import kotlinx.android.synthetic.main.dialog_changetitle.*
import kotlinx.android.synthetic.main.dialog_password.*
import kotlinx.android.synthetic.main.dialog_reallydelete.*
import kotlinx.android.synthetic.main.dialog_reallysave.*
import kotlinx.android.synthetic.main.dialog_serverpassword.*
import java.util.*


object Dialogs {

    fun reallyDelete(view: View, fragment: ItemDetailFragment) {
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_reallydelete, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()

        dialog.delete_okBtn.setOnClickListener {

            val confirmText = if (viewModel(fragment).delete(fragment.getEntryName().toString()))
                "Deleted..." else "Failed!"

            Snackbar.make(view, confirmText, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    val intent = Intent(view.context, MainActivity::class.java)
                    view.context.startActivity(intent)
                }
            }, 1000)
        }
        dialog.delete_cancelBtn.setOnClickListener { dialog.cancel() }
    }


    fun reallySave(view: View, fragment: ItemDetailFragment) {
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_reallysave, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()

        dialog.save_okBtn.setOnClickListener {

            val viewModel = viewModel(fragment)

            val confirmText = if (!fragment.isValid())
                "invalid..."
            else
                if (fragment.isNewEntry()) {
                    if (viewModel.insert(
                            fragment.getEntryName().toString(),
                            fragment.getEntryText().text.toString()
                        )
                    ) "Saved..." else "Failed..."
                } else {
                    if (viewModel.update(
                            fragment.getEntryName().toString(),
                            fragment.getEntryText().text.toString()
                        )
                    ) "Failed!" else "Failed..."
                }

            Snackbar.make(view, confirmText, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    val intent = Intent(view.context, MainActivity::class.java)
                    view.context.startActivity(intent)
                }
            }, 1000)
        }
        dialog.save_cancelBtn.setOnClickListener { dialog.cancel() }
    }

    fun changeTitle(view: View, fragment: ItemDetailFragment) {
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_changetitle, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()
        val oldName =
            if (fragment.getEntryName() != null) fragment.getEntryName().toString() else ""
        dialog.nameEditText.setText(oldName)

        dialog.title_okBtn.setOnClickListener {
            val newName = dialog.nameEditText.text.toString()
            dialog.cancel()
            if (newName.isNotBlank()) {
                fragment.arguments?.putString(ItemDetailFragment.ARG_ITEM_ID, newName)
                val confirmText = if (viewModel(fragment).changeName(
                        oldName,
                        newName,
                        fragment.getEntryText().text.toString()
                    )
                ) "Saved..." else "Failed!"
                Snackbar.make(view, confirmText, Snackbar.LENGTH_SHORT).setAction("Action", null)
                    .show()
            }
        }
        dialog.title_cancelBtn.setOnClickListener { dialog.cancel() }
    }


    fun newEntry(view: View) {
        if (InMemoryState.getPassword() == null)
            return
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_changetitle, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()

        dialog.title_okBtn.setOnClickListener {
            val newName = dialog.nameEditText.text.toString()
            dialog.cancel()
            if (newName.isNotBlank()) {
                val intent = Intent(view.context, ItemDetailActivity::class.java).apply {
                    putExtra(ItemDetailFragment.ARG_NEW_ENTRYNAME, newName)
                }
                view.context.startActivity(intent)
            }
        }
        dialog.title_cancelBtn.setOnClickListener { dialog.cancel() }
    }

    fun password(view: View, viewModel: ViewModel) {
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_password, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()

        dialog.password_cancelBtn.setOnClickListener {
            InMemoryState.setPassword(null)
            dialog.cancel()
        }
        dialog.password_okBtn.setOnClickListener {
            val pw = dialog.password.text.toString()
            if (pw.isNotBlank()) {
                if (viewModel.isClientPasswordCorrect(pw)) {
                    InMemoryState.setPassword(pw)
                    CleanupScheduler.schedule(view.context)
                    dialog.cancel()
                } else {
                    dialog.password.text.clear()
                    Snackbar.make(view, "wrong password", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun serverPassword(view: View, viewModel: ViewModel) {
        val layoutInflater = LayoutInflater.from(view.context)
        val promptView = layoutInflater.inflate(R.layout.dialog_serverpassword, null)
        val dialogBuilder = AlertDialog.Builder(view.context)
        dialogBuilder.setView(promptView)
        val dialog = dialogBuilder.create()
        dialog.show()

        dialog.serverpassword_okBtn.setOnClickListener {
            viewModel.setServerPassword(dialog.serverpassword.text.toString())
            dialog.cancel()
            CallServerWorkManager().getEntries(viewModel.getServerPassword()!!, view.context)
            Snackbar.make(view, "server password saved...", Snackbar.LENGTH_SHORT)
                .show()
        }
        dialog.serverpassword_cancelBtn.setOnClickListener {
            dialog.cancel()
        }
    }

    fun infoDialog(title: String, text: String, callback: () -> Unit, activity: Activity) {
        activity.runOnUiThread {
            val alertDialog = AlertDialog.Builder(activity).create()
            alertDialog.setTitle(title)
            alertDialog.setMessage(text)
            alertDialog.setButton(
                AlertDialog.BUTTON_NEUTRAL,
                "OK"
            ) { dialog, _ -> callback(); dialog.dismiss() }
            alertDialog.show()
        }
    }

    private fun viewModel(fragment: ItemDetailFragment) =
        ViewModelProviders.of(fragment).get(ViewModel::class.java)
}