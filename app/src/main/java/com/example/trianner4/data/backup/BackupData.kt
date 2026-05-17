package com.example.trianner4.data.backup

import com.example.trianner4.data.local.entity.*

data class BackupData(
    val userProfiles: List<UserProfileEntity>,
    val routines: List<RoutineEntity>,
    val routineSchedules: List<RoutineScheduleEntity>,
    val bodyZones: List<BodyZoneEntity>,
    val biomechanicalChains: List<BiomechanicalChainEntity>,
    val exercises: List<ExerciseEntity>,
    val chainVariants: List<ChainVariantEntity>,
    val graduationCriteria: List<GraduationCriterionEntity>,
    val routinePhaseExercises: List<RoutinePhaseExerciseEntity>,
    val tags: List<TagEntity>,
    val exerciseTags: List<ExerciseTagEntity>,
    val sessions: List<SessionEntity>,
    val sessionExerciseSnapshots: List<SessionExerciseSnapshotEntity>,
    val setLogs: List<SetLogEntity>,
    val assistiveLogs: List<AssistiveLogEntity>,
    val discomforts: List<DiscomfortEntity>,
    val discomfortTags: List<DiscomfortTagEntity>,
    val adaptationLogs: List<AdaptationLogEntity>,
    val streakStates: List<StreakStateEntity>,
    val deloadCycles: List<DeloadCycleEntity>,
    val freezePeriods: List<FreezePeriodEntity>,
    val progressionTriggers: List<ProgressionTriggerEntity>,
    val backupMetadata: List<BackupMetadataEntity>,
    val plannedSessions: List<PlannedSessionEntity>
)
