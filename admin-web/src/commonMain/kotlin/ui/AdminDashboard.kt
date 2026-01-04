package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.serverdrivenui.core.data.SupabaseGymRepository
import com.example.serverdrivenui.core.data.dto.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Add
import ui.components.AdminScaffold
import ui.components.StatsCard
import ui.components.UserTable
import ui.components.ScheduleView
import ui.components.PlanCard

@Composable
fun AdminDashboard(service: SupabaseGymRepository) {
    var selectedTab by remember { mutableStateOf("Dashboard") }
    
    AdminScaffold(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            when (selectedTab) {
                "Dashboard" -> DashboardContent(service)
                "Users" -> UsersContent(service)
                "Schedule" -> ScheduleContent(service)
                "Plans" -> PlansContent(service)
            }
        }
    }
}

@Composable
fun DashboardContent(service: SupabaseGymRepository) {
    var activeMembers by remember { mutableStateOf("...") }
    var classesCount by remember { mutableStateOf("...") }
    var revenue by remember { mutableStateOf("...") }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            val users = service.getAllUsers()
            activeMembers = users.size.toString()
            
            val schedule = service.getWeeklySchedule("2026-01-01")
            classesCount = schedule.size.toString()
            
            val payments = service.getAllPayments()
            val totalRevenue = payments.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
            revenue = "$${totalRevenue.toInt()}"
        }
    }

    Column {
        Text("Overview", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatsCard(
                title = "Total Members",
                value = activeMembers,
                icon = Icons.Outlined.People,
                trend = "Registered Users"
            )
            StatsCard(
                title = "Revenue",
                value = revenue,
                icon = Icons.Default.AttachMoney,
                trend = "Total Collected"
            )
            StatsCard(
                title = "Classes This Week",
                value = classesCount,
                icon = Icons.Default.Event, // Changed icon for variety
                trend = "Scheduled Sessions"
            )
        }
    }
}

