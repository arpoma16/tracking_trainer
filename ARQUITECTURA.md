# Documento de Arquitectura Conceptual — App Android de Entrenamiento de Fuerza con Enfoque Preventivo

> **Alcance:** Definición técnica, estructural, funcional y de flujos. No contiene código.
> **Plataforma objetivo:** Android nativo, persistencia 100% local.
> **Stack conceptual asumido:** MVVM + Repository, Room (SQLite), WorkManager (para deloads/recordatorios), DataStore (preferencias), Glance (Widget).

---

## 1. ARQUITECTURA DE LA INFORMACIÓN Y NAVEGACIÓN

### 1.1 Estructura macro de la app

La app se organiza en **5 secciones de primer nivel** accesibles vía **Bottom Navigation Bar persistente**. Cada sección es una pila de navegación independiente (back stack aislado). El usuario nunca debe estar a más de 2 toques de iniciar una sesión.

### 1.2 Bottom Navigation (5 destinos)

| # | Destino | Icono conceptual | Función principal |
|---|---------|------------------|-------------------|
| 1 | **Hoy** | Llama / Rayo | Dashboard del día: rutina prevista, estado de racha, CTA "Iniciar Sesión". Pantalla de aterrizaje al abrir la app. |
| 2 | **Calendario** | Cuadrícula | Vista mensual con código de colores, drill-down al historial diario (snapshots). |
| 3 | **Rutinas** | Lista / Plantilla | CRUD de rutinas, asignación de frecuencia, gestión de bloques Pre/Núcleo/Post y ejercicios. |
| 4 | **Bienestar** | Corazón / Cruz | Registro de molestias y condiciones crónicas, historial de adaptaciones, activación del Modo Congelación. |
| 5 | **Ajustes** | Engranaje | Perfil, triggers de progreso, backup manual, preferencias de unidades (kg/lb), accesibilidad. |

### 1.3 Jerarquía de navegación (back stack)

```
RAÍZ (Bottom Nav)
├── HOY
│   ├── Detalle Rutina del Día (preview)
│   ├── Sesión Activa (modal full-screen, bloquea bottom nav)
│   │   ├── Fase Pre
│   │   ├── Fase Núcleo
│   │   ├── Fase Post
│   │   └── Resumen Final
│   └── Modal: Reportar Molestia Rápida
│
├── CALENDARIO
│   ├── Vista Mensual
│   ├── Detalle Día (snapshot histórico)
│   └── Comparativa Inter-mensual (opcional)
│
├── RUTINAS
│   ├── Lista de Rutinas
│   ├── Editor de Rutina
│   │   ├── Editor de Fase (Pre/Núcleo/Post)
│   │   ├── Selector de Ejercicio (DB)
│   │   └── Editor de Cadena Biomecánica
│   └── Biblioteca de Ejercicios (DB)
│       └── Detalle de Ejercicio
│
├── BIENESTAR
│   ├── Lista de Molestias/Condiciones Activas
│   ├── Selector Jerárquico de Zona Anatómica
│   ├── Editor de Condición (texto libre + tags + severidad)
│   └── Historial de Adaptaciones
│
└── AJUSTES
    ├── Perfil Usuario
    ├── Triggers de Progreso (umbrales globales)
    ├── Backup & Restore (export/import JSON/DB)
    ├── Modo Congelación Global (toggle de emergencia)
    └── Sobre la App
```

### 1.4 Reglas de navegación

- **Sesión Activa = modal bloqueante**: ocultar bottom nav, deshabilitar back físico (confirmar con diálogo "¿Abandonar sesión? Se perderá el progreso no guardado").
- **Deep linking desde Widget**: abre directamente la pantalla de Sesión Activa en su fase Pre.
- **FAB contextual** sólo en pantalla "Hoy" cuando hay sesión pendiente; desaparece tras iniciar.

---

## 2. DETALLE DE VISTAS (SCREENS)

### 2.1 Pantalla HOY (Dashboard)

**Propósito:** Punto único de inicio. Comunica estado, racha y CTA.

**Componentes (de arriba hacia abajo):**

1. **Header de Estado del Día**
   - Etiqueta de día: `ENTRENO` | `DESCANSO OBLIGATORIO` | `DESCARGA (DELOAD)` | `CONGELADO` | `ADAPTADO`
   - Fecha completa.
2. **Card de Racha Dual**
   - Racha de Consistencia Semanal: número grande + icono llama.
   - Racha de Adherencia a Rutina: número + icono cadena.
   - Tooltip explicativo al tap largo.
