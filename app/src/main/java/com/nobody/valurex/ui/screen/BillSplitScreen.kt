package com.nobody.valurex.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapePill
import com.nobody.valurex.ui.viewmodel.BillSplitViewModel
import com.nobody.valurex.ui.viewmodel.LoanConflict

@Composable
fun BillSplitScreen(
    initialTotal: Int = 0,
    initialPeople: Int = 0,
    onNavigateBack: () -> Unit,
    vm: BillSplitViewModel = viewModel()
) {
    val context = LocalContext.current
    val vc = ValurexColors
    val vt = ValurexTypography

    var totalText by remember { mutableStateOf(if (initialTotal > 0) initialTotal.toString() else "") }
    var peopleText by remember { mutableStateOf(if (initialPeople > 0) initialPeople.toString() else "") }
    var iPaid by remember { mutableStateOf(true) }
    var whoPaidName by remember { mutableStateOf("") }
    
    val numPeople = peopleText.toIntOrNull() ?: 0
    val totalAmount = totalText.toIntOrNull() ?: 0
    val share = if (numPeople > 0) totalAmount / numPeople else 0

    var otherNames by remember { mutableStateOf(List(maxOf(0, numPeople - 1)) { "" }) }
    
    LaunchedEffect(numPeople) {
        val needed = maxOf(0, numPeople - 1)
        if (otherNames.size != needed) {
            otherNames = List(needed) { i -> if (i < otherNames.size) otherNames[i] else "" }
        }
    }

    var currentConflict by remember { mutableStateOf<LoanConflict?>(null) }

    if (currentConflict != null) {
        val conflict = currentConflict!!
        AlertDialog(
            onDismissRequest = { /* Force decision */ },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("${conflict.name} already exists", color = vc.TextHigh) },
            text             = {
                Text(
                    "${conflict.name} already exists in your loans (active amount Rs ${conflict.existingAmount}). What to do?",
                    color = vc.TextMedium
                )
            },
            confirmButton    = {
                Button(
                    onClick = { conflict.onDecision(true); currentConflict = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Add to existing") }
            },
            dismissButton    = {
                TextButton(onClick = { conflict.onDecision(false); currentConflict = null }) {
                    Text("Create new ${conflict.name} 2", color = vc.Accent)
                }
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
                    text    = "Split bill",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Step 1
                    Surface(shape = shapeLarge, color = vc.Surface) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value           = totalText,
                                onValueChange   = { totalText = it.filter(Char::isDigit) },
                                label           = { Text("Total amount") },
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
                            OutlinedTextField(
                                value           = peopleText,
                                onValueChange   = { peopleText = it.filter(Char::isDigit) },
                                label           = { Text("Total persons (including you)") },
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
                    }

                    // Step 2
                    Text("WHO PAID?", style = vt.sectionLabel, color = vc.TextMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(vc.Surface, shapePill)
                    ) {
                        listOf(true to "I paid", false to "Someone else paid").forEach { (value, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (iPaid == value) vc.Accent else Color.Transparent, shapePill)
                                    .clickable { iPaid = value }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, style = vt.pillButton, color = if (iPaid == value) vc.TextHigh else vc.TextMedium)
                            }
                        }
                    }

                    // Step 3
                    if (!iPaid) {
                        OutlinedTextField(
                            value         = whoPaidName,
                            onValueChange = { whoPaidName = it },
                            label         = { Text("Who paid? (Person name)") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = vc.Accent,
                                unfocusedBorderColor = vc.Hairline,
                                cursorColor          = vc.Accent
                            )
                        )
                        Surface(shape = shapeLarge, color = vc.Surface) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Your share: ", style = vt.rowTitle, color = vc.TextMedium)
                                Text("Rs %,d".format(share), style = vt.rowTitle.copy(fontWeight = FontWeight.Medium), color = vc.TextHigh)
                            }
                        }
                    } else {
                        Surface(shape = shapeLarge, color = vc.Surface) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Your share: ", style = vt.rowTitle, color = vc.TextMedium)
                                Text("Rs %,d".format(share), style = vt.rowTitle.copy(fontWeight = FontWeight.Medium), color = vc.TextHigh)
                            }
                        }
                        
                        if (numPeople >= 2) {
                            Text("OTHER PERSONS (AUTO-CALCULATED ${numPeople - 1} NAMES NEEDED):", style = vt.sectionLabel, color = vc.TextMedium)
                            otherNames.forEachIndexed { index, name ->
                                OutlinedTextField(
                                    value         = name,
                                    onValueChange = { new ->
                                        val list = otherNames.toMutableList()
                                        list[index] = new
                                        otherNames = list
                                    },
                                    label         = { Text("Person ${index + 1}") },
                                    singleLine    = true,
                                    modifier      = Modifier.fillMaxWidth(),
                                    trailingIcon  = {
                                        IconButton(onClick = {
                                            val list = otherNames.toMutableList()
                                            list[index] = ""
                                            otherNames = list
                                        }) { Icon(Icons.Default.Close, null, tint = vc.TextLow) }
                                    },
                                    colors        = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor   = vc.Accent,
                                        unfocusedBorderColor = vc.Hairline,
                                        cursorColor          = vc.Accent
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            vm.createSplit(
                                totalAmount, numPeople, iPaid, whoPaidName, otherNames,
                                onConflict = { currentConflict = it },
                                onComplete = {
                                    Toast.makeText(context, "Bill split created", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh),
                        shape    = shapeLarge,
                        contentPadding = PaddingValues(16.dp),
                        enabled = totalAmount > 0 && numPeople >= 2 && (!iPaid || otherNames.all { it.isNotBlank() }) && (iPaid || whoPaidName.isNotBlank())
                    ) {
                        Text("Create split", style = vt.rowTitle.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}
