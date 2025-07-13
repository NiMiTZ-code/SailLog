package com.niemi.saillog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.niemi.saillog.ui.screens.AddSailboatScreen
import com.niemi.saillog.ui.theme.SailLogTheme

class AddSailboatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SailLogTheme {
                AddSailboatScreen(
                    onNavigateBack = {
                        finish()
                    }
                )
            }
        }
    }
}