3. **Card de Rutina Prevista**
   - Nombre de la rutina, tags (ej. "Tren Inferior", "Empuje").
   - Resumen: nº ejercicios por fase (Pre 5 · Núcleo 4 · Post 3).
   - Banner amarillo si hay adaptación por molestia activa: "Sesión adaptada por: Rótula".
4. **Botón Primario "INICIAR SESIÓN"** (full width, sticky-bottom si scroll).
5. **Botones Secundarios:**
   - "Reportar molestia" (abre modal).
   - "Saltar a descanso" (con confirmación).

**Estados visuales:**
- Estado normal: paleta primaria (azul/verde).
- Día de descanso obligatorio: paleta calma (gris/azul claro), CTA cambia a "Marcar día de descanso como cumplido".
- Día de deload: badge naranja "Descarga al 60%".
- Modo Congelación: overlay con icono de copo de nieve, CTA "Reanudar entrenamiento".

---

### 2.2 SESIÓN ACTIVA — Estructura común

La sesión es un **Pager horizontal de 3 segmentos** (Pre · Núcleo · Post) con un **Stepper superior** que indica la fase actual y el progreso intra-fase.

**Componentes globales de la sesión:**

- **AppBar de sesión:** título de la fase, contador "Ejercicio X de Y", botón menú (pausar, abandonar, reportar molestia).
- **Stepper de fase:** 3 puntos conectados, el activo se resalta. Toque permite saltar entre fases solo si la anterior está completa o se confirma "saltar".
- **Barra de progreso lineal:** progreso global de la sesión (0–100%).
- **Cronómetro de sesión total:** tiempo transcurrido desde el inicio (no se puede pausar; congelable solo con la pausa formal).

---

#### 2.2.A Fase PRE — Movilidad y Calentamiento

**Layout por ejercicio (tipo Asistencial / Trackeable=false):**

- **Header:** nombre del ejercicio + tag de objetivo (ej. "Movilidad de cadera").
- **Visual:** GIF o imagen estática del ejercicio (placeholder si no hay asset).
- **Bloque central — Temporizador o Reps fijas:**
  - **Si es temporizado:** dial circular grande con cuenta regresiva (ej. 2:00). Botones: ▶ Iniciar · ⏸ Pausa · ⏭ Saltar.
  - **Si es por repeticiones fijas:** display "12 reps · 2 lados", botón único "✓ Completado".
- **Pie:** notas técnicas breves (ej. "Mantener pelvis neutra"), indicador "Ejercicio inyectado por adaptación" si aplica (icono médico).
- **Navegación entre ejercicios:** swipe horizontal o botón "Siguiente".

**Estados:**
- Pendiente · En curso · Completado (check verde) · Saltado (icono gris).

---

#### 2.2.B Fase NÚCLEO — Fuerza con Sobrecarga Progresiva

**Layout por ejercicio (tipo Fuerza / Trackeable=true):**

- **Header:** nombre + variante mecánica actual (ej. "Puente de glúteo a una pierna · Nivel 3/5").
- **Card de Referencia Histórica:**
  - "Última sesión: 4×8 @ 40kg · RIR 2"
  - "Récord personal: 4×8 @ 45kg · RIR 1"
  - "Sugerencia del motor: +2.5kg si RIR ≥ 2 en todas las series"
- **Tabla de Series (componente repetible):**

  | Set | Objetivo | Peso (kg) | Reps | RIR | Estado |
  |-----|----------|-----------|------|-----|--------|
  | 1   | 8 reps   | [input]   | [input] | [chip 0-4] | ✓ / ◌ |
  | 2   | 8 reps   | ...       | ...  | ... | ... |

  - **Input de Peso:** numeric stepper con incremento configurable (default 2.5kg), permite teclado directo.
  - **Input de Reps:** stepper +/–.
  - **Chip de RIR:** segmented control horizontal con valores [0, 1, 2, 3, 4, 5+]. Color codificado:
    - 0–1: rojo (fallo cercano).
    - 2–3: verde (zona óptima).
    - 4+: amarillo (subestimación, sugerir subir carga).
  - **Botón "Confirmar serie"** → bloquea fila y arranca temporizador de descanso.

- **Temporizador de Descanso Inter-series:**
  - Dial flotante (FAB expandido) con cuenta regresiva (default 90s, configurable por ejercicio).
  - Notificación push + vibración al finalizar.
  - Botón "Iniciar siguiente serie" cuando llega a 0.

- **Acciones extra (overflow menu):**
  - "Sustituir ejercicio" (abre selector con ejercicios equivalentes por tag).
  - "Marcar molestia durante este ejercicio" (registra evento atado a serie).
  - "Activar criterio de graduación" (solo visible si el ejercicio cumple el trigger acumulado).

