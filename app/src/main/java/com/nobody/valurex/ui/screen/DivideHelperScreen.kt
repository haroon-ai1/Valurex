package com.nobody.valurex.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge

@Composable
fun DivideHelperScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBillSplit: (Int, Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var peopleText by remember { mutableStateOf("") }
    val vc = ValurexColors
    val vt = ValurexTypography

    val totalAmount = amountText.toIntOrNull() ?: 0
    val numPeople   = peopleText.toIntOrNull() ?: 1
    val result      = if (numPeople > 0) totalAmount / numPeople else 0

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
                    text    = "Divide bill",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = shapeLarge, color = vc.Surface) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value           = amountText,
                                onValueChange   = { amountText = it.filter(Char::isDigit) },
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
                                label           = { Text("Number of people") },
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

                    Surface(shape = shapeLarge, color = vc.Surface) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("EACH PERSON PAYS", style = vt.sectionLabel, color = vc.TextMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("Rs %,d".format(result), style = vt.heroNumber.copy(fontSize = 32.sp), color = vc.TextHigh)
                            if (totalAmount > 0 && numPeople > 1) {
                                Spacer(Modifier.height(4.dp))
                                Text("%,d ÷ %d".format(totalAmount, numPeople), style = vt.rowSubtitle, color = vc.TextMedium)
                            }
                        }
                    }

                    Button(
                        onClick = { onNavigateToBillSplit(totalAmount, numPeople) },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh),
                        shape    = shapeLarge,
                        contentPadding = PaddingValues(16.dp),
                        enabled = totalAmount > 0 && numPeople >= 2
                    ) {
                        Text("Create bill split from this", style = vt.rowTitle.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}
