package com.nobody.valurex.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.parser.TransactionType
import com.nobody.valurex.ui.theme.Accent
import com.nobody.valurex.ui.theme.PillShape
import com.nobody.valurex.ui.theme.TextHigh
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val app     = context.applicationContext as ValurexApplication
    val scope   = rememberCoroutineScope()
    val categories by app.categoryRepository.getAll().collectAsState(initial = emptyList())

    var amountText       by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
    var note             by remember { mutableStateOf("") }
    var expanded         by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (selectedCategory == null) selectedCategory = categories.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value           = amountText,
                onValueChange   = { amountText = it.filter(Char::isDigit) },
                label           = { Text("Amount (Rs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value         = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Category") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.name) },
                            onClick = { selectedCategory = cat; expanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value         = note,
                onValueChange = { note = it },
                label         = { Text("Note (optional)") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )
            Button(
                onClick = {
                    val amount = amountText.toIntOrNull() ?: return@Button
                    val catId  = selectedCategory?.id ?: return@Button
                    scope.launch {
                        val type = if (selectedCategory?.name == "Income" && selectedCategory?.isDefault == true)
                            TransactionType.INCOME else TransactionType.EXPENSE
                        app.transactionRepository.insertTransaction(
                            amount, catId, note.trim().ifBlank { null }, "manual", type
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = amountText.isNotBlank() && selectedCategory != null,
                shape    = PillShape,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor   = TextHigh
                )
            ) { Text("Save") }
        }
    }
}
