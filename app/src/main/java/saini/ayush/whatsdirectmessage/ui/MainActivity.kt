package saini.ayush.whatsdirectmessage.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import layout.PreFilledMsgAdapter
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.DataManager
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CountriesViewAdapter.Interaction,
    PreFilledMsgAdapter.Interaction, View.OnClickListener {

    private lateinit var messagesAdapter: PreFilledMsgAdapter

    @Inject
    lateinit var dataManager: DataManager

    private val viewModel: MainViewModel by viewModels()
    private lateinit var bottomSheet: CountryListDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMessages()
        setCountryCode()
        setListeners()
    }

    private fun setListeners() {
        findViewById<ImageView>(R.id.country_flag).setOnClickListener(this)
        findViewById<TextView>(R.id.country_code).setOnClickListener(this)
        findViewById<MaterialTextView>(R.id. add_new_pre_filled_msg).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.send_whatsapp).setOnClickListener(this)

        hideKeyboard()

        findViewById<EditText>(R.id.phone).setText(viewModel.contact)
        findViewById<TextInputEditText>(R.id.message).setText(viewModel.message)

        findViewById<EditText>(R.id.phone).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotBlank()) {
                        viewModel.setContactValue(it.toString())
                    } else {
                        viewModel.setContactValue("")
                    }
                }
            }
        })

        findViewById<TextInputEditText>(R.id.message).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.isNotBlank()) {
                        viewModel.setMessageValue(it.toString())
                    } else {
                        viewModel.setMessageValue("")
                    }
                }
            }
        })
    }

    override fun onCountrySelected(position: Int, item: Country) {
        viewModel.temp_selectedCountry = item
        onDoneClick()
    }

    override fun onItemSelected(position: Int, item: Message) {
        findViewById<TextInputEditText>(R.id.message).setText(item.message)
        viewModel.setMessageValue(item.message)
    }

    override fun onEditClicked(position: Int, item: Message) {
        if (!viewModel.lock) {
            viewModel.setLockValue(true)
            viewModel.list[position].editing = true
            messagesAdapter.notifyItemChanged(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
            viewModel.setListToState()
        }
    }

    override fun onRemoveClicked(position: Int, item: Message) {
        if (!viewModel.lock) {
            viewModel.list.removeAt(position)
            messagesAdapter.notifyItemRemoved(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
            dataManager.updateMessages(viewModel.list)
            viewModel.setListToState()
        }

    }

    override fun onEditDone(position: Int, item: Message, newMsg: String) {
        if (newMsg.isNotEmpty()) {
            viewModel.list[position].editing = false
            viewModel.list[position].message = newMsg
            messagesAdapter.notifyItemChanged(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
            viewModel.setLockValue(false)
            dataManager.updateMessages(viewModel.list)
            viewModel.setListToState()
        } else {
            showSnackbar("Please enter new message")
        }
    }

    override fun onEditCancel(position: Int) {
        viewModel.list[position].editing = false
        messagesAdapter.notifyItemChanged(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)
        viewModel.setLockValue(false)
        viewModel.setListToState()
    }

    override fun onNewDone(position: Int, newMsg: String) {
        if (newMsg.isNotEmpty()) {
            viewModel.list[position].new = false
            viewModel.list[position].message = newMsg
            messagesAdapter.notifyItemChanged(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
            viewModel.setLockValue(false)
            dataManager.updateMessages(viewModel.list)
            viewModel.setListToState()
        } else {
            showSnackbar("Please enter new message")
        }
    }

    override fun onNewCancel(position: Int) {
        viewModel.list.removeAt(position)
        messagesAdapter.notifyItemRemoved(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)
        viewModel.setLockValue(false)
        viewModel.setListToState()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.country_flag -> openBottomSheet()
            R.id.country_code -> openBottomSheet()
            R.id.add_new_pre_filled_msg -> newMsgItem()
            R.id.send_whatsapp -> sendMessage()

        }
    }

    private fun sendMessage() {

        val phoneNumberWithCountryCode = getContact()
        if (validatePhoneNumber()) {
            Snackbar.make(findViewById(R.id.message), "Please enter mobile number", Snackbar.LENGTH_LONG).show()
        } else {
            val uri = Uri.parse(
                String.format(
                    "https://api.whatsapp.com/send?phone=%s&text=%s",
                    phoneNumberWithCountryCode,
                    viewModel.message
                )
            )
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    uri
                )
                intent.setPackage("com.whatsapp")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    )
                )
            }
        }

    }

    private fun validatePhoneNumber(): Boolean {
        return viewModel.contact.isEmpty()
    }

    private fun getContact(): String {
        return viewModel.selectedCountry.dialCode + viewModel.contact
    }

    private fun newMsgItem() {
        if (!viewModel.lock) {
            viewModel.setLockValue(true)
            if (viewModel.list.size < 10) {
                viewModel.list.sortByDescending {
                    it.id
                }
                viewModel.list.add(
                    0,
                    Message(
                        id = 0,
                        message = "",
                        editing = false,
                        new = true
                    )
                )

                messagesAdapter.notifyItemInserted(0)
                messagesAdapter.notifyItemRangeInserted(0, 0)
                findViewById<RecyclerView>(R.id.preMessagesRV).scrollToPosition(0)
                viewModel.setListToState()

            } else showSnackbar("Maximum messages can be 10")
        }


    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById<TextInputEditText>(R.id.message), msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun initMessages() {
        findViewById<RecyclerView>(R.id.preMessagesRV).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            messagesAdapter =
                PreFilledMsgAdapter(this@MainActivity)
            adapter = messagesAdapter
        }

        messagesAdapter.submitList(viewModel.list)

    }


    private fun setCountryCode() {
        findViewById<ImageView>(R.id.country_flag).setImageDrawable(
            ContextCompat.getDrawable(
                baseContext,
                viewModel.selectedCountry.flag
            )
        )

        findViewById<TextView>(R.id.country_code).text = viewModel.selectedCountry.dialCode
    }

    private fun openBottomSheet() {
        bottomSheet = CountryListDialog()
        bottomSheet.show(supportFragmentManager, "TAG")
    }

    private fun onDoneClick() {
        viewModel.onDoneCountry()
        setCountryCode()
        bottomSheet.dismiss()
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }


}


