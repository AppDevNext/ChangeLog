package info.hannes.changelog

import kotlinx.serialization.Serializable

@Serializable
data class Gitlog(
    var version: String? = null,
    var code: Int? = 0,
    var date: String? = null,
    var message: String? = null
)
