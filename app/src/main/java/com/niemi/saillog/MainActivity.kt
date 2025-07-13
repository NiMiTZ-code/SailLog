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
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
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
import com.niemi.saillog.ui.components.AutoRefreshingSailboatCard
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
                SailLogScaffold(onSignOut = { signOut() }) {
                    MainScreen(it,
                        viewModel = viewModel,
                        onAddSailboat = { navigateToAddSailboat() }
                    )
                }
            }
        }
    }
    private fun navigateToAddSailboat() {
        startActivity(Intent(this, AddSailboatActivity::class.java))
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

    private fun signOut() {
        auth.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}

@Composable
fun MainScreen(paddingValues: PaddingValues,
               viewModel: MainViewModel,
               onAddSailboat: () -> Unit
) {

    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSailboat by viewModel.selectedSailboat.collectAsState()
    val sailboats by viewModel.sailboats.collectAsState()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
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

        // Scrollable weather
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            WeatherScreen()
        }
    }
}