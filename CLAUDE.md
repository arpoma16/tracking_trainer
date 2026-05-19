# AGENTS.md — App Android de Entrenamiento de Fuerza

**Persistencia:** 100% local (Room/SQLite). Sin backend remoto. **Plataforma:** Android nativo.

> **Arquitectura completa → [`ARQUITECTURA.md`](./ARQUITECTURA.md)** (leer antes de implementar).

## Stack

- UI: Jetpack Compose · Arquitectura: MVVM + Repository
- BD: Room (SQLite) v3 · Background: WorkManager · Prefs: DataStore · Widget: Glance · Kotlin

## Estructura — 5 tabs

- **Hoy** — Dashboard + lista de rutinas del día con estado Pendiente/Completada
- **Calendario** — Vista mensual con código de colores
- **Rutinas** — CRUD rutinas/ejercicios
- **Bienestar** — Molestias y condiciones crónicas
- **Ajustes** — Perfil, backup, preferencias

## Reglas de Negocio Clave

- **Sesión:** flujo secuencial PRE → NÚCLEO → POST.
- **STRENGTH:** registra peso/series/reps/RIR. Alimenta motor de sobrecarga.
- **ASSISTIVE:** temporizador o reps fijas. Métrica = cumplimiento (check).
- **Adaptación:** `DiscomfortTag` ↔ `ExerciseTag` → reduce carga, sustituye/inyecta ejercicios. Persiste `AdaptationLog`.
- **Cadenas biomecánicas:** jerarquía de variantes; promoción requiere `GraduationCriterion`.
- **Inmutabilidad:** `SessionExerciseSnapshot`, `SetLog`, `AssistiveLog` no editables tras cerrar sesión.
- **Múltiples rutinas/día:** `TodayRepository` filtra todas las rutinas activas para hoy; cada una tiene estado `isDone` independiente.

## Entidades Room (26) — DB v3

- `UserProfile` · `Routine` · `RoutineSchedule` · `RoutinePhaseExercise`
- `Exercise` (STRENGTH|ASSISTIVE; +`defaultSets/Reps/Rir/DurationSec` desde v3)
- `BiomechanicalChain` · `ChainVariant` · `GraduationCriterion`
- `Tag` · `ExerciseTag`
- `Session` · `SessionExerciseSnapshot` · `SetLog` · `AssistiveLog`
- `BodyZone` · `Discomfort` · `DiscomfortTag` · `AdaptationLog`
- `StreakState` (1) · `DeloadCycle` · `FreezePeriod` · `ProgressionTrigger`
- `BackupMetadata` · `PlannedSession`

## Convenciones

- Unidades: `kg`/`lb` según `UserProfile.unitSystem`.
- Strings: siempre por clave de localización, nunca hardcoded.
- Accesibilidad: color codes + icono/etiqueta (TalkBack). Inputs numéricos accesibles.
- RIR color: 0–1 rojo · 2–3 verde · 4+ ámbar.
- `session.date` siempre = midnight epoch (`atStartOfDay`).

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

- **Modelo de datos:** todos los DAOs y entidades (ver lista arriba).
- **Navegación:** 5 tabs + modal sesión activa.
- **Pantalla Hoy:** múltiples rutinas del día, badge Pendiente/Completada por rutina, CTA deshabilitado al completar, reactivo vía `observeAllByDate`.
- **Pantalla Calendario:** grid mensual navegable + `CalendarioDiaScreen` (snapshot read-only). Sesiones indexadas por midnight epoch.
- **Pantalla Rutinas:** lista + FAB + `RutinaEditorScreen` + `BibliotecaEjerciciosScreen`.
- **Pantalla Bienestar:** flujo completo de registro de molestias con `ModalBottomSheet`.
- **Pantalla Ajustes:** Perfil, Triggers de Progresión, Backup.
- **`TodayRepository` + `AdaptationResolver`:** adaptación por molestia; soporta N rutinas/día.
- **`SesionActivaViewModel` + `SesionActivaScreen`:** flujo PRE→NÚCLEO→POST, timers, `SurveyPending` state, persistencia, resumen final.
- **Motor de progresión** — `CheckGraduationEligibilityUseCase`.
- **Motor de rachas** — `PlannedSession` + roll-forward + streak calc.
- **Widget Glance** — Home Screen + Deep Linking.
- **CRUD Rutinas/Ejercicios** — editor de rutina, biblioteca global, integración.
- **DB v3** — `MIGRATION_2_3` añade `defaultSets/Reps/Rir/DurationSec` a `Exercise`.
- **Seed de ejercicios** — `ExerciseSeeder` inserta 5 ejercicios STRENGTH por defecto al arrancar (Press Banca, Sentadilla, Peso Muerto, Jalón, Press Militar).

### 🔲 Pendiente

- Integrar `CheckGraduationEligibilityUseCase` en `SesionActivaViewModel.closeSession()`.
- Diálogo UI de graduación (`GraduationDialog` Compose).
- Flujo completo de Bienestar (CRUD molestias, selector jerárquico).
- Sistema de Backup/Restore (export JSON/DB).
- Notificaciones push (descanso inter-series, recordatorios).

## Último Cambio — Multi-rutina + Estado por Rutina en TodayScreen

- **`TodayUiState`:** añadido `TodayRoutineItem(routineId, routineName, plan, isDone)`; `Ready` ahora tiene `routines: List<TodayRoutineItem>` en lugar de campos sueltos.
- **`SessionDao`:** nuevo `observeAllByDate(epoch): Flow<List<SessionEntity>>`.
- **`TodayRepository`:** `firstOrNull` → `filter`; itera todas las rutinas del día; `overallDayStatus` = ADAPTED > DELOAD > TRAINING.
- **`TodayViewModel`:** combina con `observeAllByDate`; marca `isDone` por `routineId` en sesiones completadas.
- **`TodayScreen`:** `ReadyContent` usa `items(state.routines)` → `RoutineCard` por rutina con badge `RoutineStatusBadge` ("⏳ Pendiente" / "✓ Completada") y CTA deshabilitado si `isDone`.
- **`TrainingWidget`:** adaptado a `state.routines.firstOrNull()?.routineName`.
