package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    trend: String? = null,
    trendUp: Boolean = true
) {
    Card(
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                if (trend != null) {
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trendUp) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
