package com.baonhutminh.multifood.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val idFavoritePost: List<String>
)
