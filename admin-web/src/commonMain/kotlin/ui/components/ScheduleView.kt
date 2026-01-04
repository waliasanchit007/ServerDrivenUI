package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serverdrivenui.core.data.dto.TrainingDayDto
import kotlinx.datetime.LocalDate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*

@Composable
fun ScheduleView(
    schedule: List<TrainingDayDto>,
    onEdit: (TrainingDayDto) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(schedule) { day ->
            TrainingDayCard(day, onEdit, onDelete)
        }
    }
}

@Composable
fun TrainingDayCard(day: TrainingDayDto, onEdit: (TrainingDayDto) -> Unit, onDelete: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = day.date, // Format date properly in real app
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (day.isRestDay) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "REST DAY",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    
                    Box {
                        IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { 
                                    expanded = false
                                    onEdit(day) 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expanded = false
                                    onDelete(day.id) 
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(day.focus, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(day.description ?: "", style = MaterialTheme.typography.bodyMedium)
            
            if (day.goals.isNotEmpty()) {
               Spacer(Modifier.height(16.dp))
               HorizontalDivider()
               Spacer(Modifier.height(8.dp))
               Text("Goals: ${day.goals.joinToString()}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
