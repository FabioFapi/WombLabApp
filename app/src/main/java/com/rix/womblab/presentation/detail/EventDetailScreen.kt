package com.rix.womblab.presentation.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rix.womblab.domain.model.*
import com.rix.womblab.presentation.components.ErrorMessage
import com.rix.womblab.presentation.components.LoadingIndicator
import com.rix.womblab.presentation.detail.EventDetailViewModel
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.DescriptionParser
import com.rix.womblab.utils.ParsedEventInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettaglio Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.shareEvent() }) {
                    }

                    IconButton(
                        onClick = { viewModel.onToggleFavorite() },
                        enabled = !uiState.isTogglingFavorite
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Default.StarBorder,
                            contentDescription = if (uiState.isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                            tint = if (uiState.isFavorite) Color(0xFFFFD700) else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentEvent = uiState.event

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(
                        message = uiState.error ?: "Errore sconosciuto",
                        onRetryClick = { viewModel.onRetry() }
                    )
                }
            }

            currentEvent != null -> {
                EventDetailContent(
                    event = currentEvent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Evento non trovato",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    modifier: Modifier = Modifier
) {
    val parsedInfo = remember(event.description) {
        DescriptionParser.parseEventDescription(event.description)
    }

    val sections = remember(parsedInfo.cleanDescription) {
        extractEventSections(parsedInfo.cleanDescription)
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            HeroSection(event = event)
        }

        if (parsedInfo.eventDate != null) {
            item {
                DateSection(parsedInfo = parsedInfo)
            }
        }

        if (sections.eventInfo.isNotEmpty()) {
            item {
                EventInfoSection(eventInfo = sections.eventInfo)
            }
        }

        if (sections.presentation.isNotEmpty()) {
            item {
                PresentationSection(presentation = sections.presentation)
            }
        }

        if (sections.professions.isNotEmpty()) {
            item {
                ProfessionsSection(professions = sections.professions)
            }
        }

        if (sections.locationInfo.isNotEmpty()) {
            item {
                LocationInfoSection(locationInfo = sections.locationInfo)
            }
        }

        if (event.venue != null) {
            item {
                LocationSection(venue = event.venue)
            }
        }

        if (event.organizer.isNotEmpty()) {
            item {
                OrganizerSection(organizers = event.organizer)
            }
        }

        if (event.categories.isNotEmpty()) {
            item {
                CategoriesSection(categories = event.categories)
            }
        }

        item {
            val context = LocalContext.current

            ActionButtonsSection(
                event = event,
                eventLink = parsedInfo.eventLink,
                onWebsiteClick = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                    }
                },
                onShareClick = {
                    val shareText = buildString {
                        append("🎯 ${event.title}\n\n")

                        parsedInfo.eventDate?.let { date ->
                            append("📅 Data: $date\n")
                        }

                        event.venue?.let { venue ->
                            append("📍 Sede: ${venue.name}, ${venue.city}\n")
                        }

                        parsedInfo.eventLink?.let { link ->
                            append("\n🔗 Iscriviti: $link")
                        } ?: event.website?.let { website ->
                            append("\n🔗 Info: $website")
                        }
                    }

                    try {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Condividi evento"))
                    } catch (e: Exception) {
                    }
                }
            )
        }
    }
}

data class EventSections(
    val eventInfo: String = "",
    val presentation: String = "",
    val professions: String = "",
    val locationInfo: String = ""
)

