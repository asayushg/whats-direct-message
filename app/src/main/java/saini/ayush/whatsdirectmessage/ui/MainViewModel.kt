package saini.ayush.whatsdirectmessage.ui

import android.widget.ImageView
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.DataManager


const val SAVE_STATE_MESSAGE = "ayush.whats.state.message"
const val SAVE_STATE_CONTACT = "ayush.whats.state.contact"
const val SAVE_STATE_SHEET = "ayush.whats.state.sheet"
const val SAVE_STATE_LOCK = "ayush.whats.state.lock"
const val SAVE_STATE_MESSAGE_LIST = "ayush.whats.state.message.list"


class MainViewModel
@ViewModelInject
constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    var dataManager: DataManager
) : ViewModel() {


    // get from savedState
    var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    var selectedCountry = dataManager.getCountryCode()
    var temp_selectedCountry = dataManager.getCountryCode()
    var tempSelectedView: ImageView? = null
    var lock = false
    var message: String = ""
    var contact: String = ""
    val list: MutableList<Message> = mutableListOf()

    init {

        savedStateHandle.get<String>(SAVE_STATE_MESSAGE)?.let {
            message = it
        }

        savedStateHandle.get<String>(SAVE_STATE_CONTACT)?.let {
            contact = it
        }

        savedStateHandle.get<Int>(SAVE_STATE_SHEET)?.let {
            bottomSheetState = it
        }

        savedStateHandle.get<Boolean>(SAVE_STATE_LOCK)?.let {
            lock = it
        }


        savedStateHandle.get<List<Message>>(SAVE_STATE_MESSAGE_LIST)?.let {
            list.addAll(it)
        }

        if (list.isEmpty()) list.addAll(dataManager.getMessages())

    }

    fun onDoneCountry() {
        selectedCountry = temp_selectedCountry
        dataManager.updateCountryCode(selectedCountry)
    }

    fun setMessageValue(msg: String) {
        message = msg
        savedStateHandle.set(SAVE_STATE_MESSAGE, msg)
    }

    fun setContactValue(contact: String) {
        this.contact = contact
        savedStateHandle.set(SAVE_STATE_CONTACT, contact)
    }

    fun setBottomSheetValue(int: Int) {
        bottomSheetState = int
        savedStateHandle.set(SAVE_STATE_SHEET, int)
    }

    fun setListToState() {
        savedStateHandle.set(SAVE_STATE_MESSAGE_LIST, list)
    }

    fun setLockValue(b: Boolean) {
        lock = b
        savedStateHandle.set(SAVE_STATE_LOCK, b)
    }

}