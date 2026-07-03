package com.nobody.valurex.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.BudgetSettings
import com.nobody.valurex.ui.components.ManageRow
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.RowPosition
import com.nobody.valurex.ui.components.SectionLabel
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapePill
import com.nobody.valurex.ui.viewmodel.ReminderViewModel
import com.nobody.valurex.ui.viewmodel.SettingsViewModel

@Composable
fun AccountScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToReminders: () -> Unit,
    vm: SettingsViewModel = viewModel(),
    rvm: ReminderViewModel = viewModel()
) {
    val budget     by vm.budgetSettings.collectAsState()
    val recurring  by vm.recurringExpenses.collectAsState()
    val categories by vm.categories.collectAsState()
    val reminders  by rvm.settings.collectAsState()
    var showBudgetDialog  by remember { mutableStateOf(false) }
    var showSeedDialog    by remember { mutableStateOf(false) }
    var showSeedDoneDialog by remember { mutableStateOf(false) }
    val vc = ValurexColors

    if (showSeedDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDialog = false },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Load demo data?", color = vc.TextHigh) },
            text             = {
                Text(
                    "This will add 2 months of realistic transactions, loans, wishlist items, and recurring expenses so every screen looks complete.",
                    color = vc.TextMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSeedDialog = false
                        vm.seedDemoData { showSeedDoneDialog = true }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Load") }
            },
            dismissButton = {
                TextButton(onClick = { showSeedDialog = false }) { Text("Cancel", color = vc.TextMedium) }
            }
        )
    }

    if (showSeedDoneDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDoneDialog = false },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Done!", color = vc.TextHigh) },
            text             = { Text("Demo data loaded. Every screen now has realistic data ready for screenshots.", color = vc.TextMedium) },
            confirmButton    = {
                Button(
                    onClick = { showSeedDoneDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Got it") }
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            current   = budget,
            onConfirm = { amount, period ->
                vm.saveSettings(budget.copy(monthlyBudget = amount, budgetPeriod = period))
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
        ) {
            item {
                PageTitle(
                    text     = "Account",
                    trailing = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, null, tint = vc.TextMedium)
                        }
                    }
                )
            }

            item { SectionLabel("Manage") }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    val periodLabel    = if (budget.budgetPeriod == "WEEKLY") "Weekly" else "Monthly"
                    val budgetSubtitle = if (budget.monthlyBudget == 0) "Not set"
                        else "$periodLabel · Rs %,d".format(budget.monthlyBudget)

                    ManageRow(
                        icon     = Icons.Outlined.Tune,
                        iconBg   = vc.IconBgBudget,
                        iconFg   = vc.Accent,
                        title    = "Budget",
                        subtitle = budgetSubtitle,
                        position = RowPosition.TOP,
                        onClick  = { showBudgetDialog = true }
                    )

                    val recurringSize     = recurring.size
                    val recurringSubtitle = if (recurringSize == 0) "No items yet"
                        else "$recurringSize item${if (recurringSize == 1) "" else "s"}"

                    ManageRow(
                        icon     = Icons.Outlined.Repeat,
                        iconBg   = vc.IconBgRecurring,
                        iconFg   = vc.IconFgFood,
                        title    = "Recurring expenses",
                        subtitle = recurringSubtitle,
                        position = RowPosition.MIDDLE,
                        onClick  = onNavigateToRecurring
                    )

                    val catSize      = categories.size
                    val catSubtitle  = "$catSize categor${if (catSize == 1) "y" else "ies"}"

                    ManageRow(
                        icon     = Icons.Outlined.Category,
                        iconBg   = vc.IconBgCategories,
                        iconFg   = vc.IncomeGreen,
                        title    = "Categories",
                        subtitle = catSubtitle,
                        position = RowPosition.MIDDLE,
                        onClick  = onNavigateToCategories
                    )

                    val activeReminders = listOf(
                        reminders.nightly_enabled,
                        reminders.daily1_enabled,
                        reminders.daily2_enabled,
                        reminders.daily3_enabled
                    ).count { it }
                    val reminderSubtitle = if (activeReminders == 0) "Not set up"
                        else "$activeReminders active"

                    ManageRow(
                        icon     = Icons.Outlined.Notifications,
                        iconBg   = vc.IconBgBudget, // bell suggests yellow/orange but I'll use budget bg (purple) or similar
                        iconFg   = vc.Accent,
                        title    = "Reminders",
                        subtitle = reminderSubtitle,
                        position = RowPosition.BOTTOM,
                        onClick  = onNavigateToReminders
                    )
                }
            }

            item { SectionLabel("Demo") }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    ManageRow(
                        icon     = Icons.Outlined.AutoAwesome,
                        iconBg   = vc.IconBgBudget,
                        iconFg   = vc.Accent,
                        title    = "Load demo data",
                        subtitle = "2 months of realistic transactions",
                        position = RowPosition.SINGLE,
                        onClick  = { showSeedDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    current: BudgetSettings,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    val vt = ValurexTypography
    var text   by remember { mutableStateOf(if (current.monthlyBudget == 0) "" else current.monthlyBudget.toString()) }
    var period by remember { mutableStateOf(current.budgetPeriod) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        shape            = shapeLarge,
        title            = { Text("Set budget", color = vc.TextHigh) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(vc.Background, shapePill)
                ) {
                    listOf("MONTHLY" to "Monthly", "WEEKLY" to "Weekly").forEach { (value, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (period == value) vc.Accent else Color.Transparent,
                                    shapePill
                                )
                                .clickable { period = value }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                style = vt.pillButton,
                                color = if (period == value) vc.TextHigh else vc.TextMedium
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value           = text,
                    onValueChange   = { text = it.filter(Char::isDigit) },
                    label           = { Text("Amount (Rs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = vc.Accent,
                        unfocusedBorderColor = vc.Hairline,
                        cursorColor          = vc.Accent
                    )
                )
            }
        },
        confirmButton    = {
            Button(
                onClick = {
                    val amount = text.toIntOrNull() ?: return@Button
                    onConfirm(amount, period)
                },
                colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
            ) { Text("Save") }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
        }
    )
}
