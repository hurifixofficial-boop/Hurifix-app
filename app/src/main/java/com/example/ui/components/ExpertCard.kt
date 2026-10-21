package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.ExpertEntity

private enum class ExpertCardAction {
    CALL,
    WHATSAPP,
    MAP,
    EDIT,
    DELETE,
    WORK_HISTORY,
    TOGGLE_AVAILABILITY
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpertCard(
    expert: ExpertEntity,
    isAdmin: Boolean = false,
    onCall: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleAvailability: (Boolean) -> Unit = {},
    onViewMap: () -> Unit = {},
    onViewWorkHistory: () -> Unit = {},
    onSendWelcome: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    var showFullScreenImage by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<ExpertCardAction?>(null) }
    var pendingAvailabilityToggle by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = { /* Free space click does NOT open edit - Requirement 5 */ },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Profile Avatar, Name, Category, Phone, Availability Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            if (expert.profilePicUrl.isNotBlank()) {
                                showFullScreenImage = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (expert.profilePicUrl.isNotBlank()) {
                        AsyncImage(
                            model = expert.profilePicUrl,
                            contentDescription = expert.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = expert.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = expert.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("🛠️", fontSize = 10.sp)
                                Text(
                                    text = expert.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📱", fontSize = 12.sp)
                        Text(
                            text = expert.phone,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = expert.isAvailable,
                    onCheckedChange = { newChecked ->
                        pendingAvailabilityToggle = newChecked
                        pendingAction = ExpertCardAction.TOGGLE_AVAILABILITY
                    }
                )
            }

            // Row 2: Address & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = expert.address.ifBlank { "Local Area" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", expert.rating)} (${expert.completedJobsCount} jobs)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Row 3: Welcome WhatsApp Status Banner
            if (onSendWelcome != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (expert.isWelcomeMessageSent) Color(0xFFDCFCE7) else Color(0xFFFEF9C3),
                    border = BorderStroke(
                        1.dp,
                        if (expert.isWelcomeMessageSent) Color(0xFFBBF7D0) else Color(0xFFFEF08A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (expert.isWelcomeMessageSent) "✅" else "⚠️", fontSize = 13.sp)
                            Text(
                                text = if (expert.isWelcomeMessageSent) "Welcome Message Sent" else "Welcome Message not sent yet",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (expert.isWelcomeMessageSent) Color(0xFF166534) else Color(0xFF854D0E)
                            )
                        }

                        Button(
                            onClick = onSendWelcome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (expert.isWelcomeMessageSent) Color(0xFF22C55E) else Color(0xFFEAB308)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (expert.isWelcomeMessageSent) "Send Again" else "Send Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Row 4: View Work History & Customers Button
            OutlinedButton(
                onClick = { pendingAction = ExpertCardAction.WORK_HISTORY },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "View Work History & Customers",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Row 5: Added by info
            if (!expert.added_by_user_name.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("👤", fontSize = 11.sp)
                        Text(
                            text = "Added by: ${expert.added_by_user_name}${if (!expert.added_by_designation.isNullOrBlank()) " (${expert.added_by_designation})" else ""}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Row 6: Action Buttons (Call, WhatsApp, Map, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call Button
                Button(
                    onClick = { pendingAction = ExpertCardAction.CALL },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        Text("Call", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // WhatsApp Button ("Wha")
                Button(
                    onClick = { pendingAction = ExpertCardAction.WHATSAPP },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                        Text("Wha", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Map location button
                IconButton(
                    onClick = { pendingAction = ExpertCardAction.MAP },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "View Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isAdmin) {
                    // Edit button
                    IconButton(
                        onClick = { pendingAction = ExpertCardAction.EDIT },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button (Red)
                    IconButton(
                        onClick = { pendingAction = ExpertCardAction.DELETE },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFullScreenImage && expert.profilePicUrl.isNotBlank()) {
        FullScreenImageDialog(
            imageUrl = expert.profilePicUrl,
            title = expert.name,
            onDismiss = { showFullScreenImage = false }
        )
    }

    // Confirmation Popups for all Expert Card Buttons (Requirement 3)
    pendingAction?.let { action ->
        when (action) {
            ExpertCardAction.CALL -> HurifixConfirmDialog(
                title = "Call Expert?",
                message = "Do you want to make a direct phone call to ${expert.name} on +91 ${expert.phone}?",
                confirmText = "Call Now",
                dismissText = "Cancel",
                icon = Icons.Default.Call,
                onConfirm = {
                    onCall()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.WHATSAPP -> HurifixConfirmDialog(
                title = "Open WhatsApp Chat?",
                message = "Do you want to open WhatsApp chat with expert ${expert.name} (+91 ${expert.phone})?",
                confirmText = "Open WhatsApp",
                dismissText = "Cancel",
                icon = Icons.AutoMirrored.Filled.Send,
                onConfirm = {
                    onWhatsApp()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.MAP -> HurifixConfirmDialog(
                title = "View on Google Maps?",
                message = "Do you want to open Google Maps to view the location of ${expert.name} (${expert.address.ifBlank { "Local Area" }})?",
                confirmText = "Open Maps",
                dismissText = "Cancel",
                icon = Icons.Default.LocationOn,
                onConfirm = {
                    onViewMap()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.EDIT -> HurifixConfirmDialog(
                title = "Edit Expert Details?",
                message = "Do you want to edit profile, category, and details for ${expert.name}?",
                confirmText = "Proceed to Edit",
                dismissText = "Cancel",
                icon = Icons.Default.Edit,
                onConfirm = {
                    onEdit()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.DELETE -> HurifixConfirmDialog(
                title = "Move to Recycle Bin?",
                message = "Are you sure you want to move expert '${expert.name}' to the Recycle Bin? (Kept for 30 days)",
                confirmText = "Yes, Delete",
                dismissText = "Cancel",
                icon = Icons.Default.Delete,
                isDestructive = true,
                onConfirm = {
                    onDelete()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.WORK_HISTORY -> HurifixConfirmDialog(
                title = "View Work History?",
                message = "Do you want to view work history and assigned customer jobs for ${expert.name}?",
                confirmText = "View History",
                dismissText = "Cancel",
                icon = Icons.Default.History,
                onConfirm = {
                    onViewWorkHistory()
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
            ExpertCardAction.TOGGLE_AVAILABILITY -> HurifixConfirmDialog(
                title = if (pendingAvailabilityToggle) "Mark Expert Available?" else "Mark Expert Busy / Offline?",
                message = "Are you sure you want to change availability status of ${expert.name} to ${if (pendingAvailabilityToggle) "Available (Online)" else "Busy / Offline"}?",
                confirmText = "Yes, Change",
                dismissText = "Cancel",
                icon = Icons.Default.Warning,
                onConfirm = {
                    onToggleAvailability(pendingAvailabilityToggle)
                    pendingAction = null
                },
                onDismiss = { pendingAction = null }
            )
        }
    }
}

@Composable
private fun FullScreenImageDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.95f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
