package com.niemi.saillog

import WeatherScreen
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.niemi.saillog.data.Sailboat
import com.niemi.saillog.AddMaintenanceActivity
import com.niemi.saillog.ui.components.AutoRefreshingSailboatCard
import com.niemi.saillog.ui.components.MaintenancePreviewCard
import com.niemi.saillog.ui.components.SailLogScaffold
import com.niemi.saillog.ui.components.SailboatCard
import com.niemi.saillog.ui.components.SailboatCardPreview
import com.niemi.saillog.ui.components.SailboatCardWithPlaceholder
import com.niemi.saillog.ui.screens.MainViewModel
import com.niemi.saillog.ui.theme.SailLogTheme
import java.time.LocalDate
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth


        enableEdgeToEdge()
        setContent {
            SailLogTheme {
                val selectedSailboat by viewModel.selectedSailboat.collectAsState()

                Scaffold(
                    floatingActionButton = {
                        selectedSailboat?.let {
                            FloatingActionButton(
                                onClick = { navigateToAddMaintenance(it.id) },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Add maintenance"
                                )
                            }
                        }
                    }
                ) { scaffoldPadding ->
                    SailLogScaffold(
                        onSignOut = { signOut() },
                        onNavigateToMaintenance = {
                            selectedSailboat?.let { navigateToMaintenanceList(it.id) }
                        }
                    ) { innerPadding ->
                        MainScreen(
                            paddingValues = PaddingValues(
                                top = innerPadding.calculateTopPadding(),
                                bottom = scaffoldPadding.calculateBottomPadding()
                            ),
                            viewModel = viewModel,
                            onAddSailboat = { navigateToAddSailboat() },
                            onAddMaintenance = {
                                selectedSailboat?.let { navigateToAddMaintenance(it.id) }
                            },
                            onViewAllMaintenance = {
                                selectedSailboat?.let { navigateToMaintenanceList(it.id) }
                            }
                        )
                    }
                }
            }
        }
    }
    private fun navigateToAddSailboat() {
        startActivity(Intent(this, AddSailboatActivity::class.java))
    }
    private fun navigateToAddMaintenance(sailboatId: String) {
        startActivity(Intent(this, AddMaintenanceActivity::class.java).apply {
            putExtra("SAILBOAT_ID", sailboatId)
        })
    }

    private fun navigateToMaintenanceList(sailboatId: String) {
        startActivity(Intent(this, MaintenanceListActivity::class.java).apply {
            putExtra("SAILBOAT_ID", sailboatId)
        })
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Not signed in, go to sign in
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        // Refresh maintenance data when returning to this activity
        viewModel.refreshMaintenance()
    }

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues,
               viewModel: MainViewModel,
               onAddSailboat: () -> Unit,
               onAddMaintenance: () -> Unit,
               onViewAllMaintenance: () -> Unit

) {

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSailboat by viewModel.selectedSailboat.collectAsState()
    val sailboats by viewModel.sailboats.collectAsState()
    val maintenanceList by viewModel.maintenanceList.collectAsState()

    //SAMPLE - when no boats added yet
    val sampleSailboat = remember {
        Sailboat(
            boatName = "My Sailboat",
            modelName = "Tap to add your first sailboat",
            manufacturer = "SailLog",
            year = LocalDate.now().year,
            imageUrl = ""
        )
    }

    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            when {
                isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                selectedSailboat != null -> {
                    // Show selected sailboat
                    AutoRefreshingSailboatCard(
                        sailboat = selectedSailboat!!,
                        onRefreshUrl = { viewModel.refreshSignedUrl(it) },
                        modifier = Modifier.padding(top = 16.dp),
                        onClick = {
                            // TODO: Navigate to sailboat selection screen
                        }
                    )
                }

                sailboats.isEmpty() -> {
                    // No sailboats yet
                    SailboatCardWithPlaceholder(
                        sailboat = sampleSailboat,
                        modifier = Modifier.padding(top = 16.dp),
                        onClick = onAddSailboat
                    )
                }

            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                WeatherScreen()
            }
        }
        item {
            selectedSailboat?.let {
                MaintenancePreviewCard(
                    maintenanceList = maintenanceList,
                    onAddClick = onAddMaintenance,
                    onViewAllClick = onViewAllMaintenance,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}