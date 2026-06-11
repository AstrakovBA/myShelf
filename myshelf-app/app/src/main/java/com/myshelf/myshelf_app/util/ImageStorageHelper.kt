package com.myshelf.myshelf_app.util

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorageHelper {

    fun copyToCache(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.cacheDir, "item_images").apply { mkdirs() }
            val extension = context.contentResolver.getType(uri)
                ?.substringAfter('/')
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"
            val dest = File(dir, "img_${System.currentTimeMillis()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
