# CLAUDE.md — App Android de Entrenamiento de Fuerza

**Persistencia:** 100% local (Room/SQLite). Sin backend remoto. **Plataforma:** Android nativo.
> **Arquitectura completa → [`ARQUITECTURA.md`](./ARQUITECTURA.md)** (leer antes de implementar).

## Stack

| Capa | Tecnología |
|---|---|
| UI | Jetpack Compose |
| Arquitectura | MVVM + Repository |
| BD | Room (SQLite) |
| Background | WorkManager |
| Prefs | DataStore |
| Widget | Glance |
| Lenguaje | Kotlin |

## Estructura — 5 tabs

1. **Hoy** — Dashboard + CTA "Iniciar Sesión"
2. **Calendario** — Vista mensual con código de colores
3. **Rutinas** — CRUD rutinas/ejercicios
4. **Bienestar** — Molestias y condiciones crónicas
5. **Ajustes** — Perfil, backup, preferencias

## Reglas de Negocio Clave

- **Sesión:** flujo secuencial PRE → NÚCLEO → POST.
- **STRENGTH:** registra peso/series/reps/RIR. Alimenta motor de sobrecarga.
- **ASSISTIVE:** temporizador o reps fijas. Métrica = cumplimiento (check).
- **Adaptación:** `DiscomfortTag` ↔ `ExerciseTag` → reduce carga, sustituye/inyecta ejercicios. Persiste `AdaptationLog`.
- **Cadenas biomecánicas:** jerarquía de variantes; promoción requiere `GraduationCriterion`.
- **Inmutabilidad:** `SessionExerciseSnapshot`, `SetLog`, `AssistiveLog` no editables tras cerrar sesión.

## Entidades Room (25 + 1 = 26) — DB v2

`UserProfile`(1) · `Routine` · `RoutineSchedule` · `RoutinePhaseExercise` · `Exercise`(STRENGTH|ASSISTIVE) · `BiomechanicalChain` · `ChainVariant` · `GraduationCriterion` · `Tag` · `ExerciseTag` · `Session` · `SessionExerciseSnapshot` · `SetLog` · `AssistiveLog` · `BodyZone` · `Discomfort` · `DiscomfortTag` · `AdaptationLog` · `StreakState`(1) · `DeloadCycle` · `FreezePeriod` · `ProgressionTrigger` · `BackupMetadata` · **`PlannedSession`**

**`PlannedSession`** (`planned_session`) — materializa cada ocurrencia de `RoutineSchedule` en un registro rastreable.
- `id`, `routineId` FK→routine CASCADE, `plannedDate` (epoch ms medianoche)
- `status: PlannedSessionStatus` — `PENDING | COMPLETED | ROLLED_FORWARD | SKIPPED`
- `originalDate?` — fecha raíz de la cadena si fue roll-forward; `linkedSessionId?` — FK lógica a `session`
- UNIQUE `(routineId, plannedDate)`

## Convenciones

- Unidades: `kg`/`lb` según `UserProfile.unitSystem`.
- Strings: siempre por clave de localización, nunca hardcoded.
- Accesibilidad: color codes + icono/etiqueta (TalkBack). Inputs numéricos accesibles.
- RIR color: 0–1 rojo · 2–3 verde · 4+ ámbar.

## Grafo de Navegación

```
NavHost (startDestination = hoy_graph)
├── hoy_graph        → TodayScreen  (onStartSession: (routineId) → sesionActiva(routineId))
├── calendario_graph → CalendarioScreen / CalendarioDiaScreen
├── rutinas_graph    → RutinasScreen / RutinaEditorScreen / BibliotecaEjerciciosScreen / EjercicioDetalleScreen
├── bienestar_graph  → BienestarScreen
├── ajustes_graph    → AjustesScreen / PerfilScreen / TriggersProgresoScreen / BackupRestoreScreen
└── sesion/activa/{routineId}  ← top-level modal, sin tab graph → bottom bar oculto
    └── SesionActivaScreen (BackHandler + diálogo de confirmación)
```