**Variante de carga por Banda Elástica:** el campo "Peso (kg)" se reemplaza por "Tensión" con selector de color de banda + nivel de estiramiento (1–5).

---

#### 2.2.C Fase POST — Recuperación y Fisioterapia

**Layout por ejercicio (Asistencial, similar a Pre pero con énfasis terapéutico):**

- **Header:** nombre + zona objetivo (ej. "Liberación miofascial cuádriceps").
- **Visual:** GIF/imagen + indicador de presión/intensidad si aplica (escala 1–3).
- **Temporizador o Reps:** análogo a Pre.
- **Badge especial** si el ejercicio fue inyectado por una molestia activa: "🩹 Mitigación: Condromalacia rotuliana".
- **Slider opcional post-ejercicio:** "¿Sientes alivio?" (0–10) — alimenta el módulo de Bienestar.

---

#### 2.2.D Resumen Final de Sesión

**Componentes:**

1. **Mensaje motivacional** según desempeño (RIR consistente, PR detectado, etc.).
2. **Métricas agregadas:**
   - Tonelaje total (suma de peso × reps × series del Núcleo).
   - Duración total.
   - Series completadas / saltadas.
   - RIR promedio.
3. **Logros desbloqueados** (PR por ejercicio, hito de racha, criterio de graduación cumplido).
4. **Encuesta corta (3 ítems):**
   - Fatiga percibida (1–10).
   - Dolor/molestia general (0–10).
   - Disposición para mañana (1–5).
5. **Botón "Guardar y cerrar"** → escribe snapshot inmutable en historial, actualiza rachas, dispara cálculo del motor de progresión.

---

### 2.3 Pantalla CALENDARIO

**Vista Mensual:**

- Grid 7×6 con celdas de día.
- **Códigos de color por estado:**
  - 🟢 Verde sólido: Completado al 100%.
  - 🟡 Amarillo: Media racha (parcial: <70% del volumen previsto).
  - ⚫ Gris oscuro: Descanso programado / obligatorio.
  - 🔴 Rojo: No hecho (día con rutina prevista que se saltó).
  - 🟣 Morado: Adaptado por molestia.
  - 🔵 Azul: Deload activo.
  - ❄️ Overlay copo: Día en Modo Congelación.
- **Header del mes:** navegación mes anterior/siguiente, selector de año, leyenda colapsable.
- **Footer:** resumen mensual (sesiones, tonelaje, % adherencia).

**Detalle Día (snapshot):**

- Datos congelados de la rutina ejecutada ese día: ejercicios, series, peso, RIR, notas, molestias reportadas, adaptaciones aplicadas.
- Sólo lectura — los snapshots no son editables, sólo anotables (campo notas).

---

### 2.4 Pantalla RUTINAS — Editor

**Estructura del editor:**

- **Header:** nombre rutina, tags multi-select (Empuje, Tracción, Tren Inferior, Core...).
- **Frecuencia:** selector flexible (Días de semana específicos | Cada N días | Personalizado).
- **3 secciones acordeón** (Pre · Núcleo · Post):
  - Cada sección lista los ejercicios añadidos con drag handle para reordenar.
  - FAB "+ Añadir ejercicio" abre el Selector de la biblioteca.
  - Cada ejercicio expandible para configurar: series objetivo, reps objetivo, RIR objetivo, descanso, tempo (opcional).
  - En Núcleo: cada ejercicio enlazable a una **Cadena Biomecánica** (variantes jerárquicas).

**Editor de Cadena Biomecánica:**

- Lista vertical ordenada de variantes (nivel 1 → N).
- Para cada nivel: ejercicio asociado + **Checklist de Criterio de Graduación**:
  - [ ] Control técnico (3 sesiones consecutivas RIR ≥ 2).
  - [ ] Rango completo de movimiento.
  - [ ] Dolor reportado = 0/10 durante el ejercicio.
- Botón "Promover ejercicio al siguiente nivel" — solo activo si todos los checks están marcados (manual + auto-validado).

---

### 2.5 Pantalla BIENESTAR

**Lista de molestias/condiciones activas:**

- Card por condición: zona anatómica · etiqueta corta · severidad (1–5) · fecha de inicio · toggle "Activa para adaptación".
- CTA "+ Nueva molestia" abre flujo de selección jerárquica.

**Selector Jerárquico (3 niveles):**

