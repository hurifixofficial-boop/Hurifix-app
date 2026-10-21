package com.example.ui.components

import android.content.Intent
import android.net.Uri

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.util.LocationHelper
import com.example.util.LocationHelper.calculateTaskDuration
import com.example.util.WhatsAppHelper
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Full-Screen Page for Viewing Completed Order Details & Full History
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedOrderDetailDialog(
    job: CustomerJobEntity,
    onDismiss: () -> Unit,
    onSaveRating: (CustomerJobEntity) -> Unit
) {
    BackHandler(onBack = onDismiss)
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Order Details #${job.orderNumberFormatted}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completed Lifecycle History",
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A),
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
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Back to Orders",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 700.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Completion Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    border = BorderStroke(1.2.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF16A34A),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Job Successfully Completed",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            val durationText = calculateTaskDuration(job.createdAt, job.completedAt)
                            Text(
                                text = "Total Execution Duration: $durationText",
                                fontSize = 13.5.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Customer Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Customer Information", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text("Name: ${job.customerName}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Mobile: +91 ${job.customerPhone}", fontSize = 15.sp, fontWeight = FontWeight.Normal)
                        Text("Service Requested: ${job.serviceType}", fontSize = 15.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Address: ${job.address}", fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (job.issueDescription.isNotBlank()) {
                            Text("Service Notes / Issue: ${job.issueDescription}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { WhatsAppHelper.openDialer(context, job.customerPhone) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Call Customer", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { WhatsAppHelper.openDirectWhatsApp(context, job.customerPhone) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("WhatsApp", fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Assigned Expert & Performance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B))
                            Text("Technician / Expert Details", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text("Assigned Expert: ${job.assignedExpertName ?: "None"}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        if (!job.assignedExpertPhone.isNullOrBlank()) {
                            Text("Expert Contact: +91 ${job.assignedExpertPhone}", fontSize = 14.5.sp)
                        }

                        if (job.assigned_at_timestamp != null && job.assigned_at_timestamp > 0) {
                            Text("Dispatched At: ${dateFormat.format(Date(job.assigned_at_timestamp))}", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (!job.managed_by_user_name.isNullOrBlank()) {
                            Text("Dispatched By Staff: ${job.managed_by_user_name} (${job.managed_by_designation ?: "Staff"})", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Editable customer rating and review feedback
                        val ratingState = remember(job.id, job.ratingGiven) { mutableStateOf(job.ratingGiven?.toInt()?.coerceIn(0, 5) ?: 0) }
                        val feedbackState = remember(job.id, job.reviewFeedback) { mutableStateOf(job.reviewFeedback.orEmpty()) }
                        val savedMessage = remember(job.id) { mutableStateOf(false) }
                        val showSaveConfirmation = remember(job.id) { mutableStateOf(false) }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Customer Rating (tap stars to edit):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    (1..5).forEach { star ->
                                        Text(
                                            text = if (star <= ratingState.value) "★" else "☆",
                                            fontSize = 28.sp,
                                            color = if (star <= ratingState.value) Color(0xFFF59E0B) else Color.Gray,
                                            modifier = Modifier.clickable { ratingState.value = star; savedMessage.value = false }
                                        )
                                    }
                                    Text("${ratingState.value}/5", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                }
                                OutlinedTextField(
                                    value = feedbackState.value,
                                    onValueChange = { feedbackState.value = it; savedMessage.value = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Review feedback (optional)") },
                                    minLines = 2
                                )
                                Button(
                                    onClick = { showSaveConfirmation.value = true },
                                    enabled = ratingState.value in 1..5,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Save Rating & Feedback") }
                                if (savedMessage.value) Text("Rating update requested.", color = Color(0xFF15803D), fontSize = 12.sp)

                                if (showSaveConfirmation.value) {
                                    AlertDialog(
                                        onDismissRequest = { showSaveConfirmation.value = false },
                                        title = { Text("Save Review Changes?") },
                                        text = { Text("Are you sure you want to save this rating and feedback?") },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    onSaveRating(
                                                        job.copy(
                                                            ratingGiven = ratingState.value.takeIf { it > 0 }?.toFloat(),
                                                            reviewFeedback = feedbackState.value.trim().ifBlank { null }
                                                        )
                                                    )
                                                    savedMessage.value = true
                                                    showSaveConfirmation.value = false
                                                }
                                            ) {
                                                Text("Yes, Save")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showSaveConfirmation.value = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                    }
                }

                // Drive media links for the two distinct media groups.
                if (!job.customerMediaUrls.isNullOrBlank() || !job.technicianBeforePhotoUrl.isNullOrBlank() || !job.technicianAfterPhotoUrl.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Order Photos & Videos (Google Drive)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            val customerLinks = job.customerMediaUrls.orEmpty().split(",").map { it.trim() }.filter { it.startsWith("https://") }
                            customerLinks.forEachIndexed { index, link ->
                                OutlinedButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    context.startActivity(intent)
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (index == 0) "Photo/Video sent by Customer" else "Additional Customer Media ${index + 1}")
                                }
                            }
                            if (!job.technicianBeforePhotoUrl.isNullOrBlank()) {
                                OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.technicianBeforePhotoUrl))) }, modifier = Modifier.fillMaxWidth()) { Text("Mistri Before Photo") }
                            }
                            if (!job.technicianAfterPhotoUrl.isNullOrBlank()) {
                                OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.technicianAfterPhotoUrl))) }, modifier = Modifier.fillMaxWidth()) { Text("Mistri After Photo") }
                            }
                        }
                    }
                }

                // Timestamps & Location Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Timeline & Coordinates", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text("Order Received: ${dateFormat.format(Date(job.createdAt))}", fontSize = 14.sp)
                        if (job.completedAt != null && job.completedAt > 0) {
                            Text("Job Completed: ${dateFormat.format(Date(job.completedAt))}", fontSize = 14.sp)
                        }
                        Text("Coordinates: ${job.latitude}, ${job.longitude}", fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedButton(
                            onClick = { LocationHelper.openInGoogleMaps(context, job.latitude, job.longitude, job.customerName) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open Location on Map", fontSize = 13.5.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
