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
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.countries_list.*
import layout.PreFilledMsgAdapter
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.Constants.countries
import saini.ayush.whatsdirectmessage.utils.DataManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CountriesViewAdapter.Interaction,
    PreFilledMsgAdapter.Interaction, View.OnClickListener {

    private lateinit var countriesViewAdapter: CountriesViewAdapter
    private lateinit var messagesAdapter: PreFilledMsgAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    @Inject
    lateinit var dataManager: DataManager

    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRV()
        initMessages()
        setCountryCode()
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                viewModel.setBottomSheetValue(newState)
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    hideKeyboard()
                }
            }
        })

        bottomSheetBehavior.state = viewModel.bottomSheetState

        cancel_button.setOnClickListener(this)
        done_button.setOnClickListener(this)
        country_flag.setOnClickListener(this)
        country_code.setOnClickListener(this)
        add_new_pre_filled_msg.setOnClickListener(this)
        send_whatsapp.setOnClickListener(this)

        hideKeyboard()

        phone.setText(viewModel.contact)
        message.setText(viewModel.message)

        phone.addTextChangedListener(object : TextWatcher {
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

        message.addTextChangedListener(object : TextWatcher {
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

    override fun onCountrySelected(position: Int, item: Country, imageView: ImageView) {
        viewModel.tempSelectedView?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.temp_selectedCountry = position
        imageView.visibility = View.VISIBLE
        viewModel.tempSelectedView = imageView
    }

    override fun onItemSelected(position: Int, item: Message) {
        message.setText(item.message)
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

            R.id.cancel_button -> hideBottomSheet()
            R.id.done_button -> onDoneClick()
            R.id.country_flag -> openBottomSheet()
            R.id.country_code -> openBottomSheet()
            R.id.add_new_pre_filled_msg -> newMsgItem()
            R.id.send_whatsapp -> sendMessage()

        }
    }

    private fun sendMessage() {

        val phoneNumberWithCountryCode = getContact()

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        String.format(
                            "https://api.whatsapp.com/send?phone=%s&text=%s",
                            phoneNumberWithCountryCode,
                            viewModel.message
                        )
                    )
                )
            )
        } catch (e: Exception) {

        }

    }

    private fun getContact(): String {
        return countries[viewModel.selectedCountry].dialCode + viewModel.contact
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
                preMessagesRV.scrollToPosition(0)
                viewModel.setListToState()

            } else showSnackbar("Maximum messages can be 10")
        }


    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(header_des, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun initMessages() {
        preMessagesRV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            messagesAdapter =
                PreFilledMsgAdapter(this@MainActivity)
            adapter = messagesAdapter
        }

        messagesAdapter.submitList(viewModel.list)

    }


    private fun setCountryCode() {
        country_flag.setImageDrawable(
            ContextCompat.getDrawable(
                baseContext,
                countries[viewModel.selectedCountry].flag
            )
        )

        country_code.text = countries[viewModel.selectedCountry].dialCode
    }

    private fun initRV() {

        countryListRV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            countriesViewAdapter =
                CountriesViewAdapter(this@MainActivity)
            adapter = countriesViewAdapter
        }

        countriesViewAdapter.submitList(countries)
    }

    private fun openBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        viewModel.setBottomSheetValue(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun onDoneClick() {
        hideBottomSheet()
        viewModel.onDoneCountry()
        setCountryCode()
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        viewModel.setBottomSheetValue(BottomSheetBehavior.STATE_COLLAPSED)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }


}