```
Nivel 1: Región       (Tren Superior | Tren Inferior | Core | Columna)
Nivel 2: Sub-región   (ej. Tren Inferior → Cadera | Rodilla | Tobillo)
Nivel 3: Estructura   (ej. Rodilla → Rótula/Condromalacia | LCA | Menisco)
```

- Botón final "Otro / Texto libre" siempre disponible.
- Campo de notas libre tras la selección.
- Selector de severidad (slider 1–5).
- Toggle "Aplicar adaptación automática a próximas sesiones".

**Historial de Adaptaciones:**

- Lista cronológica: fecha · rutina · ejercicios sustituidos · ejercicios inyectados · resultado (slider alivio reportado).

---

### 2.6 WIDGET — Diseño conceptual

**Tamaño:** 4×2 (medio). Opcionalmente 4×1 compacto y 4×4 expandido.

**Variantes de UI según estado del día:**

| Estado | Fondo | Texto principal | Texto secundario | CTA |
|--------|-------|------------------|-------------------|-----|
| Entreno previsto | Gradiente activo | "Hoy: Tren Inferior" | "Racha 🔥 14 días · 4 ejercicios" | "Iniciar →" |
| Descanso obligatorio | Tono calma | "Día de descanso" | "Tu recuperación protege la racha" | "Ver calendario" |
| Deload | Tono naranja suave | "Semana de descarga" | "Volumen al 60% · escucha tu cuerpo" | "Ver rutina ligera" |
| Adaptado | Tono morado | "Sesión adaptada · Rodilla" | "Carga reducida + masaje post" | "Iniciar →" |
| Congelado | Tono nieve | "Modo Congelación" | "Rachas protegidas · descansa" | "Reanudar" |
| Sesión en curso | Pulso animado | "Sesión activa · Núcleo 3/4" | "Toca para continuar" | "Continuar →" |

**Estructura interna del widget:**
- Línea 1: estado + nombre rutina.
- Línea 2: indicadores de racha (dual).
- Línea 3: CTA / deep link a la pantalla relevante.

**Actualización:** vía `WorkManager` programado a las 00:05 local + tras cada sesión guardada.

---

### 2.7 Manejo de estados globales (resumen)

| Estado | Trigger | Efecto en UI | Efecto en lógica |
|--------|---------|--------------|------------------|
| Sesión Pendiente | Día con rutina asignada | CTA "Iniciar" en Hoy + Widget | — |
| Sesión Activa | Usuario tocó "Iniciar" | Modal bloqueante, widget pulsa | Cronómetro corriendo |
| Sesión Pausada | Botón pausa | Overlay "Pausada" | Cronómetro detenido |
| Sesión Adaptada | Molestia activa con tag coincidente | Banner amarillo + ejercicios marcados | Sustitución/inyección automática |
| Deload Semana | Detección de fatiga acumulada | Badge naranja | Cargas ×0.6, series ×0.7 |
| Modo Congelación | Usuario activa en Ajustes | Overlay copo, rachas neutralizadas | Días no cuentan negativos |

---

## 3. MODELO DE DATOS LOCAL (Room / SQLite)

### 3.1 Listado de entidades

1. `UserProfile`
2. `Routine`
3. `RoutineSchedule`
4. `Phase` (catálogo: PRE / CORE / POST)
5. `RoutinePhaseExercise` (tabla puente con orden y configuración)
6. `Exercise`
7. `ExerciseType` (catálogo: STRENGTH / ASSISTIVE)
8. `BiomechanicalChain`
9. `ChainVariant`
10. `GraduationCriterion`
11. `Tag`
12. `ExerciseTag` (tabla puente)
13. `Session` (instancia ejecutada)
14. `SessionExerciseSnapshot`
15. `SetLog` (registro por serie)
16. `AssistiveLog` (registro por ejercicio asistencial)
17. `BodyZone` (catálogo jerárquico)
18. `Discomfort` (molestia/condición)
19. `DiscomfortTag` (puente molestia↔tag)
20. `AdaptationLog`
21. `StreakState`
22. `DeloadCycle`
23. `FreezePeriod`
24. `ProgressionTrigger` (configuración por ejercicio)
25. `BackupMetadata`

---

### 3.2 Detalle de entidades clave

#### 3.2.1 `UserProfile`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | singleton (siempre 1) |
| name | TEXT | |
| birthDate | DATE | para cálculos opcionales |
| unitSystem | ENUM(KG, LB) | |
| defaultRestSec | INT | default 90 |
| weeklyTargetSessions | INT | base para racha semanal |
| createdAt | DATETIME | |

