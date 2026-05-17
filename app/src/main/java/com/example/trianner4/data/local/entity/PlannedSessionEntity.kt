package com.example.trianner4.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.trianner4.data.local.PlannedSessionStatus

/**
 * Materializes one recurrence of a routine schedule into a concrete trackable record.
 * One row per (routineId, plannedDate); the UNIQUE index enforces this.
 *
 * Status flow:
 *   PENDING → COMPLETED (session done on that day)
 *   PENDING → ROLLED_FORWARD (date passed without a session; replaced by next-day PENDING)
 *   PENDING → SKIPPED (user explicitly skipped)
 */
@Entity(
    tableName = "planned_session",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index(value = ["routineId", "plannedDate"], unique = true)
    ]
)
data class PlannedSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: Long,
    /** Epoch millis at midnight (start of the training day). */
    val plannedDate: Long,
    val status: PlannedSessionStatus = PlannedSessionStatus.PENDING,
    /** Non-null when this row was created by rolling an overdue session forward.
     *  Preserves the chain's original obligation date for auditing. */
    val originalDate: Long? = null,
    /** Set to the session.id once the planned session is fulfilled. */
    val linkedSessionId: Long? = null
)
