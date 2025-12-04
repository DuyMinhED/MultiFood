package com.baonhutminh.multifood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey(autoGenerate = true)
    val dbId: Int = 0,
    val followerId: String,  // Người theo dõi
    val followingId: String  // Người được theo dõi
)

