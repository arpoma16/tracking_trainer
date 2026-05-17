package com.example.trianner4.ui.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trianner4.R
import com.example.trianner4.data.local.SessionStatus
import com.example.trianner4.ui.theme.Trianner4Theme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// ── Pantalla principal: Vista mensual ───────────────────────────────────────────

@Composable
fun CalendarioScreen(
    onDayClick: (fecha: String) -> Unit = {},
    viewModel: CalendarioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    CalendarioScreenContent(
        uiState = uiState,
        onDayClick = { date ->
            viewModel.selectDay(date)
            onDayClick(date.toString())
        },
        onNavigateMonth = viewModel::navigateMonth,
        onClearSelection = viewModel::clearSelection
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreenContent(
    uiState: CalendarioUiState,
    onDayClick: (LocalDate) -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onClearSelection: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_calendar)) })
        }
    ) { innerPadding ->
        when (val state = uiState) {
            CalendarioUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is CalendarioUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            is CalendarioUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                ) {
                    MonthHeader(
                        yearMonth = state.currentMonth,
                        onPrevious = { onNavigateMonth(-1) },
                        onNext = { onNavigateMonth(1) },
                    )
                    Spacer(Modifier.height(8.dp))
                    WeekDayLabels()
                    Spacer(Modifier.height(4.dp))
                    MonthGrid(
                        yearMonth = state.currentMonth,
                        days = state.days,
                        onDayClick = onDayClick,
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    MonthlySummaryRow(state.summary)
                    Spacer(Modifier.height(8.dp))
                    SessionStatusLegend()
                }

                // Bottom sheet de detalle de día (preloaded)
                state.selectedDayDetail?.let { detail ->
                    DayDetailSheet(
                        detail = detail,
                        onDismiss = onClearSelection
                    )
                }
            }
        }
    }
}

// ── Cabecera del mes ───────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val label = yearMonth
        .atDay(1)
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        .replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.calendar_prev_month)
            )
        }
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.calendar_next_month)
            )
        }
    }
}

// ── Etiquetas de día de semana ─────────────────────────────────────────────────

@Composable
private fun WeekDayLabels() {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(Modifier.fillMaxWidth()) {
        days.forEach { d ->
            Text(
                text = d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Grid mensual ───────────────────────────────────────────────────────────────

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    days: List<DayEntry>,
    onDayClick: (LocalDate) -> Unit,
) {
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value   // 1=Lun … 7=Dom
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = firstDayOfWeek - 1 + daysInMonth
    val rows = (totalCells + 6) / 7
    val today = LocalDate.now()

    // Indexar DayEntry por día del mes para acceso O(1)
    val entryByDay = days.associateBy { it.date.dayOfMonth }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNumber = row * 7 + col - (firstDayOfWeek - 2)
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = yearMonth.atDay(dayNumber)
                        DayCell(
                            day = dayNumber,
                            isToday = date == today,
                            entry = entryByDay[dayNumber],
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(date) },
                        )
                    }
                }
            }
        }
    }
}

// ── Celda de día con color de estado ──────────────────────────────────────────

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    entry: DayEntry?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isToday) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
    val statusColor = entry?.let { dayStatusColor(it) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .semantics { contentDescription = day.toString() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = day.toString(), style = MaterialTheme.typography.bodyMedium, color = textColor)
            if (statusColor != null && !isToday) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(statusColor, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
private fun dayStatusColor(entry: DayEntry): Color? = when {
    entry.isAdapted -> Color(0xFF9C27B0)     // morado
    entry.isDeload -> Color(0xFF1565C0)       // azul
    entry.isFrozen -> Color(0xFF0097A7)       // cian / nieve
    entry.status == SessionStatus.COMPLETED -> Color(0xFF2E7D32)   // verde sólido
    entry.status == SessionStatus.PARTIAL -> Color(0xFFF9A825)     // amarillo
    entry.status == SessionStatus.SKIPPED -> Color(0xFF616161)     // gris oscuro
    else -> null
}

// ── Resumen mensual ────────────────────────────────────────────────────────────

@Composable
private fun MonthlySummaryRow(summary: MonthlySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryChip(label = "Sesiones", value = summary.totalSessions.toString())
        SummaryChip(label = "Completadas", value = summary.completedSessions.toString())
        SummaryChip(label = "Adaptadas", value = summary.adaptedSessions.toString())
        SummaryChip(label = "Adherencia", value = "${summary.adherencePercent}%")
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Leyenda de colores ────────────────────────────────────────────────────────

@Composable
private fun SessionStatusLegend() {
    val items = listOf(
        Color(0xFF2E7D32) to "Completado",
        Color(0xFFF9A825) to "Parcial",
        Color(0xFF9C27B0) to "Adaptado",
        Color(0xFF1565C0) to "Deload",
        Color(0xFF616161) to "Saltado",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (color, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(Modifier.size(8.dp).background(color, CircleShape))
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Bottom sheet: detalle del día ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(detail: DayDetail, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        val dateLabel = detail.session.date.let { epoch ->
            java.time.Instant.ofEpochMilli(epoch)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Estado: ${detail.session.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            detail.session.totalTonnage?.let { tonnage ->
                Text(
                    text = "Tonelaje: ${"%.1f".format(tonnage)} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (detail.snapshots.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Ejercicios realizados (${detail.snapshots.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(detail.snapshots) { snap ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = snap.exerciseNameSnapshot,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            if (detail.adaptations.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Adaptaciones aplicadas (${detail.adaptations.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

// ── Pantalla de detalle de día ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioDiaScreen(
    fecha: String,
    onBack: () -> Unit = {},
) {
    val date = runCatching { LocalDate.parse(fecha) }.getOrNull()
    val label = date?.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
    ) ?: fecha

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.calendar_day_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun CalendarioScreenPreview() {
    Trianner4Theme {
        CalendarioScreenContent(
            uiState = CalendarioUiState.Ready(
                currentMonth = YearMonth.now(),
                days = emptyList(),
                summary = MonthlySummary(0, 0, 0, 0),
                selectedDayDetail = null
            ),
            onDayClick = {},
            onNavigateMonth = {},
            onClearSelection = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarioDiaScreenPreview() {
    Trianner4Theme { CalendarioDiaScreen(fecha = LocalDate.now().toString()) }
}
