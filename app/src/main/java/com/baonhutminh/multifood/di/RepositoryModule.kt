package com.baonhutminh.multifood.di

import com.baonhutminh.multifood.data.repository.AuthRepositoryImpl
import com.baonhutminh.multifood.data.repository.CommentRepositoryImpl
import com.baonhutminh.multifood.data.repository.ReviewRepositoryImpl
import com.baonhutminh.multifood.data.repository.UserRepositoryImpl
import com.baonhutminh.multifood.domain.repository.AuthRepository
import com.baonhutminh.multifood.domain.repository.CommentRepository
import com.baonhutminh.multifood.domain.repository.ReviewRepository
import com.baonhutminh.multifood.domain.repository.UserRepository
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
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}

