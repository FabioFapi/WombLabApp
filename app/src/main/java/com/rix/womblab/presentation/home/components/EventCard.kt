package com.rix.womblab.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.rix.womblab.utils.EventCardImage
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import com.rix.womblab.R

@Stable
data class EventCardAnimationState(
    val isVisible: Boolean = false,
    val cardScale: Float = 1f,
    val favoriteScale: Float = 1f,
    val favoriteColor: Color = Color.Gray,
    val gradientAlpha: Float = 0.7f
)

@Composable
private fun rememberEventCardState(
    isVisible: Boolean,
    isPressed: Boolean,
    isFavoritePressed: Boolean,
    isFavorite: Boolean
): EventCardAnimationState {
    return remember(isVisible, isPressed, isFavoritePressed, isFavorite) {
        derivedStateOf {
            EventCardAnimationState(
                isVisible = isVisible,
                cardScale = if (isPressed) 0.95f else 1f,
                favoriteScale = if (isFavoritePressed) 1.3f else 1f,
                favoriteColor = if (isFavorite) Color(0xFFFFD700) else Color.Gray,
                gradientAlpha = if (isPressed) 0.9f else 0.7f
            )
        }
    }.value
}

@Composable
fun EventCard(
    event: Event,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = true,
    isCompact: Boolean = false,
    animationDelay: Int = 0
) {
    var isVisible by rememberSaveable(key = "visibility_${event.id}") { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var isFavoritePressed by remember { mutableStateOf(false) }

    val animationState = rememberEventCardState(
        isVisible = isVisible,
        isPressed = isPressed,
        isFavoritePressed = isFavoritePressed,
        isFavorite = event.isFavorite
    )

    LaunchedEffect(event.id) {
        if (animationDelay > 0) {
            delay(animationDelay.toLong())
        }
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = animationState.cardScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale_${event.id}"
    )

    val favoriteScale by animateFloatAsState(
        targetValue = animationState.favoriteScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { isFavoritePressed = false },
        label = "favoriteScale_${event.id}"
    )

    val favoriteColor by animateColorAsState(
        targetValue = animationState.favoriteColor,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favoriteColor_${event.id}"
    )

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(600)),
        modifier = modifier
    ) {
        if (isCompact) {
            CompactEventContent(
                event = event,
                onEventClick = onEventClick,
                onFavoriteClick = onFavoriteClick,
                showFavoriteButton = showFavoriteButton,
                cardScale = cardScale,
                favoriteScale = favoriteScale,
                favoriteColor = favoriteColor,
                onPress = { isPressed = true },
                onRelease = { isPressed = false },
                onFavoritePress = { isFavoritePressed = true }
            )
        } else {
            FullEventContent(
                event = event,
                onEventClick = onEventClick,
                onFavoriteClick = onFavoriteClick,
                showFavoriteButton = showFavoriteButton,
                cardScale = cardScale,
                favoriteScale = favoriteScale,
                favoriteColor = favoriteColor,
                gradientAlpha = animationState.gradientAlpha,
                onPress = { isPressed = true },
                onRelease = { isPressed = false },
                onFavoritePress = { isFavoritePressed = true }
            )
        }
    }
}

@Composable
private fun FullEventContent(
    event: Event,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    showFavoriteButton: Boolean,
    cardScale: Float,
    favoriteScale: Float,
    favoriteColor: Color,
    gradientAlpha: Float,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onFavoritePress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .optimizedClickable(
                onPress = onPress,
                onRelease = onRelease,
                onClick = { onEventClick(event.id) }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animateDpAsState(
                targetValue = if (cardScale < 1f) 2.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "cardElevation_${event.id}"
            ).value
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            HeroImageSection(
                event = event,
                showFavoriteButton = showFavoriteButton,
                favoriteScale = favoriteScale,
                favoriteColor = favoriteColor,
                gradientAlpha = gradientAlpha,
                onFavoriteClick = onFavoriteClick,
                onFavoritePress = onFavoritePress
            )

            ContentSection(event = event)
        }
    }
}

@Composable
private fun CompactEventContent(
    event: Event,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    showFavoriteButton: Boolean,
    cardScale: Float,
    favoriteScale: Float,
    favoriteColor: Color,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onFavoritePress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .optimizedClickable(
                onPress = onPress,
                onRelease = onRelease,
                onClick = { onEventClick(event.id) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactImageSection(event = event)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                event.venue?.let { venue ->
                    Text(
                        text = "${venue.name}, ${venue.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showFavoriteButton) {
                OptimizedFavoriteButton(
                    isFavorite = event.isFavorite,
                    scale = favoriteScale,
                    color = favoriteColor,
                    onClick = {
                        onFavoritePress()
                        onFavoriteClick(event.id)
                    },
                    size = 24.dp
                )
            }
        }
    }
}

@Composable
private fun HeroImageSection(
    event: Event,
    showFavoriteButton: Boolean,
    favoriteScale: Float,
    favoriteColor: Color,
    gradientAlpha: Float,
    onFavoriteClick: (String) -> Unit,
    onFavoritePress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        EventCardImage(
            imageUrl = event.image?.url,
            eventId = event.id,
            isCompact = false,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = gradientAlpha)
                        )
                    )
                )
        )

        if (event.featured) {
            FeaturedBadgeOptimized()
        }

        if (showFavoriteButton) {
            OptimizedFavoriteButton(
                isFavorite = event.isFavorite,
                scale = favoriteScale,
                color = favoriteColor,
                onClick = {
                    onFavoritePress()
                    onFavoriteClick(event.id)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContentSection(event: Event) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        event.venue?.let { venue ->
            OptimizedInfoRow(
                icon = Icons.Default.LocationOn,
                text = "${venue.name}, ${venue.city}"
            )
        }

        if (event.excerpt.isNotBlank()) {
            Text(
                text = event.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
        }

        if (event.categories.isNotEmpty()) {
            OptimizedCategories(categories = event.categories.take(2))
        }
    }
}

@Composable
private fun CompactImageSection(event: Event) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        EventCardImage(
            imageUrl = event.image?.thumbnailUrl ?: event.image?.url,
            eventId = event.id,
            isCompact = true,
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
                    .padding(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.card_event_star),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun OptimizedFavoriteButton(
    isFavorite: Boolean,
    scale: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 28.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                rotationZ = if (scale > 1.1f) 15f else 0f
            }
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isFavorite) stringResource(id = R.string.card_event_remove_star) else stringResource(id = R.string.card_event_added_star),
            tint = color,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
private fun OptimizedInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OptimizedCategories(categories: List<com.rix.womblab.domain.model.EventCategory>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { category ->
            SuggestionChip(
                onClick = { },
                label = {
                    Text(
                        text = category.name,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
private fun FeaturedBadgeOptimized() {
    val infiniteTransition = rememberInfiniteTransition(label = stringResource( id= R.string.card_event_featuredBadge))
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = stringResource( id= R.string.card_event_featuredBadge_label)
    )

    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = shimmer)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.card_event_featured),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun Modifier.optimizedClickable(
    onPress: () -> Unit = {},
    onRelease: () -> Unit = {},
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        onClick()
    }
}

@Preview(showBackground = true)
@Composable
fun OptimizedEventCardPreview() {
    WombLabTheme {
        val sampleEvent = Event(
            id = "1",
            title = "GESTIONE DELL'IPERTENSIONE ARTERIOSA PERIOPERATORIA",
            description = "Un evento formativo per medici specialisti",
            excerpt = "L'ipertensione arteriosa perioperatoria rappresenta una problematica molto frequente.",
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
        }
    }
}