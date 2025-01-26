package saini.ayush.whatsdirectmessage.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country

class CountriesViewAdapter(
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Country>() {

        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)
    private var countriesList = listOf<Country>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return CountryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.country_list_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CountryViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getFilter(): Filter {
        var countriesFilteredList: List<Country>
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                countriesFilteredList = if (charString.isEmpty()) countriesList else {
                    val filteredList = ArrayList<Country>()
                    countriesList
                        .filter {
                            (it.name.lowercase().contains(constraint!!.toString().trim().lowercase())) or
                                    (it.code.lowercase().contains(constraint.toString().trim().lowercase()))

                        }
                        .forEach { filteredList.add(it) }
                    filteredList

                }
                return FilterResults().apply { values = countriesFilteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                countriesFilteredList = if (results?.values == null)
                    ArrayList()
                else
                    results.values as List<Country>
                differ.submitList(countriesFilteredList)
            }
        }
    }

    fun submitList(list: List<Country>) {
        countriesList = list
        differ.submitList(list)
    }

    class CountryViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Country) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onCountrySelected(adapterPosition, item)
            }

            itemView.findViewById<TextView>(R.id.country_name).text = item.name
            itemView.findViewById<TextView>(R.id.country_code) .text = item.dialCode
            itemView.findViewById<ImageView>(R.id.country_flag)
                .setImageDrawable(ContextCompat.getDrawable(context, item.flag))
        }
    }

    interface Interaction {
        fun onCountrySelected(position: Int, item: Country)
    }
}