- Bottom bar: oculto si ningún ancestro pertenece a un `TabGraph`.
- Back stack aislado por tab (`saveState`/`restoreState`).
- Rutas centralizadas en `NavRoutes.kt`; grafos en `TabGraph`.

## Estado del Proyecto

### ✅ Completado

- **Modelo de datos:** 26 entidades Room + DAOs + `AppDatabase` v2 + `Converters`.
- **Navegación base:** `AppNavigation` con 5 tabs + modal sesión activa.
- **Pantalla Hoy (`TodayScreen`):** estado reactivo, banner de adaptación, bottom sheet, CTA funcional.
- **Pantalla Calendario:** grid mensual navegable + `CalendarioDiaScreen` (snapshot read-only).
- **Pantalla Rutinas:** lista + FAB + `RutinaEditorScreen` + `BibliotecaEjerciciosScreen`.
- **Pantalla Bienestar:** estructura base (`BienestarScreen`).
- **Pantalla Ajustes:** secciones + sub-pantallas (Perfil, Triggers, Backup).
- **`TodayRepository` + `AdaptationResolver`:** lógica de adaptación por molestia.
- **`SesionActivaViewModel` + `SesionActivaScreen`:** sesión completa, timers, persistencia, resumen final.
- **Motor de progresión — UseCase de graduación.**
- **Motor de rachas — `PlannedSession` + roll-forward + streak calc** ← *último hito*

### 🔲 Pendiente

- Integrar `CheckGraduationEligibilityUseCase` en `SesionActivaViewModel.closeSession()`.
- Diálogo UI de criterio de graduación (`GraduationDialog` Compose).
- Flujo completo de Bienestar (CRUD molestias, selector jerárquico).
- Widget Glance.
- Sistema de Backup/Restore (export JSON/DB).
- Notificaciones push (descanso inter-series, recordatorios).

## Último Cambio — Motor de Rachas (Consistencia Semanal + Adherencia a Rutina)

**Archivos nuevos:** `PlannedSessionEntity.kt` · `PlannedSessionDao.kt` · `UserProfileDao.kt` · `MaterializePlannedSessionsUseCase.kt` · `UpdateStreakUseCase.kt`
**Archivos modificados:** `Enums.kt` · `Converters.kt` · `SessionDao.kt` · `AppDatabase.kt` (v2 + `MIGRATION_1_2`) · `DatabaseModule.kt` · `SesionActivaViewModel.kt` · `TodayViewModel.kt`

### DAOs nuevos
- **`PlannedSessionDao`** — `getOverduePending` · `markRolledForward` · `markCompleted` · `getByRoutineAndDate` · `getRecentForAdherenceStreak` · `insertAll` (IGNORE).
- **`UserProfileDao`** — `get()` / `observe()` / `upsert()` sobre el singleton.

### `SessionDao` — query añadida
- `countCompletedInRange(startEpoch, endEpoch): Int` — para la racha semanal.

### UseCases (`domain/streak/`)
- **`MaterializePlannedSessionsUseCase`** — genera `PlannedSession` PENDING para los próximos 14 días; idempotente (INSERT OR IGNORE). Llama a `TodayViewModel.init`.
- **`UpdateStreakUseCase`** — tres pasos en orden:
  1. **Roll-forward**: `PENDING` con `plannedDate < hoy` → `ROLLED_FORWARD` + nuevo `PENDING` para `+1 día`. Cascada ASC si hay varios días perdidos. La racha no se rompe.
  2. **Racha Semanal**: itera semanas ISO hacia atrás (máx. 52). Semana actual no penaliza si aún no cumplió el target. Rompe al encontrar semana pasada < `weeklyTargetSessions`.
  3. **Racha de Adherencia**: recorre últimas 60 `PlannedSession` (excluye `ROLLED_FORWARD` en DB). `COMPLETED` +1; `SKIPPED` o `PENDING` pasado → corta.

### Integración
- `TodayViewModel.init` → `materializePlannedSessions()` + `updateStreak()` (cada apertura de app).
- `SesionActivaViewModel.closeSession()` → `markCompleted(plannedId, sessionId)` + `updateStreak()`.
