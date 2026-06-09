package com.myshelf.myshelf_app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {

    private fun dateFormatter(): DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

    fun formatItemDate(timestampMillis: Long): String {
        return formatDate(timestampMillis)
    }

    fun formatOutfitDate(timestampMillis: Long): String {
        return formatDate(timestampMillis)
    }

    private fun formatDate(timestampMillis: Long): String {
        val date = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return dateFormatter().format(date)
    }
}
