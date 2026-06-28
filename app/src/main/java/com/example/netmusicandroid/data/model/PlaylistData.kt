package com.example.netmusicandroid.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistData<T>(
    val list: List<T>
)