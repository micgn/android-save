package de.mg.androidsave

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.InMemoryState
import de.mg.androidsave.server.CallServerWorkManager
import de.mg.androidsave.util.Utils.textWatcher
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModel


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        viewModel = this.run { ViewModelProviders.of(this).get(ViewModel::class.java) }

        addButton.setOnClickListener { view -> Dialogs.newEntry(view) }
        safe.setOnClickListener { view -> Dialogs.password(view, viewModel) }

        clearSearch.setOnClickListener { searchField.setText("") }

        setupRecyclerView(item_list)

        viewModel.filteredEntries.observe(this, Observer<List<EntryModel>> {
            (item_list.adapter as SimpleItemRecyclerViewAdapter).updateValue(it)
        })

        searchField.addTextChangedListener(textWatcher { editable ->
            viewModel.search.value = editable.toString()
            viewModel.updateFilteredEntries()
        })

        registerForContextMenu(onlineStatus)

        viewModel.online.observeForever { model ->
            if (model?.online == true) {
                onlineStatus.setImageResource(R.drawable.online)
                onlineStatus.setBackgroundResource(R.color.colorOnline)
            } else {
                onlineStatus.setImageResource(R.drawable.offline)
                onlineStatus.setBackgroundResource(R.color.colorOffline)
            }
        }

        viewModel.getServerPassword()?.let { CallServerWorkManager().getEntries(it, this) }

        setBackground(InMemoryState.getPassword())
        InMemoryState.observePassword(this) { password ->
            setBackground(password)
        }

        // TODO cancel needed?
        ExpiryCountDownTimer.schedule(counterText)
    }


    private fun setBackground(password: String?) {
        if (password != null)
            mainListId.setBackgroundColor(
                resources.getColor(
                    R.color.colorBackground,
                    null
                )
            )
        else
            mainListId.setBackgroundColor(
                resources.getColor(
                    R.color.colorBackgroundLocked,
                    null
                )
            )
    }

    var view: View? = null

    @SuppressLint("ResourceType")
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.layout.menu, menu)
        menu.setHeaderTitle("SAVE")
        this.view = v
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_password -> Dialogs.serverPassword(view!!, viewModel)
            R.id.work_status -> Dialogs.infoDialog(
                "WorkManager Status",
                CallServerWorkManager().getQueueInfo().orEmpty(),
                {},
                this
            )
            R.id.cancel_work -> {
                CallServerWorkManager().cancelAll()
                Snackbar.make(view!!, "canceled...", Snackbar.LENGTH_SHORT).show()
            }
            else -> return false
        }
        return true
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(viewModel.entries.value.orEmpty())
    }

    class SimpleItemRecyclerViewAdapter(var values: List<EntryModel>) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { view ->

                if (InMemoryState.getPassword() != null) {
                    val item = view.tag as EntryModel

                    val intent = Intent(view.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.name)
                    }
                    view.context.startActivity(intent)
                }
            }
        }

        fun updateValue(values: List<EntryModel>) {
            this.values = values
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.nameView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameView: TextView = view.itemListText
        }
    }
}