@Composable
fun UsersContent(service: SupabaseGymRepository) {
    var users by remember { mutableStateOf<List<ProfileDto>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        users = service.getAllUsers()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("User Management", style = MaterialTheme.typography.displaySmall)
            Button(onClick = { 
                 scope.launch { users = service.getAllUsers() }
            }) {
                Text("Refresh")
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // User List (Left, larger)
            Box(modifier = Modifier.weight(3f)) {
                var userToDelete by remember { mutableStateOf<String?>(null) }
                var userToEdit by remember { mutableStateOf<ProfileDto?>(null) }

                UserTable(
                    users = users,
                    onEdit = { userToEdit = it },
                    onDelete = { userToDelete = it },
                    onCheckIn = { id ->
                        scope.launch {
                             val success = service.checkInUser(id)
                             // Ideally show a snackbar here
                             println("Check-in result for $id: $success")
                        }
                    }
                )
                
                // Delete Dialog
                if (userToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { userToDelete = null },
                        title = { Text("Confirm Deletion") },
                        text = { Text("Are you sure you want to delete this user? This action cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    scope.launch {
                                        service.deleteUser(userToDelete!!)
                                        users = service.getAllUsers()
                                        userToDelete = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { userToDelete = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                // Edit Dialog
                if (userToEdit != null) {
                    EditUserDialog(
                        user = userToEdit!!,
                        onDismiss = { userToEdit = null },
                        onSave = { updatedUser ->
                            scope.launch {
                                service.updateUser(updatedUser)
                                users = service.getAllUsers()
                                userToEdit = null
                            }
                        }
                    )
                }
            }
            
            // Actions (Right, smaller)
            Column(modifier = Modifier.weight(1f)) {
                 Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New User", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        
                        var newEmail by remember { mutableStateOf("") }
                        var newPassword by remember { mutableStateOf("") }
                        var createStatus by remember { mutableStateOf("") }
                        
                        var emailError by remember { mutableStateOf(false) }
                        var passwordError by remember { mutableStateOf(false) }
                        
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { 
                                newEmail = it
                                emailError = false
                            },
                            label = { Text("Email") },
                            isError = emailError,
                            supportingText = { if (emailError) Text("Invalid email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it 
                                passwordError = false
                            },
                            label = { Text("Password") },
                            isError = passwordError,
                            supportingText = { if (passwordError) Text("Min 6 characters") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                val isEmailValid = newEmail.contains("@") && newEmail.length > 5
                                val isPasswordValid = newPassword.length >= 6
                                
                                if (!isEmailValid) emailError = true
                                if (!isPasswordValid) passwordError = true
                                
                                if (isEmailValid && isPasswordValid) {
                                    scope.launch { 
                                        createStatus = "Creating..."
                                        val success = service.createUser(newEmail, newPassword)
                                        createStatus = if (success) "Created!" else "Failed."
                                        if (success) {
                                            users = service.getAllUsers()
                                            newEmail = ""
                                            newPassword = ""
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create User")
                        }
                        if (createStatus.isNotEmpty()) {
                             val color = if (createStatus == "Failed.") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                             Text(createStatus, style = MaterialTheme.typography.bodySmall, color = color, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                 }
                 
                 Spacer(Modifier.height(16.dp))
                 
                 Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Debug Actions", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { scope.launch { service.checkInUser("1") } },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Simulate Check-In")
                        }
                    }
                 }
            }
        }
    }
}

@Composable
fun ScheduleContent(service: SupabaseGymRepository) {
    var schedule by remember { mutableStateOf<List<TrainingDayDto>>(emptyList()) }
    var dayToEdit by remember { mutableStateOf<TrainingDayDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var dayToDelete by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    fun refreshSchedule() {
        scope.launch { schedule = service.getWeeklySchedule("2026-01-01") }
    }
    
    LaunchedEffect(Unit) {
        refreshSchedule()
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Weekly Schedule", style = MaterialTheme.typography.displaySmall)
            Button(onClick = { refreshSchedule() }) {
                Text("Refresh")
            }
        }
        Spacer(Modifier.height(24.dp))
        
        if (schedule.isEmpty()) {
            Text("No classes scheduled.", style = MaterialTheme.typography.bodyLarge)
        } else {
            ScheduleView(
                schedule = schedule,
                onEdit = { dayToEdit = it },
                onDelete = { dayToDelete = it }
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { showAddDialog = true }) {
            Text("Add Training Day")
        }
        
        // Add/Edit Dialog
        if (showAddDialog || dayToEdit != null) {
            TrainingDayDialog(
                day = dayToEdit,
                onDismiss = { 
                    showAddDialog = false
                    dayToEdit = null
                },
                onSave = { day ->
                    scope.launch {
                        if (dayToEdit != null) {
                             service.updateTrainingDay(day)
                        } else {
                             service.createTrainingDay(day)
                        }
                        refreshSchedule()
                        showAddDialog = false
                        dayToEdit = null
                    }
                }
            )
        }
        
        // Delete Dialog
        if (dayToDelete != null) {
            AlertDialog(
                onDismissRequest = { dayToDelete = null },
                title = { Text("Delete Class?") },
                text = { Text("Are you sure you want to delete this training day?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                service.deleteTrainingDay(dayToDelete!!)
                                refreshSchedule()
                                dayToDelete = null
                            }
                        },
                         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dayToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PlansContent(service: SupabaseGymRepository) {
    var plans by remember { mutableStateOf<List<MembershipPlanDto>>(emptyList()) }
    var planToEdit by remember { mutableStateOf<MembershipPlanDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    fun refreshPlans() {
        scope.launch { plans = service.getMembershipPlans() }
    }
    
    LaunchedEffect(Unit) {
        refreshPlans()
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Membership Plans", style = MaterialTheme.typography.displaySmall)
            Button(onClick = { refreshPlans() }) { Text("Refresh") }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            plans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    onEdit = { planToEdit = it },
                    onDelete = { planToDelete = it }
                )
            }
            // Add Plan Card
            Card(
                modifier = Modifier.width(300.dp).height(200.dp), // Approx height to match
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                onClick = { showAddDialog = true }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Create New Plan")
                    }
                }
            }
        }
        
        // Add/Edit Dialog
        if (showAddDialog || planToEdit != null) {
            PlanDialog(
                plan = planToEdit,
                onDismiss = { 
                    showAddDialog = false
                    planToEdit = null
                },
                onSave = { plan ->
                    scope.launch {
                        if (planToEdit != null) {
                             service.updateMembershipPlan(plan)
                        } else {
                             service.createMembershipPlan(plan)
                        }
                        refreshPlans()
                        showAddDialog = false
                        planToEdit = null
                    }
                }
            )
        }
        
        // Delete Dialog
        if (planToDelete != null) {
            AlertDialog(
                onDismissRequest = { planToDelete = null },
                title = { Text("Delete Plan?") },
                text = { Text("Are you sure you want to delete this membership plan?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                service.deleteMembershipPlan(planToDelete!!)
                                refreshPlans()
                                planToDelete = null
                            }
                        },
                         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { planToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun EditUserDialog(
    user: ProfileDto,
    onDismiss: () -> Unit,
    onSave: (ProfileDto) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var membershipStatus by remember { mutableStateOf(user.membershipStatus) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Simple dropdown or text for status for now
                OutlinedTextField(
                    value = membershipStatus,
                    onValueChange = { membershipStatus = it },
                    label = { Text("Status (active/inactive)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(user.copy(fullName = fullName, membershipStatus = membershipStatus))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TrainingDayDialog(
    day: TrainingDayDto? = null,
    onDismiss: () -> Unit,
    onSave: (TrainingDayDto) -> Unit
) {
    var date by remember { mutableStateOf(day?.date ?: "") }
    var focus by remember { mutableStateOf(day?.focus ?: "") }
    var description by remember { mutableStateOf(day?.description ?: "") }
    var goalsString by remember { mutableStateOf(day?.goals?.joinToString(", ") ?: "") }
    
    val isEdit = day != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Training Day" else "New Training Day") },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = focus,
                    onValueChange = { focus = it },
                    label = { Text("Focus (e.g., Cardio)") },
                    modifier = Modifier.fillMaxWidth()
                )
                 Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                 Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = goalsString,
                    onValueChange = { goalsString = it },
                    label = { Text("Goals (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val goals = goalsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val newDay = if (isEdit) {
                    day!!.copy(date = date, focus = focus, description = description, goals = goals)
                } else {
                    // Start of Mock ID generation if backend doesn't handle it on Create, but Supabase usually does or we send UUID. 
                    // For now, let's assume backend handles ID or we generate random. 
                    // Dto likely has ID. I'll use random UUID or empty if backend ignores.
                    TrainingDayDto(
                         id = "", // Backend should generate or ignore
                         date = date,
                         focus = focus,
                         description = description,
                         goals = goals,
                         isRestDay = false
                    )
                }
                onSave(newDay)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun PlanDialog(
    plan: MembershipPlanDto? = null,
    onDismiss: () -> Unit,
    onSave: (MembershipPlanDto) -> Unit
) {
    var name by remember { mutableStateOf(plan?.name ?: "") }
    var price by remember { mutableStateOf(plan?.price ?: "") }
    var duration by remember { mutableStateOf(plan?.duration ?: "Month") }
    var featuresString by remember { mutableStateOf(plan?.features?.joinToString(", ") ?: "") }
    
    val isEdit = plan != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Plan" else "New Plan") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (e.g., 29.99)") },
                    modifier = Modifier.fillMaxWidth()
                )
                 Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g., Month)") },
                    modifier = Modifier.fillMaxWidth()
                )
                 Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = featuresString,
                    onValueChange = { featuresString = it },
                    label = { Text("Features (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val features = featuresString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val newPlan = if (isEdit) {
                    plan!!.copy(name = name, price = price, duration = duration, features = features)
                } else {
                    MembershipPlanDto(
                         id = "", // Backend generated
                         name = name,
                         price = price,
                         duration = duration,
                         features = features,
                         priceLabel = "/ $duration",
                         isRecommended = false
                    )
                }
                onSave(newPlan)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
