package com.recall.app.core.common

import java.util.Calendar
import java.util.Locale

/**
 * Detects date/time references in free text and converts them to Unix-ms timestamps.
 * Patterns from spec Section 7.
 */
object DateDetector {

    data class DetectedDate(val displayText: String, val triggerAtMs: Long)

    private val MONTH_NAMES = mapOf(
        "january" to 1, "february" to 2, "march" to 3, "april" to 4,
        "may" to 5, "june" to 6, "july" to 7, "august" to 8,
        "september" to 9, "october" to 10, "november" to 11, "december" to 12,
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
        "jun" to 6, "jul" to 7, "aug" to 8, "sep" to 9,
        "oct" to 10, "nov" to 11, "dec" to 12
    )

    private val DAY_NAMES = mapOf(
        "monday" to Calendar.MONDAY, "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY, "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY, "saturday" to Calendar.SATURDAY,
        "sunday" to Calendar.SUNDAY
    )

    /** Returns the first date found in [text], or null if none detected. */
    fun detect(text: String): DetectedDate? {
        val lower = text.lowercase(Locale.getDefault())

        // "tomorrow"
        if (lower.contains("tomorrow")) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            return DetectedDate("tomorrow at 9:00 AM", cal.timeInMillis)
        }

        // "next Monday" / "next Friday" etc.
        for ((dayName, calDay) in DAY_NAMES) {
            if (lower.contains("next $dayName")) {
                val cal = nextDayOfWeek(calDay)
                return DetectedDate("next ${dayName.replaceFirstChar { it.uppercase() }}", cal)
            }
        }

        // "on March 14" / "March 14th" / "March 14"
        for ((monthName, monthNum) in MONTH_NAMES) {
            val regex = Regex("""(?:on\s+)?$monthName\s+(\d{1,2})(?:st|nd|rd|th)?""", RegexOption.IGNORE_CASE)
            val match = regex.find(lower)
            if (match != null) {
                val day = match.groupValues[1].toIntOrNull() ?: continue
                val cal = Calendar.getInstance().apply {
                    set(Calendar.MONTH, monthNum - 1)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    if (timeInMillis < System.currentTimeMillis()) add(Calendar.YEAR, 1)
                }
                return DetectedDate("${monthName.replaceFirstChar { it.uppercase() }} $day", cal.timeInMillis)
            }
        }

        // "14/03" or "03/14" (dd/mm or mm/dd — assume dd/mm)
        val slashRegex = Regex("""(\d{1,2})/(\d{1,2})""")
        slashRegex.find(lower)?.let { match ->
            val a = match.groupValues[1].toInt()
            val b = match.groupValues[2].toInt()
            val day = if (a <= 31 && b <= 12) a else b
            val month = if (a <= 31 && b <= 12) b else a
            val cal = Calendar.getInstance().apply {
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) add(Calendar.YEAR, 1)
            }
            return DetectedDate("$day/$month", cal.timeInMillis)
        }

        // ISO 8601 "2025-12-25"
        val isoRegex = Regex("""(\d{4})-(\d{2})-(\d{2})""")
        isoRegex.find(lower)?.let { match ->
            val year = match.groupValues[1].toInt()
            val month = match.groupValues[2].toInt()
            val day = match.groupValues[3].toInt()
            val cal = Calendar.getInstance().apply {
                set(year, month - 1, day, 9, 0, 0)
            }
            if (cal.timeInMillis > System.currentTimeMillis()) {
                return DetectedDate("$year-${"%02d".format(month)}-${"%02d".format(day)}", cal.timeInMillis)
            }
        }

        // "in X days" / "in X hours"
        val relDays = Regex("""in (\d+) days?""").find(lower)
        if (relDays != null) {
            val n = relDays.groupValues[1].toInt()
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, n)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
            }
            return DetectedDate("in $n day${if (n != 1) "s" else ""}", cal.timeInMillis)
        }

        val relHours = Regex("""in (\d+) hours?""").find(lower)
        if (relHours != null) {
            val n = relHours.groupValues[1].toInt()
            val cal = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, n) }
            return DetectedDate("in $n hour${if (n != 1) "s" else ""}", cal.timeInMillis)
        }

        return null
    }

    private fun nextDayOfWeek(dayOfWeek: Int): Long {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_WEEK)
        var daysUntil = dayOfWeek - today
        if (daysUntil <= 0) daysUntil += 7
        cal.add(Calendar.DAY_OF_YEAR, daysUntil)
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }
}
