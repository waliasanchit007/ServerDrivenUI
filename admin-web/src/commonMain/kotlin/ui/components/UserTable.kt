package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serverdrivenui.core.data.dto.ProfileDto
import androidx.compose.runtime.*

@Composable
fun UserTable(
    users: List<ProfileDto>,
    onEdit: (ProfileDto) -> Unit,
    onDelete: (String) -> Unit,
    onCheckIn: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Email", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Batch", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(48.dp)) // Actions
            }
            
            HorizontalDivider()
            
            // Rows
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                items(users) { user ->
                    UserRow(user, onEdit, onDelete, onCheckIn)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun UserRow(user: ProfileDto, onEdit: (ProfileDto) -> Unit, onDelete: (String) -> Unit, onCheckIn: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(user.fullName.ifEmpty { "N/A" }, style = MaterialTheme.typography.bodyMedium)
        }
        Text(user.email ?: "N/A", modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
        
        // Status Badge
        Box(modifier = Modifier.weight(1f)) {
            StatusBadge(user.membershipStatus)
        }
        
        Text(user.batch ?: "-", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Check In") },
                    onClick = { 
                        expanded = false
                        onCheckIn(user.id) 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { 
                        expanded = false
                        onEdit(user) 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { 
                        expanded = false
                        onDelete(user.id) 
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "active" -> Color(0xFFE8F5E9)
        "expired" -> Color(0xFFFFEBEE)
        else -> Color(0xFFF5F5F5)
    }
    val textColor = when (status.lowercase()) {
        "active" -> Color(0xFF2E7D32)
        "expired" -> Color(0xFFC62828)
        else -> Color(0xFF616161)
    }
    
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}
