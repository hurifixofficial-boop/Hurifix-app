package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.example.util.LocationHelper
import com.example.util.WhatsAppHelper
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity
import com.example.data.model.RankedExpert

/**
 * Dedicated Full-Screen Page for Finding & Dispatching Nearest Experts to an Order
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearestExpertsDialog(
    job: CustomerJobEntity,
    rankedExperts: List<RankedExpert>,
    canManageOrders: Boolean = true,
    onDismiss: () -> Unit,
    onAssignExpert: (RankedExpert) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    val filteredRankedExperts = remember(rankedExperts, searchQuery) {
        if (searchQuery.isBlank()) rankedExperts
        else {
            val q = searchQuery.trim()
            rankedExperts.filter {
                it.expert.name.contains(q, ignoreCase = true) ||
                it.expert.phone.contains(q) ||
                it.expert.address.contains(q, ignoreCase = true) ||
                it.expert.category.contains(q, ignoreCase = true)
            }
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("Confirm") }
    var confirmIcon by remember { mutableStateOf(Icons.Default.Warning) }
    var confirmAction by remember { mutableStateOf<() -> Unit>({}) }

    fun requestConfirm(
        title: String,
        message: String,
        buttonText: String = "Confirm",
        icon: ImageVector = Icons.Default.Warning,
        action: () -> Unit
    ) {
        confirmTitle = title
        confirmMessage = message
        confirmButtonText = buttonText
        confirmIcon = icon
        confirmAction = action
        showConfirmDialog = true
    }

    // Modal quick confirmation alert remains an overlay
    if (showConfirmDialog) {
        HurifixConfirmDialog(
            title = confirmTitle,
            message = confirmMessage,
            confirmText = confirmButtonText,
            icon = confirmIcon,
            onConfirm = {
                showConfirmDialog = false
                confirmAction()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nearest Experts",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order #${job.orderNumberFormatted} • ${job.serviceType}",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close & Return to Orders",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Order Information Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📍", fontSize = 20.sp)
                                Text(
                                    text = "Target Order: #${job.orderNumberFormatted} • ${job.customerName}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Service: ${job.serviceType} | Mobile: +91 ${job.customerPhone}",
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Address: ${job.address}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!canManageOrders) {
                                Spacer(Modifier.height(4.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "View Only: 'Manage & Dispatch Orders' permission required to assign.",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Bar for Nearest Experts Tab (As requested)
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search expert by name, phone or area...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                item {
                    Text(
                        text = "AVAILABLE EXPERTS SORTED BY DISTANCE (${filteredRankedExperts.size})",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                if (filteredRankedExperts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍 No Matching Experts Found", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) {
                                            "No experts found matching '$searchQuery'."
                                        } else {
                                            "No registered technicians for '${job.serviceType}' in this vicinity."
                                        },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredRankedExperts, key = { it.expert.id }) { ranked ->
                        val expert = ranked.expert
                        val distanceFormatted = LocationHelper.formatDistance(ranked.distanceKm)
                        val etaText = LocationHelper.formatEstimatedTravelTime(ranked.distanceKm)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(52.dp),
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        if (!expert.profilePicUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = expert.profilePicUrl,
                                                contentDescription = expert.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = expert.name.take(1).uppercase(),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = expert.name,
                                            fontSize = 16.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${expert.category} • ⭐ ${expert.rating} (${expert.completedJobsCount} done)",
                                            fontSize = 13.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "📍 $distanceFormatted ($etaText)",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            requestConfirm(
                                                title = "Call Expert?",
                                                message = "Place phone call to ${expert.name} at ${expert.phone}?",
                                                buttonText = "Call Now",
                                                icon = Icons.Default.Phone,
                                                action = { WhatsAppHelper.openDialer(context, expert.phone) }
                                            )
                                        },
                                        modifier = Modifier.weight(0.8f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Call", fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            requestConfirm(
                                                title = "WhatsApp Expert?",
                                                message = "Open WhatsApp chat with ${expert.name}?",
                                                buttonText = "Open WhatsApp",
                                                icon = Icons.AutoMirrored.Filled.Send,
                                                action = { WhatsAppHelper.openDirectWhatsApp(context, expert.phone) }
                                            )
                                        },
                                        modifier = Modifier.weight(0.9f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF16A34A))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Chat", fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            if (!canManageOrders) {
                                                Toast.makeText(context, "🔒 Permission required: Manage & Dispatch Orders", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            requestConfirm(
                                                title = "Assign Expert to Order?",
                                                message = "Assign ${expert.name} ($distanceFormatted) to Order #${job.orderNumberFormatted} for ${job.customerName}?",
                                                buttonText = "Yes, Assign",
                                                icon = Icons.Default.CheckCircle,
                                                action = { onAssignExpert(ranked) }
                                            )
                                        },
                                        enabled = canManageOrders,
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (canManageOrders) "Assign Expert" else "🔒 Restricted",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
