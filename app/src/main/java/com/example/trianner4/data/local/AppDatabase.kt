package com.example.trianner4.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trianner4.data.local.dao.AdaptationLogDao
import com.example.trianner4.data.local.dao.AssistiveLogDao
import com.example.trianner4.data.local.dao.BackupDao
import com.example.trianner4.data.local.dao.BiomechanicalChainDao
import com.example.trianner4.data.local.dao.BodyZoneDao
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
import com.example.trianner4.data.local.entity.AdaptationLogEntity
import com.example.trianner4.data.local.entity.AssistiveLogEntity
import com.example.trianner4.data.local.entity.BackupMetadataEntity
import com.example.trianner4.data.local.entity.BiomechanicalChainEntity
import com.example.trianner4.data.local.entity.BodyZoneEntity
import com.example.trianner4.data.local.entity.ChainVariantEntity
import com.example.trianner4.data.local.entity.DeloadCycleEntity
import com.example.trianner4.data.local.entity.DiscomfortEntity
import com.example.trianner4.data.local.entity.DiscomfortTagEntity
import com.example.trianner4.data.local.entity.ExerciseEntity
import com.example.trianner4.data.local.entity.ExerciseTagEntity
import com.example.trianner4.data.local.entity.FreezePeriodEntity
import com.example.trianner4.data.local.entity.GraduationCriterionEntity
import com.example.trianner4.data.local.entity.PlannedSessionEntity
import com.example.trianner4.data.local.entity.ProgressionTriggerEntity
import com.example.trianner4.data.local.entity.RoutineEntity
import com.example.trianner4.data.local.entity.RoutinePhaseExerciseEntity
import com.example.trianner4.data.local.entity.RoutineScheduleEntity
import com.example.trianner4.data.local.entity.SessionEntity
import com.example.trianner4.data.local.entity.SessionExerciseSnapshotEntity
import com.example.trianner4.data.local.entity.SetLogEntity
import com.example.trianner4.data.local.entity.StreakStateEntity
import com.example.trianner4.data.local.entity.TagEntity
import com.example.trianner4.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        RoutineEntity::class,
        RoutineScheduleEntity::class,
        BodyZoneEntity::class,
        BiomechanicalChainEntity::class,
        ExerciseEntity::class,
        ChainVariantEntity::class,
        GraduationCriterionEntity::class,
        RoutinePhaseExerciseEntity::class,
        TagEntity::class,
        ExerciseTagEntity::class,
        SessionEntity::class,
        SessionExerciseSnapshotEntity::class,
        SetLogEntity::class,
        AssistiveLogEntity::class,
        DiscomfortEntity::class,
        DiscomfortTagEntity::class,
        AdaptationLogEntity::class,
        StreakStateEntity::class,
        DeloadCycleEntity::class,
        FreezePeriodEntity::class,
        ProgressionTriggerEntity::class,
        BackupMetadataEntity::class,
        PlannedSessionEntity::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun backupDao(): BackupDao
    abstract fun bodyZoneDao(): BodyZoneDao
    abstract fun biomechanicalChainDao(): BiomechanicalChainDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun discomfortDao(): DiscomfortDao
    abstract fun routineDao(): RoutineDao
    abstract fun sessionDao(): SessionDao
    abstract fun setLogDao(): SetLogDao
    abstract fun assistiveLogDao(): AssistiveLogDao
    abstract fun adaptationLogDao(): AdaptationLogDao
    abstract fun streakStateDao(): StreakStateDao
    abstract fun tagDao(): TagDao
    abstract fun statusDao(): StatusDao
    abstract fun chainVariantDao(): ChainVariantDao
    abstract fun progressionTriggerDao(): ProgressionTriggerDao
    abstract fun plannedSessionDao(): PlannedSessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `planned_session` (
                        `id`              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `routineId`       INTEGER NOT NULL,
                        `plannedDate`     INTEGER NOT NULL,
                        `status`          TEXT    NOT NULL DEFAULT 'PENDING',
                        `originalDate`    INTEGER,
                        `linkedSessionId` INTEGER,
                        FOREIGN KEY(`routineId`) REFERENCES `routine`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_planned_session_routineId` " +
                    "ON `planned_session` (`routineId`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_planned_session_routineId_plannedDate` " +
                    "ON `planned_session` (`routineId`, `plannedDate`)"
                )
            }
        }

        // Añade columnas de configuración por defecto a exercise
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `exercise` ADD COLUMN `defaultSets`        INTEGER")
                db.execSQL("ALTER TABLE `exercise` ADD COLUMN `defaultReps`        INTEGER")
                db.execSQL("ALTER TABLE `exercise` ADD COLUMN `defaultRir`         INTEGER")
                db.execSQL("ALTER TABLE `exercise` ADD COLUMN `defaultDurationSec` INTEGER")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "training_app.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}

