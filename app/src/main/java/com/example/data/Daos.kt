package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY startedAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY startedAt DESC LIMIT 1")
    fun getLatestTask(): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    // Task Steps
    @Query("SELECT * FROM task_steps WHERE taskId = :taskId ORDER BY stepIndex ASC")
    fun getStepsForTask(taskId: String): Flow<List<TaskStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: TaskStepEntity)

    @Query("UPDATE task_steps SET status = :status, resultSummary = :summary WHERE taskId = :taskId AND stepIndex = :stepIndex")
    suspend fun updateStepStatus(taskId: String, stepIndex: Int, status: String, summary: String?)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY suitabilityScore DESC")
    fun getAllProducts(): Flow<List<ProductItemEntity>>

    @Query("SELECT * FROM products WHERE taskId = :taskId")
    suspend fun getProductsForTask(taskId: String): List<ProductItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductItemEntity>)

    @Query("DELETE FROM products")
    suspend fun clearProducts()
}

@Dao
interface PinDraftDao {
    @Query("SELECT * FROM pin_drafts ORDER BY rowid DESC")
    fun getAllPinDrafts(): Flow<List<PinDraftEntity>>

    @Query("SELECT * FROM pin_drafts WHERE status = 'PENDING_APPROVAL'")
    fun getPendingApprovalPins(): Flow<List<PinDraftEntity>>

    @Query("SELECT * FROM pin_drafts WHERE status = 'PUBLISHED' AND publishedAt >= :sinceTimestamp")
    suspend fun getPublishedPinsSince(sinceTimestamp: Long): List<PinDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinDrafts(drafts: List<PinDraftEntity>)

    @Update
    suspend fun updatePinDraft(draft: PinDraftEntity)

    @Query("UPDATE pin_drafts SET status = :status WHERE id = :id")
    suspend fun updatePinStatus(id: String, status: String)

    @Query("UPDATE pin_drafts SET status = 'APPROVED' WHERE status = 'PENDING_APPROVAL'")
    suspend fun approveAllPending()

    @Query("DELETE FROM pin_drafts")
    suspend fun clearPinDrafts()
}

@Dao
interface RobotLogDao {
    @Query("SELECT * FROM robot_logs ORDER BY id DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<RobotLogEntity>>

    @Insert
    suspend fun insertLog(log: RobotLogEntity)

    @Query("DELETE FROM robot_logs")
    suspend fun clearLogs()
}
