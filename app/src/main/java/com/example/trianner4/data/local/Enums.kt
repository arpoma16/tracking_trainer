package com.example.trianner4.data.local

enum class UnitSystem { KG, LB }

enum class ScheduleType { WEEKDAYS, EVERY_N_DAYS, CUSTOM }

enum class Phase { PRE, CORE, POST }

enum class ExerciseType { STRENGTH, ASSISTIVE }

enum class TrackingMode { WEIGHT_REPS, BAND_TENSION, TIMER, FIXED_REPS }

enum class SessionStatus { ACTIVE, COMPLETED, PARTIAL, SKIPPED, ADAPTED, DELOAD }

enum class AdaptationActionType { LOAD_REDUCTION, SUBSTITUTION, INJECTION_PRE, INJECTION_POST }

enum class ProgressionMode { LOAD, MECHANICAL, BOTH }

enum class TagCategory { PATTERN, BODY, GOAL, JOINT, MUSCLE }

enum class PlannedSessionStatus { PENDING, COMPLETED, ROLLED_FORWARD, SKIPPED }
