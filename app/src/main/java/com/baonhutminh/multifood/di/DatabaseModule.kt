package com.baonhutminh.multifood.di

import android.content.Context
import androidx.room.Room
import com.baonhutminh.multifood.data.local.AppDatabase
import com.baonhutminh.multifood.data.local.CommentDao
import com.baonhutminh.multifood.data.local.CommentLikeDao
import com.baonhutminh.multifood.data.local.PostDao
import com.baonhutminh.multifood.data.local.PostImageDao
import com.baonhutminh.multifood.data.local.PostLikeDao
import com.baonhutminh.multifood.data.local.RestaurantDao
import com.baonhutminh.multifood.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        // Sử dụng giải pháp này để tự động tạo lại DB khi có thay đổi cấu trúc
        // trong giai đoạn phát triển. Sẽ thay bằng migration thủ công khi phát hành.
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun providePostDao(appDatabase: AppDatabase): PostDao {
        return appDatabase.postDao()
    }

    @Provides
    fun provideCommentDao(appDatabase: AppDatabase): CommentDao {
        return appDatabase.commentDao()
    }

    @Provides
    fun provideCommentLikeDao(appDatabase: AppDatabase): CommentLikeDao {
        return appDatabase.commentLikeDao()
    }

    @Provides
    fun providePostLikeDao(appDatabase: AppDatabase): PostLikeDao {
        return appDatabase.postLikeDao()
    }

    @Provides
    fun provideRestaurantDao(appDatabase: AppDatabase): RestaurantDao {
        return appDatabase.restaurantDao()
    }

    @Provides
    fun providePostImageDao(appDatabase: AppDatabase): PostImageDao {
        return appDatabase.postImageDao()
    }
}
