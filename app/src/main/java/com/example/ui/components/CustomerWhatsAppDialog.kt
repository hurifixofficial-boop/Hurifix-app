package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity
import com.example.data.model.JobStatus
import com.example.util.CloudinaryHelper
import com.example.util.LocationHelper
import com.example.util.NetworkErrorHelper
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 1. Popup on Expert Assignment: Informs customer on WhatsApp with expert name, phone, and
 * estimated arrival time (which is calculated distance time + 30 minutes).
 */
@Composable
fun CustomerAssignWhatsAppDialog(
    job: CustomerJobEntity,
    expertName: String,
    expertPhone: String,
    estimatedTimeText: String,
    onSendWhatsApp: () -> Unit,
    onLater: () -> Unit
) {
    val messageText = remember(job, expertName, expertPhone, estimatedTimeText) {
        WhatsAppHelper.createCustomerAssignmentNotificationMessage(
            customerName = job.customerName,
            expertName = expertName,
            expertPhone = expertPhone,
            serviceType = job.serviceType,
            estimatedTimeText = estimatedTimeText
        )
    }

    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Inform Customer on WhatsApp",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Expert Assigned Successfully",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Key details summary card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "👤 Customer: ${job.customerName} (${job.customerPhone})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "👨‍🔧 Assigned Expert: $expertName",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "📞 Expert Contact: $expertPhone",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⏱ Estimated Arrival (+30 min buffer): $estimatedTimeText",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Message Preview:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Message text preview box
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "Note: If you tap 'Later', the customer status will show 'WhatsApp pending' and you can send anytime.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Send WhatsApp", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onLater,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Later")
            }
        }
    )
}

/**
 * 2. Popup when clicking "Mark Complete" before Review:
 * Informs customer of completed task and invites them to follow Hurifix on Instagram.
 */
@Composable
fun CustomerCompletionWhatsAppDialog(
    job: CustomerJobEntity,
    onSendWhatsApp: () -> Unit,
    onLater: () -> Unit
) {
    val messageText = remember(job) {
        WhatsAppHelper.createCompletionCustomerMessage(job.customerName)
    }

    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎉", fontSize = 18.sp)
                    }
                }
                Column {
                    Text(
                        text = "Work Completed Notice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Inform Customer on WhatsApp before Review",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Send work completion notice and Instagram link to the customer:",
                    style = MaterialTheme.typography.bodySmall
                )

                // Message preview
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "Note: If you tap 'Later', this order will be saved as 'Completion WhatsApp pending' so you can send anytime.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Send WhatsApp", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onLater,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Later")
            }
        }
    )
}

/**
 * 3. Location Permission Denied Dialog with direct Open Settings action.
 */
@Composable
fun LocationPermissionDeniedDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Location Permission Required", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Text(
                text = "Location permission is required to detect GPS coordinates. Please grant Location permission in app settings.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Open App Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 4. Device GPS / Location Disabled Dialog with direct Location Settings action.
 */
