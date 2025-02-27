package layout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import saini.ayush.whatsdirectmessage.R
import saini.ayush.whatsdirectmessage.model.Message

class PreFilledMsgAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MSG = 0
        private const val TYPE_MSG_EDIT = 1
        private const val TYPE_MSG_NEW = 2
    }

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {

        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_MSG -> {
                MessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_prefilled_msg,
                        parent,
                        false
                    ),
                    interaction
                )
            }

            TYPE_MSG_EDIT -> {
                EditMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_prefilled_msg_edit,
                        parent,
                        false
                    ),
                    interaction
                )
            }

            TYPE_MSG_NEW -> {
                NewMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_prefilled_msg_new,
                        parent,
                        false
                    ),
                    interaction
                )
            }

            else -> MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_prefilled_msg,
                    parent,
                    false
                ),
                interaction
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MessageViewHolder -> {
                holder.bind(differ.currentList[position])
            }
            is EditMessageViewHolder -> {
                holder.bind(differ.currentList[position])
            }

            is NewMessageViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Message>) {
        differ.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            differ.currentList[position].editing -> TYPE_MSG_EDIT
            differ.currentList[position].new -> TYPE_MSG_NEW
            else -> TYPE_MSG
        }
    }


    class MessageViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Message) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            findViewById<MaterialTextView>(R.id.msg).text = item.message
            findViewById<ImageView>(R.id.edit).setOnClickListener {
                interaction?.onEditClicked(adapterPosition, item)
            }

            findViewById<ImageView>(R.id.remove).setOnClickListener {
                interaction?.onRemoveClicked(adapterPosition, item)
            }

        }
    }

    class EditMessageViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Message) = with(itemView) {

            val msgEdit= findViewById<EditText>(R.id.msg_edit)
            msgEdit.setText(item.message)

            findViewById<ImageView>(R.id.okay).setOnClickListener {
                interaction?.onEditDone(adapterPosition, item, msgEdit.text.toString())
            }

            findViewById<ImageView>(R.id.cancel).setOnClickListener {
                interaction?.onEditCancel(adapterPosition)
            }

        }
    }

    class NewMessageViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Message) = with(itemView) {

            val msgNew = findViewById<EditText>(R.id.msg_new)
            msgNew.setText(item.message)

            findViewById<ImageView>(R.id.new_okay).setOnClickListener {
                interaction?.onNewDone(adapterPosition, msgNew.text.toString())
            }

            findViewById<ImageView>(R.id.new_cancel).setOnClickListener {
                interaction?.onNewCancel(adapterPosition)
            }

        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Message)
        fun onEditClicked(position: Int, item: Message)
        fun onRemoveClicked(position: Int, item: Message)
        fun onEditDone(position: Int, item: Message, newMsg: String)
        fun onEditCancel(position: Int)
        fun onNewDone(position: Int, newMsg: String)
        fun onNewCancel(position: Int)

    }
}
