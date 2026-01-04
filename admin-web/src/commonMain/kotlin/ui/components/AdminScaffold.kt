package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AdminScaffold(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        AdminNavigationRail(selectedTab, onTabSelected)
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {
            content()
        }
    }
}

@Composable
fun AdminNavigationRail(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(80.dp) // Collapsed rail style
    ) {
        Spacer(Modifier.height(24.dp))
        
        NavRailItem("Dashboard", Icons.Default.Dashboard, selectedTab, onTabSelected)
        NavRailItem("Users", Icons.Default.People, selectedTab, onTabSelected)
        NavRailItem("Schedule", Icons.Default.DateRange, selectedTab, onTabSelected)
        NavRailItem("Plans", Icons.Default.Settings, selectedTab, onTabSelected)
    }
}

@Composable
private fun NavRailItem(
    label: String,
    icon: ImageVector,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationRailItem(
        selected = selectedTab == label,
        onClick = { onTabSelected(label) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
