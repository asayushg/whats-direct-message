package saini.ayush.whatsdirectmessage.ui

import android.widget.ImageView
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior


class MainViewModel
@ViewModelInject
constructor(

) : ViewModel() {


    // get from savedState
    var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
    var selectedCountry = 20
    var temp_selectedCountry = 0
    var tempSelectedView: ImageView? = null

    fun onDoneCountry() {
        selectedCountry = temp_selectedCountry
        // todo save to pref
    }

}