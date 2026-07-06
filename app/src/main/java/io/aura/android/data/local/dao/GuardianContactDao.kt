package io.aura.android.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.aura.android.data.local.entity.GuardianContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianContactDao {
    @Query("SELECT * FROM guardian_contacts ORDER BY isPrimary DESC, displayName ASC")
    fun observeContacts(): Flow<List<GuardianContactEntity>>

    @Query("SELECT * FROM guardian_contacts ORDER BY isPrimary DESC, displayName ASC")
    suspend fun getContacts(): List<GuardianContactEntity>

    @Query("SELECT * FROM guardian_contacts WHERE id = :contactId")
    suspend fun getContact(contactId: String): GuardianContactEntity?

    @Query("DELETE FROM guardian_contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)

    @Upsert
    suspend fun upsert(contact: GuardianContactEntity)
}
