package com.nobody.valurex.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nobody.valurex.ui.theme.ValurexColors

@Composable
fun RowDivider() = HorizontalDivider(
    color     = ValurexColors.HairlineSubtle,
    thickness = 1.dp
)
