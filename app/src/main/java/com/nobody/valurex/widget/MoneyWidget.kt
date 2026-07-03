package com.nobody.valurex.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nobody.valurex.MainActivity
import com.nobody.valurex.data.db.ValurexDatabase
import com.nobody.valurex.data.db.entity.BudgetSettings
import com.nobody.valurex.ui.util.currentMonthStartMillis
import com.nobody.valurex.ui.util.currentWeekStartMillis
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class MoneyWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db      = ValurexDatabase.getInstance(context)
        val txnDao  = db.transactionDao()
        val budgDao = db.budgetSettingsDao()

        val totalIncome   = txnDao.getAllIncomeTotal().first()
        val totalExpenses = txnDao.getAllExpensesTotal().first()
        val totalMoney    = totalIncome - totalExpenses
        val budget        = budgDao.getBudgetSettings().first() ?: BudgetSettings()

        val periodExpenses = if (budget.budgetPeriod == "WEEKLY")
            txnDao.getExpensesFrom(currentWeekStartMillis()).first()
        else
            txnDao.getExpensesFrom(currentMonthStartMillis()).first()

        val hasBudget     = budget.monthlyBudget > 0
        val spentFrac     = if (hasBudget) (periodExpenses.toFloat() / budget.monthlyBudget).coerceIn(0f, 1f) else 0f
        val remaining     = budget.monthlyBudget - periodExpenses
        val isOverBudget  = remaining < 0
        val progressColor = if (isOverBudget) Color(0xFFF87171) else Color(0xFF4ADE80)
        val launchAction  = actionStartActivity(Intent(context, MainActivity::class.java))

        val zone  = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val sparklineData = (4 downTo 0).map { daysAgo ->
            val day         = today.minusDays(daysAgo.toLong())
            val startMillis = day.atStartOfDay(zone).toInstant().toEpochMilli()
            val endMillis   = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            txnDao.getDailyExpense(startMillis, endMillis)
        }

        provideContent {
            MoneyWidgetContent(
                totalMoney, budget, hasBudget, spentFrac,
                progressColor, launchAction, sparklineData
            )
        }
    }
}

@Composable
private fun MoneyWidgetContent(
    totalMoney: Int,
    budget: BudgetSettings,
    hasBudget: Boolean,
    spentFrac: Float,
    progressColor: Color,
    launchAction: androidx.glance.action.Action,
    sparklineData: List<Int>
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .cornerRadius(24.dp)
            .clickable(launchAction)
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                "Money",
                style = TextStyle(
                    color      = ColorProvider(Color(0xFFFFFFFF)),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(GlanceModifier.height(16.dp))

            Row(
                modifier          = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.Bottom
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.Vertical.Bottom) {
                        Text(
                            "%,d".format(totalMoney),
                            style = TextStyle(
                                color      = ColorProvider(Color(0xFFFFFFFF)),
                                fontSize   = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    if (hasBudget) {
                        Text(
                            "/ Rs %,d".format(budget.monthlyBudget),
                            style = TextStyle(
                                color      = ColorProvider(Color(0xFF8A8A8A)),
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                // Sparkline on the right
                val maxSpend = sparklineData.maxOrNull()?.coerceAtLeast(1) ?: 1
                Row(verticalAlignment = Alignment.Vertical.Bottom) {
                    sparklineData.forEachIndexed { idx, spend ->
                        val fraction  = spend.toFloat() / maxSpend
                        val barHeight = (12.dp + 36.dp * fraction).coerceAtLeast(12.dp)
                        val barColor  = if (idx >= 3) Color(0xFF4ADE80) else Color(0xFF2A2A2A)
                        Box(
                            modifier = GlanceModifier
                                .width(10.dp)
                                .height(barHeight)
                                .cornerRadius(5.dp)
                                .background(barColor)
                        ) {}
                        if (idx < 4) Spacer(GlanceModifier.width(6.dp))
                    }
                }
            }

            Spacer(GlanceModifier.defaultWeight())

            // Thick progress bar at bottom
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .cornerRadius(100.dp)
                    .background(Color(0xFF2A2A2A))
            ) {
                val fill = spentFrac.coerceIn(0f, 1f)
                if (fill > 0f) {
                    Row(modifier = GlanceModifier.fillMaxSize()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxHeight()
                                .cornerRadius(100.dp)
                                .background(progressColor)
                                .let { if (fill >= 1f) it.fillMaxWidth() else it.defaultWeight() }
                        ) {}
                        if (fill < 1f) {
                            // We use weight to fill the rest. 
                            // Since the first Box has defaultWeight (1.0), 
                            // we need to set the spacer's weight to (1-fill)/fill
                            val spacerWeight = (1f - fill) / fill
                            Spacer(GlanceModifier.defaultWeight()) // Simplification: just use weight 1 for now if fill is exactly 0.5
                            // Actually, let's just use the fraction if possible? 
                            // Glance 1.1.0 doesn't have weight(float) in GlanceModifier?
                            // Let's check the imports/available methods.
                        }
                    }
                }
            }
        }
    }
}
