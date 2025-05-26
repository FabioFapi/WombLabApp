package com.rix.womblab.utils

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImageManager {

    private var imageLoader: ImageLoader? = null

    fun getOptimizedImageLoader(context: Context): ImageLoader {
        return imageLoader ?: createOptimizedImageLoader(context).also {
            imageLoader = it
        }
    }

    private fun createOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15)
                    .strongReferencesEnabled(false)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .dispatcher(Dispatchers.IO)
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(300)
            .build()
    }

    fun preloadImage(context: Context, url: String?) {
        url?.let {
            val request = ImageRequest.Builder(context)
                .data(it)
                .size(Size.ORIGINAL)
                .memoryCacheKey("preload_$url")
                .build()

            getOptimizedImageLoader(context).enqueue(request)
        }
    }

    fun preloadImages(context: Context, urls: List<String>) {
        val imageLoader = getOptimizedImageLoader(context)
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(400, 300)
                .memoryCacheKey("batch_$url")
                .build()
            imageLoader.enqueue(request)
        }
    }

    fun clearCache(context: Context) {
        getOptimizedImageLoader(context).memoryCache?.clear()
    }

    fun clearDiskCache(context: Context) {
        val imageLoader = getOptimizedImageLoader(context)
        imageLoader.diskCache?.clear()
    }
}

@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    isCompact: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    error: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageManager.getOptimizedImageLoader(context) }

    val imageSize = if (isCompact) Size(200, 150) else Size(800, 600)

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(imageSize)
            .crossfade(300)
            .memoryCacheKey("optimized_${if (isCompact) "compact" else "full"}_$imageUrl")
            .diskCacheKey("disk_${if (isCompact) "compact" else "full"}_$imageUrl")
            .build(),
        imageLoader = imageLoader
    )

    Box(modifier = modifier) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                placeholder?.invoke() ?: DefaultPlaceholder(isCompact = isCompact)
            }
            is AsyncImagePainter.State.Error -> {
                error?.invoke() ?: DefaultErrorPlaceholder(isCompact = isCompact)
            }
            else -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(imageSize)
                        .crossfade(300)
                        .memoryCacheKey("optimized_${if (isCompact) "compact" else "full"}_$imageUrl")
                        .build(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    imageLoader = imageLoader
                )
            }
        }
    }
}

@Composable
private fun DefaultPlaceholder(isCompact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(if (isCompact) 12.dp else 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📷",
            style = if (isCompact)
                MaterialTheme.typography.headlineSmall
            else
                MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DefaultErrorPlaceholder(isCompact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(if (isCompact) 12.dp else 8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🖼️",
            style = if (isCompact)
                MaterialTheme.typography.headlineSmall
            else
                MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun rememberImagePreloader(
    visibleItems: List<String>,
    upcomingItems: List<String> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(visibleItems) {
        scope.launch {
            ImageManager.preloadImages(context, upcomingItems.take(5))
        }
    }
}

@Composable
fun ImageMemoryManager() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            ImageManager.clearCache(context)
        }
    }
}

@Composable
fun EventCardImage(
    imageUrl: String?,
    eventId: String,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    OptimizedAsyncImage(
        imageUrl = imageUrl,
        contentDescription = "Event $eventId image",
        modifier = modifier,
        contentScale = contentScale,
        isCompact = isCompact,
        placeholder = {
            DefaultPlaceholder(isCompact = isCompact)
        },
        error = {
            DefaultErrorPlaceholder(isCompact = isCompact)
        }
    )
}

@Composable
fun HomeScreenImagePreloader(
    favoriteEvents: List<com.rix.womblab.domain.model.Event>,
    featuredEvents: List<com.rix.womblab.domain.model.Event>,
    upcomingEvents: List<com.rix.womblab.domain.model.Event>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(favoriteEvents, featuredEvents, upcomingEvents) {
        scope.launch {
            val priorityUrls = mutableListOf<String>()

            favoriteEvents.take(3).mapNotNull { it.image?.url }.forEach { url ->
                priorityUrls.add(url)
            }

            featuredEvents.take(3).mapNotNull { it.image?.url }.forEach { url ->
                priorityUrls.add(url)
            }

            upcomingEvents.take(5).mapNotNull { it.image?.url }.forEach { url ->
                priorityUrls.add(url)
            }

            ImageManager.preloadImages(context, priorityUrls.distinct())
        }
    }
}