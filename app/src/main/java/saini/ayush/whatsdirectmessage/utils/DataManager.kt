package saini.ayush.whatsdirectmessage.utils

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import saini.ayush.whatsdirectmessage.di.Data
import saini.ayush.whatsdirectmessage.model.Message
import saini.ayush.whatsdirectmessage.utils.Constants.COUNTRY_CODE
import saini.ayush.whatsdirectmessage.utils.Constants.MESSAGE_LIST
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DataManager
@Inject
constructor(
    @Data private val pref: SharedPreferences,
    @Data private val prefEditor: SharedPreferences.Editor
) {


    fun updateMessages(list: List<Message>) {

        if (list.isNotEmpty()) {
            val gson = Gson()
            val json: String = gson.toJson(list)
            prefEditor.putString(MESSAGE_LIST, json).apply()
        }

    }

    fun getMessages(): List<Message> {
        val gson = Gson()
        val json: String? = pref.getString(MESSAGE_LIST, null)
        return if (json != null) {
            val type: Type = object : TypeToken<List<Message?>?>() {}.type
            gson.fromJson(json, type)
        } else {
            getMessagesList()
        }
    }

    private fun getMessagesList(): List<Message> {

        val list = mutableListOf<Message>()
        val msgList = mutableListOf<String>()
        msgList.add("Hello!")
        msgList.add("Hey, Ayush Here")
        msgList.add("These are the prefilled messages. Tap to set this as message.")
        msgList.add("You can edit, delete and add new custom messages")
        msgList.add("Maximum 10 messages can be saved")

        for (i in 0 until msgList.size) {
            list.add(
                Message(
                    id = msgList.size - i,
                    message = msgList[i],
                    editing = false,
                    new = false
                )
            )
        }

        list.sortByDescending {
            it.id
        }

        return list
    }

    fun updateCountryCode(int: Int) {

        prefEditor.putInt(COUNTRY_CODE, int).apply()

    }

    fun getCountryCode(): Int {

        return pref.getInt(COUNTRY_CODE, 0)

    }

}