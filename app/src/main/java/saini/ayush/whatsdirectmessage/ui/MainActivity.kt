package saini.ayush.whatsdirectmessage.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.countries_list.*
import layout.PreFilledMsgAdapter
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.Constants.countries


class MainActivity : AppCompatActivity(), CountriesViewAdapter.Interaction,
    PreFilledMsgAdapter.Interaction, View.OnClickListener {

    private lateinit var countriesViewAdapter: CountriesViewAdapter
    private lateinit var messagesAdapter: PreFilledMsgAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val viewModel: MainViewModel by viewModels()
    val list: MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setCountryCode()
        initRV()
        initMessages()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

                viewModel.bottomSheetState = newState
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
    }

    override fun onEditClicked(position: Int, item: Message) {
        list[position].editing = true
        messagesAdapter.notifyItemChanged(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)
    }

    override fun onRemoveClicked(position: Int, item: Message) {
        list.removeAt(position)
        messagesAdapter.notifyItemRemoved(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)

    }

    override fun onEditDone(position: Int, item: Message, newMsg: String) {
        if (newMsg.isNotEmpty()) {
            list[position].editing = false
            list[position].message = newMsg
            messagesAdapter.notifyItemChanged(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
        } else {
            showSnackbar("Please enter new message")
        }
    }

    override fun onEditCancel(position: Int) {
        list[position].editing = false
        messagesAdapter.notifyItemChanged(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)
    }

    override fun onNewDone(position: Int, newMsg: String) {
        if (newMsg.isNotEmpty()) {
            list[position].new = false
            list[position].message = newMsg
            messagesAdapter.notifyItemChanged(position)
            messagesAdapter.notifyItemRangeChanged(position, 1)
        } else {
            showSnackbar("Please enter new message")
        }
    }

    override fun onNewCancel(position: Int) {
        list.removeAt(position)
        messagesAdapter.notifyItemRemoved(position)
        messagesAdapter.notifyItemRangeChanged(position, 1)
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
                            message.text.toString()
                        )
                    )
                )
            )
        } catch (e: Exception) {

        }

    }

    private fun getContact(): String {
        return countries[viewModel.selectedCountry].dialCode + phone.text.toString()
    }

    private fun newMsgItem() {

        if (list.size < 10) {
            list.sortByDescending {
                it.id
            }
            list.add(
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

        } else showSnackbar("Maximum messages can be 10")


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

        messagesAdapter.submitList(getMessagesList())

    }

    private fun getMessagesList(): List<Message> {

        for (i in 1..10) {
            list.add(
                Message(
                    i,
                    "Hello Sir $i",
                    false,
                    false
                )
            )
        }

        list.sortByDescending {
            it.id
        }

        return list
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
        viewModel.bottomSheetState = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun onDoneClick() {
        hideBottomSheet()
        viewModel.onDoneCountry()
        setCountryCode()
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        viewModel.bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
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


