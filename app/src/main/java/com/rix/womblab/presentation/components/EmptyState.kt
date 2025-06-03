package com.rix.womblab.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rix.womblab.presentation.theme.WombLabTheme
import com.rix.womblab.R

@Composable
fun EmptyState(
    title: String,
    description: String,
    emoji: String = stringResource(id = R.string.emoji_calendar),
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = actionText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentsPreview() {
    WombLabTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LoadingIndicator(
                message = stringResource(id = R.string.events_loading),
                modifier = Modifier.height(120.dp)
            )

            CompactLoadingIndicator()

            ErrorMessage(
                message = stringResource(id = R.string.error_impossible_load_data),
                onRetryClick = { }
            )

            CompactErrorMessage(
                message = stringResource(id = R.string.error_connect),
                onRetryClick = { }
            )

            EmptyState(
                title =  stringResource(id = R.string.home_nothing_events),
                description = stringResource(id = R.string.nothing_events_program_description),
                emoji = stringResource(id = R.string.emoji_calendar),
                actionText = stringResource(id = R.string.upload),
                onActionClick = { }
            )
        }
    }
}