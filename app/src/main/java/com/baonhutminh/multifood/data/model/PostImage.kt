package com.baonhutminh.multifood.data.model

/**
 * Đại diện cho một ảnh trong sub-collection "images" của một bài viết.
 * Đây là một DTO (Data Transfer Object).
 */
data class PostImage(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val order: Int = 0
)
