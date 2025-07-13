package com.niemi.saillog

import WeatherScreen
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.niemi.saillog.data.Sailboat
import com.niemi.saillog.ui.components.SailLogScaffold
import com.niemi.saillog.ui.components.SailboatCard
import com.niemi.saillog.ui.components.SailboatCardPreview
import com.niemi.saillog.ui.theme.SailLogTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth


        enableEdgeToEdge()
        setContent {
            SailLogTheme {
                SailLogScaffold(onSignOut = { signOut() }) {
                    MainScreen(it)
                }
            }
        }
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
fun MainScreen(paddingValues: PaddingValues) {

    //SAMPLE TO BE CHNG as firebase
    val sampleSailboat = remember {
        Sailboat(
            id = "1",
            boatName = "My Sailboat",
            modelName = "Bavaria 38 Cruiser",
            manufacturer = "Bavaria Yachts",
            year = 2020,
            imageUrl = ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Fixed header
        SailboatCard(
            sailboat = sampleSailboat,
            modifier = Modifier.padding(top = 16.dp),
            onClick = { /* Handle click */ }
        )

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