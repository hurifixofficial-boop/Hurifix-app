package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class RecycleBinAction {
    data class RestoreJob(val job: CustomerJobEntity) : RecycleBinAction()
    data class DeleteJobForever(val job: CustomerJobEntity) : RecycleBinAction()
    data class RestoreExpert(val expert: ExpertEntity) : RecycleBinAction()
    data class DeleteExpertForever(val expert: ExpertEntity) : RecycleBinAction()
}

@Composable
fun RecycleBinDialog(
    deletedJobs: List<CustomerJobEntity>,
    deletedExperts: List<ExpertEntity>,
    onRestoreJob: (Long) -> Unit,
    onDeleteJobPermanently: (Long) -> Unit,
    onRestoreExpert: (Long) -> Unit,
    onDeleteExpertPermanently: (Long) -> Unit,
    onEmptyRecycleBin: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<RecycleBinAction?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    fun calculateDaysRemaining(deletedAt: Long?): Long {
        if (deletedAt == null) return 30L
        val elapsedMillis = System.currentTimeMillis() - deletedAt
        val elapsedDays = elapsedMillis / (24L * 60 * 60 * 1000)
        return maxOf(0L, 30L - elapsedDays)
    }

    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            title = {
                Text("Empty Recycle Bin?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to permanently delete all items in the Recycle Bin? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyRecycleBin()
                        showEmptyConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Empty Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    confirmAction?.let { action ->
        when (action) {
            is RecycleBinAction.RestoreJob -> {
                HurifixConfirmDialog(
                    title = "Restore Order #${action.job.id}?",
                    message = "Do you want to restore order #${action.job.id} for ${action.job.customerName} back to active orders?",
                    confirmText = "Yes, Restore",
                    isDestructive = false,
                    icon = Icons.Default.Restore,
                    onConfirm = {
                        confirmAction = null
                        onRestoreJob(action.job.id)
                    },
                    onDismiss = { confirmAction = null }
                )
            }
            is RecycleBinAction.DeleteJobForever -> {
                HurifixConfirmDialog(
                    title = "Delete Order Permanently?",
                    message = "Permanently delete order #${action.job.id} for ${action.job.customerName}? This action cannot be undone.",
                    confirmText = "Delete Forever",
                    isDestructive = true,
                    icon = Icons.Default.DeleteForever,
                    onConfirm = {
                        confirmAction = null
                        onDeleteJobPermanently(action.job.id)
                    },
                    onDismiss = { confirmAction = null }
                )
            }
            is RecycleBinAction.RestoreExpert -> {
                HurifixConfirmDialog(
                    title = "Restore Expert?",
                    message = "Do you want to restore expert '${action.expert.name}' (${action.expert.category}) back to active experts?",
                    confirmText = "Yes, Restore",
                    isDestructive = false,
                    icon = Icons.Default.Restore,
                    onConfirm = {
                        confirmAction = null
                        onRestoreExpert(action.expert.id)
                    },
                    onDismiss = { confirmAction = null }
                )
            }
            is RecycleBinAction.DeleteExpertForever -> {
                HurifixConfirmDialog(
                    title = "Delete Expert Permanently?",
                    message = "Permanently delete expert '${action.expert.name}' (${action.expert.category})? This action cannot be undone.",
                    confirmText = "Delete Forever",
                    isDestructive = true,
                    icon = Icons.Default.DeleteForever,
                    onConfirm = {
                        confirmAction = null
                        onDeleteExpertPermanently(action.expert.id)
                    },
                    onDismiss = { confirmAction = null }
                )
            }
        }
    }

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with rich styling & top back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444), Color(0xFFF97316))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗑️", fontSize = 18.sp)
                        }

                        Column {
                            Text(
                                text = "Recycle Bin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "30-Day Auto Retention • Safe Restore",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (deletedJobs.isNotEmpty() || deletedExperts.isNotEmpty()) {
                            TextButton(
                                onClick = { showEmptyConfirmDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Empty Bin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

                // 30-Day Info Banner
                Surface(
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                        Text(
                            text = "Deleted items are stored here for 30 days before permanent automatic deletion. You can restore them anytime!",
                            fontSize = 11.5.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Tabs: Deleted Orders & Deleted Experts
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Orders (${deletedJobs.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Experts (${deletedExperts.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                val coroutineScope = rememberCoroutineScope()
                val jobsListState = rememberLazyListState()
                val expertsListState = rememberLazyListState()
                val showJobsScrollToTop by remember {
                    derivedStateOf {
                        jobsListState.firstVisibleItemIndex > 0 || jobsListState.firstVisibleItemScrollOffset > 20
                    }
                }
                val showExpertsScrollToTop by remember {
                    derivedStateOf {
                        expertsListState.firstVisibleItemIndex > 0 || expertsListState.firstVisibleItemScrollOffset > 20
                    }
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> {
                            if (deletedJobs.isEmpty()) {
                                EmptyBinState(itemName = "orders")
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = jobsListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(deletedJobs, key = { it.id }) { job ->
                                            val daysLeft = calculateDaysRemaining(job.deletedAt)
                                            DeletedJobItemCard(
                                                job = job,
                                                daysLeft = daysLeft,
                                                dateFormat = dateFormat,
                                                onRestore = { confirmAction = RecycleBinAction.RestoreJob(job) },
                                                onDeletePermanently = { confirmAction = RecycleBinAction.DeleteJobForever(job) }
                                            )
                                        }
                                    }

                                    ScrollToTopButton(
                                        visible = showJobsScrollToTop,
                                        onClick = {
                                            coroutineScope.launch {
                                                jobsListState.animateScrollToItem(0)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .navigationBarsPadding()
                                            .padding(end = 24.dp, bottom = 36.dp)
                                    )
                                }
                            }
                        }
                        1 -> {
                            if (deletedExperts.isEmpty()) {
                                EmptyBinState(itemName = "experts")
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    LazyColumn(
                                        state = expertsListState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(deletedExperts, key = { it.id }) { expert ->
                                            val daysLeft = calculateDaysRemaining(expert.deletedAt)
                                            DeletedExpertItemCard(
                                                expert = expert,
                                                daysLeft = daysLeft,
                                                dateFormat = dateFormat,
                                                onRestore = { confirmAction = RecycleBinAction.RestoreExpert(expert) },
                                                onDeletePermanently = { confirmAction = RecycleBinAction.DeleteExpertForever(expert) }
                                            )
                                        }
                                    }

                                    ScrollToTopButton(
                                        visible = showExpertsScrollToTop,
                                        onClick = {
                                            coroutineScope.launch {
                                                expertsListState.animateScrollToItem(0)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .navigationBarsPadding()
                                            .padding(end = 24.dp, bottom = 36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tablet-safe bottom bar
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
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 500.dp)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Dashboard", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
        }
    }
}

@Composable
private fun EmptyBinState(itemName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🗑️", fontSize = 48.sp)
            Text(
                text = "Recycle Bin is empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No deleted $itemName found. Deleted items will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeletedJobItemCard(
    job: CustomerJobEntity,
    daysLeft: Long,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Order #${job.id}: ${job.customerName}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "🛠️ ${job.serviceType} • 📱 ${job.customerPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (daysLeft <= 5) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (daysLeft <= 5) Color(0xFFFCA5A5) else Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "⏳ $daysLeft days left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (daysLeft <= 5) Color(0xFFB91C1C) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (job.address.isNotBlank()) {
                Text(
                    text = "📍 ${job.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            job.deletedAt?.let {
                Text(
                    text = "Deleted on: ${dateFormat.format(Date(it))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDeletePermanently,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Forever", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DeletedExpertItemCard(
    expert: ExpertEntity,
    daysLeft: Long,
    dateFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = expert.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "🛠️ ${expert.category} • 📱 ${expert.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (daysLeft <= 5) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (daysLeft <= 5) Color(0xFFFCA5A5) else Color(0xFFFDE68A))
                ) {
                    Text(
                        text = "⏳ $daysLeft days left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (daysLeft <= 5) Color(0xFFB91C1C) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (expert.address.isNotBlank()) {
                Text(
                    text = "📍 ${expert.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            expert.deletedAt?.let {
                Text(
                    text = "Deleted on: ${dateFormat.format(Date(it))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDeletePermanently,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Forever", fontSize = 12.sp)
                }
            }
        }
    }
}
