package com.nobody.valurex.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nobody.valurex.data.db.entity.WishlistItem
import com.nobody.valurex.ui.components.PageTitle
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeLarge
import com.nobody.valurex.ui.theme.shapeMedium
import com.nobody.valurex.ui.theme.shapeSmall
import com.nobody.valurex.ui.util.CopyImageToInternal
import com.nobody.valurex.ui.viewmodel.WishlistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val imageBgColors = listOf(
    Color(0xFF1A1410), Color(0xFF0F1620), Color(0xFF1A1014),
    Color(0xFF0F1A12), Color(0xFF161616), Color(0xFF1A1422)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(vm: WishlistViewModel = viewModel()) {
    val items   by vm.items.collectAsState()
    val count   by vm.count.collectAsState()
    val total   by vm.total.collectAsState()
    val context =  LocalContext.current
    val scope   =  rememberCoroutineScope()
    val vc      =  ValurexColors
    val vt      =  ValurexTypography

    var showAddDialog   by remember { mutableStateOf(false) }
    var editTarget      by remember { mutableStateOf<WishlistItem?>(null) }
    var deleteTarget    by remember { mutableStateOf<WishlistItem?>(null) }
    var dialogImageUri  by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editTarget, showAddDialog) {
        dialogImageUri = if (editTarget != null) editTarget?.imageUri else null
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val internal = withContext(Dispatchers.IO) { CopyImageToInternal.copy(context, uri) }
                dialogImageUri = internal
            }
        }
    }

    fun launchPicker() = pickImageLauncher.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )

    if (showAddDialog) {
        AddEditWishDialog(
            initial        = null,
            currentImage   = dialogImageUri,
            onPickImage    = { launchPicker() },
            onClearImage   = { dialogImageUri = null },
            onConfirm      = { name, price, note ->
                vm.addWish(name, price, note, dialogImageUri)
                showAddDialog  = false
                dialogImageUri = null
            },
            onDismiss      = { showAddDialog = false; dialogImageUri = null }
        )
    }

    editTarget?.let { item ->
        AddEditWishDialog(
            initial        = item,
            currentImage   = dialogImageUri,
            onPickImage    = { launchPicker() },
            onClearImage   = {
                item.imageUri?.let { CopyImageToInternal.delete(it) }
                dialogImageUri = null
            },
            onConfirm      = { name, price, note ->
                vm.updateWish(item.copy(name = name, price = price, note = note, imageUri = dialogImageUri))
                editTarget     = null
                dialogImageUri = null
            },
            onDismiss      = { editTarget = null; dialogImageUri = null }
        )
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = vc.Surface,
            shape            = shapeLarge,
            title            = { Text("Delete this wish?", color = vc.TextHigh) },
            text             = { Text("\"${item.name}\" will be removed.", color = vc.TextMedium) },
            confirmButton    = {
                Button(
                    onClick = { vm.deleteWish(item); deleteTarget = null },
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
            ) { Icon(Icons.Default.Add, null) }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            )
        ) {
            item {
                PageTitle(
                    text     = "Wishlist",
                    trailing = {
                        if (count > 0) {
                            Text(
                                "$count ${if (count == 1) "item" else "items"} · Rs %,d".format(total),
                                style = vt.captionTiny,
                                color = vc.TextMedium,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                )
            }

            if (items.isEmpty()) {
                item {
                    Column(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Star, null,
                            tint     = vc.TextLow,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No wishes yet", style = vt.rowTitle, color = vc.TextMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Add things you want to buy", style = vt.rowSubtitle, color = vc.TextLow)
                    }
                }
            } else {
                itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                    WishCard(
                        item     = item,
                        bgHint   = imageBgColors[index % imageBgColors.size],
                        vc       = vc,
                        vt       = vt,
                        onClick  = { editTarget = item },
                        onDelete = { deleteTarget = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun WishCard(
    item: WishlistItem,
    bgHint: Color,
    vc: ValurexColors,
    vt: ValurexTypography,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = shapeMedium,
        color = vc.Surface
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(72.dp)
                    .background(bgHint, shapeSmall)
                    .clip(shapeSmall),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUri != null) {
                    AsyncImage(
                        model              = Uri.parse(item.imageUri),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.Photo, null, tint = vc.TextMedium, modifier = Modifier.size(28.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style    = vt.rowTitle,
                    color    = vc.TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                if (item.price != null) {
                    Text("Rs %,d".format(item.price), style = vt.amount, color = vc.TextHigh)
                } else {
                    Text(
                        "Price TBD",
                        style     = vt.rowSubtitle.copy(fontStyle = FontStyle.Italic),
                        color     = vc.TextLow
                    )
                }
                if (!item.note.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.note,
                        style    = vt.rowSubtitle,
                        color    = vc.TextMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = vc.TextMedium)
                }
                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor   = vc.Surface
                ) {
                    DropdownMenuItem(
                        text    = { Text("Edit", color = vc.TextHigh) },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text    = { Text("Delete", color = vc.ExpenseRed) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditWishDialog(
    initial: WishlistItem?,
    currentImage: String?,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onConfirm: (name: String, price: Int?, note: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val vc = ValurexColors
    val vt = ValurexTypography
    var name       by remember { mutableStateOf(initial?.name ?: "") }
    var priceText  by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var note       by remember { mutableStateOf(initial?.note ?: "") }
    var nameError  by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = vc.Surface,
        shape            = shapeLarge,
        title            = { Text(if (initial == null) "Add wish" else "Edit wish", color = vc.TextHigh) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Photo slot
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(shapeMedium)
                        .background(vc.SurfaceElevated, shapeMedium)
                        .border(1.dp, vc.Hairline, shapeMedium)
                        .clickable { onPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentImage != null) {
                        AsyncImage(
                            model              = Uri.parse(currentImage),
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize().clip(shapeMedium)
                        )
                        Box(
                            modifier         = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(vc.Background.copy(alpha = 0.8f), CircleShape)
                                .clickable { onClearImage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", style = vt.captionTiny, color = vc.TextHigh)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddAPhoto, null, tint = vc.TextMedium, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Add photo", style = vt.rowSubtitle, color = vc.TextMedium)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it; nameError = false },
                    label         = { Text("Name") },
                    singleLine    = true,
                    isError       = nameError,
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = vc.Accent,
                        unfocusedBorderColor = vc.Hairline,
                        cursorColor          = vc.Accent
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value           = priceText,
                    onValueChange   = { priceText = it.filter(Char::isDigit) },
                    label           = { Text("Price (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = vc.Accent,
                        unfocusedBorderColor = vc.Hairline,
                        cursorColor          = vc.Accent
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Note (optional)") },
                    maxLines      = 3,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = vc.TextMedium)
                }
                Button(
                    onClick = {
                        if (name.isBlank()) { nameError = true; return@Button }
                        onConfirm(name.trim(), priceText.toIntOrNull(), note.trim().ifBlank { null })
                    },
                    shape  = shapeMedium,
                    colors = ButtonDefaults.buttonColors(containerColor = vc.Accent, contentColor = vc.TextHigh)
                ) { Text("Save") }
            }
        },
        dismissButton    = null
    )
}
