# AGENTS.md — App Android de Entrenamiento de Fuerza

**Persistencia:** 100% local (Room/SQLite). Sin backend remoto. **Plataforma:** Android nativo.

> **Arquitectura completa → [`ARQUITECTURA.md`](./ARQUITECTURA.md)** (leer antes de implementar).

## Stack

*   UI: Jetpack Compose
*   Arquitectura: MVVM + Repository
*   BD: Room (SQLite)
*   Background: WorkManager
*   Prefs: DataStore
*   Widget: Glance
*   Lenguaje: Kotlin

## Estructura — 5 tabs

*   **Hoy** — Dashboard + CTA "Iniciar Sesión"
*   **Calendario** — Vista mensual con código de colores
*   **Rutinas** — CRUD rutinas/ejercicios
*   **Bienestar** — Molestias y condiciones crónicas
*   **Ajustes** — Perfil, backup, preferencias

## Reglas de Negocio Clave

*   **Sesión:** flujo secuencial PRE → NÚCLEO → POST.
*   **STRENGTH:** registra peso/series/reps/RIR. Alimenta motor de sobrecarga.
*   **ASSISTIVE:** temporizador o reps fijas. Métrica = cumplimiento (check).
*   **Adaptación:** `DiscomfortTag` ↔ `ExerciseTag` → reduce carga, sustituye/inyecta ejercicios. Persiste `AdaptationLog`.
*   **Cadenas biomecánicas:** jerarquía de variantes; promoción requiere `GraduationCriterion`.
*   **Inmutabilidad:** `SessionExerciseSnapshot`, `SetLog`, `AssistiveLog` no editables tras cerrar sesión.

## Entidades Room (26) — DB v2

*   `UserProfile` (1)
*   `Routine`
*   `RoutineSchedule`
*   `RoutinePhaseExercise`
*   `Exercise` (STRENGTH|ASSISTIVE)
*   `BiomechanicalChain`
*   `ChainVariant`
*   `GraduationCriterion`
*   `Tag`
*   `ExerciseTag`
*   `Session`
*   `SessionExerciseSnapshot`
*   `SetLog`
*   `AssistiveLog`
*   `BodyZone`
*   `Discomfort`
*   `DiscomfortTag`
*   `AdaptationLog`
*   `StreakState` (1)
*   `DeloadCycle`
*   `FreezePeriod`
*   `ProgressionTrigger`
*   `BackupMetadata`
*   `PlannedSession`

## Convenciones

*   Unidades: `kg`/`lb` según `UserProfile.unitSystem`.
*   Strings: siempre por clave de localización, nunca hardcoded.
*   Accesibilidad: color codes + icono/etiqueta (TalkBack). Inputs numéricos accesibles.
*   RIR color: 0–1 rojo · 2–3 verde · 4+ ámbar.

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

*   Bottom bar: oculto si ningún ancestro pertenece a un `TabGraph`.
*   Back stack aislado por tab (`saveState`/`restoreState`).
*   Rutas centralizadas en `NavRoutes.kt`; grafos en `TabGraph`.

## Estado del Proyecto

### ✅ Completado

*   **Modelo de datos:** DAOs y entidades Room para: `UserProfile`, `Routine`, `RoutineSchedule`, `BodyZone`, `BiomechanicalChain`, `Exercise`, `ChainVariant`, `GraduationCriterion`, `RoutinePhaseExercise`, `Tag`, `ExerciseTag`, `Session`, `SessionExerciseSnapshot`, `SetLog`, `AssistiveLog`, `Discomfort`, `DiscomfortTag`, `AdaptationLog`, `StreakState`, `DeloadCycle`, `FreezePeriod`, `ProgressionTrigger`, `BackupMetadata`, `PlannedSession`.
*   **Navegación:** `AppNavigation` con 5 tabs + modal sesión activa + navegación de ejercicios.
*   **Pantalla Hoy (`TodayScreen`):** estado reactivo, banner de adaptación, bottom sheet, CTA funcional.
*   **Pantalla Calendario:** grid mensual navegable + `CalendarioDiaScreen` (snapshot read-only).
*   **Pantalla Rutinas:** lista + FAB + `RutinaEditorScreen` + `BibliotecaEjerciciosScreen`.
*   **Pantalla Bienestar:** flujo completo de registro de molestias con `ModalBottomSheet`.
*   **Pantalla Ajustes:** secciones + sub-pantallas (Perfil, Triggers, Backup).
*   **`TodayRepository` + `AdaptationResolver`:** lógica de adaptación por molestia.
*   **`SesionActivaViewModel` + `SesionActivaScreen`:** sesión completa, timers, persistencia, resumen final.
*   **Motor de progresión — UseCase de graduación.**
*   **Motor de rachas — `PlannedSession` + roll-forward + streak calc.**
*   **Widget Glance — Home Screen + Deep Linking.**
*   **Creación y Edición de Rutinas y Ejercicios:** Flujo completo de UI/ViewModel para crear/editar rutinas (nombre, frecuencia, ejercicios por fase) e integrar/crear ejercicios en la biblioteca global.

### 🔲 Pendiente

*   Integrar `CheckGraduationEligibilityUseCase` en `SesionActivaViewModel.closeSession()`.
*   Diálogo UI de criterio de graduación (`GraduationDialog` Compose).
*   Flujo completo de Bienestar (CRUD molestias, selector jerárquico).
*   Sistema de Backup/Restore (export JSON/DB).
*   Notificaciones push (descanso inter-series, recordatorios).

## Último Cambio — Creación y Edición de Rutinas y Ejercicios

*   **Rutinas:** Implementado editor de rutina (nombre, frecuencia, añadir/gestionar ejercicios por fase).
*   **Biblioteca de Ejercicios:** Desarrollada la pantalla de biblioteca (listar, buscar, crear ejercicio global con tipo y tags).
*   **Integración:** Conectada la biblioteca con el editor de rutinas para seleccionar ejercicios.
