package saini.ayush.whatsdirectmessage.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.CallLogEntry


class CallLogsViewAdapter(
    private val interaction: Interaction? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var callLogsList = listOf<CallLogEntry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("@AYUSH", "onCreateViewHolder: ")
        return CallLogItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.call_log_list_item,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("@AYUSH", "onBindViewHolder: $holder $position")
        when (holder) {
            is CallLogItemViewHolder -> {
                holder.bind(callLogsList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return callLogsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<CallLogEntry>) {
        callLogsList = list
        notifyDataSetChanged()
    }

    class CallLogItemViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: CallLogEntry) = with(itemView) {
            Log.d("@AYUSH", "Binding: ${item.number}, ${item.type}")
            setOnClickListener {
                interaction?.onContactSelected(adapterPosition, item)
            }
            val details = item.date + "  \u2022  " + item.type
            findViewById<TextView>(R.id.tv_details).text = details
            val number = item.number
            findViewById<TextView>(R.id.tv_number) .text = number
        }
    }

    interface Interaction {
        fun onContactSelected(position: Int, item: CallLogEntry)
    }
}