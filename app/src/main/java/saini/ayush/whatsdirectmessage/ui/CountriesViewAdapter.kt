package saini.ayush.whatsdirectmessage.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.country_list_item.view.*
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Country

class CountriesViewAdapter(
    private val interaction: Interaction? = null
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

    fun submitList(list: List<Country>) {
        differ.submitList(list)
    }

    class CountryViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Country) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onCountrySelected(adapterPosition, item, country_selected)
            }

            country_name.text = item.name
            country_code.text = item.dialCode
            country_flag.setImageDrawable(ContextCompat.getDrawable(context, item.flag))
            country_selected.visibility = View.INVISIBLE
        }
    }

    interface Interaction {
        fun onCountrySelected(position: Int, item: Country, imageView: ImageView)
    }
}
