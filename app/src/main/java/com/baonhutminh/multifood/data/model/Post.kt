package com.baonhutminh.multifood.data.model

data class Post(

    val id: String,
    val title: String,
    val content: String,
    val images: List<String>,
    val rating: Float,
    val address: String,
    val comments: List<String>,
    val date: String,
    val author: String
)