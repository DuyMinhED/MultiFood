package com.baonhutminh.multifood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.baonhutminh.multifood.data.model.Comment
import com.baonhutminh.multifood.data.model.Converters
import com.baonhutminh.multifood.data.model.PostEntity
import com.baonhutminh.multifood.data.model.UserProfile

@Database(
    entities = [UserProfile::class, PostEntity::class, Comment::class],
    version = 5, // <-- Đã tăng phiên bản lên 5
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao

    companion object {
        const val DATABASE_NAME = "multifood_db"
    }
}