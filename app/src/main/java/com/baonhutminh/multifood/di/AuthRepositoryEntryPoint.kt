package com.baonhutminh.multifood.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import com.baonhutminh.multifood.data.repository.AuthRepository

@EntryPoint
@InstallIn(ActivityComponent::class)
interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}