private fun extractEventSections(description: String): EventSections {
    var eventInfo = ""
    var presentation = ""
    var professions = ""
    var locationInfo = ""

    val parts = description.split(Regex("""\*\*(Presentazione|Elenco delle professioni.*?|Localizzazione)\*\*""", RegexOption.IGNORE_CASE))

    if (parts.isNotEmpty()) {
        eventInfo = parts[0].trim()
    }

    val presentationMatch = Regex("""\*\*Presentazione\*\*\s*(.*?)(?=\*\*[^*]+\*\*|$)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        .find(description)
    if (presentationMatch != null) {
        presentation = presentationMatch.groupValues[1].trim()
    }

    val professionsMatch = Regex("""\*\*Elenco delle professioni.*?\*\*\s*(.*?)(?=\*\*[^*]+\*\*|$)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        .find(description)
    if (professionsMatch != null) {
        professions = professionsMatch.groupValues[1].trim()
    }

    val locationMatch = Regex("""\*\*Localizzazione\*\*\s*(.*?)(?=\*\*[^*]+\*\*|$)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        .find(description)
    if (locationMatch != null) {
        locationInfo = locationMatch.groupValues[1].trim()
    }

    return EventSections(
        eventInfo = eventInfo,
        presentation = presentation,
        professions = professions,
        locationInfo = locationInfo
    )
}

@Composable
private fun EventInfoSection(eventInfo: String) {
    SectionCard(
        title = "Informazioni Evento",
        icon = Icons.Default.Info
    ) {
        FormattedDescriptionText(
            text = eventInfo,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PresentationSection(presentation: String) {
    SectionCard(
        title = "Presentazione",
        icon = Icons.Default.Description
    ) {
        Text(
            text = presentation,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProfessionsSection(professions: String) {
    SectionCard(
        title = "Professioni e Discipline",
        icon = Icons.Default.Work
    ) {
        Text(
            text = professions,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LocationInfoSection(locationInfo: String) {
    SectionCard(
        title = "Sede dell'Evento",
        icon = Icons.Default.LocationOn
    ) {
        FormattedDescriptionText(
            text = locationInfo,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HeroSection(event: Event) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Background Image
        if (event.image?.url != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.image.url)
                    .crossfade(true)
                    .build(),
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            )
        }

        if (event.featured) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "In Evidenza",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
        }
    }
}

@Composable
private fun DateSection(parsedInfo: ParsedEventInfo) {
    SectionCard(
        title = "Data e Ora",
        icon = Icons.Default.Event
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            parsedInfo.eventDate?.let { date ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            parsedInfo.eventTime?.let { time ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = time,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            parsedInfo.extractedDateTime?.let { dateTime ->
                val formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")
                Text(
                    text = dateTime.format(formatter),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationSection(venue: EventVenue) {
    SectionCard(
        title = "Sede dell'evento",
        icon = Icons.Default.LocationOn
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = venue.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (venue.address.isNotBlank()) {
                Text(
                    text = venue.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = venue.city,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (venue.website != null) {
                TextButton(
                    onClick = { /* TODO: Open venue website */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sito web della sede")
                }
            }
        }
    }
}

@Composable
private fun FormattedDescriptionText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    val sections = text.split("\n\n").filter { it.isNotEmpty() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEach { section ->
            val lines = section.split("\n").filter { it.isNotEmpty() }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                lines.forEach { line ->
                    if (line.contains("**")) {
                        FormattedLineText(
                            text = line.trim(),
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        )
                    } else {
                        Text(
                            text = line.trim(),
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattedLineText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    val parts = text.split(Regex("""(\*\*[^*]+\*\*)""")).filter { it.isNotEmpty() }

    if (parts.size == 1 && parts[0].startsWith("**") && parts[0].endsWith("**")) {
        Text(
            text = parts[0].removePrefix("**").removeSuffix("**"),
            fontSize = (fontSize.value * 1.1).sp,
            lineHeight = lineHeight,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.Top
        ) {
            parts.forEach { part ->
                when {
                    part.startsWith("**") && part.endsWith("**") -> {
                        Text(
                            text = part.removePrefix("**").removeSuffix("**"),
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    part.isNotEmpty() -> {
                        Text(
                            text = part,
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizerSection(organizers: List<EventOrganizer>) {
    SectionCard(
        title = "Organizzatori",
        icon = Icons.Default.People
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            organizers.forEach { organizer ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column {
                        Text(
                            text = organizer.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        organizer.email?.let { email ->
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        organizer.phone?.let { phone ->
                            Text(
                                text = phone,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(categories: List<EventCategory>) {
    SectionCard(
        title = "Categorie",
        icon = Icons.Default.Label
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                SuggestionChip(
                    onClick = { },
                    label = {
                        Text(
                            text = category.name,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    event: Event,
    eventLink: String?,
    onWebsiteClick: (String) -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val linkToUse = eventLink ?: event.website
        if (linkToUse != null) {
            Button(
                onClick = { onWebsiteClick(linkToUse) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (eventLink != null) "Iscriviti all'evento" else "Vai al sito dell'evento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        OutlinedButton(
            onClick = onShareClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Condividi evento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventDetailScreenPreview() {
    WombLabTheme {
        EventDetailScreen(
            eventId = "123",
            onBack = { }
        )
    }
}