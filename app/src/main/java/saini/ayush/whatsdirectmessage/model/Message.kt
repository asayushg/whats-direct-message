package saini.ayush.whatsdirectmessage.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Message(
    var id: Int,
    var message: String,
    var editing: Boolean,
    var new: Boolean
) : Parcelable