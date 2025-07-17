package com.niemi.saillog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niemi.saillog.data.Maintenance
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePreviewCard(
    maintenanceList: List<Maintenance>,
    onAddClick: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Maintenance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add maintenance"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (maintenanceList.isEmpty()) {
                Text(
                    text = "No maintenance records yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                maintenanceList.forEach { maintenance ->
                    MaintenancePreviewItem(maintenance = maintenance)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (maintenanceList.size >= 3) {
                    TextButton(
                        onClick = onViewAllClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("View All")
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenancePreviewItem(maintenance: Maintenance) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = maintenance.category.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            maintenance.notes.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        maintenance.timestamp?.toDate()?.let { date ->
            Text(
                text = dateFormat.format(date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}