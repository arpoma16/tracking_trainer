package com.example.trianner4.di

import android.content.Context
import com.example.trianner4.data.local.AppDatabase
import com.example.trianner4.data.local.dao.AdaptationLogDao
import com.example.trianner4.data.local.dao.AssistiveLogDao
import com.example.trianner4.data.local.dao.BackupDao
import com.example.trianner4.data.local.dao.BodyZoneDao
import com.example.trianner4.data.local.dao.BiomechanicalChainDao
import com.example.trianner4.data.local.dao.ChainVariantDao
import com.example.trianner4.data.local.dao.DiscomfortDao
import com.example.trianner4.data.local.dao.ExerciseDao
import com.example.trianner4.data.local.dao.PlannedSessionDao
import com.example.trianner4.data.local.dao.ProgressionTriggerDao
import com.example.trianner4.data.local.dao.RoutineDao
import com.example.trianner4.data.local.dao.SessionDao
import com.example.trianner4.data.local.dao.SetLogDao
import com.example.trianner4.data.local.dao.StatusDao
import com.example.trianner4.data.local.dao.StreakStateDao
import com.example.trianner4.data.local.dao.TagDao
import com.example.trianner4.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()
    @Provides fun provideDiscomfortDao(db: AppDatabase): DiscomfortDao = db.discomfortDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
    @Provides fun provideStreakStateDao(db: AppDatabase): StreakStateDao = db.streakStateDao()
    @Provides fun provideStatusDao(db: AppDatabase): StatusDao = db.statusDao()
    @Provides fun provideSetLogDao(db: AppDatabase): SetLogDao = db.setLogDao()
    @Provides fun provideAssistiveLogDao(db: AppDatabase): AssistiveLogDao = db.assistiveLogDao()
    @Provides fun provideAdaptationLogDao(db: AppDatabase): AdaptationLogDao = db.adaptationLogDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
    @Provides fun provideChainVariantDao(db: AppDatabase): ChainVariantDao = db.chainVariantDao()
    @Provides fun provideProgressionTriggerDao(db: AppDatabase): ProgressionTriggerDao = db.progressionTriggerDao()
    @Provides fun providePlannedSessionDao(db: AppDatabase): PlannedSessionDao = db.plannedSessionDao()
    @Provides fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideBackupDao(db: AppDatabase): BackupDao = db.backupDao()
    @Provides fun provideBodyZoneDao(db: AppDatabase): BodyZoneDao = db.bodyZoneDao()
    @Provides fun provideBiomechanicalChainDao(db: AppDatabase): BiomechanicalChainDao = db.biomechanicalChainDao()
}