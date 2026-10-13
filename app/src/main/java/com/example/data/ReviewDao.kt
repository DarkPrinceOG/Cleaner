package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_items WHERE decision = 'DELETE' ORDER BY decidedAt DESC")
    fun getMarkedForDeletion(): Flow<List<ReviewItemEntity>>

    @Query("SELECT * FROM review_items ORDER BY decidedAt DESC")
    fun getAllReviewed(): Flow<List<ReviewItemEntity>>

    @Query("SELECT * FROM review_items WHERE uri = :uri LIMIT 1")
    suspend fun getDecisionForUri(uri: String): ReviewItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ReviewItemEntity)

    @Query("DELETE FROM review_items WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM review_items WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)

    @Query("DELETE FROM review_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM review_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM review_items WHERE decision = 'DELETE'")
    fun getMarkedDeleteCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM review_items WHERE decision = 'DELETE'")
    fun getMarkedDeleteTotalBytes(): Flow<Long>

    @Query("SELECT COUNT(*) FROM review_items")
    fun getTotalReviewedCount(): Flow<Int>

    @Query("SELECT uri FROM review_items")
    suspend fun getAllReviewedUris(): List<String>

    @Query("SELECT uri FROM review_items")
    fun observeAllReviewedUris(): Flow<List<String>>

    @Query("SELECT mediaId FROM review_items")
    suspend fun getAllReviewedMediaIds(): List<Long>

    /**
     * Resets KEEP and SKIP review history for a specific category.
     * CRITICAL SAFETY: NEVER deletes rows with decision = 'DELETE'.
     * Preserves pending deletions staged in the deletion queue.
     */
    @Query("DELETE FROM review_items WHERE (category = :categoryId OR :categoryId = 'all_media') AND decision IN ('KEEP', 'SKIP')")
    suspend fun resetKeepSkipHistoryForCategory(categoryId: String): Int

    /**
     * Resets KEEP and SKIP review history across all categories.
     * CRITICAL SAFETY: NEVER deletes rows with decision = 'DELETE'.
     * Preserves pending deletions staged in the deletion queue.
     */
    @Query("DELETE FROM review_items WHERE decision IN ('KEEP', 'SKIP')")
    suspend fun resetAllKeepSkipHistory(): Int

    @Query("SELECT COUNT(*) FROM review_items WHERE decision IN ('KEEP', 'SKIP')")
    suspend fun getCountOfKeepSkipAll(): Int

    @Query("SELECT COUNT(*) FROM review_items WHERE (category = :categoryId OR :categoryId = 'all_media') AND decision IN ('KEEP', 'SKIP')")
    suspend fun getCountOfKeepSkipForCategory(categoryId: String): Int

    @Query("SELECT COUNT(*) FROM review_items WHERE (category = :categoryId OR :categoryId = 'all_media') AND decision = 'DELETE'")
    suspend fun getCountOfPendingDeleteForCategory(categoryId: String): Int
}
