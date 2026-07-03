package com.nobody.valurex.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.Loan
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.PillChip
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.viewmodel.LoansViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    onNavigateToDivide: () -> Unit,
    onNavigateToSplit: () -> Unit,
    vm: LoansViewModel = viewModel()
) {
    val loans         by vm.loans.collectAsState()
    var selectedTab   by remember { mutableStateOf("I_OWE") }
    var showAddDialog by remember { mutableStateOf(false) }
    val vc = ValurexColors
    val vt = ValurexTypography

    val filtered = loans.filter { it.direction == selectedTab }

    if (showAddDialog) {
        AddLoanDialog(
            onConfirm = { name, amount, direction, note ->
                vm.addLoan(name, amount, direction, note)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    Scaffold(
        containerColor      = vc.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick        = onNavigateToSplit,
                    containerColor = vc.Surface,
                    contentColor   = vc.Accent,
                    shape          = CircleShape,
                    modifier       = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Group, null)
                }
                FloatingActionButton(
                    onClick        = { showAddDialog = true },
                    containerColor = vc.Accent,
                    contentColor   = vc.TextHigh,
                    shape          = CircleShape
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp)
        ) {
            item {
                PageTitle(
                    text     = "Loans",
                    trailing = {
                        TextButton(onClick = onNavigateToDivide) {
                            Text("Divide", style = vt.pillButton, color = vc.Accent)
                        }
                    }
                )
            }

            item {
                Row(
                    modifier              = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillChip(
                        text     = "I owe",
                        selected = selectedTab == "I_OWE",
                        onClick  = { selectedTab = "I_OWE" }
                    )
                    PillChip(
                        text     = "Owed to me",
                        selected = selectedTab == "OWED_TO_ME",
                        onClick  = { selectedTab = "OWED_TO_ME" }
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No loans here", style = vt.rowTitle, color = vc.TextMedium)
                    }
                }
            } else {
                itemsIndexed(filtered, key = { _, it -> it.id }) { _, loan ->
                    LoanCard(
                        loan          = loan,
                        vc            = vc,
                        vt            = vt,
                        onMarkSettled = { vm.markSettled(loan.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoanCard(
    loan: Loan,
    vc: ValurexColors,
    vt: ValurexTypography,
    onMarkSettled: () -> Unit
) {
    val settled  = loan.settledAt != null
    val dateText = Instant.ofEpochMilli(loan.createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMM d"))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = shapeLarge,
        color = vc.Surface
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    loan.personName,
                    style = vt.rowTitle,
                    color = if (settled) vc.TextLow else vc.TextHigh
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Rs %,d".format(loan.amount),
                    style          = vt.amount,
                    textDecoration = if (settled) TextDecoration.LineThrough else TextDecoration.None,
                    color          = if (settled) vc.TextLow else vc.TextHigh
                )
                if (!loan.note.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(loan.note, style = vt.rowSubtitle, color = vc.TextMedium)
                }
                Spacer(Modifier.height(2.dp))
                Text(dateText, style = vt.rowSubtitle, color = vc.TextMedium)
            }
            if (!settled) {
                TextButton(
                    onClick = onMarkSettled,
                    colors  = ButtonDefaults.textButtonColors(contentColor = vc.Accent)
                ) {
                    Text("Mark settled", style = vt.pillButton)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLoanDialog(
    onConfirm: (name: String, amount: Int, direction: String, note: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    var name       by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var direction  by remember { mutableStateOf("I_OWE") }
    var note       by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        shape            = com.nobody.valurex.ui.theme.shapeLarge,
        title            = { Text("Add Loan", color = vc.TextHigh) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Person name") },
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
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RadioButton(
                        selected = direction == "I_OWE",
                        onClick  = { direction = "I_OWE" },
                        colors   = RadioButtonDefaults.colors(selectedColor = vc.Accent)
                    )
                    Text("I owe them", color = vc.TextHigh)
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = direction == "OWED_TO_ME",
                        onClick  = { direction = "OWED_TO_ME" },
                        colors   = RadioButtonDefaults.colors(selectedColor = vc.Accent)
                    )
                    Text("They owe me", color = vc.TextHigh)
                }
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Note (optional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
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
                    if (name.isNotBlank())
                        onConfirm(name.trim(), amount, direction, note.trim().ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
            ) { Text("Save") }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
        }
    )
}
