package saini.ayush.whatsdirectmessage.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country
import saini.ayush.whatsdirectmessage.utils.Constants.countries

class MainActivity : AppCompatActivity(), CountriesViewAdapter.Interaction {


    private lateinit var countriesViewAdapter: CountriesViewAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottomSheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                 //   initRV()
                }


            }
        })


    }

    override fun onCountrySelected(position: Int, item: Country) {

    }

    private fun initRV() {
        val lists = findViewById<ConstraintLayout>(R.id.lists)
        val listRV = lists.findViewById<RecyclerView>(R.id.countryListRV)

        listRV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            countriesViewAdapter = CountriesViewAdapter(this@MainActivity, 0)
            adapter = countriesViewAdapter
        }

        countriesViewAdapter.submitList(countries)
    }

}


