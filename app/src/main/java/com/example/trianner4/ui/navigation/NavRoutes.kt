package com.example.trianner4.ui.navigation

/** Rutas de cada pantalla del grafo de navegación. */
object NavRoutes {

    // ── Claves de argumentos ────────────────────────────────────────────────────
    const val ARG_FECHA = "fecha"
    const val ARG_RUTINA_ID = "rutinaId"
    const val ARG_EJERCICIO_ID = "ejercicioId"
    const val ARG_ROUTINE_ID_SESSION = "routineIdForSession"

    // ── Raíces de cada tab ──────────────────────────────────────────────────────
    const val HOY = "hoy"
    const val CALENDARIO = "calendario"
    const val RUTINAS = "rutinas"
    const val BIENESTAR = "bienestar"
    const val AJUSTES = "ajustes"

    // ── Sesión activa: modal top-level, oculta bottom bar ───────────────────────
    const val SESION_ACTIVA = "sesion/activa/{$ARG_ROUTINE_ID_SESSION}"
    fun sesionActiva(routineId: Long) = "sesion/activa/$routineId"

    // ── Sub-rutas: Calendario ───────────────────────────────────────────────────
    const val CALENDARIO_DIA = "calendario/dia/{$ARG_FECHA}"
    fun calendarioDia(fecha: String) = "calendario/dia/$fecha"

    // ── Sub-rutas: Rutinas ──────────────────────────────────────────────────────
    const val RUTINA_NUEVA = "rutinas/nueva"
    const val RUTINA_EDITOR = "rutinas/editor/{$ARG_RUTINA_ID}"
    fun rutinaEditor(rutinaId: Long) = "rutinas/editor/$rutinaId"
    const val BIBLIOTECA = "rutinas/biblioteca"
    const val EJERCICIO_DETALLE = "rutinas/biblioteca/{$ARG_EJERCICIO_ID}"
    fun ejercicioDetalle(ejercicioId: Long) = "rutinas/biblioteca/$ejercicioId"
    const val EJERCICIO_CREAR = "rutinas/ejercicio/nuevo"
    const val EJERCICIO_EDITOR = "rutinas/ejercicio/editor/{$ARG_EJERCICIO_ID}"
    fun ejercicioEditor(ejercicioId: Long) = "rutinas/ejercicio/editor/$ejercicioId"

    // ── Sub-rutas: Bienestar ─────────────────────────────────────────────────────

    // ── Sub-rutas: Ajustes ──────────────────────────────────────────────────────
    const val PERFIL = "ajustes/perfil"
    const val TRIGGERS = "ajustes/triggers"
    const val BACKUP = "ajustes/backup"
}

/** Rutas de los grafos anidados por tab — usadas para switching en bottom bar. */
object TabGraph {
    const val HOY = "hoy_graph"
    const val CALENDARIO = "calendario_graph"
    const val RUTINAS = "rutinas_graph"
    const val BIENESTAR = "bienestar_graph"
    const val AJUSTES = "ajustes_graph"

    val all = setOf(HOY, CALENDARIO, RUTINAS, BIENESTAR, AJUSTES)
}
