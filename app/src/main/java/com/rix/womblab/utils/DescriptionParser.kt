package com.rix.womblab.utils

import android.text.Html
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class ParsedEventInfo(
    val cleanDescription: String,
    val eventDate: String? = null,
    val eventTime: String? = null,
    val extractedDateTime: LocalDateTime? = null,
    val eventLink: String? = null
)

object DescriptionParser {

    private const val TAG = "DescriptionParser"

    fun parseEventDescription(description: String): ParsedEventInfo {
        Log.d(TAG, "🔍 Parsing descrizione: ${description.take(100)}...")

        var cleanDesc = description
        var eventDate: String? = null
        var eventTime: String? = null
        var extractedDateTime: LocalDateTime? = null
        var eventLink: String? = null

        try {
            eventLink = extractEventLink(description)

            cleanDesc = decodeAndCleanHtml(cleanDesc)

            cleanDesc = removeSubscriptionText(cleanDesc)

            val dateTimeInfo = extractDateTime(cleanDesc)
            eventDate = dateTimeInfo.first
            eventTime = dateTimeInfo.second
            extractedDateTime = dateTimeInfo.third

            if (eventDate != null) {
                cleanDesc = removeDateFromDescription(cleanDesc, eventDate, eventTime)
            }

            cleanDesc = formatDescription(cleanDesc)

            cleanDesc = finalCleanup(cleanDesc)

        } catch (e: Exception) {
        }

        return ParsedEventInfo(
            cleanDescription = cleanDesc.trim(),
            eventDate = eventDate,
            eventTime = eventTime,
            extractedDateTime = extractedDateTime,
            eventLink = eventLink
        )
    }

    private fun extractEventLink(htmlText: String): String? {
        val linkPattern = Regex("""<a[^>]*href="([^"]*trainingecm\.womblab\.com/event/[^"]*)"[^>]*>""", RegexOption.IGNORE_CASE)
        val match = linkPattern.find(htmlText)

        return match?.groupValues?.get(1)?.let { url ->
            url.replace("\\u003C", "<")
                .replace("\\u003E", ">")
                .replace("\\u0026", "&")
        }
    }