#### 3.2.2 `Routine`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| name | TEXT | |
| description | TEXT | |
| isActive | BOOL | |
| createdAt | DATETIME | |

#### 3.2.3 `RoutineSchedule`
Define frecuencia/cuándo aparece una rutina.
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| routineId | FK → Routine | |
| scheduleType | ENUM(WEEKDAYS, EVERY_N_DAYS, CUSTOM) | |
| weekdaysMask | INT | bitmask Lun–Dom |
| everyNDays | INT | nullable |
| anchorDate | DATE | para cálculo de “cada N días” |

#### 3.2.4 `RoutinePhaseExercise` (puente)
Liga ejercicio a rutina dentro de una fase concreta.
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| routineId | FK → Routine | |
| phase | ENUM(PRE, CORE, POST) | |
| exerciseId | FK → Exercise | |
| orderIndex | INT | |
| targetSets | INT | nullable si asistencial |
| targetReps | INT | nullable |
| targetRir | INT | nullable |
| targetDurationSec | INT | nullable (asistenciales) |
| restSec | INT | nullable |
| chainVariantId | FK → ChainVariant | nullable (apunta a variante mecánica actual) |

#### 3.2.5 `Exercise`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| name | TEXT | |
| type | ENUM(STRENGTH, ASSISTIVE) | discriminador clave |
| trackingMode | ENUM(WEIGHT_REPS, BAND_TENSION, TIMER, FIXED_REPS) | |
| description | TEXT | |
| mediaRef | TEXT | path/uri de GIF o imagen |
| primaryBodyZoneId | FK → BodyZone | |
| chainId | FK → BiomechanicalChain | nullable |

**Regla:** los `STRENGTH` alimentan el motor de sobrecarga y exponen `SetLog`; los `ASSISTIVE` no se progresan en carga y se registran en `AssistiveLog`.

#### 3.2.6 `BiomechanicalChain` y `ChainVariant`

`BiomechanicalChain`
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| name | TEXT | ej. "Puente de glúteo" |

`ChainVariant`
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| chainId | FK → BiomechanicalChain | |
| level | INT | 1..N (jerarquía) |
| exerciseId | FK → Exercise | la variante apunta a un Exercise concreto |
| previousVariantId | FK → ChainVariant | nullable (back-pointer) |

#### 3.2.7 `GraduationCriterion`
Checklist para promover de una variante a la siguiente.
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| chainVariantId | FK → ChainVariant | |
| requiredSuccessfulSessions | INT | default 5 |
| requiredMinRir | INT | default 2 |
| requireFullRom | BOOL | |
| requireZeroPain | BOOL | |
| manualConfirmed | BOOL | el usuario confirma checklist |

#### 3.2.8 `Tag` y `ExerciseTag`
`Tag`: `(id, name, category)` — categorías: PATTERN (empuje/tracción), BODY (cadera, rodilla), GOAL (movilidad, fuerza, masaje).
`ExerciseTag`: puente N:N.

#### 3.2.9 `Session` — Instancia ejecutada
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| routineId | FK → Routine | |
| date | DATE | |
| startedAt | DATETIME | |
| endedAt | DATETIME | nullable |
| status | ENUM(COMPLETED, PARTIAL, SKIPPED, ADAPTED, DELOAD) | |
| totalTonnage | REAL | calculado al cerrar |
| avgRir | REAL | |
| fatigueScore | INT | encuesta final |
| painScore | INT | encuesta final |
| readinessScore | INT | encuesta final |
| isAdapted | BOOL | hubo adaptación por molestia |
| isDeload | BOOL | |

#### 3.2.10 `SessionExerciseSnapshot`
Congela el estado del ejercicio tal como se ejecutó (datos inmutables).
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| sessionId | FK → Session | |
| phase | ENUM(PRE, CORE, POST) | |
| exerciseId | FK → Exercise | |
| exerciseNameSnapshot | TEXT | nombre congelado (defensa ante renombrados) |
| chainVariantLevelSnapshot | INT | nivel mecánico en el momento |
| wasSubstituted | BOOL | |
| substitutionReason | TEXT | nullable |
| orderIndex | INT | |

#### 3.2.11 `SetLog` (solo ejercicios STRENGTH)
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| snapshotId | FK → SessionExerciseSnapshot | |
| setIndex | INT | |
| weightKg | REAL | nullable si banda |
| bandTension | TEXT | nullable (ej. "Roja/3") |
| reps | INT | |
| rir | INT | 0..5 |
| isCompleted | BOOL | |
| isPr | BOOL | calculado al cierre |

