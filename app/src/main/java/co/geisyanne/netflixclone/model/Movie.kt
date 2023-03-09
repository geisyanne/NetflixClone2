package co.geisyanne.netflixclone.model


data class Movie(
    val id: Int,
    val coverUrl: String,
    val title: String = "",
    val desc: String = "",
    val cast: String = ""
    )
