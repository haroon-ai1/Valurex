package com.nobody.valurex.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.ui.components.CategoryIcon
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.PillChip
import com.nobody.valurex.ui.components.StatTile
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.viewmodel.CategoryStat
import com.nobody.valurex.ui.viewmodel.DailyStat
import com.nobody.valurex.ui.viewmodel.StatsUiState
import com.nobody.valurex.ui.viewmodel.StatsViewModel
import com.nobody.valurex.ui.viewmodel.TimeRange
import com.nobody.valurex.ui.viewmodel.TransactionWithCategory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val range by vm.selectedRange.collectAsState()
    val vc = ValurexColors
    val vt = ValurexTypography

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
        ) {
            item { PageTitle(text = "Stats") }

            item {
                Row(
                    modifier              = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { r ->
                        PillChip(text = r.label, selected = range == r, onClick = { vm.selectRange(r) })
                    }
                }
            }

            if (state.totalSpent == 0 && state.totalIncome == 0) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data for this period", style = vt.rowTitle, color = vc.TextMedium)
                    }
                }
            } else {
                item { StatTileGrid(state, vc) }

                if (state.categoryStats.isNotEmpty()) {
                    item { DonutCard(state, vc, vt) }
                }

                if (state.dailyStats.isNotEmpty()) {
                    item { BarChartCard(state.dailyStats, vc, vt) }
                }

                state.biggestTransaction?.let { twc ->
                    item { BiggestSpendCard(twc, vc, vt) }
                }
            }
        }
    }
}

@Composable
private fun StatTileGrid(state: StatsUiState, vc: ValurexColors) {
    Column(
        modifier            = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                label      = "Spent",
                value      = "Rs %,d".format(state.totalSpent),
                valueColor = if (state.totalSpent > 0) vc.ExpenseRed else vc.TextHigh,
                modifier   = Modifier.weight(1f)
            )
            StatTile(
                label      = "Income",
                value      = "Rs %,d".format(state.totalIncome),
                valueColor = if (state.totalIncome > 0) vc.IncomeGreen else vc.TextHigh,
                modifier   = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val netColor = when {
                state.net > 0 -> vc.IncomeGreen
                state.net < 0 -> vc.ExpenseRed
                else          -> vc.TextHigh
            }
            StatTile(
                label      = "Net",
                value      = "Rs %,d".format(state.net),
                valueColor = netColor,
                modifier   = Modifier.weight(1f)
            )
            StatTile(
                label    = "Avg / day",
                value    = "Rs %,d".format(state.avgDailySpend),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DonutCard(state: StatsUiState, vc: ValurexColors, vt: ValurexTypography) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp),
        shape = shapeLarge,
        color = vc.Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("BY CATEGORY", style = vt.sectionLabel, color = vc.TextMedium)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                DonutChart(
                    categories  = state.categoryStats,
                    totalAmount = state.totalSpent,
                    modifier    = Modifier.size(110.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.categoryStats.take(5).forEach { stat ->
                        CategoryLegendRow(stat, vc, vt)
                    }
                    if (state.categoryStats.size > 5) {
                        val othersAmount = state.categoryStats.drop(5).sumOf { it.amount }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(vc.TextLow, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Other", style = vt.rowSubtitle, color = vc.TextMedium,
                                modifier = Modifier.weight(1f))
                            Text("Rs %,d".format(othersAmount), style = vt.rowSubtitle, color = vc.TextMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendRow(stat: CategoryStat, vc: ValurexColors, vt: ValurexTypography) {
    val color = runCatching {
        Color(android.graphics.Color.parseColor(stat.category.color))
    }.getOrElse { Color.Gray }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            stat.category.name,
            style    = vt.rowSubtitle,
            color    = vc.TextHigh,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text("${(stat.percent * 100).toInt()}%", style = vt.rowSubtitle, color = vc.TextMedium)
    }
}

@Composable
private fun DonutChart(
    categories: List<CategoryStat>,
    totalAmount: Int,
    modifier: Modifier = Modifier
) {
    val vc = ValurexColors
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw     = 14.dp.toPx()
            val radius = (size.minDimension / 2f) - sw / 2f
            val cx     = size.width / 2f
            val cy     = size.height / 2f
            val tl     = Offset(cx - radius, cy - radius)
            val sz     = Size(radius * 2, radius * 2)

            drawArc(
                color      = vc.Hairline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = tl,
                size       = sz,
                style      = Stroke(width = sw)
            )

            var startAngle = -90f
            categories.take(5).forEach { stat ->
                val color = runCatching {
                    Color(android.graphics.Color.parseColor(stat.category.color))
                }.getOrElse { vc.TextMedium }
                val sweep = 360f * stat.percent
                if (sweep > 0f) {
                    drawArc(
                        color      = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter  = false,
                        topLeft    = tl,
                        size       = sz,
                        style      = Stroke(width = sw, cap = StrokeCap.Butt)
                    )
                }
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOTAL", style = ValurexTypography.captionTiny, color = ValurexColors.TextMedium)
            Text(
                "Rs %,d".format(totalAmount),
                style = ValurexTypography.rowSubtitle.copy(fontWeight = FontWeight.Medium),
                color = ValurexColors.TextHigh
            )
        }
    }
}

@Composable
private fun BarChartCard(dailyStats: List<DailyStat>, vc: ValurexColors, vt: ValurexTypography) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp),
        shape = shapeLarge,
        color = vc.Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("DAILY SPENDING", style = vt.sectionLabel, color = vc.TextMedium)
            Spacer(Modifier.height(16.dp))
            BarChart(
                dailyStats    = dailyStats,
                accentColor   = vc.Accent,
                defaultColor  = vc.SurfaceElevated,
                hairlineColor = vc.Hairline,
                modifier      = Modifier.fillMaxWidth().height(120.dp)
            )
            if (dailyStats.size >= 2) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dailyStats.first().label, style = vt.captionTiny, color = vc.TextMedium)
                    Text(dailyStats.last().label, style = vt.captionTiny, color = vc.TextMedium)
                }
            }
        }
    }
}

