package saini.ayush.whatsdirectmessage.model

import java.io.Serializable

data class Country(

    val code:String,
    val name:String,
    val dialCode:String,
    val flag:Int

): Serializable