package com.rix.womblab.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rix.womblab.domain.model.Event
import com.rix.womblab.domain.model.EventVenue
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.utils.toRelativeString
import java.time.LocalDateTime

@Composable
fun EventCard(
    event: Event,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = true,
    isCompact: Boolean = false
) {
    var isFavoritePressed by remember { mutableStateOf(false) }

    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavoritePressed) 1.2f else 1f,
        animationSpec = tween(150),
        finishedListener = { isFavoritePressed = false }
    )

    val favoriteColor by animateColorAsState(
        targetValue = if (event.isFavorite) Color(0xFFFFD700) else Color.Gray,
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEventClick(event.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (isCompact) {
            CompactEventContent(
                event = event,
                onFavoriteClick = {
                    isFavoritePressed = true
                    onFavoriteClick(event.id)
                },
                showFavoriteButton = showFavoriteButton,
                favoriteScale = favoriteScale,
                favoriteColor = favoriteColor
            )
        } else {
            FullEventContent(
                event = event,
                onFavoriteClick = {
                    isFavoritePressed = true
                    onFavoriteClick(event.id)
                },
                showFavoriteButton = showFavoriteButton,
                favoriteScale = favoriteScale,
                favoriteColor = favoriteColor
            )
        }
    }
}

@Composable
private fun FullEventContent(
    event: Event,
    onFavoriteClick: () -> Unit,
    showFavoriteButton: Boolean,
    favoriteScale: Float,
    favoriteColor: Color
) {
    Column {
        // Immagine con overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.image?.url ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Badge featured
            if (event.featured) {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "In evidenza",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottone preferiti
            if (showFavoriteButton) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .scale(favoriteScale)
                ) {
                    Icon(
                        imageVector = if (event.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (event.isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                        tint = favoriteColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Titolo overlay
            Text(
                text = event.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Contenuto card
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Luogo (senza data)
            event.venue?.let { venue ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${venue.name}, ${venue.city}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Descrizione
            if (event.excerpt.isNotBlank()) {
                Text(
                    text = event.excerpt,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            // Categorie
            if (event.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRowCategories(categories = event.categories.take(2))
            }
        }
    }
}

@Composable
private fun CompactEventContent(
    event: Event,
    onFavoriteClick: () -> Unit,
    showFavoriteButton: Boolean,
    favoriteScale: Float,
    favoriteColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Immagine compatta
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.image?.thumbnailUrl ?: event.image?.url ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (event.featured) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(bottomEnd = 8.dp)
                        )
                        .padding(2.dp)
                ) {
                    Text(
                        text = "★",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contenuto
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                event.venue?.let { venue ->
                    Text(
                        text = "${venue.name}, ${venue.city}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Bottone preferiti compatto
        if (showFavoriteButton) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(40.dp)
                    .scale(favoriteScale)
            ) {
                Icon(
                    imageVector = if (event.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (event.isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                    tint = favoriteColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LazyRowCategories(categories: List<com.rix.womblab.domain.model.EventCategory>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        text = category.name,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
    WombLabTheme {
        val sampleEvent = Event(
            id = "1",
            title = "GESTIONE DELL'IPERTENSIONE ARTERIOSA PERIOPERATORIA",
            description = "Un evento formativo per medici specialisti",
            excerpt = "L'ipertensione arteriosa perioperatoria rappresenta una problematica molto frequente che necessita di trattamento specifico.",
            url = "https://example.com",
            image = null,
            startDate = LocalDateTime.now().plusDays(3),
            endDate = LocalDateTime.now().plusDays(3),
            allDay = true,
            timezone = "Europe/Rome",
            cost = "",
            website = null,
            venue = EventVenue(
                id = "1",
                name = "Glam Hotel Milano",
                address = "Piazza Duca d'Aosta, 4/6",
                city = "Milano",
                country = "Italy",
                province = "MI",
                zip = null,
                website = null,
                showMap = true
            ),
            organizer = emptyList(),
            categories = emptyList(),
            tags = emptyList(),
            featured = true,
            status = "publish",
            isFavorite = false
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            EventCard(
                event = sampleEvent,
                onEventClick = { },
                onFavoriteClick = { }
            )

            EventCard(
                event = sampleEvent.copy(isFavorite = true),
                onEventClick = { },
                onFavoriteClick = { },
                isCompact = true
            )
        }
    }
}