#### 3.2.12 `AssistiveLog` (ejercicios ASSISTIVE)
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| snapshotId | FK → SessionExerciseSnapshot | |
| durationActualSec | INT | nullable |
| repsActual | INT | nullable |
| completed | BOOL | check de cumplimiento |
| reliefScore | INT | nullable, 0–10 |
| injectedByDiscomfortId | FK → Discomfort | nullable |

#### 3.2.13 `BodyZone` (jerárquico)
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| parentId | FK → BodyZone | nullable (raíz) |
| level | INT | 1=Región, 2=Sub, 3=Estructura |
| name | TEXT | |

#### 3.2.14 `Discomfort`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| bodyZoneId | FK → BodyZone | |
| label | TEXT | corto, ej. "Condromalacia" |
| freeText | TEXT | descripción libre |
| severity | INT | 1–5 |
| startedAt | DATE | |
| resolvedAt | DATE | nullable |
| isActive | BOOL | aplica adaptaciones |

#### 3.2.15 `DiscomfortTag`
Puente entre `Discomfort` y `Tag` — define qué ejercicios se ven afectados (por tag) cuando la molestia está activa.

#### 3.2.16 `AdaptationLog`
Registra cada adaptación aplicada en una sesión.
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| sessionId | FK → Session | |
| discomfortId | FK → Discomfort | |
| actionType | ENUM(LOAD_REDUCTION, SUBSTITUTION, INJECTION_PRE, INJECTION_POST) | |
| affectedExerciseId | FK → Exercise | nullable |
| replacementExerciseId | FK → Exercise | nullable |
| loadFactor | REAL | ej. 0.7 |

#### 3.2.17 `StreakState`
Singleton actualizado tras cada sesión.
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | (=1) |
| weeklyConsistencyCount | INT | |
| weeklyConsistencyAnchorWeek | INT | |
| routineAdherenceCount | INT | |
| routineAdherenceAnchorDate | DATE | |
| lastUpdated | DATETIME | |

#### 3.2.18 `DeloadCycle`
| Campo | Tipo | Notas |
|-------|------|-------|
| id | INT PK | |
| startDate | DATE | |
| endDate | DATE | |
| triggerReason | TEXT | "fatigue_accumulated_4w" |
| loadFactor | REAL | 0.6 |

#### 3.2.19 `FreezePeriod`
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| startDate | DATE | |
| endDate | DATE | nullable mientras esté activo |
| reason | TEXT | |

#### 3.2.20 `ProgressionTrigger`
Config por ejercicio (override del global).
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| exerciseId | FK → Exercise | |
| successfulSessionsNeeded | INT | default 5 |
| minRirRequired | INT | default 2 |
| weightIncrementKg | REAL | default 2.5 |
| progressionMode | ENUM(LOAD, MECHANICAL, BOTH) | |

#### 3.2.21 `BackupMetadata`
| Campo | Tipo | |
|-------|------|---|
| id | INT PK | |
| lastExportAt | DATETIME | |
| lastExportPath | TEXT | |
| schemaVersion | INT | |

---

### 3.3 Relaciones principales (resumen lógico)

- `Routine` 1—N `RoutineSchedule`
- `Routine` 1—N `RoutinePhaseExercise` N—1 `Exercise`
- `Exercise` N—M `Tag` (vía `ExerciseTag`)
- `Exercise` N—1 `BodyZone`
- `Exercise` N—0..1 `BiomechanicalChain` (a través de `ChainVariant`)
- `BiomechanicalChain` 1—N `ChainVariant`
- `ChainVariant` 1—1 `GraduationCriterion`
- `Session` 1—N `SessionExerciseSnapshot` 1—N (`SetLog` | `AssistiveLog`)
- `Session` 1—N `AdaptationLog` N—1 `Discomfort`
- `Discomfort` N—1 `BodyZone`, `Discomfort` N—M `Tag` (vía `DiscomfortTag`)
- `Exercise` 0..1—1 `ProgressionTrigger` (override) — fallback a config global

**Invariante de inmutabilidad:** los `SessionExerciseSnapshot`, `SetLog`, `AssistiveLog` no se editan tras `Session.status` ≠ ACTIVE. Cualquier corrección genera una nota adicional, nunca sobrescritura.

---

## 4. FLUJOS DE USUARIO (User Journeys)

### 4.A FLUJO COMPLETO DE SESIÓN ADAPTADA POR CONDROMALACIA ROTULIANA

**Contexto:** El usuario tiene registrada en `Discomfort` la condición "Condromalacia rotuliana" (zona = Rodilla > Rótula, severidad 3, activa). La rutina del día es "Tren Inferior".