    private fun decodeAndCleanHtml(htmlText: String): String {
        var decoded = htmlText
            .replace("\\u003C", "<")
            .replace("\\u003E", ">")
            .replace("\\u0026", "&")
            .replace("&#8217;", "'")

        decoded = decoded.replace(Regex("""<img[^>]*>""", RegexOption.IGNORE_CASE), "")

        decoded = decoded.replace(Regex("""<h2[^>]*>(.*?)</h2>""", RegexOption.IGNORE_CASE), "\n**$1**\n")
        decoded = decoded.replace(Regex("""<h3[^>]*>(.*?)</h3>""", RegexOption.IGNORE_CASE), "\n**$1**\n")

        decoded = decoded.replace(Regex("""<(strong|b)[^>]*>(.*?)</(strong|b)>""", RegexOption.IGNORE_CASE), "**$2**")

        decoded = decoded.replace(Regex("""<p[^>]*>""", RegexOption.IGNORE_CASE), "\n")
        decoded = decoded.replace(Regex("""</p>""", RegexOption.IGNORE_CASE), "\n")

        decoded = decoded.replace(Regex("""<br\s*/?>\s*""", RegexOption.IGNORE_CASE), "\n")

        decoded = decoded.replace(Regex("""<a[^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE), "$1")

        decoded = Html.fromHtml(decoded, Html.FROM_HTML_MODE_LEGACY).toString()

        return decoded
    }

    private fun formatDescription(text: String): String {
        var formatted = text

        val keywords = listOf(
            "Inizio iscrizioni:",
            "Fine iscrizione:",
            "Crediti ECM:",
            "ID Agenas:",
            "Segreteria organizzativa",
            "Presentazione",
            "Elenco delle professioni",
            "Localizzazione"
        )

        keywords.forEach { keyword ->
            formatted = formatted.replace(
                Regex("""(?<!\n)($keyword)""", RegexOption.IGNORE_CASE),
                "\n$1"
            )
        }

        val majorSections = listOf(
            "Presentazione",
            "Elenco delle professioni",
            "Localizzazione"
        )

        majorSections.forEach { section ->
            formatted = formatted.replace(
                Regex("""(?<!\n\n)(\*\*$section.*?\*\*)""", RegexOption.IGNORE_CASE),
                "\n\n$1"
            )
        }

        formatted = formatted.replace(
            Regex("""(\*\*Presentazione\*\*)(?!\n)""", RegexOption.IGNORE_CASE),
            "$1\n"
        )

        formatted = formatted.replace(
            Regex("""(\*\*Elenco delle professioni.*?\*\*)(?!\n)""", RegexOption.IGNORE_CASE),
            "$1\n"
        )

        formatted = formatted.replace(
            Regex("""(\*\*Localizzazione\*\*)(?!\n)""", RegexOption.IGNORE_CASE),
            "$1\n"
        )

        formatted = formatted.replace(
            Regex("""(Tel\.?\s*)(\d{2,3}[\s-]?\d{3,4}[\s-]?\d{3,4})""", RegexOption.IGNORE_CASE),
            "$1**$2**"
        )

        formatted = formatted.replace(
            Regex("""(\*\*\d{2}-\d{2}-\d{4}\*\*)(?!\n)"""),
            "$1\n"
        )

        return formatted
    }

    private fun removeSubscriptionText(text: String): String {
        val patterns = listOf(
            "ISCRIVITI",
            "ISCRIVITI QUI",
            "REGISTRATI",
            "REGISTRATI QUI",
            "CLICCA QUI PER ISCRIVERTI",
            "CLICCA QUI PER REGISTRARTI"
        )

        var cleaned = text
        patterns.forEach { pattern ->
            cleaned = cleaned.replace(pattern, "", ignoreCase = true)
        }

        return cleaned
    }

    private fun extractDateTime(text: String): Triple<String?, String?, LocalDateTime?> {
        Log.d(TAG, "🕐 Cercando data/ora in: ${text.take(200)}...")

        var dateString: String? = null
        var timeString: String? = null
        var parsedDateTime: LocalDateTime? = null

        val wombLabDatePattern = Regex("""Dal\s+(\d{1,2})-(\d{1,2})-(\d{4})\s+al\s+(\d{1,2})-(\d{1,2})-(\d{4})""", RegexOption.IGNORE_CASE)
        val wombLabMatch = wombLabDatePattern.find(text)

        if (wombLabMatch != null) {
            val startDay = wombLabMatch.groupValues[1]
            val startMonth = wombLabMatch.groupValues[2]
            val startYear = wombLabMatch.groupValues[3]
            val endDay = wombLabMatch.groupValues[4]
            val endMonth = wombLabMatch.groupValues[5]
            val endYear = wombLabMatch.groupValues[6]

            dateString = if (startDay == endDay && startMonth == endMonth && startYear == endYear) {
                "$startDay/$startMonth/$startYear"
            } else {
                "$startDay/$startMonth/$startYear - $endDay/$endMonth/$endYear"
            }

            Log.d(TAG, "📅 Data WombLab trovata: $dateString")

            parsedDateTime = tryParseWombLabDate(startDay, startMonth, startYear)
        }

        if (dateString == null) {
            val datePatterns = listOf(
                Regex("""(\d{1,2})\s+(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\s+(\d{4})""", RegexOption.IGNORE_CASE),
                Regex("""(\d{1,2})/(\d{1,2})/(\d{4})"""),
                Regex("""(\d{1,2})-(\d{1,2})-(\d{4})""")
            )

            for (pattern in datePatterns) {
                val match = pattern.find(text)
                if (match != null) {
                    dateString = match.value
                    Log.d(TAG, "📅 Data generica trovata: $dateString")
                    break
                }
            }
        }

        if (wombLabMatch == null) {
            val timePatterns = listOf(
                Regex("""alle\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE),
                Regex("""ore\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE),
                Regex("""(\d{1,2}):(\d{2})""")
            )

            for (pattern in timePatterns) {
                val match = pattern.find(text)
                if (match != null) {
                    timeString = match.value
                    Log.d(TAG, "🕐 Ora trovata: $timeString")
                    break
                }
            }
        }

        return Triple(dateString, timeString, parsedDateTime)
    }

    private fun tryParseWombLabDate(day: String, month: String, year: String): LocalDateTime? {
        return try {
            val dayInt = day.toInt()
            val monthInt = month.toInt()
            val yearInt = year.toInt()

            val result = LocalDateTime.of(yearInt, monthInt, dayInt, 0, 0)
            result
        } catch (e: Exception) {
            null
        }
    }

    private fun removeDateFromDescription(text: String, dateStr: String, timeStr: String?): String {
        var cleaned = text

        val wombLabPattern = Regex("""Dal\s+\d{1,2}-\d{1,2}-\d{4}\s+al\s+\d{1,2}-\d{1,2}-\d{4}""", RegexOption.IGNORE_CASE)
        cleaned = wombLabPattern.replace(cleaned, "")

        val h3DatePattern = Regex("""\*\*Dal\s+\d{1,2}-\d{1,2}-\d{4}\s+al\s+\d{1,2}-\d{1,2}-\d{4}\*\*""", RegexOption.IGNORE_CASE)
        cleaned = h3DatePattern.replace(cleaned, "")

        val dateHeaderPattern = Regex("""\*\*\d{1,2}\s+\w{3}\s+\d{4}\*\*""", RegexOption.IGNORE_CASE)
        cleaned = dateHeaderPattern.replace(cleaned, "")

        return cleaned
    }

    private fun finalCleanup(text: String): String {
        return text
            .replace(Regex("""\n{3,}"""), "\n\n")
            .replace(Regex("""^\s*\n+"""), "")
            .replace(Regex("""\n+\s*$"""), "")
            .trim()
    }
}