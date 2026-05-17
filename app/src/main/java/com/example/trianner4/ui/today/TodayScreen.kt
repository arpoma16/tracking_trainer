package com.example.trianner4.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trianner4.R
import com.example.trianner4.data.local.ExerciseType
import com.example.trianner4.data.local.TrackingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import com.example.trianner4.ui.theme.Trianner4Theme

@Composable
fun TodayScreen(
    onStartSession: (routineId: Long) -> Unit = {},
    onReportDiscomfort: () -> Unit = {},
    viewModel: TodayViewModel? = null,
) {
    if (LocalInspectionMode.current) {
        TodayScreenContent(TodayUiState.Loading)
    } else {
        val actualViewModel: TodayViewModel = viewModel ?: hiltViewModel()
        val uiState by actualViewModel.uiState.collectAsStateWithLifecycle()
        TodayScreenContent(
            uiState,
            onStartSession = onStartSession,
            onReportDiscomfort = onReportDiscomfort,
        )
    }
}

@Composable
fun TodayScreenContent(
    uiState: TodayUiState,
    onStartSession: (routineId: Long) -> Unit = {},
    onReportDiscomfort: () -> Unit = {},
) {
    when (val state = uiState) {
        is TodayUiState.Loading -> LoadingContent()
        is TodayUiState.NoRoutineToday -> NoRoutineContent(state)
        is TodayUiState.Ready -> ReadyContent(
            state,
            onStartSession = { onStartSession(state.routineId) },
            onReportDiscomfort = onReportDiscomfort,
        )
    }
}

@Preview(showBackground = true, name = "Hoy - Entrenamiento")
@Composable
private fun TodayScreenReadyPreview() {
    val mockState = TodayUiState.Ready(
        routineId = 1,
        routineName = "Empuje (Pecho/Hombro)",
        dayStatus = DayStatus.TRAINING,
        streak = StreakData(5, 85),
        plan = AdaptedPlan(
            preExercises = listOf(
                ExerciseItem(1, "Movilidad Hombro", ExerciseType.STRENGTH, TrackingMode.FIXED_REPS, targetSets = 2, targetReps = 10)
            ),
            coreExercises = listOf(
                ExerciseItem(2, "Press Banca", ExerciseType.STRENGTH, TrackingMode.WEIGHT_REPS, targetSets = 3, targetReps = 8),
                ExerciseItem(3, "Aperturas", ExerciseType.STRENGTH, TrackingMode.WEIGHT_REPS, targetSets = 3, targetReps = 12)
            ),
            postExercises = emptyList(),
            isAdapted = false,
            discomfortLabels = emptyList()
        )
    )
    Trianner4Theme {
        Surface {
            TodayScreenContent(uiState = mockState)
        }
    }
}

@Preview(showBackground = true, name = "Hoy - Adaptado")
@Composable
private fun TodayScreenAdaptedPreview() {
    val mockState = TodayUiState.Ready(
        routineId = 1,
        routineName = "Tren Inferior",
        dayStatus = DayStatus.ADAPTED,
        streak = StreakData(14, 92),
        plan = AdaptedPlan(
            preExercises = listOf(
                ExerciseItem(1, "Movilidad Cadera", ExerciseType.ASSISTIVE, TrackingMode.TIMER, targetDurationSec = 120),
                ExerciseItem(2, "Activación VMO", ExerciseType.ASSISTIVE, TrackingMode.TIMER, isInjected = true, discomfortLabel = "Condromalacia", targetDurationSec = 90),
            ),
            coreExercises = listOf(
                ExerciseItem(3, "Sentadilla", ExerciseType.STRENGTH, TrackingMode.WEIGHT_REPS, effectiveLoadFactor = 0.7, targetSets = 3, targetReps = 8),
                ExerciseItem(4, "Prensa", ExerciseType.STRENGTH, TrackingMode.WEIGHT_REPS, effectiveLoadFactor = 0.7, targetSets = 3, targetReps = 10),
            ),
            postExercises = listOf(
                ExerciseItem(5, "Liberación cuádriceps", ExerciseType.ASSISTIVE, TrackingMode.TIMER, isInjected = true, discomfortLabel = "Condromalacia", targetDurationSec = 120),
            ),
            isAdapted = true,
            discomfortLabels = listOf("Condromalacia rotuliana")
        )
    )
    Trianner4Theme {
        Surface {
            TodayScreenContent(uiState = mockState)
        }
    }
}

