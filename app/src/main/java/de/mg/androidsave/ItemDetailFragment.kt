package de.mg.androidsave

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.mg.androidsave.model.EntryModel
import de.mg.androidsave.model.InMemoryState
import de.mg.androidsave.util.EncryptionUtil
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*


class ItemDetailFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var entryText: EditText
    private lateinit var viewModel: ViewModel
    private var selectedEntry: EntryModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = this.run { ViewModelProviders.of(this).get(ViewModel::class.java) }
        viewModel.entries.observe(this, Observer<List<EntryModel>> {
            updateValue(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.item_detail, container, false)
        entryText = rootView.entryText
        return rootView
    }

    fun updateValue(entries: List<EntryModel>) {
        arguments?.let { arg ->
            if (arg.get(ARG_ITEM_ID) != null) {
                val param = arg.getString(ARG_ITEM_ID)
                selectedEntry = entries.firstOrNull { it.name == param }

                activity?.toolbar_layout?.title = " "
                activity?.detailName?.text = selectedEntry?.name
                entryText.setText(getText())

            } else if (arg.get(ARG_NEW_ENTRYNAME) != null) {
                val param = arg.getString(ARG_NEW_ENTRYNAME)
                activity?.toolbar_layout?.title = " "
                activity?.detailName?.text = param
            }
        }
    }

    private fun getText(): String =
        if (InMemoryState.getPassword() == null || selectedEntry == null) "encrypted"
        else EncryptionUtil.decrypt(
            InMemoryState.getPassword()!!,
            selectedEntry!!.entry,
            selectedEntry!!.hash
        ) ?: "wrong password"


    fun isNewEntry() = arguments?.get(ARG_NEW_ENTRYNAME) != null
    fun getEntryName(): CharSequence? = activity?.detailName?.text
    fun getEntryText(): EditText = entryText

    fun isValid() =
        getEntryName() != null && getEntryName()!!.isNotBlank() && getEntryText().text.isNotBlank()


    companion object {
        const val ARG_ITEM_ID = "item_id"
        const val ARG_NEW_ENTRYNAME = "entry_name"
    }
}
