package com.nobody.valurex.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.ui.components.CategoryIcon
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.util.formatRelativeTime
import com.nobody.valurex.ui.viewmodel.TransactionWithCategory
import com.nobody.valurex.ui.viewmodel.WalletCheckinViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun WalletCheckinScreen(
    onNavigateBack: () -> Unit,
    vm: WalletCheckinViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    var actualText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf<Int?>(null) }
    val vc = ValurexColors
    val vt = ValurexTypography
    val scope = rememberCoroutineScope()

    if (showDialog != null) {
        val diff = showDialog!!
        AlertDialog(
            onDismissRequest = { showDialog = null },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text(if (diff > 0) "Unaccounted spending" else "Unaccounted income", color = vc.TextHigh) },
            text             = {
                Text(
                    if (diff > 0) "Rs $diff unaccounted. Add to Misc as untracked expense?"
                    else "You have Rs ${-diff} more than expected. Add to Income as untracked income?",
                    color = vc.TextMedium
                )
            },
            confirmButton    = {
                Button(
                    onClick = {
                        val actual = actualText.toIntOrNull() ?: 0
                        vm.reconcile(actual) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                        showDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text(if (diff > 0) "Add to Misc" else "Add to Income") }
            },
            dismissButton    = {
                TextButton(onClick = { showDialog = null }) { Text("Cancel", color = vc.TextMedium) }
            }
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
                    text    = "Nightly check-in",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 24.dp),
                    shape    = shapeLarge,
                    color    = vc.Surface
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("EXPECTED IN WALLET", style = vt.sectionLabel, color = vc.TextMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Rs %,d".format(state.expectedAmount), style = vt.heroNumber.copy(fontSize = 32.sp), color = vc.TextHigh)
                        
                        Spacer(Modifier.height(24.dp))
                        Text("TODAY'S TRANSACTIONS", style = vt.sectionLabel, color = vc.TextMedium)
                        Spacer(Modifier.height(12.dp))
                        
                        if (state.todayTransactions.isEmpty()) {
                            Text("No transactions today", style = vt.rowSubtitle, color = vc.TextMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                        } else {
                            state.todayTransactions.take(5).forEach { twc ->
                                CompactTransactionRow(twc, vc, vt)
                            }
                            if (state.todayTransactions.size > 5) {
                                Text("... and ${state.todayTransactions.size - 5} more", style = vt.captionTiny, color = vc.TextMedium)
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("HOW MUCH IS ACTUALLY IN YOUR WALLET?", style = vt.rowTitle.copy(fontWeight = FontWeight.Medium), color = vc.TextHigh)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value           = actualText,
                        onValueChange   = { actualText = it.filter(Char::isDigit) },
                        label           = { Text("Actual amount") },
                        prefix          = { Text("Rs ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine      = true,
                        modifier        = Modifier.fillMaxWidth(),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = vc.Accent,
                            unfocusedBorderColor = vc.Hairline,
                            cursorColor          = vc.Accent
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val actual = actualText.toIntOrNull() ?: return@Button
                            val diff = state.expectedAmount - actual
                            if (diff == 0) {
                                vm.reconcile(actual) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            } else {
                                showDialog = diff
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh),
                        shape    = shapeLarge,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Reconcile", style = vt.rowTitle.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTransactionRow(twc: TransactionWithCategory, vc: ValurexColors, vt: ValurexTypography) {
    val t = twc.transaction
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            CategoryIcon(category = twc.category, size = 28.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(twc.category.name, style = vt.rowTitle, color = vc.TextHigh, maxLines = 1)
                if (!t.note.isNullOrBlank()) {
                    Text(t.note, style = vt.rowSubtitle, color = vc.TextMedium, maxLines = 1)
                }
            }
        }
        Text(
            "Rs %,d".format(t.amount),
            style = vt.amount,
            color = if (t.type == "INCOME") vc.IncomeGreen else vc.TextHigh
        )
    }
}