@Preview(showBackground = true, name = "Hoy - Descanso")
@Composable
private fun TodayScreenRestPreview() {
    val mockState = TodayUiState.NoRoutineToday(
        dayStatus = DayStatus.REST,
        streak = StreakData(5, 85)
    )
    Trianner4Theme {
        Surface {
            TodayScreenContent(uiState = mockState)
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ── No routine today (REST / DELOAD / FROZEN) ─────────────────────────────────

@Composable
private fun NoRoutineContent(state: TodayUiState.NoRoutineToday) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DayStatusHeader(state.dayStatus) }
            item { state.streak?.let { StreakCard(it) } }
            item { RestDayCard(state.dayStatus) }
        }
    }
}

// ── Ready (has routine) ────────────────────────────────────────────────────────

@Composable
private fun ReadyContent(
    state: TodayUiState.Ready,
    onStartSession: () -> Unit = {},
    onReportDiscomfort: () -> Unit = {},
) {
    var showAdaptationSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DayStatusHeader(state.dayStatus) }
            item { state.streak?.let { StreakCard(it) } }
            if (state.plan.isAdapted) {
                item {
                    AdaptationBanner(
                        discomfortLabels = state.plan.discomfortLabels,
                        onClick = { showAdaptationSheet = true }
                    )
                }
            }
            item { RoutinePreviewCard(state) }
        }

        // Botones sticky en el fondo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartSession,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.today_cta_start),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onReportDiscomfort) {
                    Text(stringResource(R.string.today_btn_report_discomfort))
                }
                TextButton(onClick = { /* saltar a descanso — implementar con TodayViewModel */ }) {
                    Text(stringResource(R.string.today_btn_skip_to_rest))
                }
            }
        }
    }

    if (showAdaptationSheet) {
        AdaptationDetailSheet(
            plan = state.plan,
            onDismiss = { showAdaptationSheet = false },
            onStartSession = {
                showAdaptationSheet = false
                onStartSession()
            }
        )
    }
}

// ── Componentes ────────────────────────────────────────────────────────────────

@Composable
private fun DayStatusHeader(dayStatus: DayStatus) {
    val (label, color) = dayStatusStyle(dayStatus)
    val dateLabel = LocalDate.now()
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.clip(RoundedCornerShape(6.dp))
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StreakCard(streak: StreakData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StreakItem(
                value = streak.weeklyConsistency,
                label = stringResource(R.string.streak_weekly_label),
                icon = "🔥"
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
            StreakItem(
                value = streak.routineAdherence,
                label = stringResource(R.string.streak_adherence_label),
                icon = "⛓️"
            )
        }
    }
}

@Composable
private fun StreakItem(value: Int, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(text = icon, style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdaptationBanner(discomfortLabels: List<String>, onClick: () -> Unit = {}) {
    val joined = discomfortLabels.joinToString(", ")
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF8E1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "⚠️", style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.today_adaptation_banner, joined),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF795548),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.today_adaptation_banner_cta),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF795548),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RoutinePreviewCard(state: TodayUiState.Ready) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = state.routineName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            PhaseSummaryRow(
                preCount = state.plan.preExercises.size,
                coreCount = state.plan.coreExercises.size,
                postCount = state.plan.postExercises.size,
                injectedPreCount = state.plan.preExercises.count { it.isInjected },
                injectedPostCount = state.plan.postExercises.count { it.isInjected }
            )

            if (state.plan.preExercises.isNotEmpty()
                || state.plan.coreExercises.isNotEmpty()
                || state.plan.postExercises.isNotEmpty()
            ) {
                HorizontalDivider()
                PhaseExerciseList(
                    phaseLabel = stringResource(R.string.phase_pre),
                    exercises = state.plan.preExercises
                )
                PhaseExerciseList(
                    phaseLabel = stringResource(R.string.phase_core),
                    exercises = state.plan.coreExercises
                )
                PhaseExerciseList(
                    phaseLabel = stringResource(R.string.phase_post),
                    exercises = state.plan.postExercises
                )
            }
        }
    }
}

@Composable
private fun PhaseSummaryRow(
    preCount: Int,
    coreCount: Int,
    postCount: Int,
    injectedPreCount: Int,
    injectedPostCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PhaseChip(
            label = stringResource(R.string.phase_pre),
            count = preCount,
            injectedCount = injectedPreCount,
            color = MaterialTheme.colorScheme.tertiary
        )
        PhaseChip(
            label = stringResource(R.string.phase_core),
            count = coreCount,
            injectedCount = 0,
            color = MaterialTheme.colorScheme.primary
        )
        PhaseChip(
            label = stringResource(R.string.phase_post),
            count = postCount,
            injectedCount = injectedPostCount,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun PhaseChip(label: String, count: Int, injectedCount: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (injectedCount > 0) {
            Text(
                text = stringResource(R.string.phase_injected_count, injectedCount),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF795548)
            )
        }
    }
}

