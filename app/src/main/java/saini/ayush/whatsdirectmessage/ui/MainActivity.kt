package saini.ayush.whatsdirectmessage.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import saini.ayush.whatsdirectmessage.model.CallLogEntry
import saini.ayush.whatsdirectmessage.model.Country
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.DataManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), CountriesViewAdapter.Interaction,
    PreFilledMsgAdapter.Interaction, View.OnClickListener, CallLogsViewAdapter.Interaction {

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

        findViewById<ImageView>(R.id.phone_logs).setOnClickListener{
            showRecentCalls()
        }

        findViewById<TextView>(R.id.phone_logs_title).setOnClickListener {
            showRecentCalls()
        }

    }

    private fun showRecentCalls() {
        val callLogPermission = Manifest.permission.READ_CALL_LOG

        if (ContextCompat.checkSelfPermission(this, callLogPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(callLogPermission), 101)
        } else {
            val logs = accessCallLogs()
            CallLogsDialog(
                interaction = this,
                callLogs = logs
            ).show(supportFragmentManager, "TAG")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val logs = accessCallLogs()
            CallLogsDialog(
                interaction = this,
                callLogs = logs
            ).show(supportFragmentManager, "TAG")
        } else {
            // Handle permission denial
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
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


    @SuppressLint("Range") // Suppress warnings about deprecated getColumnIndexOrThrow
    private fun accessCallLogs(): List<CallLogEntry> {
        val callLogs = mutableListOf<CallLogEntry>()
        val callLogUri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls.NUMBER,       // Phone number
            CallLog.Calls.TYPE,         // Call type
            CallLog.Calls.DATE,         // Call date
            CallLog.Calls.DURATION      // Call duration
        )

        val cursor = contentResolver.query(
            callLogUri,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC" // Sort by most recent calls
        )

        cursor?.use {
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val typeCode = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                val duration = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                // Convert typeCode to a readable string
                val type = when (typeCode) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    CallLog.Calls.REJECTED_TYPE -> "Rejected"
                    else -> "Unknown"
                }

                // Format the date and duration
                val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(date))
                val formattedDuration = "$duration sec"

                // Add to list
                callLogs.add(CallLogEntry(number, type, formattedDate, formattedDuration))
            }
        }
        Log.d("@AYUSH", "accessCallLogs: $callLogs")
        return callLogs
    }

    override fun onContactSelected(position: Int, item: CallLogEntry) {
        findViewById<EditText>(R.id.phone).setText(item.number)
    }


}


