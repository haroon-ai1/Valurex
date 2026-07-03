package com.nobody.valurex.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.data.db.entity.Transaction
import com.nobody.valurex.ui.components.CategoryIcon
import com.nobody.valurex.ui.components.SavingsBar
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.PillChip
import com.nobody.valurex.ui.components.RowDivider
import com.nobody.valurex.ui.components.SectionLabel
import com.nobody.valurex.ui.components.StatTile
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapePill
import com.nobody.valurex.ui.util.formatRelativeTime
import com.nobody.valurex.ui.viewmodel.HomeUiState
import com.nobody.valurex.ui.viewmodel.HomeViewModel
import com.nobody.valurex.ui.viewmodel.TransactionWithCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToCategories: () -> Unit,
    onNavigateToAddManual: () -> Unit,
    onNavigateToAccount: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val state              by vm.uiState.collectAsState()
    var inputText          by remember { mutableStateOf("") }
    var showOverflow       by remember { mutableStateOf(false) }
    var editTarget         by remember { mutableStateOf<TransactionWithCategory?>(null) }
    var deleteTarget       by remember { mutableStateOf<Transaction?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    val vc = ValurexColors
    val vt = ValurexTypography

    val spentToday = remember(state.transactions) {
        val today = LocalDate.now(ZoneId.systemDefault())
        state.transactions
            .filter { it.transaction.type == "EXPENSE" }
            .filter {
                Instant.ofEpochMilli(it.transaction.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate() == today
            }
            .sumOf { it.transaction.amount }
    }
    val avgPerDay = remember(state.monthlyExpenses) {
        val day = LocalDate.now().dayOfMonth
        state.monthlyExpenses / maxOf(1, day)
    }
    val filteredTransactions = remember(state.transactions, selectedCategoryId) {
        if (selectedCategoryId == null) state.transactions
        else state.transactions.filter { it.transaction.categoryId == selectedCategoryId }
    }

    state.pendingNeedsCategory?.let { pending ->
        NeedsCategorySheet(
            unknownText = pending.unknownText,
            categories  = state.categories,
            onSave      = { catId, learn -> vm.onResolveCategory(pending.amount, pending.unknownText, catId, learn) },
            onDismiss   = vm::dismissNeedsCategory
        )
    }

    editTarget?.let { twc ->
        EditTransactionSheet(
            twc        = twc,
            categories = state.categories,
            onSave     = { updated -> vm.updateTransaction(updated); editTarget = null },
            onDelete   = { deleteTarget = twc.transaction; editTarget = null },
            onDismiss  = { editTarget = null }
        )
    }

    deleteTarget?.let { txn ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Delete transaction?", color = vc.TextHigh) },
            text             = { Text("This cannot be undone.", color = vc.TextMedium) },
            confirmButton    = {
                Button(
                    onClick = { vm.deleteTransaction(txn); deleteTarget = null },
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
        bottomBar           = {
            InputBar(
                inputText    = inputText,
                onValueChange = { inputText = it },
                onSend        = {
                    val t = inputText.trim()
                    if (t.isNotBlank()) { vm.onAddTransaction(t); inputText = "" }
                },
                vc = vc, vt = vt
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
        ) {
            item {
                PageTitle(
                    text     = "Valurex",
                    trailing = {
                        Box {
                            IconButton(onClick = { showOverflow = true }) {
                                Icon(Icons.Default.MoreVert, null, tint = vc.TextMedium)
                            }
                            DropdownMenu(
                                expanded         = showOverflow,
                                onDismissRequest = { showOverflow = false },
                                containerColor   = vc.Surface
                            ) {
                                DropdownMenuItem(
                                    text    = { Text("Add manually", color = vc.TextHigh) },
                                    onClick = { showOverflow = false; onNavigateToAddManual() }
                                )
                                DropdownMenuItem(
                                    text    = { Text("Manage categories", color = vc.TextHigh) },
                                    onClick = { showOverflow = false; onNavigateToCategories() }
                                )
                            }
                        }
                    }
                )
            }

            item { HeroCard(state = state, vc = vc, vt = vt, onNavigateToAccount = onNavigateToAccount) }

            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(label = "Today", value = "Rs %,d".format(spentToday),
                        valueColor = if (spentToday > 0) vc.ExpenseRed else vc.TextHigh,
                        modifier   = Modifier.weight(1f))
                    StatTile(label = "Avg / day", value = "Rs %,d".format(avgPerDay),
                        modifier = Modifier.weight(1f))
                }
            }

            item {
                AnimatedVisibility(
                    visible = state.savingsBarVisible,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically()
                ) {
                    SavingsBar(
                        savedAmount = state.savingsBarSavedAmount,
                        percentLess = state.savingsBarPercentLess,
                        onDismiss   = vm::dismissSavingsBar,
                        modifier    = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    )
                }
            }

            item { SectionLabel("Recent") }

            if (state.categories.isNotEmpty()) {
                item {
                    Row(
                        modifier              = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PillChip(
                            text     = "All",
                            selected = selectedCategoryId == null,
                            onClick  = { selectedCategoryId = null }
                        )
                        state.categories.forEach { cat ->
                            PillChip(
                                text     = cat.name,
                                selected = selectedCategoryId == cat.id,
                                onClick  = {
                                    selectedCategoryId =
                                        if (selectedCategoryId == cat.id) null else cat.id
                                }
                            )
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions", style = vt.rowTitle, color = vc.TextMedium)
                    }
                }
            } else {
                itemsIndexed(filteredTransactions, key = { _, it -> it.transaction.id }) { index, twc ->
                    TransactionRow(twc = twc, vc = vc, vt = vt, onLongClick = { editTarget = twc })
                    if (index < filteredTransactions.lastIndex) RowDivider()
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HeroCard(
    state: HomeUiState,
    vc: ValurexColors,
    vt: ValurexTypography,
    onNavigateToAccount: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 14.dp),
        shape    = shapeLarge,
        color    = vc.Surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Text(
                text  = "TOTAL MONEY",
                style = vt.sectionLabel,
                color = vc.TextMedium
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text     = "Rs",
                    style    = vt.rowTitle.copy(fontSize = 13.sp, color = vc.TextMedium),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = "%,d".format(state.totalMoney),
                    style = vt.heroNumber,
                    color = if (state.totalMoney < 0) vc.ExpenseRed else vc.TextHigh
                )
            }

            if (state.monthlyBudget == 0) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick      = onNavigateToAccount,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Set your budget →", color = vc.Accent, style = vt.rowTitle)
                }
            } else {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = vc.Hairline)
                Spacer(Modifier.height(16.dp))
                val periodLabel = if (state.budgetPeriod == "WEEKLY") "Budget this week" else "Budget this month"
                val budgetText  = buildAnnotatedString {
                    withStyle(SpanStyle(color = vc.TextHigh, fontWeight = FontWeight.Medium, fontSize = 12.sp)) {
                        append("Rs %,d".format(state.monthlyExpenses))
                    }
                    withStyle(SpanStyle(color = vc.TextMedium, fontSize = 12.sp)) {
                        append(" / Rs %,d".format(state.monthlyBudget))
                    }
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(periodLabel, style = vt.rowTitle.copy(fontSize = 12.sp), color = vc.TextMedium)
                    Text(budgetText)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress   = { (state.monthlyExpenses.toFloat() / state.monthlyBudget).coerceIn(0f, 1f) },
                    modifier   = Modifier.fillMaxWidth().height(3.dp).clip(shapePill),
                    color      = if (state.isOverBudget) vc.ExpenseRed else vc.Accent,
                    trackColor = vc.Hairline
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionRow(
    twc: TransactionWithCategory,
    vc: ValurexColors,
    vt: ValurexTypography,
    onLongClick: () -> Unit
) {
    val t = twc.transaction
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryIcon(category = twc.category, size = 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(twc.category.name, style = vt.rowTitle, color = vc.TextHigh)
            if (!t.note.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(t.note, style = vt.rowSubtitle, color = vc.TextMedium)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text  = "Rs %,d".format(t.amount),
                style = vt.amount,
                color = if (t.type == "INCOME") vc.IncomeGreen else vc.TextHigh
            )
            Spacer(Modifier.height(2.dp))
            Text(formatRelativeTime(t.timestamp), style = vt.rowSubtitle, color = vc.TextMedium)
        }
    }
}

@Composable
private fun InputBar(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    vc: ValurexColors,
    vt: ValurexTypography
) {
    Surface(
        modifier = Modifier.fillMaxWidth().imePadding(),
        color    = vc.Background
    ) {
        Row(
            modifier              = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(vc.Surface, shapePill)
                    .border(1.dp, vc.Hairline, shapePill)
                    .padding(horizontal = 18.dp, vertical = 13.dp)
            ) {
                BasicTextField(
                    value         = inputText,
                    onValueChange = onValueChange,
                    textStyle     = vt.rowTitle.copy(color = vc.TextHigh),
                    singleLine    = true,
                    cursorBrush   = SolidColor(vc.Accent),
                    modifier      = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (inputText.isEmpty()) {
                            Text("Add: biryani 350", style = vt.rowTitle, color = vc.TextPlaceholder)
                        }
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color  = if (inputText.isBlank()) vc.Accent.copy(alpha = 0.4f) else vc.Accent,
                        shape  = CircleShape
                    )
                    .clickable(enabled = inputText.isNotBlank(), onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionSheet(
    twc: TransactionWithCategory,
    categories: List<Category>,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    var amount      by remember { mutableStateOf(twc.transaction.amount.toString()) }
    var selectedCat by remember { mutableStateOf(twc.category) }
    var note        by remember { mutableStateOf(twc.transaction.note ?: "") }
    var type        by remember { mutableStateOf(twc.transaction.type) }
    var expanded    by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        contentColor     = vc.TextHigh
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Transaction", style = ValurexTypography.rowTitle.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp), color = vc.TextHigh)

            OutlinedTextField(
                value           = amount,
                onValueChange   = { amount = it.filter(Char::isDigit) },
                label           = { Text("Amount (Rs)") },
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
                    value         = selectedCat.name,
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
                            onClick = { selectedCat = cat; expanded = false }
                        )
                    }
                }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "EXPENSE", onClick = { type = "EXPENSE" },
                    colors = RadioButtonDefaults.colors(selectedColor = vc.Accent))
                Text("Expense", color = vc.TextHigh)
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = type == "INCOME", onClick = { type = "INCOME" },
                    colors = RadioButtonDefaults.colors(selectedColor = vc.Accent))
                Text("Income", color = vc.TextHigh)
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = vc.ExpenseRed)
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amt = amount.toIntOrNull() ?: return@Button
                        onSave(twc.transaction.copy(
                            amount     = amt,
                            categoryId = selectedCat.id,
                            note       = note.trim().ifBlank { null },
                            type       = type
                        ))
                    },
                    enabled = amount.isNotBlank(),
                    colors  = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Save") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeedsCategorySheet(
    unknownText: String,
    categories: List<Category>,
    onSave: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    var selectedId   by remember { mutableStateOf<Long?>(null) }
    var learnKeyword by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        contentColor     = vc.TextHigh
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                "Couldn't categorize \"$unknownText\". Pick category:",
                style    = ValurexTypography.rowTitle.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
                color    = vc.TextHigh,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    PillChip(
                        text     = cat.name,
                        selected = selectedId == cat.id,
                        onClick  = { selectedId = cat.id }
                    )
                }
            }
            Row(
                modifier          = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked         = learnKeyword,
                    onCheckedChange = { learnKeyword = it },
                    colors          = CheckboxDefaults.colors(checkedColor = vc.Accent)
                )
                Spacer(Modifier.width(4.dp))
                Text("Remember this word", color = vc.TextHigh)
            }
            Row(
                modifier              = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick  = { selectedId?.let { onSave(it, learnKeyword) } },
                    enabled  = selectedId != null,
                    colors   = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Save") }
            }
        }
    }
}
