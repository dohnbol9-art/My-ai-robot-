package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val userPrompt: String,
    val status: String, // IDLE, PLANNING, RUNNING, PAUSED_APPROVAL, PAUSED_SECURITY, COMPLETED, STOPPED, FAILED
    val currentStepIndex: Int,
    val totalSteps: Int,
    val currentStepTitle: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val thinkingProcess: String = "",
    val errorSummary: String? = null
)

@Entity(tableName = "task_steps")
data class TaskStepEntity(
    @PrimaryKey(autoGenerate = true) val stepId: Long = 0,
    val taskId: String,
    val stepIndex: Int,
    val title: String,
    val description: String,
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    val resultSummary: String? = null,
    val timestamp: Long
)

@Entity(tableName = "products")
data class ProductItemEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val sourcePlatform: String, // Temu, AliExpress
    val originalUrl: String,
    val affiliateTrackingUrl: String,
    val price: Double,
    val originalPrice: Double,
    val rating: Double,
    val reviewsCount: Int,
    val commissionRate: Double, // e.g. 0.15 for 15%
    val niche: String,
    val suitabilityScore: Int, // 1 to 100
    val imageUrl: String,
    val isVerified: Boolean = true
)

@Entity(tableName = "pin_drafts")
data class PinDraftEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val productId: String,
    val title: String,
    val description: String,
    val keywords: String,
    val hashtags: String,
    val altText: String,
    val boardName: String,
    val imageUrl: String,
    val localImageUri: String? = null,
    val affiliateUrl: String,
    val status: String, // DRAFT, PENDING_APPROVAL, APPROVED, PUBLISHED, FAILED
    val livePinUrl: String? = null,
    val publishedAt: Long? = null,
    val errorMessage: String? = null
)

@Entity(tableName = "robot_logs")
data class RobotLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val level: String, // INFO, WARN, ERROR, SECURITY, SUCCESS
    val serviceTag: String,
    val message: String
)
