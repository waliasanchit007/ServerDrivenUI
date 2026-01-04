package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.serverdrivenui.core.data.dto.MembershipPlanDto

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*

@Composable
fun PlanCard(
    plan: MembershipPlanDto,
    onEdit: (MembershipPlanDto) -> Unit,
    onDelete: (String) -> Unit
) {
    val containerColor = if (plan.id == "p2") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (plan.id == "p2") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.width(300.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                 Text(plan.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = contentColor)
                 
                 Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = contentColor)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                expanded = false
                                onEdit(plan) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                expanded = false
                                onDelete(plan.id) 
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            // Features
            plan.features.take(3).forEach { feature ->
                Text("• $feature", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            }
            Spacer(Modifier.height(24.dp))
            
            Text(
                "$${plan.price}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text("/ ${plan.duration}", style = MaterialTheme.typography.labelLarge, color = contentColor)
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onEdit(plan) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.id == "p2") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (plan.id == "p2") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Edit Plan")
            }
        }
    }
}
