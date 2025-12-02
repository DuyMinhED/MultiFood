package com.baonhutminh.multifood.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.UserProfile

data class CommentWithAuthor(
    @Embedded
    val comment: Comment,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val author: UserProfile
)
