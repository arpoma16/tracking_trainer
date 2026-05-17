Actúa como un Arquitecto de Software, Diseñador de UX/UI Senior y Líder Técnico especializado en desarrollo móvil nativo para Android, con amplios conocimientos en Ciencias del Deporte, Fisioterapia aplicada y Readaptación de lesiones.

Quiero que diseñes la arquitectura conceptual, el mapa detallado de vistas (screens), los componentes visuales y el modelo de datos para una aplicación Android de seguimiento de entrenamiento de fuerza con un enfoque integral en preparación articular, sobrecarga progresiva inteligente, autorregulación y prevención estricta de lesiones.

REGLA CRÍTICA: NO generes código de programación (ni Kotlin, ni XML, ni Jetpack Compose). Solo necesito la definición técnica, estructural, funcional y de flujos de la aplicación.

Aquí tienes los requerimientos definitivos y la lógica de negocio:

1. Estructura de Sesión en Tres Fases (Pre, Núcleo, Post):

- Cada sesión de entrenamiento se divide visual y funcionalmente en:
  A) Fase Pre (Preparación): Ejercicios de movilidad articular y calentamiento (general y específico basado en las etiquetas de la rutina).
  B) Fase Núcleo (Fuerza): Bloque principal de sobrecarga progresiva autorregulada.
  C) Fase Post (Recuperación/Fisioterapia): Ejercicios de estiramiento, automasaje o liberación miofascial para mitigar dolores o condiciones crónicas (ej. condromalacia).

2. Tipificación de Ejercicios en la Base de Datos:

- Ejercicios de Fuerza (Trackeables): Registran Peso/Tensión, Series, Repeticiones y RIR (Repeticiones en Recámara). Afectan directamente al motor de sobrecarga.
- Ejercicios Asistenciales (Movilidad/Recuperación): No usan peso ni RIR. Se gestionan mediante temporizadores (ej. mantener un estiramiento o masaje durante 2 minutos) o repeticiones fijas de salud articular. Su métrica es puramente de cumplimiento (Check).

3. Sistema de Rachas Duales y Calendario Dinámico:

- Calendario Visual: Historial mensual con códigos de color para el estado de cada día (Completado, Media Racha, Descanso, No Hecho, Adaptado por Molestia).
- Racha de Consistencia Semanal (Hábito general) y Racha de Adherencia a la Rutina (Frecuencias flexibles, ej. cada 3 días). Si se salta un día, el calendario dinámico desplaza la rutina sin resetear destructivamente la racha.

4. Motor de Sobrecarga Progresiva y Variantes Mecánicas:

- Trigger de Progreso por Ejercicio: Configurable (ej. tras 5 entrenamientos exitosos con RIR controlado).
- Progresión por Carga (kg o tensión de banda) y Progresión por Variación Mecánica (Cadenas biomecánicas jerárquicas, ej. Puente de glúteo -> Puente de glúteo a una pierna). Requiere un "Checklist de Criterio de Graduación" (control técnico, rango completo y dolor 0/10) antes de evolucionar el ejercicio.
- Histórico con Instantáneas (Snapshots): Los datos se congelan estáticamente en el historial del día completado.

5. Módulo de Bienestar, Condiciones Crónicas y Mitigación Preventiva:

- Registro de Molestias/Condiciones: Selección de zona afectada mediante lista jerárquica (ej. Tren Inferior -> Rótula/Condromalacia) y texto libre.
- Relación por Etiquetas (Tags): Conexión entre las zonas con molestia y los ejercicios de la base de datos.
- Lógica de Adaptación Dinámica: Si hay una molestia o condición activa, al iniciar la sesión la app: a) Reduce la carga o sustituye los ejercicios del bloque de Fuerza (Núcleo), y b) Inyecta o amplía automáticamente ejercicios específicos de movilidad previa (Pre) y masajes/estiramientos terapéuticos al final (Post).

6. Enfoque en Recuperación (Descansos, Descargas y Congelación):

- Días de Descanso Obligatorio: Programados para evitar sobreentrenamiento (protegen la racha de forma neutral).
- Descarga Dinámica Automatizada (Deload): Reduce el volumen e intensidad al 60% si se detecta fatiga acumulada tras varias semanas.
- Modo Congelación por Lesión: Pausa voluntaria para congelar rachas ante lesiones severas.

7. Android Home Screen Widget y Almacenamiento Local:

- Widget Informativo: Muestra la rutina de hoy, estado de la racha y estatus. Si toca Descanso Obligatorio o Semana de Descarga, el widget cambia su UI/texto para concientizar sobre la recuperación. Funciona como Deep Link.
- Persistencia Local y Backup Manual: 100% local (Room/SQLite). Opción de exportar base de datos a un archivo (JSON/DB) para respaldo manual en Google Drive.

Por favor , no investiges nada del directorio esoy recien iniciando. crea un documento estructurado talvez no tan visual pero si que sea mas entendible para ti que tenga la siguiente informacion :

1. Arquitectura de la Información y Sistema de Navegación (Bottom Navigation).
2. Detalle de Vistas (Screens): Pantallas clave de la sesión dividida en 3 fases (con sus respectivos temporizadores y layouts de carga de peso), componentes de la interfaz, manejo de estados y el diseño conceptual del Widget.
3. Modelo de Datos Local (Entidades y Relaciones): Estructura detallando tablas para perfiles, rutinas, ejercicios (diferenciando tipos de fuerza vs asistenciales), histórico de series con RIR y reportes de molestias vinculadas por etiquetas.
4. Flujos de Usuario paso a paso (User Journeys) para:
   A) Flujo completo de una sesión (Pre-Núcleo-Post) adaptada por una condición crónica/molestia (con inyección de masajes post-entrenamiento).
   B) Flujo de graduación de un ejercicio hacia una variante mecánica superior.
