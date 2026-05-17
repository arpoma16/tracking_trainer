package com.example.trianner4.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.trianner4.data.local.entity.*

@Dao
interface BackupDao {

    @Query("SELECT * FROM user_profile")
    suspend fun getAllUserProfiles(): List<UserProfileEntity>

    @Query("SELECT * FROM routine")
    suspend fun getAllRoutines(): List<RoutineEntity>

    @Query("SELECT * FROM routine_schedule")
    suspend fun getAllRoutineSchedules(): List<RoutineScheduleEntity>

    @Query("SELECT * FROM body_zone")
    suspend fun getAllBodyZones(): List<BodyZoneEntity>

    @Query("SELECT * FROM biomechanical_chain")
    suspend fun getAllBiomechanicalChains(): List<BiomechanicalChainEntity>

    @Query("SELECT * FROM exercise")
    suspend fun getAllExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM chain_variant")
    suspend fun getAllChainVariants(): List<ChainVariantEntity>

    @Query("SELECT * FROM graduation_criterion")
    suspend fun getAllGraduationCriteria(): List<GraduationCriterionEntity>

    @Query("SELECT * FROM routine_phase_exercise")
    suspend fun getAllRoutinePhaseExercises(): List<RoutinePhaseExerciseEntity>

    @Query("SELECT * FROM tag")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM exercise_tag")
    suspend fun getAllExerciseTags(): List<ExerciseTagEntity>

    @Query("SELECT * FROM session")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM session_exercise_snapshot")
    suspend fun getAllSessionExerciseSnapshots(): List<SessionExerciseSnapshotEntity>

    @Query("SELECT * FROM set_log")
    suspend fun getAllSetLogs(): List<SetLogEntity>

    @Query("SELECT * FROM assistive_log")
    suspend fun getAllAssistiveLogs(): List<AssistiveLogEntity>

    @Query("SELECT * FROM discomfort")
    suspend fun getAllDiscomforts(): List<DiscomfortEntity>

    @Query("SELECT * FROM discomfort_tag")
    suspend fun getAllDiscomfortTags(): List<DiscomfortTagEntity>

    @Query("SELECT * FROM adaptation_log")
    suspend fun getAllAdaptationLogs(): List<AdaptationLogEntity>

    @Query("SELECT * FROM streak_state")
    suspend fun getAllStreakStates(): List<StreakStateEntity>

    @Query("SELECT * FROM deload_cycle")
    suspend fun getAllDeloadCycles(): List<DeloadCycleEntity>

    @Query("SELECT * FROM freeze_period")
    suspend fun getAllFreezePeriods(): List<FreezePeriodEntity>

    @Query("SELECT * FROM progression_trigger")
    suspend fun getAllProgressionTriggers(): List<ProgressionTriggerEntity>

    @Query("SELECT * FROM backup_metadata")
    suspend fun getAllBackupMetadata(): List<BackupMetadataEntity>

    @Query("SELECT * FROM planned_session")
    suspend fun getAllPlannedSessions(): List<PlannedSessionEntity>
}
