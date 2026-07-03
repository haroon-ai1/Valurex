package com.nobody.valurex.ui.screen

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.valurex.ui.components.ManageRow
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.components.RowPosition
import com.nobody.valurex.ui.components.SectionLabel
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapeRowBottom
import com.nobody.valurex.ui.theme.shapeRowMiddle
import com.nobody.valurex.ui.theme.shapeRowSingle
import com.nobody.valurex.ui.theme.shapeRowTop
import com.nobody.valurex.ui.viewmodel.ReminderViewModel

@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    vm: ReminderViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsState()
    val vc = ValurexColors
    val vt = ValurexTypography

    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
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
                    text    = "Reminders",
                    leading = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = vc.TextHigh)
                        }
                    }
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                item {
                    PermissionCard(
                        onGrant = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        vc = vc, vt = vt
                    )
                }
            }

            item { SectionLabel("NIGHTLY CHECK-IN") }
            item {
                ReminderCard(
                    title     = "Nightly wallet check-in",
                    subtitle  = "Reconcile your spending each night",
                    enabled   = settings.nightly_enabled,
                    hour      = settings.nightly_hour,
                    minute    = settings.nightly_minute,
                    onToggle  = { vm.updateNightly(it, settings.nightly_hour, settings.nightly_minute) },
                    onTimeSet = { h, m -> vm.updateNightly(settings.nightly_enabled, h, m) },
                    vc = vc, vt = vt
                )
            }

            item { SectionLabel("DAILY REMINDERS") }
            item {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    DailyRow(
                        slot     = 1,
                        enabled  = settings.daily1_enabled,
                        hour     = settings.daily1_hour,
                        minute   = settings.daily1_minute,
                        onToggle = { vm.updateDaily(1, it, settings.daily1_hour, settings.daily1_minute) },
                        onTimeSet = { h, m -> vm.updateDaily(1, settings.daily1_enabled, h, m) },
                        position = RowPosition.TOP,
                        vc = vc, vt = vt
                    )
                    DailyRow(
                        slot     = 2,
                        enabled  = settings.daily2_enabled,
                        hour     = settings.daily2_hour,
                        minute   = settings.daily2_minute,
                        onToggle = { vm.updateDaily(2, it, settings.daily2_hour, settings.daily2_minute) },
                        onTimeSet = { h, m -> vm.updateDaily(2, settings.daily2_enabled, h, m) },
                        position = RowPosition.MIDDLE,
                        vc = vc, vt = vt
                    )
                    DailyRow(
                        slot     = 3,
                        enabled  = settings.daily3_enabled,
                        hour     = settings.daily3_hour,
                        minute   = settings.daily3_minute,
                        onToggle = { vm.updateDaily(3, it, settings.daily3_hour, settings.daily3_minute) },
                        onTimeSet = { h, m -> vm.updateDaily(3, settings.daily3_enabled, h, m) },
                        position = RowPosition.BOTTOM,
                        vc = vc, vt = vt
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit, onOpenSettings: () -> Unit, vc: ValurexColors, vt: ValurexTypography) {
    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        shape    = shapeLarge,
        color    = vc.Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notifications are disabled", style = vt.rowTitle, color = vc.TextHigh)
            Text("Enable to receive reminders and nightly check-ins.", style = vt.rowSubtitle, color = vc.TextMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGrant,
                    colors  = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Grant permission") }
                TextButton(onClick = onOpenSettings) { Text("Open settings", color = vc.TextMedium) }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    title: String, subtitle: String, enabled: Boolean,
    hour: Int, minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeSet: (Int, Int) -> Unit,
    vc: ValurexColors, vt: ValurexTypography
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape    = shapeLarge,
        color    = vc.Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = vt.rowTitle, color = vc.TextHigh)
                    Text(subtitle, style = vt.rowSubtitle, color = vc.TextMedium)
                }
                Switch(
                    checked        = enabled,
                    onCheckedChange = onToggle,
                    colors         = SwitchDefaults.colors(checkedThumbColor = vc.Accent)
                )
            }
            if (enabled) {
                Spacer(Modifier.height(12.dp))
                TimeStrip(hour, minute, onClick = {
                    TimePickerDialog(context, { _, h, m -> onTimeSet(h, m) }, hour, minute, false).show()
                }, vc = vc, vt = vt)
            }
        }
    }
}

@Composable
private fun DailyRow(
    slot: Int, enabled: Boolean, hour: Int, minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeSet: (Int, Int) -> Unit,
    position: RowPosition,
    vc: ValurexColors, vt: ValurexTypography
) {
    val context = LocalContext.current
    val shape = when (position) {
        RowPosition.TOP    -> shapeRowTop
        RowPosition.MIDDLE -> shapeRowMiddle
        RowPosition.BOTTOM -> shapeRowBottom
        RowPosition.SINGLE -> shapeRowSingle
    }
    Surface(
        shape = shape,
        color = vc.Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reminder $slot", style = vt.rowTitle, color = vc.TextHigh)
                    Text("Nudge to log spending", style = vt.rowSubtitle, color = vc.TextMedium)
                }
                Switch(
                    checked        = enabled,
                    onCheckedChange = onToggle,
                    colors         = SwitchDefaults.colors(checkedThumbColor = vc.Accent)
                )
            }
            if (enabled) {
                Spacer(Modifier.height(12.dp))
                TimeStrip(hour, minute, onClick = {
                    TimePickerDialog(context, { _, h, m -> onTimeSet(h, m) }, hour, minute, false).show()
                }, vc = vc, vt = vt)
            }
        }
    }
    if (position == RowPosition.TOP || position == RowPosition.MIDDLE) {
        Spacer(
            modifier = Modifier.fillMaxWidth().height(2.dp).background(vc.Background)
        )
    }
}

@Composable
private fun TimeStrip(hour: Int, minute: Int, onClick: () -> Unit, vc: ValurexColors, vt: ValurexTypography) {
    val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    val timeStr = "%d:%02d %s".format(displayHour, minute, amPm)

    Surface(
        onClick = onClick,
        color   = vc.SurfaceElevated,
        shape   = shapeLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Time", style = vt.rowSubtitle, color = vc.TextMedium)
            Text(timeStr, style = vt.rowTitle, color = vc.Accent)
        }
    }
}
