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

    /**
     * Estrae informazioni dalla descrizione e la pulisce
     */
    fun parseEventDescription(description: String): ParsedEventInfo {
        Log.d(TAG, "🔍 Parsing descrizione: ${description.take(100)}...")

        var cleanDesc = description
        var eventDate: String? = null
        var eventTime: String? = null
        var extractedDateTime: LocalDateTime? = null
        var eventLink: String? = null

        try {
            // 1. Estrai il link prima di pulire l'HTML
            eventLink = extractEventLink(description)

            // 2. Decodifica HTML entities e pulisce
            cleanDesc = decodeAndCleanHtml(cleanDesc)

            // 3. Rimuovi "ISCRIVITI" e varianti
            cleanDesc = removeSubscriptionText(cleanDesc)

            // 4. Estrai data e ora
            val dateTimeInfo = extractDateTime(cleanDesc)
            eventDate = dateTimeInfo.first
            eventTime = dateTimeInfo.second
            extractedDateTime = dateTimeInfo.third

            // 5. Pulisci ulteriormente la descrizione rimuovendo la data estratta
            if (eventDate != null) {
                cleanDesc = removeDateFromDescription(cleanDesc, eventDate, eventTime)
            }

            // 6. Formatta la descrizione per renderla più leggibile
            cleanDesc = formatDescription(cleanDesc)

            // 7. Pulizia finale
            cleanDesc = finalCleanup(cleanDesc)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore nel parsing descrizione", e)
        }

        Log.d(TAG, "✅ Risultato parsing - Data: $eventDate, Ora: $eventTime, Link: $eventLink")
        return ParsedEventInfo(
            cleanDescription = cleanDesc.trim(),
            eventDate = eventDate,
            eventTime = eventTime,
            extractedDateTime = extractedDateTime,
            eventLink = eventLink
        )
    }

    /**
     * Estrae il link dell'evento dalla descrizione HTML
     */
    private fun extractEventLink(htmlText: String): String? {
        val linkPattern = Regex("""<a[^>]*href="([^"]*trainingecm\.womblab\.com/event/[^"]*)"[^>]*>""", RegexOption.IGNORE_CASE)
        val match = linkPattern.find(htmlText)

        return match?.groupValues?.get(1)?.let { url ->
            // Decodifica eventuali HTML entities nell'URL
            url.replace("\\u003C", "<")
                .replace("\\u003E", ">")
                .replace("\\u0026", "&")
        }
    }

    /**
     * Decodifica HTML entities e converte HTML in testo leggibile
     */
    private fun decodeAndCleanHtml(htmlText: String): String {
        // 1. Decodifica unicode escapes come \u003C
        var decoded = htmlText
            .replace("\\u003C", "<")
            .replace("\\u003E", ">")
            .replace("\\u0026", "&")
            .replace("&#8217;", "'")

        // 2. Rimuovi immagini e tag img
        decoded = decoded.replace(Regex("""<img[^>]*>""", RegexOption.IGNORE_CASE), "")

        // 3. Converti heading HTML in markdown
        decoded = decoded.replace(Regex("""<h2[^>]*>(.*?)</h2>""", RegexOption.IGNORE_CASE), "\n**$1**\n")
        decoded = decoded.replace(Regex("""<h3[^>]*>(.*?)</h3>""", RegexOption.IGNORE_CASE), "\n**$1**\n")

        // 4. Converti strong/b in grassetto markdown
        decoded = decoded.replace(Regex("""<(strong|b)[^>]*>(.*?)</(strong|b)>""", RegexOption.IGNORE_CASE), "**$2**")

        // 5. Converti paragrafi in newline
        decoded = decoded.replace(Regex("""<p[^>]*>""", RegexOption.IGNORE_CASE), "\n")
        decoded = decoded.replace(Regex("""</p>""", RegexOption.IGNORE_CASE), "\n")

        // 6. Gestisci i br
        decoded = decoded.replace(Regex("""<br\s*/?>\s*""", RegexOption.IGNORE_CASE), "\n")

        // 7. Rimuovi link HTML ma mantieni il contenuto
        decoded = decoded.replace(Regex("""<a[^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE), "$1")

        // 8. Rimuovi tutti gli altri tag HTML rimanenti
        decoded = Html.fromHtml(decoded, Html.FROM_HTML_MODE_LEGACY).toString()

        return decoded
    }

    /**
     * Formatta la descrizione per renderla più leggibile
     */
    private fun formatDescription(text: String): String {
        var formatted = text

        // 1. Aggiungi a capo prima delle parole chiave specifiche
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

        // 2. Aggiungi doppio a capo prima delle sezioni principali (titoli)
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

        // 3. Assicurati che i titoli abbiano spazio dopo
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

        // 4. Formatta numeri di telefono
        formatted = formatted.replace(
            Regex("""(Tel\.?\s*)(\d{2,3}[\s-]?\d{3,4}[\s-]?\d{3,4})""", RegexOption.IGNORE_CASE),
            "$1**$2**"
        )

        // 5. Assicurati che ogni informazione ECM sia su una riga separata
        formatted = formatted.replace(
            Regex("""(\*\*\d{2}-\d{2}-\d{4}\*\*)(?!\n)"""),
            "$1\n"
        )

        return formatted
    }

    /**
     * Rimuove testo "ISCRIVITI" e varianti
     */
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

    /**
     * Estrai data e ora dalla descrizione
     */
    private fun extractDateTime(text: String): Triple<String?, String?, LocalDateTime?> {
        Log.d(TAG, "🕐 Cercando data/ora in: ${text.take(200)}...")

        var dateString: String? = null
        var timeString: String? = null
        var parsedDateTime: LocalDateTime? = null

        // Pattern specifico per "Dal DD-MM-YYYY al DD-MM-YYYY"
        val wombLabDatePattern = Regex("""Dal\s+(\d{1,2})-(\d{1,2})-(\d{4})\s+al\s+(\d{1,2})-(\d{1,2})-(\d{4})""", RegexOption.IGNORE_CASE)
        val wombLabMatch = wombLabDatePattern.find(text)

        if (wombLabMatch != null) {
            val startDay = wombLabMatch.groupValues[1]
            val startMonth = wombLabMatch.groupValues[2]
            val startYear = wombLabMatch.groupValues[3]
            val endDay = wombLabMatch.groupValues[4]
            val endMonth = wombLabMatch.groupValues[5]
            val endYear = wombLabMatch.groupValues[6]

            // Formato user-friendly
            dateString = if (startDay == endDay && startMonth == endMonth && startYear == endYear) {
                // Stesso giorno
                "$startDay/$startMonth/$startYear"
            } else {
                // Range di date
                "$startDay/$startMonth/$startYear - $endDay/$endMonth/$endYear"
            }

            Log.d(TAG, "📅 Data WombLab trovata: $dateString")

            // Converti in LocalDateTime (usa la data di inizio)
            parsedDateTime = tryParseWombLabDate(startDay, startMonth, startYear)
        }

        // Fallback: cerca pattern comuni per date italiane se non trova il formato WombLab
        if (dateString == null) {
            val datePatterns = listOf(
                // Formato: "29 maggio 2025"
                Regex("""(\d{1,2})\s+(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)\s+(\d{4})""", RegexOption.IGNORE_CASE),
                // Formato: "29/05/2025"
                Regex("""(\d{1,2})/(\d{1,2})/(\d{4})"""),
                // Formato: "29-05-2025"
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

        // Cerca orari se non abbiamo già una data WombLab
        if (wombLabMatch == null) {
            val timePatterns = listOf(
                // Formato: "alle 20:00"
                Regex("""alle\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE),
                // Formato: "ore 20:00"
                Regex("""ore\s+(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE),
                // Formato: "20:00"
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

    /**
     * Parsing specifico per date WombLab formato DD-MM-YYYY
     */
    private fun tryParseWombLabDate(day: String, month: String, year: String): LocalDateTime? {
        return try {
            val dayInt = day.toInt()
            val monthInt = month.toInt()
            val yearInt = year.toInt()

            val result = LocalDateTime.of(yearInt, monthInt, dayInt, 0, 0)
            Log.d(TAG, "✅ Data WombLab parsata: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Errore parsing data WombLab: $day-$month-$year", e)
            null
        }
    }

    /**
     * Rimuove la data estratta dalla descrizione
     */
    private fun removeDateFromDescription(text: String, dateStr: String, timeStr: String?): String {
        var cleaned = text

        // Rimuovi il pattern specifico "Dal XX-XX-XXXX al XX-XX-XXXX"
        val wombLabPattern = Regex("""Dal\s+\d{1,2}-\d{1,2}-\d{4}\s+al\s+\d{1,2}-\d{1,2}-\d{4}""", RegexOption.IGNORE_CASE)
        cleaned = wombLabPattern.replace(cleaned, "")

        // Rimuovi anche eventuali titoli con la data
        val h3DatePattern = Regex("""\*\*Dal\s+\d{1,2}-\d{1,2}-\d{4}\s+al\s+\d{1,2}-\d{1,2}-\d{4}\*\*""", RegexOption.IGNORE_CASE)
        cleaned = h3DatePattern.replace(cleaned, "")

        // Rimuovi header con solo la data "29 Mag 2025"
        val dateHeaderPattern = Regex("""\*\*\d{1,2}\s+\w{3}\s+\d{4}\*\*""", RegexOption.IGNORE_CASE)
        cleaned = dateHeaderPattern.replace(cleaned, "")

        return cleaned
    }

    /**
     * Pulizia finale della descrizione
     */
    private fun finalCleanup(text: String): String {
        return text
            .replace(Regex("""\n{3,}"""), "\n\n") // Massimo 2 newline consecutive
            .replace(Regex("""^\s*\n+"""), "") // Rimuovi newline all'inizio
            .replace(Regex("""\n+\s*$"""), "") // Rimuovi newline alla fine
            .trim()
    }
}