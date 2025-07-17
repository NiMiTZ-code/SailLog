package com.niemi.saillog.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SailLogScaffold(
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
    onNavigateToMaintenance: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit

) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SailLogTopBarWithLogo(
                    modifier = Modifier.padding(vertical = 16.dp),

                )

                HorizontalDivider()

                // Navigation items
                //TODO: FUTURE drawer items
//                NavigationDrawerItem(
//                    label = { Text("Logbook") },
//                    selected = false,
//                    onClick = {
//                        scope.launch { drawerState.close() }
//                        // TODO: Navigate to Logbook
//                    },
//                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
//                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Maintenance") },
                    label = { Text("Maintenance") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToMaintenance?.invoke()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                if (onSignOut != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out") },
                        label = { Text("Sign Out") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSignOut()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                SailLogTopBarWithLogo(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open drawer"
                            )
                        }
                    }
                )

            },
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content(paddingValues)
            }
        }
    }
}