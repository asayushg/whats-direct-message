package saini.ayush.whatsdirectmessage.ui

import android.widget.ImageView
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import saini.ayush.whatsdirectmessage.utils.DataManager




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

    init {



    }

    fun onDoneCountry() {
        selectedCountry = temp_selectedCountry
        dataManager.updateCountryCode(selectedCountry)
    }

}