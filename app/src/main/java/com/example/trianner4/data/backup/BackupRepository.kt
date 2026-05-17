package com.example.trianner4.data.backup

import com.example.trianner4.data.local.dao.BackupDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val backupDao: BackupDao
) {
    suspend fun getFullBackupData(): BackupData {
        return BackupData(
            userProfiles = backupDao.getAllUserProfiles(),
            routines = backupDao.getAllRoutines(),
            routineSchedules = backupDao.getAllRoutineSchedules(),
            bodyZones = backupDao.getAllBodyZones(),
            biomechanicalChains = backupDao.getAllBiomechanicalChains(),
            exercises = backupDao.getAllExercises(),
            chainVariants = backupDao.getAllChainVariants(),
            graduationCriteria = backupDao.getAllGraduationCriteria(),
            routinePhaseExercises = backupDao.getAllRoutinePhaseExercises(),
            tags = backupDao.getAllTags(),
            exerciseTags = backupDao.getAllExerciseTags(),
            sessions = backupDao.getAllSessions(),
            sessionExerciseSnapshots = backupDao.getAllSessionExerciseSnapshots(),
            setLogs = backupDao.getAllSetLogs(),
            assistiveLogs = backupDao.getAllAssistiveLogs(),
            discomforts = backupDao.getAllDiscomforts(),
            discomfortTags = backupDao.getAllDiscomfortTags(),
            adaptationLogs = backupDao.getAllAdaptationLogs(),
            streakStates = backupDao.getAllStreakStates(),
            deloadCycles = backupDao.getAllDeloadCycles(),
            freezePeriods = backupDao.getAllFreezePeriods(),
            progressionTriggers = backupDao.getAllProgressionTriggers(),
            backupMetadata = backupDao.getAllBackupMetadata(),
            plannedSessions = backupDao.getAllPlannedSessions()
        )
    }
}
