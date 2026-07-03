package com.nobody.valurex.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.RecurringExpense
import com.nobody.valurex.ui.components.ManageRow
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.RowPosition
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    onNavigateBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val recurringList by vm.recurringExpenses.collectAsState()
    val categories    by vm.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget    by remember { mutableStateOf<RecurringExpense?>(null) }
    var deleteTarget  by remember { mutableStateOf<RecurringExpense?>(null) }
    val vc = ValurexColors

    if (showAddDialog) {
        RecurringDialog(
            initial    = null,
            categories = categories,
            onConfirm  = { name, amount, catId, freq, day ->
                vm.addRecurring(name, amount, catId, freq, day)
                showAddDialog = false
            },
            onDelete   = null,
            onDismiss  = { showAddDialog = false }
        )
    }

    editTarget?.let { rec ->
        RecurringDialog(
            initial    = rec,
            categories = categories,
            onConfirm  = { name, amount, catId, freq, day ->
                vm.updateRecurring(rec.copy(
                    name        = name,
                    amount      = amount,
                    categoryId  = catId,
                    frequency   = freq,
                    dayOfPeriod = day
                ))
                editTarget = null
            },
            onDelete   = { deleteTarget = rec; editTarget = null },
            onDismiss  = { editTarget = null }
        )
    }

    deleteTarget?.let { rec ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Delete \"${rec.name}\"?", color = vc.TextHigh) },
            text             = { Text("This cannot be undone.", color = vc.TextMedium) },
            confirmButton    = {
                Button(
                    onClick = { vm.deleteRecurring(rec); deleteTarget = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = vc.ExpenseRed, contentColor = vc.TextHigh)
                ) { Text("Delete") }
            },
            dismissButton    = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = vc.TextMedium) }
            }
        )
    }

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddDialog = true },
                containerColor = vc.Accent,
                contentColor   = vc.TextHigh,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp)
        ) {
            item {
                PageTitle(
                    text    = "Recurring",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            if (recurringList.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recurring expenses yet", style = ValurexTypography.rowTitle, color = vc.TextMedium)
                    }
                }
            } else {
                itemsIndexed(recurringList, key = { _, it -> it.id }) { index, rec ->
                    val dayLabel = when {
                        rec.frequency == "WEEKLY" -> {
                            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            "Every ${days.getOrElse(rec.dayOfPeriod - 1) { "?" }}"
                        }
                        else -> "Day ${rec.dayOfPeriod} of month"
                    }
                    val position = when {
                        recurringList.size == 1      -> RowPosition.SINGLE
                        index == 0                   -> RowPosition.TOP
                        index == recurringList.lastIndex -> RowPosition.BOTTOM
                        else                         -> RowPosition.MIDDLE
                    }
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        ManageRow(
                            icon     = Icons.Outlined.Repeat,
                            iconBg   = vc.IconBgRecurring,
                            iconFg   = vc.IconFgFood,
                            title    = rec.name,
                            subtitle = "Rs %,d · $dayLabel".format(rec.amount),
                            position = position,
                            onClick  = { editTarget = rec }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringDialog(
    initial: RecurringExpense?,
    categories: List<Category>,
    onConfirm: (name: String, amount: Int, categoryId: Long, frequency: String, dayOfPeriod: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    var name       by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var selCat     by remember { mutableStateOf(categories.find { it.id == initial?.categoryId } ?: categories.firstOrNull()) }
    var frequency  by remember { mutableStateOf(initial?.frequency ?: "MONTHLY") }
    var dayText    by remember { mutableStateOf(initial?.dayOfPeriod?.toString() ?: "1") }
    var expanded   by remember { mutableStateOf(false) }

    LaunchedEffect(categories) { if (selCat == null) selCat = categories.firstOrNull() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        shape            = shapeLarge,
        title            = { Text(if (initial == null) "Add Recurring" else "Edit Recurring", color = vc.TextHigh) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = vc.Accent,
                        unfocusedBorderColor = vc.Hairline,
                        cursorColor          = vc.Accent
                    )
                )
                OutlinedTextField(
                    value           = amountText,
                    onValueChange   = { amountText = it.filter(Char::isDigit) },
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
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value         = selCat?.name ?: "",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Category") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = vc.Accent,
                            unfocusedBorderColor = vc.Hairline
                        )
                    )
                    ExposedDropdownMenu(
                        expanded         = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor   = vc.Surface
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text    = { Text(cat.name, color = vc.TextHigh) },
                                onClick = { selCat = cat; expanded = false }
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = frequency == "MONTHLY",
                        onClick  = { frequency = "MONTHLY" },
                        colors   = RadioButtonDefaults.colors(selectedColor = vc.Accent)
                    )
                    Text("Monthly", color = vc.TextHigh)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = frequency == "WEEKLY",
                        onClick  = { frequency = "WEEKLY" },
                        colors   = RadioButtonDefaults.colors(selectedColor = vc.Accent)
                    )
                    Text("Weekly", color = vc.TextHigh)
                }
                OutlinedTextField(
                    value           = dayText,
                    onValueChange   = { dayText = it.filter(Char::isDigit) },
                    label           = { Text(if (frequency == "WEEKLY") "Day (1=Mon … 7=Sun)" else "Day of month (1–31)") },
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
                    val amount = amountText.toIntOrNull() ?: return@Button
                    val catId  = selCat?.id ?: return@Button
                    val day    = dayText.toIntOrNull()
                        ?.coerceIn(1, if (frequency == "WEEKLY") 7 else 31)
                        ?: return@Button
                    if (name.isNotBlank()) onConfirm(name.trim(), amount, catId, frequency, day)
                },
                colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
            ) { Text("Save") }
        },
        dismissButton    = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = vc.ExpenseRed) }
                    Spacer(Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
            }
        }
    )
}
