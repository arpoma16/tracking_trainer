package com.example.trianner4.ui.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.trianner4.domain.today.TodayRepository
import com.example.trianner4.ui.today.DayStatus
import com.example.trianner4.ui.today.TodayUiState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class TrainingWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun todayRepository(): TodayRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).todayRepository()

        val state = try {
            repository.observeTodayState().first()
        } catch (e: Exception) {
            TodayUiState.Loading
        }

        provideContent {
            GlanceTheme {
                WidgetContent(state)
            }
        }
    }

    @Composable
    private fun WidgetContent(state: TodayUiState) {
        val context = LocalContext.current
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("app://trianner/today")
        ).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val backgroundColor = when (state) {
            is TodayUiState.Ready -> {
                if (state.dayStatus == DayStatus.DELOAD) GlanceTheme.colors.secondaryContainer
                else GlanceTheme.colors.primaryContainer
            }
            is TodayUiState.NoRoutineToday -> {
                if (state.dayStatus == DayStatus.REST || state.dayStatus == DayStatus.DELOAD) {
                    GlanceTheme.colors.secondaryContainer
                } else {
                    GlanceTheme.colors.surfaceVariant
                }
            }
            else -> GlanceTheme.colors.surfaceVariant
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(12.dp)
                .clickable(actionStartActivity(deepLinkIntent))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is TodayUiState.Ready -> {
                        Text(
                            text = "Hoy: ${state.routineName}",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GlanceTheme.colors.onPrimaryContainer
                            )
                        )
                        state.streak?.let {
                            Spacer(GlanceModifier.size(4.dp))
                            Text(
                                text = "🔥 Racha: ${it.routineAdherence} días",
                                style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onPrimaryContainer)
                            )
                        }
                    }
                    is TodayUiState.NoRoutineToday -> {
                        val msg = when (state.dayStatus) {
                            DayStatus.REST -> "Descanso Obligatorio"
                            DayStatus.DELOAD -> "Semana de Descarga"
                            DayStatus.FROZEN -> "App Congelada"
                            else -> "Recuperación Activa"
                        }
                        Text(
                            text = msg,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GlanceTheme.colors.onSecondaryContainer
                            )
                        )
                        Spacer(GlanceModifier.size(4.dp))
                        Text(
                            text = "Recuperación Activa",
                            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSecondaryContainer)
                        )
                    }
                    else -> {
                        Text(
                            text = "Trianner",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }
                }
            }
        }
    }
}
