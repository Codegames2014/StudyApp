package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyVaultDao {

    @Query("SELECT * FROM study_vaults WHERE studentProfileId = :profileId ORDER BY updatedAt DESC")
    fun getVaultsForProfile(profileId: String): Flow<List<StudyVaultEntity>>

    @Query("SELECT * FROM study_vaults WHERE vaultId = :vaultId LIMIT 1")
    suspend fun getVaultById(vaultId: String): StudyVaultEntity?

    @Query("SELECT * FROM vault_items WHERE vaultId = :vaultId ORDER BY isPinned DESC, createdAt DESC")
    fun getItemsForVault(vaultId: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE studentProfileId = :profileId ORDER BY isPinned DESC, createdAt DESC")
    fun getAllItemsForProfile(profileId: String): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: StudyVaultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity)

    @Update
    suspend fun updateVault(vault: StudyVaultEntity)

    @Delete
    suspend fun deleteVault(vault: StudyVaultEntity)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE vaultId = :vaultId")
    suspend fun deleteItemsForVault(vaultId: String)
}
