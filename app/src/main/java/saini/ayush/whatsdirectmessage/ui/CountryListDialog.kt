package saini.ayush.whatsdirectmessage.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.utils.Constants

class CountryListDialog : BottomSheetDialogFragment() {

    lateinit var listener: CountriesViewAdapter.Interaction
    private lateinit var countriesViewAdapter: CountriesViewAdapter
    private var countriesList = Constants.countries

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout =  inflater.inflate(R.layout.countries_list, container, false)
        return layout
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as CountriesViewAdapter.Interaction
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view.parent as View).setBackgroundColor(Color.TRANSPARENT)
        initRV()
        setClickListeners()
    }

    private fun setClickListeners() {

        layout.findViewById<SearchView>(R.id.searchBar).setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                countriesViewAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countriesViewAdapter.filter.filter(newText)
                return false
            }

        })
    }

    private fun initRV() {

        layout.findViewById<RecyclerView>(R.id.countryListRV).apply {
            layoutManager = LinearLayoutManager(requireContext())
            countriesViewAdapter =
                CountriesViewAdapter(listener)
            adapter = countriesViewAdapter
        }

        countriesViewAdapter.submitList(countriesList)

    }

}