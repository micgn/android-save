package de.mg.androidsave

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.mg.androidsave.model.InMemoryState
import kotlinx.android.synthetic.main.activity_item_detail.*


class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        setSupportActionBar(detail_toolbar)


        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it. For more information, see the Fragments API guide at:
        // http://developer.android.com/guide/components/fragments.html
        if (savedInstanceState == null) {
            val fragment = ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        ItemDetailFragment.ARG_ITEM_ID,
                        intent.getStringExtra(ItemDetailFragment.ARG_ITEM_ID)
                    )
                    putString(
                        ItemDetailFragment.ARG_NEW_ENTRYNAME,
                        intent.getStringExtra(ItemDetailFragment.ARG_NEW_ENTRYNAME)
                    )
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commit()

            deleteBtn.setOnClickListener { Dialogs.reallyDelete(it, fragment) }

            saveBtn.setOnClickListener { Dialogs.reallySave(it, fragment) }

            editTitleBtn.setOnClickListener { Dialogs.changeTitle(it, fragment) }

            editCancelBtn.setOnClickListener { view ->
                val intent = Intent(view.context, MainActivity::class.java)
                view.context.startActivity(intent)
            }

            ExpiryCountDownTimer.schedule(detailCounterText)

            InMemoryState.observePassword(this) { password ->
                if (password == null) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
                    applicationContext.startActivity(intent)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                navigateUpTo(Intent(this, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
