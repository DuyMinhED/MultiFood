package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Đại diện cho một ảnh trong bài viết được lưu trữ cục bộ trong Room.
 * dbId là primary key tự động tăng, postId là foreign key đến bảng posts.
 */
@Entity(tableName = "post_images")
data class PostImageEntity(
    @PrimaryKey(autoGenerate = true)
    val dbId: Long = 0,
    val postId: String = "",
    val url: String = "",
    val order: Int = 0
)







