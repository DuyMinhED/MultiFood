package com.baonhutminh.multifood.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.PostImageEntity
import com.baonhutminh.multifood.data.model.UserProfile

/**
 * Relation bao gồm Post, Author và Images để hiển thị đầy đủ thông tin
 */
data class PostWithAuthorAndImages(
    @Embedded
    val post: PostEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val author: UserProfile?,

    @Relation(
        parentColumn = "id",
        entityColumn = "postId"
    )
    val images: List<PostImageEntity>
)







