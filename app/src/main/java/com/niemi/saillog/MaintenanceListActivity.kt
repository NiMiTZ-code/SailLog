package com.niemi.saillog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niemi.saillog.data.Maintenance
import com.niemi.saillog.AddMaintenanceActivity
import com.niemi.saillog.ui.screens.MaintenanceListViewModel
import com.niemi.saillog.ui.theme.SailLogTheme
import java.text.SimpleDateFormat
import java.util.*

class MaintenanceListActivity : ComponentActivity() {

    private val viewModel: MaintenanceListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sailboatId = intent.getStringExtra("SAILBOAT_ID") ?: run {
            finish()
            return
        }

        viewModel.loadMaintenance(sailboatId)

        setContent {
            SailLogTheme {
                MaintenanceListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onAddMaintenance = {
                        startActivity(Intent(this, AddMaintenanceActivity::class.java).apply {
                            putExtra("SAILBOAT_ID", sailboatId)
                        })
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListScreen(
    viewModel: MaintenanceListViewModel,
    onNavigateBack: () -> Unit,
    onAddMaintenance: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMaintenance,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add maintenance")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.maintenanceList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No maintenance records yet",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onAddMaintenance) {
                            Text("Add First Maintenance")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.maintenanceList) { maintenance ->
                        MaintenanceListItem(maintenance = maintenance)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListItem(maintenance: Maintenance) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = maintenance.category.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    maintenance.timestamp?.toDate()?.let { date ->
                        Text(
                            text = dateFormat.format(date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AssistChip(
                    onClick = { },
                    label = { Text(maintenance.category.displayName) },
                    enabled = false
                )
            }

            if (maintenance.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = maintenance.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}