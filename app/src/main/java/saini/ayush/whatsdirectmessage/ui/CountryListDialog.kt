package saini.ayush.whatsdirectmessage.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.countries_list.*
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.utils.Constants

class CountryListDialog : BottomSheetDialogFragment() {

    lateinit var listener: CountriesViewAdapter.Interaction
    private lateinit var countriesViewAdapter: CountriesViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.countries_list, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as CountriesViewAdapter.Interaction
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view?.parent as View).setBackgroundColor(Color.TRANSPARENT)
        initRV()
    }
    private fun initRV() {

        countryListRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            countriesViewAdapter =
                CountriesViewAdapter(listener)
            adapter = countriesViewAdapter
        }

        countriesViewAdapter.submitList(Constants.countries)
    }

}