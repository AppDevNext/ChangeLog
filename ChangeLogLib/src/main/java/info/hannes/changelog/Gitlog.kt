package info.hannes.changelog

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Gitlog {

    @SerializedName("version")
    @Expose
    var version: String? = null
    @SerializedName("code")
    @Expose
    var code: String? = null
    @SerializedName("date")
    @Expose
    var date: String? = null
    @SerializedName("message")
    @Expose
    var message: String? = null

}