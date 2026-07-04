package io.aura.android.data.repository

import io.aura.android.data.local.dao.UserProfileDao
import io.aura.android.data.mapper.toDomain
import io.aura.android.data.mapper.toEntity
import io.aura.android.domain.model.UserProfile
import io.aura.android.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstUserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
) : UserProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> =
        userProfileDao.observeProfile().map { profile -> profile?.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        userProfileDao.upsert(profile.toEntity())
    }
}
