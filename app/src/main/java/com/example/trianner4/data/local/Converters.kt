package com.example.trianner4.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter fun unitSystemToString(v: UnitSystem): String = v.name
    @TypeConverter fun stringToUnitSystem(v: String): UnitSystem = UnitSystem.valueOf(v)

    @TypeConverter fun scheduleTypeToString(v: ScheduleType): String = v.name
    @TypeConverter fun stringToScheduleType(v: String): ScheduleType = ScheduleType.valueOf(v)

    @TypeConverter fun phaseToString(v: Phase): String = v.name
    @TypeConverter fun stringToPhase(v: String): Phase = Phase.valueOf(v)

    @TypeConverter fun exerciseTypeToString(v: ExerciseType): String = v.name
    @TypeConverter fun stringToExerciseType(v: String): ExerciseType = ExerciseType.valueOf(v)

    @TypeConverter fun trackingModeToString(v: TrackingMode): String = v.name
    @TypeConverter fun stringToTrackingMode(v: String): TrackingMode = TrackingMode.valueOf(v)

    @TypeConverter fun sessionStatusToString(v: SessionStatus): String = v.name
    @TypeConverter fun stringToSessionStatus(v: String): SessionStatus = SessionStatus.valueOf(v)

    @TypeConverter fun adaptationActionTypeToString(v: AdaptationActionType): String = v.name
    @TypeConverter fun stringToAdaptationActionType(v: String): AdaptationActionType = AdaptationActionType.valueOf(v)

    @TypeConverter fun progressionModeToString(v: ProgressionMode): String = v.name
    @TypeConverter fun stringToProgressionMode(v: String): ProgressionMode = ProgressionMode.valueOf(v)

    @TypeConverter fun tagCategoryToString(v: TagCategory): String = v.name
    @TypeConverter fun stringToTagCategory(v: String): TagCategory = TagCategory.valueOf(v)

    @TypeConverter fun plannedSessionStatusToString(v: PlannedSessionStatus): String = v.name
    @TypeConverter fun stringToPlannedSessionStatus(v: String): PlannedSessionStatus = PlannedSessionStatus.valueOf(v)
}
