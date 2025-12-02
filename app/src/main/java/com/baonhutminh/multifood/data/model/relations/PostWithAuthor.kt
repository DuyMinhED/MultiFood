package com.baonhutminh.multifood.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.UserProfile

data class PostWithAuthor(
    @Embedded
    val post: PostEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val author: UserProfile
)
