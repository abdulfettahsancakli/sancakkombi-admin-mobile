package com.example.utils

/**
 * Parses number formats commonly entered in the Turkish UI.
 *
 * Examples: 1200.50, 1200,50, 1.200,50 and 1,200.50.
 */
fun parseLocalizedDouble(value: String): Double? {
    var normalized = value.trim()
    if (normalized.isEmpty()) return null

    normalized = normalized
        .replace("\u00A0", "")
        .replace(" ", "")
        .replace("₺", "")
        .replace("'", "")

    val commaIndex = normalized.lastIndexOf(',')
    val dotIndex = normalized.lastIndexOf('.')
    normalized = when {
        commaIndex >= 0 && dotIndex >= 0 && commaIndex > dotIndex ->
            normalized.replace(".", "").replace(',', '.')
        commaIndex >= 0 && dotIndex >= 0 ->
            normalized.replace(",", "")
        commaIndex >= 0 ->
            normalized.replace(',', '.')
        else -> normalized
    }

    return normalized.toDoubleOrNull()
}
