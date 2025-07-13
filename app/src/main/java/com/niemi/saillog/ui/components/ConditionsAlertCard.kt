package com.niemi.saillog.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niemi.saillog.data.AlertLevel
import com.niemi.saillog.data.WeatherAlert
import com.niemi.saillog.ui.theme.AlertColors

@Composable
fun WeatherAlertCard(
    weatherAlert: WeatherAlert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, contentColor) = when (weatherAlert.alertLevel) {
        AlertLevel.NORMAL -> AlertColors.normalBackground to AlertColors.normalContent
        AlertLevel.WARNING -> AlertColors.warningBackground to AlertColors.warningContent
        AlertLevel.CRITICAL -> AlertColors.criticalBackground to AlertColors.criticalContent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Location and Alert text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = weatherAlert.location,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = weatherAlert.alertText,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right side: Weather icon or temperature
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    weatherAlert.weatherIcon != null -> {
                        Icon(
                            painter = painterResource(id = weatherAlert.weatherIcon),
                            contentDescription = "Weather icon",
                            modifier = Modifier.size(32.dp),
                            tint = contentColor
                        )
                    }
                    weatherAlert.temperature != null -> {
                        Text(
                            text = "${weatherAlert.temperature}°",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                    else -> {
                        Text(
                            text = "!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

// Keep preview in the same file for easy testing
@Preview(showBackground = true)
@Composable
private fun WeatherAlertCardPreview() {
    Column {
        WeatherAlertCard(
            weatherAlert = WeatherAlert(
                location = "New York, NY",
                alertText = "Partly cloudy",
                alertLevel = AlertLevel.NORMAL,
                temperature = 72
            )
        )
    }
}