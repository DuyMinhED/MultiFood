package com.baonhutminh.multifood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Converters
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.PostImageEntity
import com.baonhutminh.multifood.data.model.PostLikeEntity
import com.baonhutminh.multifood.data.model.RestaurantEntity
import com.baonhutminh.multifood.data.model.UserProfile

@Database(
    entities = [
        UserProfile::class,
        PostEntity::class,
        Comment::class,
        PostLikeEntity::class,
        RestaurantEntity::class,
        PostImageEntity::class
    ],
    version = 12, // Tăng từ 11 lên 12 để phản ánh thay đổi schema và fix crash Room identity hash mismatch
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun postLikeDao(): PostLikeDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun postImageDao(): PostImageDao

    companion object {
        const val DATABASE_NAME = "multifood_db"
    }
}
