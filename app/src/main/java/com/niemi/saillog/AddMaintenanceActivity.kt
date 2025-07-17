package com.niemi.saillog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.niemi.saillog.data.MaintenanceCategory
import com.niemi.saillog.ui.theme.SailLogTheme
import com.google.firebase.Timestamp
import com.niemi.saillog.ui.screens.AddMaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.getValue

class AddMaintenanceActivity : ComponentActivity() {

    private val viewModel: AddMaintenanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sailboatId = intent.getStringExtra("SAILBOAT_ID") ?: run {
            Toast.makeText(this, "No sailboat selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.setSailboatId(sailboatId)

        setContent {
            SailLogTheme {
                AddMaintenanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    viewModel: AddMaintenanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Maintenance added successfully", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Maintenance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Picker
            DatePickerField(
                selectedDate = uiState.selectedDate,
                onDateSelected = viewModel::updateDate
            )

            // Category Dropdown
            CategoryDropdown(
                selectedCategory = uiState.category,
                onCategorySelected = viewModel::updateCategory
            )

            // Notes Field
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Save Button
            Button(
                onClick = viewModel::saveMaintenance,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Maintenance")
                }
            }

            // Error Message
            uiState.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    OutlinedTextField(
        value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
        onValueChange = { },
        label = { Text("Date") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    onDateSelected(Date(it))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: MaintenanceCategory,
    onCategorySelected: (MaintenanceCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory.displayName,
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MaintenanceCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(null) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = { content() }
    )
}