@Composable
private fun BarChart(
    dailyStats: List<DailyStat>,
    accentColor: Color,
    defaultColor: Color,
    hairlineColor: Color,
    modifier: Modifier = Modifier
) {
    val lastTwoIndices = dailyStats.indices.toList().takeLast(2).toSet()
    Canvas(modifier = modifier) {
        if (dailyStats.isEmpty()) return@Canvas
        val maxAmount = dailyStats.maxOf { it.amount }.toFloat().coerceAtLeast(1f)
        val barCount  = dailyStats.size
        val gap       = 3.dp.toPx()
        val barWidth  = (size.width - gap * (barCount - 1)) / barCount

        drawLine(
            color       = hairlineColor,
            start       = Offset(0f, size.height),
            end         = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        dailyStats.forEachIndexed { i, stat ->
            val barH = (stat.amount.toFloat() / maxAmount) * (size.height - 2.dp.toPx())
            val left = i * (barWidth + gap)
            val top  = size.height - barH
            if (barH > 0f) {
                drawRoundRect(
                    color        = if (i in lastTwoIndices) accentColor else defaultColor,
                    topLeft      = Offset(left, top),
                    size         = Size(barWidth, barH),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun BiggestSpendCard(
    twc: TransactionWithCategory,
    vc: ValurexColors,
    vt: ValurexTypography
) {
    val t = twc.transaction
    val dateStr = Instant.ofEpochMilli(t.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp),
        shape = shapeLarge,
        color = vc.Surface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("BIGGEST SPEND", style = vt.sectionLabel, color = vc.TextMedium)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(category = twc.category, size = 38.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(twc.category.name, style = vt.rowTitle, color = vc.TextHigh)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (!t.note.isNullOrBlank()) "${t.note} · $dateStr" else dateStr,
                        style = vt.rowSubtitle,
                        color = vc.TextMedium
                    )
                }
                Text("Rs %,d".format(t.amount), style = vt.statTileNumber, color = vc.ExpenseRed)
            }
        }
    }
}