#### Paso 1 — Apertura de la app
- El usuario abre la app y aterriza en **Hoy**.
- El sistema, al cargar Hoy, ejecuta el **resolver de adaptación**:
  1. Lee `Discomfort` activos.
  2. Cruza sus `DiscomfortTag` con los `ExerciseTag` de los ejercicios planificados en `RoutinePhaseExercise` para la rutina de hoy.
  3. Para cada coincidencia genera entradas tentativas en `AdaptationLog` (aún no persistidas).
- Resultado visible: banner amarillo **"Sesión adaptada por: Rótula/Condromalacia"** + lista resumida de cambios.

#### Paso 2 — Vista previa de adaptación
- Tap en el banner abre una **bottom sheet** con:
  - Ejercicios sustituidos: ej. "Sentadilla profunda → Sentadilla a banco con menor flexión".
  - Ejercicios con carga reducida: ej. "Prensa: carga × 0.7".
  - Ejercicios inyectados en Pre: "Activación VMO" (2 min), "Movilidad rotuliana" (90 s).
  - Ejercicios inyectados en Post: "Liberación miofascial cuádriceps" (2 min), "Estiramiento isquios" (90 s), "Masaje banda iliotibial" (2 min).
- Botón "Aceptar y comenzar".

#### Paso 3 — Inicio de sesión
- Crea registro en `Session` con `isAdapted=true`, `status=ACTIVE`.
- Persiste todas las entradas de `AdaptationLog` ligadas a esta `Session`.
- Genera `SessionExerciseSnapshot` por cada ejercicio que se ejecutará (incluyendo los inyectados).
- Navega a la **Fase Pre**.

#### Paso 4 — Fase Pre adaptada
- El usuario ejecuta primero los ejercicios estándar de movilidad de la rutina.
- A continuación aparecen los **ejercicios inyectados** con badge "🩹 Inyectado por: Rótula":
  1. "Activación VMO" — temporizador 2:00. Usuario inicia, completa.
  2. "Movilidad rotuliana" — 90 s. Completa.
- Cada uno escribe un `AssistiveLog` con `injectedByDiscomfortId` = id de la condromalacia.

#### Paso 5 — Fase Núcleo adaptada
- El usuario ve los ejercicios de fuerza. En cada uno afectado:
  - La card de referencia muestra "Carga sugerida hoy: 28 kg (–30% por adaptación)".
  - Si fue sustituido: aparece el nuevo nombre con tooltip explicativo.
- El usuario ejecuta series, registra `weightKg`, `reps`, `rir` por cada serie.
- **Slider de dolor durante el ejercicio (opcional):** si reporta dolor ≥ 4, la app sugiere reducir aún más la carga la próxima serie y registra el evento.
- Temporizador de descanso corre entre series.

#### Paso 6 — Fase Post terapéutica
- Tras Núcleo, se ejecutan ejercicios Post estándar + los **inyectados terapéuticos**:
  1. "Liberación miofascial cuádriceps" — 2:00.
  2. "Estiramiento isquios" — 90 s × 2 lados.
  3. "Masaje banda iliotibial" — 2:00.
- Al final de cada uno: slider opcional "¿Sientes alivio?" 0–10 → escribe `reliefScore` en `AssistiveLog`.

#### Paso 7 — Resumen y cierre
- App muestra resumen con sección especial: **"Adaptaciones aplicadas hoy"** (lista detallada).
- Encuesta final: fatiga, dolor, disposición.
- Al guardar:
  - `Session.status = COMPLETED`, `endedAt` = ahora, agregados calculados.
  - `StreakState` actualizado (cuenta como sesión completa).
  - Calendario marca el día con código **🟣 Morado (Adaptado)**.
  - Widget se refresca.
- Si el `painScore` ≥ 6, la app sugiere proactivamente: "Considera mantener la adaptación 1 semana más" o "Reportar a un profesional".

---

### 4.B FLUJO DE GRADUACIÓN A VARIANTE MECÁNICA SUPERIOR

**Contexto:** El usuario lleva 7 sesiones con "Puente de glúteo bilateral" (nivel 1 de la cadena "Puente de glúteo"). El criterio global pide 5 sesiones exitosas con RIR ≥ 2 y dolor 0/10. La siguiente variante es "Puente de glúteo a una pierna" (nivel 2).

#### Paso 1 — Detección automática del trigger
- Tras guardar la última sesión, el **motor de progresión** evalúa cada `SessionExerciseSnapshot` de ejercicios STRENGTH:
  1. Cuenta sesiones consecutivas donde:
     - Todas las series cumplieron `rir ≥ minRirRequired`.
     - No hubo `painScore ≥ 4` durante ese ejercicio.
  2. Compara con `ProgressionTrigger.successfulSessionsNeeded` (o config global).
