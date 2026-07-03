package com.nobody.valurex.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.ui.components.CategoryIcon
import com.nobody.valurex.ui.components.ManageRow
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.RowPosition
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.viewmodel.CategoriesViewModel

private val ColorPresets = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#3F51B5", "#2196F3",
    "#009688", "#4CAF50", "#FF9800", "#FF5722", "#795548",
    "#607D8B", "#9E9E9E"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    vm: CategoriesViewModel = viewModel()
) {
    val categories        by vm.categories.collectAsState()
    val snackbarHostState =  remember { SnackbarHostState() }
    var showAddDialog  by remember { mutableStateOf(false) }
    var editTarget     by remember { mutableStateOf<Category?>(null) }
    var deleteTarget   by remember { mutableStateOf<Category?>(null) }
    var blockedMessage by remember { mutableStateOf(false) }
    val vc = ValurexColors

    LaunchedEffect(blockedMessage) {
        if (blockedMessage) {
            snackbarHostState.showSnackbar("Default categories cannot be deleted")
            blockedMessage = false
        }
    }

    if (showAddDialog) {
        CategoryDialog(
            title     = "New Category",
            initial   = null,
            onConfirm = { name, color, limit -> vm.insertCategory(name, color, limit); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
    editTarget?.let { cat ->
        CategoryDialog(
            title     = "Edit Category",
            initial   = cat,
            onConfirm = { name, color, limit ->
                vm.updateCategory(cat.copy(name = name, color = color, monthlyLimit = limit))
                editTarget = null
            },
            onDismiss = { editTarget = null }
        )
    }
    deleteTarget?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Delete \"${cat.name}\"?", color = vc.TextHigh) },
            text             = { Text("This cannot be undone.", color = vc.TextMedium) },
            confirmButton    = {
                Button(
                    onClick = {
                        vm.deleteCategory(cat, onBlocked = { blockedMessage = true })
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = vc.ExpenseRed, contentColor = vc.TextHigh)
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 80.dp)
        ) {
            item {
                PageTitle(
                    text    = "Categories",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            if (categories.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories", style = com.nobody.valurex.ui.theme.ValurexTypography.rowTitle, color = vc.TextMedium)
                    }
                }
            } else {
                itemsIndexed(categories, key = { _, it -> it.id }) { index, cat ->
                    val subtitle = cat.monthlyLimit?.let { "Limit: Rs %,d".format(it) } ?: "No limit"
                    val position = when {
                        categories.size == 1            -> RowPosition.SINGLE
                        index == 0                      -> RowPosition.TOP
                        index == categories.lastIndex   -> RowPosition.BOTTOM
                        else                            -> RowPosition.MIDDLE
                    }
                    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                        ManageRow(
                            leading  = { CategoryIcon(category = cat, size = 38.dp) },
                            title    = cat.name,
                            subtitle = subtitle,
                            position = position,
                            onClick  = { editTarget = cat }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initial: Category?,
    onConfirm: (name: String, color: String, limit: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    var name      by remember { mutableStateOf(initial?.name ?: "") }
    var color     by remember { mutableStateOf(initial?.color ?: "#9E9E9E") }
    var limitText by remember { mutableStateOf(initial?.monthlyLimit?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        shape            = shapeLarge,
        title            = { Text(title, color = vc.TextHigh) },
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Color", style = com.nobody.valurex.ui.theme.ValurexTypography.rowSubtitle, color = vc.TextMedium)
                    Row(
                        modifier              = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorPresets.forEach { hex ->
                            val c          = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = color.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(c, CircleShape)
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, vc.TextHigh, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { color = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint     = vc.TextHigh,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value         = limitText,
                    onValueChange = { limitText = it.filter(Char::isDigit) },
                    label         = { Text("Monthly limit (optional)") },
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
                    if (name.isNotBlank()) onConfirm(name.trim(), color, limitText.toIntOrNull())
                },
                colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
            ) { Text("Save") }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = vc.TextMedium) }
        }
    )
}
