package com.niemi.saillog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niemi.saillog.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SailLogTopBarWithLogo(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,

) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        title = {
            Image(
                painter = painterResource(id = R.drawable.saillog_logo),
                contentDescription = "SailLog Logo",
                modifier = Modifier.size(height = 40.dp, width = 100.dp)
            )
        },
        navigationIcon = {
            navigationIcon?.invoke()
        }
    )
}