- Al cumplirse el conteo: marca un flag interno `chainVariant.eligibleForPromotion = true`.

#### Paso 2 — Notificación al usuario
- En la siguiente entrada a la pantalla **Hoy** (o al abrir el ejercicio en el Núcleo de la próxima sesión), aparece un **toast / banner verde**:
  > "🎯 Listo para evolucionar: Puente de glúteo. Realiza el checklist de graduación."
- También aparece un punto verde sobre el ejercicio en la lista de la rutina activa.

#### Paso 3 — Apertura del Checklist de Criterio de Graduación
- Usuario toca el banner → navega a una pantalla dedicada **"Criterio de Graduación: Puente de glúteo → Puente a una pierna"**.
- Componentes:
  - **Card resumen automático:**
    - ✅ 5/5 sesiones consecutivas con RIR ≥ 2 (auto-validado).
    - ✅ Sin reportes de dolor en el ejercicio en las últimas 5 sesiones (auto-validado).
  - **Checklist manual del usuario:**
    - [ ] "Confirmo control técnico estable" (texto guía + GIF).
    - [ ] "Confirmo rango completo de movimiento sin compensaciones".
    - [ ] "Confirmo dolor 0/10 durante todo el rango".
  - **Vista previa de la siguiente variante:** GIF, descripción, recomendaciones de carga inicial (sugerencia del motor: comenzar con 60% del último peso y RIR objetivo 3).
  - **Botones:** "Promover ahora" (activo solo con todos los checks) · "Posponer" · "Saltar variante" (oculto por defecto, requiere confirmación).

#### Paso 4 — Confirmación de promoción
- Usuario marca los 3 checks manuales → botón "Promover ahora" se habilita.
- Tap → diálogo de confirmación con resumen: "Pasarás de Nivel 1 a Nivel 2. Tu próximo entreno cargará la nueva variante. ¿Confirmar?"
- Al confirmar:
  1. Actualiza `RoutinePhaseExercise.chainVariantId` apuntando al nuevo `ChainVariant` (nivel 2).
  2. Marca `GraduationCriterion.manualConfirmed = true` en el nivel previo.
  3. Crea registro en una tabla de historial de graduaciones (opcional: `ChainGraduationHistory` con sessionId, fromLevel, toLevel, date).
  4. Reinicia el conteo de "sesiones exitosas consecutivas" para el ejercicio (nueva variante = nueva curva).
  5. Sugiere carga inicial conservadora en el primer set del próximo entreno.
- Animación de logro: "🏆 Variante mecánica desbloqueada."

#### Paso 5 — Primer entrenamiento con la nueva variante
- En la próxima sesión, el ejercicio aparece ya con el nombre "Puente de glúteo a una pierna · Nivel 2/5".
- La card de referencia muestra:
  - "Variante nueva — sin histórico aún."
  - "Sugerencia inicial: 3×8 @ 60% de tu último peso · RIR objetivo 3."
- El usuario registra las series como de costumbre.
- A partir de aquí, el motor vuelve a contar sesiones exitosas para evaluar la futura promoción a Nivel 3, ahora basándose en los `SetLog` de la nueva variante.

#### Paso 6 — Mecanismo de retroceso (regresión)
- Si tras la promoción el usuario reporta `painScore ≥ 4` o RIR < 1 (cerca de fallo) en 2 sesiones consecutivas con la nueva variante:
  - La app sugiere proactivamente: **"¿Volver a la variante anterior?"** con CTA "Regresar a Nivel 1" (re-apunta `chainVariantId` al previo y registra el evento como regresión voluntaria, sin afectar negativamente la racha).

---

## 5. NOTAS TRANSVERSALES

- **Concurrencia de adaptaciones:** si hay varias `Discomfort` activas, el resolver aplica el **mayor factor de reducción** y unifica las inyecciones (sin duplicar ejercicios con el mismo tag).
- **Backup manual:** export genera un único archivo `.json` (más legible) o `.db` (más fiel) que incluye `schemaVersion`. Restore valida versión y aplica migraciones si corresponde.
- **Internacionalización:** todos los textos se referencian por clave (no hardcoded), unidades configurables en `UserProfile`.
- **Accesibilidad:** todos los inputs numéricos accesibles vía TalkBack, color codes acompañados de iconos/etiquetas (no depender solo del color).

---

**Fin del documento.**
