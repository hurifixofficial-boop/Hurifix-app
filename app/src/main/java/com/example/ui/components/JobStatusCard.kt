package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomerJobEntity
import com.example.data.model.JobStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobStatusCard(
    job: CustomerJobEntity,
    canManageOrders: Boolean = true,
    isViewOnly: Boolean = false,
    onStatusChange: (JobStatus) -> Unit,
    onCallCustomer: () -> Unit,
    onCallExpert: () -> Unit,
    onReDispatch: () -> Unit,
    onDelete: () -> Unit,
    onViewCustomerMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isStatusExpanded by remember { mutableStateOf(false) }

    val statusColor = when (job.status) {
        JobStatus.PENDING.name -> Color(0xFFFEF3C7)
        JobStatus.PROCESSING.name -> Color(0xFFDBEAFE)
        JobStatus.COMPLETED.name -> Color(0xFFDCFCE7)
        JobStatus.CANCELLED.name -> Color(0xFFFEE2E2)
        else -> Color(0xFFF1F5F9)
    }

    val statusTextColor = when (job.status) {
        JobStatus.PENDING.name -> Color(0xFF92400E)
        JobStatus.PROCESSING.name -> Color(0xFF1E40AF)
        JobStatus.COMPLETED.name -> Color(0xFF166534)
        JobStatus.CANCELLED.name -> Color(0xFF991B1B)
        else -> Color(0xFF475569)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Service tag + Order Number + Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Order #${job.orderNumberFormatted} • ${job.serviceType}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = isStatusExpanded && canManageOrders && !isViewOnly,
                    onExpandedChange = {
                        if (canManageOrders && !isViewOnly) {
                            isStatusExpanded = !isStatusExpanded
                        }
                    }
                ) {
                    Surface(
                        color = statusColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "● ${JobStatus.valueOf(job.status).label}",
                                color = statusTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = isStatusExpanded,
                        onDismissRequest = { isStatusExpanded = false }
                    ) {
                        JobStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${status.label} (${status.hindiLabel})",
                                        fontWeight = if (job.status == status.name) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onStatusChange(status)
                                    isStatusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Customer Info
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = job.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${job.customerPhone})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "📝 Problem: ${job.issueDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = job.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onViewCustomerMap, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = "View Customer Map", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Assigned Expert Section
            if (job.assignedExpertName != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "👨‍🔧 Assigned Expert:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${job.assignedExpertName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (job.assignedExpertPhone != null) {
                                Text(
                                    text = "📞 ${job.assignedExpertPhone}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (job.assignedExpertPhone != null) {
                            FilledTonalButton(
                                onClick = onCallExpert,
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call Expert", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Creator & Manager Attribution Footer Tag
            val creatorName = job.created_by_user_name ?: "Admin"
            val creatorDesig = job.created_by_designation?.let { " ($it)" } ?: ""
            val managerText = if (job.status == JobStatus.PROCESSING.name && !job.managed_by_user_name.isNullOrBlank()) {
                val mDesig = job.managed_by_designation?.let { " ($it)" } ?: ""
                "Managed by: ${job.managed_by_user_name}$mDesig"
            } else null

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "👤 Added by: $creatorName$creatorDesig",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (managerText != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💼 $managerText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Customer
                OutlinedButton(
                    onClick = onCallCustomer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call Customer", fontSize = 11.sp)
                }

                // Re-Dispatch or Check Nearest Experts
                FilledTonalButton(
                    onClick = onReDispatch,
                    enabled = canManageOrders && !isViewOnly,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (!canManageOrders || isViewOnly) "Locked" else "Find Nearest", fontSize = 11.sp)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(38.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
