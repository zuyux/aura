package io.aura.android.domain.repository

import io.aura.android.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
}
