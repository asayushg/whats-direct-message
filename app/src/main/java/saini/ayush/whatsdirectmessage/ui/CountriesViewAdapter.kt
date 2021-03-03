package saini.ayush.whatsdirectmessage.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country

class CountriesViewAdapter(
    private val interaction: Interaction? = null,
    private val selected: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Country>() {

        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return CountryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.country_list_item,
                parent,
                false
            ),
            interaction,
            selected
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

    fun submitList(list: List<Country>) {
        differ.submitList(list)
    }

    class CountryViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val selected: Int
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Country) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onCountrySelected(adapterPosition, item)
            }

            itemView.findViewById<TextView>(R.id.country_name).text = item.name
            itemView.findViewById<TextView>(R.id.country_code).text = item.dialCode
            itemView.findViewById<ImageView>(R.id.country_flag)
                .setImageDrawable(ContextCompat.getDrawable(context, item.flag))
            val selectedView = itemView.findViewById<ImageView>(R.id.country_selected)
            if (selected == adapterPosition) selectedView.visibility = View.VISIBLE
            else selectedView.visibility = View.INVISIBLE
        }
    }

    interface Interaction {
        fun onCountrySelected(position: Int, item: Country)
    }
}
