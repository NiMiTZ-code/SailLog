package com.niemi.saillog.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddSailboatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSailboatViewModel = viewModel()
) {
    var boatName by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = boatName,
            onValueChange = { boatName = it },
            label = { Text("Boat Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = modelName,
            onValueChange = { modelName = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text("Manufacturer (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedImageUri != null) "Change Image" else "Select Image")
        }

        selectedImageUri?.let {
            Text("Image selected: ${it.lastPathSegment}")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.saveSailboat(
                    boatName = boatName,
                    modelName = modelName,
                    manufacturer = manufacturer.ifBlank { null },
                    year = year.toIntOrNull(),
                    imageUri = selectedImageUri
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = boatName.isNotBlank() && modelName.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Sailboat")
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}