@Composable
private fun PhaseExerciseList(phaseLabel: String, exercises: List<ExerciseItem>) {
    if (exercises.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        exercises.forEach { item ->
            ExerciseRow(item)
        }
    }
}

@Composable
private fun ExerciseRow(item: ExerciseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val typeIcon = if (item.type == ExerciseType.STRENGTH) "💪" else "⏱️"
        Text(text = typeIcon, style = MaterialTheme.typography.bodyMedium)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (item.isInjected) {
                    InjectedBadge()
                }
            }
            val subLabel = buildExerciseSubLabel(item)
            if (subLabel.isNotBlank()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item.effectiveLoadFactor?.let { factor ->
            val pct = (factor * 100).toInt()
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFF8F00).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$pct%",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InjectedBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF795548).copy(alpha = 0.12f)
    ) {
        Text(
            text = "🩹 adaptado",
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF795548)
        )
    }
}

@Composable
private fun RestDayCard(dayStatus: DayStatus) {
    val (icon, title, subtitle) = when (dayStatus) {
        DayStatus.REST -> Triple(
            "💤",
            stringResource(R.string.rest_day_title),
            stringResource(R.string.rest_day_subtitle)
        )
        DayStatus.DELOAD -> Triple(
            "🟠",
            stringResource(R.string.deload_day_title),
            stringResource(R.string.deload_day_subtitle)
        )
        DayStatus.FROZEN -> Triple(
            "❄️",
            stringResource(R.string.frozen_title),
            stringResource(R.string.frozen_subtitle)
        )
        else -> Triple("", "", "")
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.displayMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Adaptation detail sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptationDetailSheet(
    plan: AdaptedPlan,
    onDismiss: () -> Unit,
    onStartSession: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.adaptation_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (plan.discomfortLabels.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.adaptation_sheet_due_to,
                        plan.discomfortLabels.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF795548)
                )
            }

            val injectedPre = plan.preExercises.filter { it.isInjected }
            if (injectedPre.isNotEmpty()) {
                AdaptationSheetSection(
                    title = stringResource(R.string.adaptation_injected_pre),
                    items = injectedPre.map { "• ${it.name}" }
                )
            }

            val injectedPost = plan.postExercises.filter { it.isInjected }
            if (injectedPost.isNotEmpty()) {
                AdaptationSheetSection(
                    title = stringResource(R.string.adaptation_injected_post),
                    items = injectedPost.map { "• ${it.name}" }
                )
            }

            val loadReduced = plan.coreExercises.filter {
                it.effectiveLoadFactor != null && it.effectiveLoadFactor < 1.0
            }
            if (loadReduced.isNotEmpty()) {
                AdaptationSheetSection(
                    title = stringResource(R.string.adaptation_load_reduced),
                    items = loadReduced.map { ex ->
                        val cut = ((1.0 - (ex.effectiveLoadFactor ?: 1.0)) * 100).toInt()
                        "• ${ex.name}  (–$cut% carga)"
                    }
                )
            }

            HorizontalDivider()

            Button(
                onClick = onStartSession,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.adaptation_sheet_cta))
            }
        }
    }
}

@Composable
private fun AdaptationSheetSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        items.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun buildExerciseSubLabel(item: ExerciseItem): String {
    val parts = mutableListOf<String>()
    if (item.targetSets != null && item.targetReps != null) {
        parts += "${item.targetSets}×${item.targetReps}"
    } else if (item.targetDurationSec != null) {
        val min = item.targetDurationSec / 60
        val sec = item.targetDurationSec % 60
        parts += if (min > 0) "${min}m ${sec}s" else "${sec}s"
    }
    if (item.effectiveLoadFactor != null && !item.isInjected) {
        val pct = (100 - item.effectiveLoadFactor * 100).toInt()
        parts += "–$pct% carga"
    }
    return parts.joinToString(" · ")
}

private data class StatusStyle(val label: String, val color: Color)

@Composable
private fun dayStatusStyle(status: DayStatus): StatusStyle = when (status) {
    DayStatus.TRAINING -> StatusStyle(
        stringResource(R.string.status_training),
        MaterialTheme.colorScheme.primary
    )
    DayStatus.ADAPTED -> StatusStyle(
        stringResource(R.string.status_adapted),
        Color(0xFFF57F17)
    )
    DayStatus.DELOAD -> StatusStyle(
        stringResource(R.string.status_deload),
        Color(0xFFE65100)
    )
    DayStatus.FROZEN -> StatusStyle(
        stringResource(R.string.status_frozen),
        Color(0xFF0277BD)
    )
    DayStatus.REST -> StatusStyle(
        stringResource(R.string.status_rest),
        MaterialTheme.colorScheme.onSurfaceVariant
    )
}
