package com.niemi.saillog.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.niemi.saillog.data.Sailboat
import kotlinx.coroutines.delay

@Composable
fun AutoRefreshingSailboatCard(
    sailboat: Sailboat,
    onRefreshUrl: (Sailboat) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Refresh URL 5 minutes before expiry (55 minutes after load)
    LaunchedEffect(sailboat.id) {
        while (true) {
            delay(55 * 60 * 1000L) // 55 minutes
            if (!sailboat.imageStoragePath.isNullOrEmpty()) {
                onRefreshUrl(sailboat)
            }
        }
    }

    SailboatCard(
        sailboat = sailboat,
        modifier = modifier,
        onClick = onClick
    )
}