package com.baonhutminh.multifood.di

import com.baonhutminh.multifood.data.repository.AuthRepositoryImpl
import com.baonhutminh.multifood.data.repository.CommentRepositoryImpl
import com.baonhutminh.multifood.data.repository.PostRepositoryImpl
import com.baonhutminh.multifood.data.repository.ProfileRepositoryImpl
import com.baonhutminh.multifood.data.repository.UserRepositoryImpl
import com.baonhutminh.multifood.data.repository.AuthRepository
import com.baonhutminh.multifood.data.repository.CommentRepository
import com.baonhutminh.multifood.data.repository.PostRepository
import com.baonhutminh.multifood.data.repository.ProfileRepository
import com.baonhutminh.multifood.data.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}