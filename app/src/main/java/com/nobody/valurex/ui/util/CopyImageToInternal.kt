package com.nobody.valurex.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

object CopyImageToInternal {
    fun copy(context: Context, sourceUri: Uri): String? = try {
        val destFile = File(context.filesDir, "wishlist_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output -> input.copyTo(output) }
        }
        destFile.toUri().toString()
    } catch (_: Exception) { null }

    fun delete(uriString: String) {
        try {
            val path = Uri.parse(uriString).path ?: return
            File(path).delete()
        } catch (_: Exception) {}
    }
}