@Composable
fun GpsDisabledDialog(
    onDismiss: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color(0xFFD97706))
                Text("Device Location is Turned Off", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Text(
                text = "Device location is currently turned off. Please turn on Location in system settings to automatically detect coordinates.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onOpenLocationSettings,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Turn On Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 5. Custom Date Range Picker Dialog.
 */
@Composable
fun CustomDateRangePickerDialog(
    currentStartDate: Long?,
    currentEndDate: Long?,
    onDismiss: () -> Unit,
    onApplyRange: (Long, Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = System.currentTimeMillis()

    var startText by remember {
        mutableStateOf(currentStartDate?.let { dateFormat.format(Date(it)) } ?: dateFormat.format(Date(now - 7L * 24 * 60 * 60 * 1000)))
    }
    var endText by remember {
        mutableStateOf(currentEndDate?.let { dateFormat.format(Date(it)) } ?: dateFormat.format(Date(now)))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Select Custom Date Range", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter date in DD/MM/YYYY format:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = startText,
                    onValueChange = { startText = it; errorMsg = null },
                    label = { Text("From Date (DD/MM/YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = endText,
                    onValueChange = { endText = it; errorMsg = null },
                    label = { Text("To Date (DD/MM/YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val parsedStart = dateFormat.parse(startText.trim())
                        val parsedEnd = dateFormat.parse(endText.trim())
                        if (parsedStart == null || parsedEnd == null) {
                            errorMsg = "Please enter valid date format (DD/MM/YYYY)"
                            return@Button
                        }
                        // Set end date to end of that day (23:59:59)
                        val endCal = Calendar.getInstance().apply {
                            time = parsedEnd
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }
                        onApplyRange(parsedStart.time, endCal.timeInMillis)
                    } catch (e: Exception) {
                        errorMsg = "Invalid date. Please verify DD/MM/YYYY format."
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply Filter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 6. Confirmation dialog before opening WhatsApp to dispatch expert.
 * As requested: Does NOT open WhatsApp automatically! Shows popup first.
 */
@Composable
fun AssignExpertWhatsAppConfirmDialog(
    job: CustomerJobEntity,
    ranked: com.example.data.model.RankedExpert,
    onSendWhatsApp: () -> Unit,
    onLater: () -> Unit
) {
    val expert = ranked.expert

    AlertDialog(
        onDismissRequest = onLater,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Send Job Details to Expert?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Expert Assigned to Order #${job.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Do you want to send customer details and location to ${expert.name} on WhatsApp right now?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👨‍🔧 Expert: ${expert.name} (${expert.phone})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "👤 Customer: ${job.customerName} (${job.customerPhone})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "🛠 Service: ${job.serviceType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "📍 Address: ${job.address}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Note: If you choose 'Later', this order will show 'WhatsApp message not sent yet' with an option to send anytime.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSendWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Send via WhatsApp", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onLater,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Later")
            }
        }
    )
}

/**
 * 7. Welcome message popup for newly added partner expert.
 */
@Composable
fun SendWelcomeExpertMessageDialog(
    expert: com.example.data.model.ExpertEntity,
    isAlreadySent: Boolean = expert.isWelcomeMessageSent,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val messageText = remember(expert.name, expert.category) {
        WhatsAppHelper.createNewExpertWelcomeMessage(expert.name, expert.category)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🤝", fontSize = 18.sp)
                    }
                }
                Column {
                    Text(
                        text = if (isAlreadySent) "Send Welcome Message Again?" else "Send Welcome Message?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Welcome ${expert.name} to Hurifix",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Do you want to send the official welcome message to ${expert.name} (${expert.phone})?",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(messageText) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Send Message", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isAlreadySent) "Cancel" else "Later")
            }
        }
    )
}

/**
 * 8. Universal Delete Confirmation Dialog.
 * User requirement: "pure app me jab bhi kuch delete ka option tap jo to ek confirmation popup aana chiye delete ka"
 */
@Composable
fun UniversalDeleteConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Move to Recycle Bin",
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🗑️", fontSize = 22.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ Items are safely preserved in the Recycle Bin for 30 days before permanent auto-deletion.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 9. Dialog to Add a New Category.
 */
@Composable
fun AddNewCategoryDialog(
    onAddCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Enter the skill or trade category name:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        errorText = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Carpenter, Painter, Appliance Repair") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = categoryName.trim()
                    if (trimmed.isBlank()) {
                        errorText = "Please enter category name"
                    } else {
                        onAddCategory(trimmed)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Category", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 10. Long Press Menu for Pending and Processing Orders:
 * As requested: 2 options - Edit Customer Details & Unassign Expert.
 */
@Composable
fun OrderLongPressActionDialog(
    job: CustomerJobEntity,
    canManageOrders: Boolean = true,
    onEditDetails: () -> Unit,
    onUnassignExpert: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPending = job.status.equals("PENDING", ignoreCase = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Order #${job.orderNumberFormatted}: ${job.customerName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Status: ${job.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!canManageOrders) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                            Text(
                                text = "🔒 Action restricted: You need 'Manage & Dispatch Orders' permission to edit or unassign.",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Select an operation for this order:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Option 1: Edit Customer Details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = canManageOrders) {
                            onDismiss()
                            onEditDetails()
                        },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = if (canManageOrders) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1. Edit Customer Details",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (canManageOrders) "Update customer name, 10-digit phone, address, coordinates, and notes." else "Requires 'Manage & Dispatch Orders' permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Unassign Expert (Only if not Pending)
                if (!isPending) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canManageOrders) {
                                onDismiss()
                                onUnassignExpert()
                            },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canManageOrders) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PersonRemove,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "2. Unassign Expert",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (canManageOrders) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (!canManageOrders) {
                                        "Requires 'Manage & Dispatch Orders' permission."
                                    } else if (job.assignedExpertName != null) {
                                        "Remove ${job.assignedExpertName} and return order back to Pending."
                                    } else {
                                        "Reset assignment status and return order back to Pending."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * 11. Edit Customer Order Details Dialog:
 * Allows modifying customer name, 10-digit phone, service category (with matching autocomplete suggestions),
 * address, coordinates (with Google Maps support), and description.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerOrderDialog(
    job: CustomerJobEntity,
    availableCategories: List<String>,
    isAdmin: Boolean = false,
    onSave: (CustomerJobEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isClosedOrder = job.status.equals(JobStatus.COMPLETED.name, ignoreCase = true) ||
            job.status.equals(JobStatus.CANCELLED.name, ignoreCase = true)
    val isLockedForStaff = isClosedOrder && !isAdmin

    var name by remember { mutableStateOf(job.customerName) }
    var phone by remember { mutableStateOf(job.customerPhone) }
    var serviceType by remember { mutableStateOf(job.serviceType) }
    var address by remember { mutableStateOf(job.address) }
    var rawLocation by remember { mutableStateOf("${job.latitude}, ${job.longitude}") }
    var issueDescription by remember { mutableStateOf(job.issueDescription) }
    var selectedStatus by remember { mutableStateOf(job.status) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val cleanPhone = remember(phone) { phone.filter { it.isDigit() }.take(10) }
    val isPhoneValid = cleanPhone.length == 10

    val matchingCategories = remember(serviceType, availableCategories) {
        if (serviceType.isNotBlank() && !isLockedForStaff) {
            val set = linkedSetOf("Electrician", "Plumber")
            set.addAll(availableCategories)
            set.filter { it.contains(serviceType.trim(), ignoreCase = true) && !it.equals(serviceType.trim(), ignoreCase = true) }.take(4)
        } else emptyList()
    }

    val parsedCoords = remember(rawLocation) {
        LocationHelper.parseCoordinatesFromText(rawLocation)
    }

    BackHandler(onBack = onDismiss)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isLockedForStaff) "Order Details (#${job.id})" else "Edit Order Details (#${job.id})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isClosedOrder) {
                            Text(
                                text = if (isLockedForStaff) "🔒 Order Closed (View Only)" else "👑 Closed Order (Admin Full Edit)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLockedForStaff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
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
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isLockedForStaff) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Cancel",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    val trimmedLocation = rawLocation.trim()

                                    if (LocationHelper.isGoogleMapsUrl(trimmedLocation)) {
                                        Toast.makeText(
                                            context,
                                            "Please wait, fetching location...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    if (parsedCoords == null) {
                                        Toast.makeText(
                                            context,
                                            "❌ Please enter valid coordinates or maps link",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    showSaveConfirmDialog = true
                                },
                                enabled = name.isNotBlank() &&
                                    isPhoneValid &&
                                    serviceType.isNotBlank(),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Save Changes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Close (View Only)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (showSaveConfirmDialog) {
                HurifixConfirmDialog(
                    title = "Save Order Changes?",
                    message = "Are you sure you want to save changes for order #${job.id}?",
                    confirmText = "Yes, Save",
                    onConfirm = {
                        showSaveConfirmDialog = false
                        val coords = parsedCoords ?: Pair(job.latitude, job.longitude)
                        val updated = job.copy(
                            customerName = name.trim(),
                            customerPhone = cleanPhone,
                            serviceType = serviceType.trim(),
                            address = address.trim(),
                            latitude = coords.first,
                            longitude = coords.second,
                            issueDescription = issueDescription.trim(),
                            status = if (isAdmin) selectedStatus else job.status
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    onDismiss = { showSaveConfirmDialog = false }
                )
            }
        },
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isLockedForStaff) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ This order is marked as ${job.status}. Non-admin editing is locked. Contact an Admin to modify closed records.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (!isLockedForStaff) name = it },
                    label = { Text("Customer Name *") },
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        if (!isLockedForStaff) {
                            val digits = input.filter { it.isDigit() }.take(10)
                            phone = digits
                        }
                    },
                    label = { Text("Customer Phone (10 Digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    singleLine = true,
                    supportingText = {
                        if (!isLockedForStaff) {
                            if (!isPhoneValid) {
                                Text("Must be exactly 10 digits (${cleanPhone.length}/10)", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("✅ Valid 10-digit number")
                            }
                        }
                    },
                    isError = !isPhoneValid && phone.isNotBlank() && !isLockedForStaff,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { if (!isLockedForStaff) serviceType = it },
                    label = { Text("Service Type / Category *") },
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (matchingCategories.isNotEmpty() && !isLockedForStaff) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matchingCategories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { serviceType = cat }
                            ) {
                                Text(
                                    text = "+ $cat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { if (!isLockedForStaff) address = it },
                    label = { Text("Customer Address (Optional)") },
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawLocation,
                        onValueChange = { input -> 
        if (!isLockedForStaff) {
            rawLocation = input
            val trimmed = input.trim()

            // 🚀 Google Maps short link ko background me resolve karega
            if (LocationHelper.isGoogleMapsUrl(trimmed)) {
                coroutineScope.launch {
                    val resolved = LocationHelper.resolveAndExtractCoordinates(trimmed)
                    if (resolved != null) {
                        rawLocation = "${resolved.first}, ${resolved.second}"
                    }
                }
            }
        }
    },

                    label = { Text("Location Coordinates / Maps Link *") },
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    singleLine = true,
                    isError = parsedCoords == null && !isLockedForStaff,
                    supportingText = {
                        if (!isLockedForStaff) {
                            if (parsedCoords == null) {
                                Text("Enter valid Lat, Lng or Maps URL", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("✅ Coords: ${parsedCoords.first}, ${parsedCoords.second}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { if (!isLockedForStaff) issueDescription = it },
                    label = { Text("Problem Description (Optional)") },
                    readOnly = isLockedForStaff,
                    enabled = !isLockedForStaff,
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isAdmin && isClosedOrder) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Admin Order Status / Reopen:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(JobStatus.PENDING.name, JobStatus.PROCESSING.name, JobStatus.COMPLETED.name, JobStatus.CANCELLED.name).forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 12. Edit User Profile Dialog:
 * Allows any Hurifix user (Admin & Team Members) to configure their Name, Phone, Role/Designation, and Avatar/Profile Picture.
 */
@Composable
fun EditUserProfileDialog(
    initialName: String,
    initialPhone: String,
    initialRole: String,
    initialPhotoUri: String? = null,
    isAdmin: Boolean = false,
    currentPassword: String = "",
    onUpdatePassword: ((String) -> Unit)? = null,
    onDeleteAccount: (() -> Unit)? = null,
    onSave: (name: String, phone: String, role: String, photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf(initialName) }
    var userPhone by remember { mutableStateOf(initialPhone) }
    var userRole by remember { mutableStateOf(initialRole) }
    var photoUri by remember { mutableStateOf<String?>(initialPhotoUri) }

    // Preset avatar templates
    val defaultAvatars = listOf(
        "avatar_exec" to "👔",
        "avatar_ops" to "🛠️",
        "avatar_dispatch" to "⚡",
        "avatar_support" to "🎧",
        "avatar_star" to "🌟",
        "avatar_partner" to "💼"
    )

    // Password change state
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showPasswordOtpModal by remember { mutableStateOf(false) }

    // Delete Account confirmation state
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showImageSourcePicker by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createCameraUri(): Uri? {
        return try {
            val tempFile = File.createTempFile("user_photo_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        } catch (_: Exception) {
            null
        }
    }

    fun uploadPickedImage(uri: Uri) {
        isUploadingPhoto = true
        coroutineScope.launch {
            val phoneClean = userPhone.filter { it.isDigit() }.ifBlank { initialPhone.filter { it.isDigit() } }
            val fixedPublicId = "hurifix_profile_${phoneClean.ifBlank { "user_${System.currentTimeMillis()}" }}"
            val result = CloudinaryHelper.compressAndUpload(context, uri, publicId = fixedPublicId, overwrite = true)
            isUploadingPhoto = false
            result.onSuccess { uploadedUrl ->
                photoUri = uploadedUrl
                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                if (NetworkErrorHelper.isNetworkOrUnavailableError(err)) {
                    NetworkErrorHelper.showNetworkErrorToast(context)
                } else {
                    Toast.makeText(context, "Failed to upload photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            uploadPickedImage(tempCameraUri!!)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createCameraUri()
            if (uri != null) {
                tempCameraUri = uri
                takePictureLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Unable to access camera cache file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadPickedImage(uri)
        }
    }

    if (showImageSourcePicker) {
        ImageSourcePickerDialog(
            title = "Update Profile Picture",
            onSelectCamera = {
                val hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCameraPermission) {
                    val uri = createCameraUri()
                    if (uri != null) {
                        tempCameraUri = uri
                        takePictureLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "Unable to access camera", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onSelectGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showImageSourcePicker = false }
        )
    }

    val cleanPhone = remember(userPhone) { userPhone.filter { it.isDigit() }.take(10) }
    val isPhoneValid = cleanPhone.length == 10

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isAdmin) "Edit Admin Profile" else "Edit My Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        var showSaveConfirmDialog by remember { mutableStateOf(false) }
                        if (showSaveConfirmDialog) {
                            HurifixConfirmDialog(
                                title = "Save Profile Changes?",
                                message = "Are you sure you want to save profile changes for $userName?",
                                confirmText = "Yes, Save",
                                onConfirm = {
                                    showSaveConfirmDialog = false
                                    if (newPassword.isNotBlank()) {
                                        onUpdatePassword?.invoke(newPassword.trim())
                                    }
                                    onSave(userName.trim(), cleanPhone, userRole.trim(), photoUri)
                                    onDismiss()
                                },
                                onDismiss = { showSaveConfirmDialog = false }
                            )
                        }

                        Button(
                            onClick = {
                                showSaveConfirmDialog = true
                            },
                            enabled = userName.isNotBlank() && isPhoneValid,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Profile Picture Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isUploadingPhoto) {
                                    showImageSourcePicker = true
                                }
                        ) {
                            if (isUploadingPhoto) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else if (!photoUri.isNullOrBlank()) {
                                if (photoUri!!.startsWith("preset:")) {
                                    val emoji = photoUri!!.removePrefix("preset:")
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 42.sp)
                                    }
                                } else {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.take(1).uppercase().ifBlank { "U" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Camera badge icon on bottom right
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !isUploadingPhoto) {
                                    showImageSourcePicker = true
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = "Choose Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Avatar preset options
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Choose Default Avatar or Upload Photo:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            defaultAvatars.forEach { (_, emoji) ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (photoUri == "preset:$emoji") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        if (photoUri == "preset:$emoji") 2.dp else 1.dp,
                                        if (photoUri == "preset:$emoji") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clickable { photoUri = "preset:$emoji" }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = emoji, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                showImageSourcePicker = true
                            },
                            enabled = !isUploadingPhoto,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (isUploadingPhoto) "Uploading..." else if (photoUri.isNullOrBlank()) "Gallery / Camera" else "Change Photo", 
                                fontSize = 11.5.sp
                            )
                        }

                        if (!photoUri.isNullOrBlank()) {
                            TextButton(
                                onClick = { photoUri = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Remove", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Text(
                    text = "Update profile information and designation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = { input ->
                        userPhone = input.filter { it.isDigit() }.take(10)
                    },
                    label = { Text("Mobile Number (10 Digits) *") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = {
                        if (!isPhoneValid) {
                            Text("Must be 10 digits (${cleanPhone.length}/10)", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("✅ Valid 10-digit number")
                        }
                    },
                    isError = !isPhoneValid && userPhone.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isAdmin) {
                    OutlinedTextField(
                        value = userRole,
                        onValueChange = { userRole = it },
                        label = { Text("Admin Designation / Role Tag") },
                        singleLine = true,
                        placeholder = { Text("e.g. Co-Founder, Operations Head, Dispatch Lead") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // For Staff: Designation is locked and managed exclusively by Administrator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Designation (Assigned by Admin):",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (initialRole.isNotBlank()) initialRole else "Team Member",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Admin Set Only 🔒",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Password Change Section
                Text(
                    text = "Security & Login Password:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Login Password") },
                    placeholder = { Text("Leave blank to keep unchanged") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (newPassword.isNotBlank()) {
                    OutlinedButton(
                        onClick = { showPasswordOtpModal = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔑 Verify & Update Password via Mobile OTP", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }

                if (isAdmin && onDeleteAccount != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Account Deletion (Danger Zone):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    OutlinedButton(
                        onClick = { showDeleteAccountConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete Current Account (ID: $cleanPhone)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteAccountConfirm && onDeleteAccount != null) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Delete Account?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Text("Are you sure you want to permanently delete your account ID ($cleanPhone)? You will be immediately logged out and your credentials will be removed from Hurifix.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirm = false
                        onDismiss()
                        onDeleteAccount.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Delete My ID", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasswordOtpModal) {
        OtpVerificationDialog(
            phone = cleanPhone,
            purposeTitle = "Verify Password Change OTP",
            purposeSubtitle = "Verify mobile number before updating login password",
            onSuccess = {
                showPasswordOtpModal = false
                if (newPassword.isNotBlank()) {
                    onUpdatePassword?.invoke(newPassword.trim())
                    Toast.makeText(context, "✅ Password updated successfully!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showPasswordOtpModal = false }
        )
    }
}

/**
 * Backward compatible alias for EditUserProfileDialog
 */
@Composable
fun EditAdminProfileDialog(
    initialName: String,
    initialPhone: String,
    initialRole: String,
    initialPhotoUri: String? = null,
    isAdmin: Boolean = true,
    currentPassword: String = "",
    onUpdatePassword: ((String) -> Unit)? = null,
    onDeleteAccount: (() -> Unit)? = null,
    onSave: (name: String, phone: String, role: String, photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    EditUserProfileDialog(
        initialName = initialName,
        initialPhone = initialPhone,
        initialRole = initialRole,
        initialPhotoUri = initialPhotoUri,
        isAdmin = isAdmin,
        currentPassword = currentPassword,
        onUpdatePassword = onUpdatePassword,
        onDeleteAccount = onDeleteAccount,
        onSave = onSave,
        onDismiss = onDismiss
    )
}
