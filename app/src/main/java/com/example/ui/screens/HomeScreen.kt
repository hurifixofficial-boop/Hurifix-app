package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.example.ui.components.ScrollToTopButton
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.R
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.ui.CustomerSubTab
import com.example.ui.DispatchViewModel
import com.example.ui.MainTab
import com.example.ui.OrderStatusTab
import com.example.ui.components.AddExpertDialog
import com.example.ui.components.AddNewCategoryDialog
import com.example.ui.components.AssignExpertWhatsAppConfirmDialog
import com.example.ui.components.CompletedOrderDetailDialog
import com.example.ui.components.CustomerAssignWhatsAppDialog
import com.example.ui.components.CustomerCompletionWhatsAppDialog
import com.example.ui.components.CustomDateRangePickerDialog
import com.example.ui.components.EditAdminProfileDialog
import com.example.ui.components.EditCustomerOrderDialog
import com.example.ui.components.ExpertsRankingDialog
import com.example.ui.components.GpsDisabledDialog
import com.example.ui.components.LocationPermissionDeniedDialog
import com.example.ui.components.MonthlyAnalyticsDialog
import com.example.ui.components.NearestExpertsDialog
import com.example.ui.components.OrderLongPressActionDialog
import com.example.ui.components.ReviewDialog
import com.example.ui.components.SendWelcomeExpertMessageDialog
import com.example.ui.components.UniversalDeleteConfirmationDialog
import com.example.ui.components.WhatsAppLeadParserDialog
import com.example.util.LocationHelper.calculateTaskDuration
import com.example.util.BackupRestoreHelper
import com.example.util.LocationHelper
import com.example.util.NetworkErrorHelper
import com.example.util.NotificationHelper
import com.example.util.SessionManager
import com.example.util.SoundHelper
import com.example.util.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class DeleteTarget {
    data class Job(val job: CustomerJobEntity) : DeleteTarget()
    data class Expert(val expert: ExpertEntity) : DeleteTarget()
    data class Category(val category: ExpertCategoryEntity) : DeleteTarget()
}

data class OrderCollisionTarget(
    val job: CustomerJobEntity,
    val managerName: String,
    val managerDesignation: String,
    val onProceed: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DispatchViewModel,
    sessionManager: SessionManager,
    isDarkMode: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val rawExperts by viewModel.allExperts.collectAsState()
    val experts = remember(rawExperts) { rawExperts.filter { !it.isDeleted } }
    val allJobs by viewModel.allJobs.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val deletedJobs by viewModel.deletedJobs.collectAsState()
    val deletedExperts by viewModel.deletedExperts.collectAsState()
    val currentMainTab by viewModel.currentMainTab.collectAsState()
    val currentCustomerSubTab by viewModel.currentCustomerSubTab.collectAsState()
    val currentOrderStatusTab by viewModel.currentOrderStatusTab.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val activeJobForNearestExperts by viewModel.activeJobForNearestExperts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Dialog & Flow States
    var showAddExpertDialog by remember { mutableStateOf(false) }
    var expertToEdit by remember { mutableStateOf<ExpertEntity?>(null) }
    var expertForWorkHistory by remember { mutableStateOf<ExpertEntity?>(null) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var showWhatsAppParserDialog by remember { mutableStateOf(false) }
    var showSaveChoicePopup by remember { mutableStateOf<CustomerJobEntity?>(null) }
    var showRankingDialog by remember { mutableStateOf(false) }
    var showMonthlyAnalyticsDialog by remember { mutableStateOf(false) }
    var showCompletedDetailJob by remember { mutableStateOf<CustomerJobEntity?>(null) }
    var completionFormJob by remember { mutableStateOf<CustomerJobEntity?>(null) }
    var reviewJobTarget by remember { mutableStateOf<Pair<CustomerJobEntity, Boolean>?>(null) }

    // Popup flow for expert assignment & customer inform
    var showAssignExpertWhatsAppPopup by remember { mutableStateOf<Pair<CustomerJobEntity, RankedExpert>?>(null) }
    var showAssignCustomerWhatsAppPopup by remember { mutableStateOf<Triple<CustomerJobEntity, RankedExpert, String>?>(null) }
    var showCompletionCustomerWhatsAppJob by remember { mutableStateOf<CustomerJobEntity?>(null) }

    // Welcome expert message popup
    var showWelcomeExpertDialog by remember { mutableStateOf<Pair<ExpertEntity, Boolean>?>(null) }

    // Admin profile state & dialog
    val syncManager = remember { com.example.data.firebase.FirestoreSyncManager.getInstance(context) }
    val syncState by syncManager.syncState.collectAsState()
    var showUserManagementDialog by remember { mutableStateOf(false) }

    var showEditAdminProfileDialog by remember { mutableStateOf(false) }
    var adminName by remember { mutableStateOf(sessionManager.getUserName()) }
    var adminPhone by remember { mutableStateOf(sessionManager.getUserPhone()) }
    var adminDesignation by remember { mutableStateOf(sessionManager.getUserDesignationTag()) }
    var adminPhotoUri by remember { mutableStateOf(sessionManager.getUserPhotoUri()) }
    var currentUserRole by remember { mutableStateOf(sessionManager.getUserRole()) }
    var isAdminUser by remember { mutableStateOf(sessionManager.isAdmin()) }
    var canManageOrders by remember { mutableStateOf(sessionManager.canManageOrders()) }
    var canAddExperts by remember { mutableStateOf(sessionManager.canAddExperts()) }
    var canAddCustomers by remember { mutableStateOf(sessionManager.canAddCustomers()) }
    var canDeleteOrders by remember { mutableStateOf(sessionManager.canDeleteOrders()) }
    var canExportReports by remember { mutableStateOf(sessionManager.canExportReports()) }
    var isViewOnly by remember { mutableStateOf(sessionManager.isViewOnly()) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        syncManager.startRealtimeSyncListeners()
        viewModel.refreshAllData()
    }

    LaunchedEffect(sessionManager.getUserPhone()) {
        adminName = sessionManager.getUserName()
        adminPhone = sessionManager.getUserPhone()
        adminDesignation = sessionManager.getUserDesignationTag()
        adminPhotoUri = sessionManager.getUserPhotoUri()
        currentUserRole = sessionManager.getUserRole()
        isAdminUser = sessionManager.isAdmin()
        canManageOrders = sessionManager.canManageOrders()
        canAddExperts = sessionManager.canAddExperts()
        canAddCustomers = sessionManager.canAddCustomers()
        canDeleteOrders = sessionManager.canDeleteOrders()
        canExportReports = sessionManager.canExportReports()
        isViewOnly = sessionManager.isViewOnly()

        val currentPhone = sessionManager.getUserPhone()
        if (currentPhone.isNotBlank()) {
            syncManager.listenToActiveUserSession(currentPhone) { updatedUser ->
                if (updatedUser == null || updatedUser.is_blocked) {
                    Toast.makeText(
                        context,
                        "Your account access has been revoked or blocked by Administrator. Please contact Admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    sessionManager.logout()
                    onLogout()
                } else {
                    sessionManager.saveUserSession(updatedUser)
                    adminName = updatedUser.name
                    adminPhone = updatedUser.phone
                    adminDesignation = updatedUser.displayDesignation
                    adminPhotoUri = updatedUser.profile_pic_url
                    currentUserRole = updatedUser.role
                    isAdminUser = updatedUser.isAdmin
                    canManageOrders = sessionManager.canManageOrders()
                    canAddExperts = sessionManager.canAddExperts()
                    canAddCustomers = sessionManager.canAddCustomers()
                    canDeleteOrders = sessionManager.canDeleteOrders()
                    canExportReports = sessionManager.canExportReports()
                    isViewOnly = sessionManager.isViewOnly()
                }
            }
        }
    }

    // Order Long Press Action & Edit Customer state
    var activeLongPressJob by remember { mutableStateOf<CustomerJobEntity?>(null) }
    var editingCustomerJob by remember { mutableStateOf<CustomerJobEntity?>(null) }

    // Collision Protection Warning state
    var collisionWarningTarget by remember { mutableStateOf<OrderCollisionTarget?>(null) }

    fun checkCollisionAndExecute(job: CustomerJobEntity, onProceed: () -> Unit) {
        val currentUserId = sessionManager.getUserPhone().replace(Regex("[^0-9]"), "")
        val jobManagerId = job.managed_by_user_id?.replace(Regex("[^0-9]"), "") ?: ""
        val isManagedByOther = jobManagerId.isNotBlank() && jobManagerId != currentUserId

        if (isManagedByOther) {
            val managerName = job.managed_by_user_name ?: "Another Team Member"
            val managerDesig = job.managed_by_designation ?: "Partner"
            collisionWarningTarget = OrderCollisionTarget(
                job = job,
                managerName = managerName,
                managerDesignation = managerDesig,
                onProceed = onProceed
            )
        } else {
            onProceed()
        }
    }

    // Universal delete confirmation dialog
    var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

    // Add category dialog
    var showAddNewCategoryDialog by remember { mutableStateOf(false) }

    // Backup restore document launcher
    val restoreFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val data = BackupRestoreHelper.parseBackupJson(stream)
                    viewModel.restoreBackupData(data.jobs, data.experts, data.categories)
                    Toast.makeText(context, "Restored ${data.jobs.size} orders and ${data.experts.size} experts!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to restore backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Permission launcher for Android 13+ notifications
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        NotificationHelper.createNotificationChannels(context)
    }

    LaunchedEffect(allJobs) {
        // Trigger background reminder checks for pending > 1hr, processing > 1hr, unsent messages
        NotificationHelper.checkAndTriggerReminders(context, allJobs)
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(310.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header with Logo, Name & Tagline
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hurifix_logo_exact_1790919761852),
                                contentDescription = "Hurifix Logo",
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column {
                                Text(
                                    text = "Hurifix",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Many Problems | One Solution",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Admin Profile Card with Edit Option (As explicitly requested)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(46.dp),
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        if (!adminPhotoUri.isNullOrBlank()) {
                                            if (adminPhotoUri!!.startsWith("preset:")) {
                                                val emoji = adminPhotoUri!!.removePrefix("preset:")
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text(text = emoji, fontSize = 24.sp)
                                                }
                                            } else {
                                                AsyncImage(
                                                    model = adminPhotoUri,
                                                    contentDescription = "Profile Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            }
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = adminName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = adminName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "📞 +91 $adminPhone",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = if (isAdminUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isAdminUser) "👑 $adminDesignation" else "👤 $adminDesignation",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        showEditAdminProfileDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Edit Profile & Security", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (isAdminUser) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch { drawerState.close() }
                                            showUserManagementDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("👑 Admin Panel & Drive Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // SECTION 1: OPERATIONS & ORDERS
                        Text(
                            text = "OPERATIONS & ORDERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )

                        if (canManageOrders && !isViewOnly) {
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "📝 Dispatch New Order",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                selected = currentMainTab == MainTab.CUSTOMER_ORDERS && currentCustomerSubTab == CustomerSubTab.DISPATCH_ORDER,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    viewModel.selectMainTab(MainTab.CUSTOMER_ORDERS)
                                    viewModel.selectCustomerSubTab(CustomerSubTab.DISPATCH_ORDER)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(Modifier.height(3.dp))
                        }

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "📋 Customer Orders (${allJobs.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            selected = currentMainTab == MainTab.CUSTOMER_ORDERS && currentCustomerSubTab == CustomerSubTab.ORDERS,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.selectMainTab(MainTab.CUSTOMER_ORDERS)
                                viewModel.selectCustomerSubTab(CustomerSubTab.ORDERS)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(3.dp))

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "🛠 Manage Experts (${experts.size})",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            selected = currentMainTab == MainTab.EXPERTS,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.selectMainTab(MainTab.EXPERTS)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))

                        // SECTION 2: PERFORMANCE & INSIGHTS
                        Text(
                            text = "PERFORMANCE & INSIGHTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "🏆 Experts Leaderboard",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                showRankingDialog = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (canExportReports && !isViewOnly) {
                            Spacer(Modifier.height(3.dp))

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "📊 Monthly Performance & Reports",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showMonthlyAnalyticsDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        if (canDeleteOrders && !isViewOnly) {
                            Spacer(Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "SYSTEM & UTILITIES",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                            )

                            NavigationDrawerItem(
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🗑️ Recycle Bin",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        val totalDeleted = deletedJobs.size + deletedExperts.size
                                        if (totalDeleted > 0) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    text = "$totalDeleted",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                selected = false,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showRecycleBinDialog = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(8.dp))

                        // SECTION 4: PREFERENCES & SETTINGS
                        Text(
                            text = "PREFERENCES & SETTINGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )

                        // Dark Theme Toggle Switch Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isDarkMode) "Dark Theme" else "Light Theme",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = { onToggleDarkMode(it) }
                                )
                            }
                        }
                    }

                    // Logout Button at bottom
                    Button(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showLogoutConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Log Out",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        if (currentMainTab == MainTab.EXPERTS) {
                            IconButton(onClick = { viewModel.selectMainTab(MainTab.CUSTOMER_ORDERS) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Orders"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                modifier = Modifier.testTag("app_navigation_drawer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Navigation Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hurifix_logo_exact_1790919761852),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(
                                text = if (currentMainTab == MainTab.EXPERTS) "Manage Experts (${experts.size})" else "Hurifix",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    actions = {}
                )
            }
        ) { paddingValues ->
            val pullToRefreshState = rememberPullToRefreshState()
            if (pullToRefreshState.isRefreshing) {
                LaunchedEffect(true) {
                    viewModel.refreshAllData()
                }
            }
            LaunchedEffect(isRefreshing) {
                if (!isRefreshing) {
                    pullToRefreshState.endRefresh()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                AnimatedContent(
                    targetState = currentMainTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 250)) + slideInVertically(
                            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                            initialOffsetY = { 35 }
                        )).togetherWith(
                            fadeOut(animationSpec = tween(durationMillis = 200))
                        )
                    },
                    label = "home_main_tab_transition"
                ) { tab ->
                    when (tab) {
                        MainTab.CUSTOMER_ORDERS -> {
                            CustomerOrdersSection(
                                viewModel = viewModel,
                                allJobs = allJobs,
                                currentSubTab = currentCustomerSubTab,
                                currentOrderStatusTab = currentOrderStatusTab,
                                canManageOrders = canManageOrders && !isViewOnly,
                                canDeleteOrders = canDeleteOrders && !isViewOnly,
                                isViewOnly = isViewOnly,
                                onOpenWhatsAppParser = { showWhatsAppParserDialog = true },
                                onOrderSaved = { savedJob ->
                                    SoundHelper.playSFX("success")
                                    showSaveChoicePopup = savedJob
                                },
                                onOpenNearestExperts = { job ->
                                    if (canManageOrders && !isViewOnly) {
                                        checkCollisionAndExecute(job) {
                                            viewModel.openFindNearestExperts(job)
                                        }
                                    } else {
                                        Toast.makeText(context, "Requires 'Manage & Dispatch Orders' permission to assign experts", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onCompleteOrCancelAction = { job, isComplete ->
                                    if (canManageOrders && !isViewOnly) {
                                        checkCollisionAndExecute(job) {
                                            if (isComplete) {
                                                completionFormJob = job
                                            } else {
                                                reviewJobTarget = Pair(job, false)
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Requires 'Manage & Dispatch Orders' permission to complete or cancel orders", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onShowCompletedDetail = { job ->
                                    showCompletedDetailJob = job
                                },
                                onDeleteJob = { job ->
                                    checkCollisionAndExecute(job) {
                                        deleteTarget = DeleteTarget.Job(job)
                                    }
                                },
                                onLongPressOrder = { job ->
                                    if (canManageOrders && !isViewOnly) {
                                        activeLongPressJob = job
                                    } else {
                                        Toast.makeText(context, "🔒 'Manage & Dispatch Orders' permission required to edit or unassign", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        MainTab.EXPERTS -> {
                            val categoryNames = remember(allCategories) {
                                allCategories.map { it.name }
                            }
                            ExpertsTabContent(
                                experts = experts,
                                categories = allCategories,
                                canAddExperts = canAddExperts && !isViewOnly,
                                isAdmin = isAdminUser,
                                onAddNewCategory = { viewModel.addNewCategory(it) },
                                onDeleteCategory = { deleteTarget = DeleteTarget.Category(it) },
                                onEditExpert = { expert ->
                                    expertToEdit = expert
                                    showAddExpertDialog = true
                                },
                                onDeleteExpert = { expert ->
                                    viewModel.deleteExpert(expert)
                                },
                                onToggleAvailability = { exp, avail -> viewModel.updateExpert(exp.copy(isAvailable = avail)) },
                                onViewWorkHistory = { expert -> expertForWorkHistory = expert },
                                onSendWelcome = { expert -> showWelcomeExpertDialog = Pair(expert, false) },
                                onAddExpert = {
                                    expertToEdit = null
                                    showAddExpertDialog = true
                                }
                            )
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    // Dialog 1: Add / Edit Expert Dialog
    if (showAddExpertDialog) {
        val catNames = remember(allCategories) { allCategories.map { it.name } }
        AddExpertDialog(
            initialExpert = expertToEdit,
            availableCategories = catNames,
            isAdmin = isAdminUser,
            currentUserId = sessionManager.getUserPhone().replace(Regex("[^0-9]"), ""),
            currentUserName = sessionManager.getUserName().ifBlank { "Admin" },
            currentUserDesignation = sessionManager.getUserDesignationTag(),
            existingPhones = experts.filter { !it.isDeleted }.map { it.phone },
            onAddNewCategory = { viewModel.addNewCategory(it) },
            onDismiss = {
                showAddExpertDialog = false
                expertToEdit = null
            },
            onSave = { expert ->
                if (expertToEdit == null) {
                    viewModel.saveNewExpert(expert) { saved ->
                        showWelcomeExpertDialog = Pair(saved, true)
                    }
                } else {
                    viewModel.updateExpert(expert)
                }
                showAddExpertDialog = false
                expertToEdit = null
            }
        )
    }

    // Dialog 2: WhatsApp Lead Parser
    if (showWhatsAppParserDialog) {
        WhatsAppLeadParserDialog(
            onDismiss = { showWhatsAppParserDialog = false },
            onParseText = { rawText ->
                viewModel.parseAndFillFromWhatsAppText(rawText)
                showWhatsAppParserDialog = false
            }
        )
    }

    // Dialog 3: Save Order Popup with choices: Find Nearest Experts vs Later
    showSaveChoicePopup?.let { job ->
        AlertDialog(
            onDismissRequest = {
                showSaveChoicePopup = null
                viewModel.selectCustomerSubTab(CustomerSubTab.ORDERS)
                viewModel.selectOrderStatusTab(OrderStatusTab.PENDING)
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✅", fontSize = 22.sp)
                    Text(
                        text = "New Customer Order Saved!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Order for '${job.customerName}' has been successfully saved to database.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Would you like to find nearest experts now or assign later?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentJob = job
                        showSaveChoicePopup = null
                        viewModel.openFindNearestExperts(currentJob)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("1. Find Nearest Experts", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSaveChoicePopup = null
                        viewModel.selectCustomerSubTab(CustomerSubTab.ORDERS)
                        viewModel.selectOrderStatusTab(OrderStatusTab.PENDING)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("2. Later (Pending List)")
                }
            }
        )
    }

    // Dialog 4: Nearest Experts List with ASSIGN button
    activeJobForNearestExperts?.let { job ->
        val rankedExperts = viewModel.getNearestExpertsForJob(job)
        NearestExpertsDialog(
            job = job,
            rankedExperts = rankedExperts,
            canManageOrders = canManageOrders && !isViewOnly,
            onDismiss = { viewModel.closeFindNearestExperts() },
            onAssignExpert = { ranked ->
                if (canManageOrders && !isViewOnly) {
                    val mId = sessionManager.getUserPhone().replace(Regex("[^0-9]"), "")
                    val mName = sessionManager.getUserName().ifBlank { "User" }
                    val mDesig = sessionManager.getUserDesignationTag()
                    viewModel.assignExpertToJob(
                        job = job,
                        ranked = ranked,
                        managedByUserId = mId,
                        managedByUserName = mName,
                        managedByDesignation = mDesig
                    )
                    viewModel.closeFindNearestExperts()
                    // Prompt user to send WhatsApp to expert (does NOT open WhatsApp automatically!)
                    showAssignExpertWhatsAppPopup = Pair(job, ranked)
                } else {
                    Toast.makeText(context, "Requires 'Manage & Dispatch Orders' permission to assign experts", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog 4b: Confirmation popup to send WhatsApp to assigned expert
    showAssignExpertWhatsAppPopup?.let { (job, ranked) ->
        AssignExpertWhatsAppConfirmDialog(
            job = job,
            ranked = ranked,
            onSendWhatsApp = {
                showAssignExpertWhatsAppPopup = null
                WhatsAppHelper.sendWhatsAppMessageToExpert(
                    context = context,
                    expert = ranked.expert,
                    customer = job
                )
                viewModel.updateExpertNotified(job.id, true)
                val estimatedTimeText = WhatsAppHelper.calculateEstimatedArrivalTimeWithBuffer(ranked.distanceKm)
                showAssignCustomerWhatsAppPopup = Triple(job, ranked, estimatedTimeText)
            },
            onLater = {
                showAssignExpertWhatsAppPopup = null
                viewModel.updateExpertNotified(job.id, false)
                viewModel.markMessageLaterDismissed(job.id)
                val estimatedTimeText = WhatsAppHelper.calculateEstimatedArrivalTimeWithBuffer(ranked.distanceKm)
                showAssignCustomerWhatsAppPopup = Triple(job, ranked, estimatedTimeText)
            }
        )
    }

    // Full-screen completion workflow: media + rating/review, then confirmation, then existing WhatsApp popup.
    completionFormJob?.let { job ->
        com.example.ui.components.CompleteOrderScreen(
            job = job,
            onDismiss = { completionFormJob = null },
            onConfirmComplete = { updatedJob, rating, feedback ->
                completionFormJob = null
                viewModel.completeOrCancelJobWithReview(
                    job = updatedJob, isCompleted = true, rating = rating, feedback = feedback,
                    beforeUrl = updatedJob.technicianBeforePhotoUrl, afterUrl = updatedJob.technicianAfterPhotoUrl
                )
                showCompletionCustomerWhatsAppJob = updatedJob
            }
        )
    }

    // Dialog 5: Review & Confirmation Dialog for Complete / Cancel
    reviewJobTarget?.let { (job, isCompleted) ->
        ReviewDialog(
            job = job,
            isComplete = isCompleted,
            onDismiss = { reviewJobTarget = null },
            onConfirm = { rating, feedback ->
                reviewJobTarget = null
                viewModel.completeOrCancelJobWithReview(job, isCompleted, rating, feedback)
            }
        )
    }

    // Dialog 6: Experts Ranking Dialog
    if (showRankingDialog) {
        ExpertsRankingDialog(
            experts = experts,
            onDismiss = { showRankingDialog = false }
        )
    }

    // Dialog 7: Monthly Analytics Dialog
    if (showMonthlyAnalyticsDialog) {
        MonthlyAnalyticsDialog(
            jobs = allJobs,
            onDismiss = { showMonthlyAnalyticsDialog = false }
        )
    }

    // Dialog 8: Completed Order Full History & Detail
    showCompletedDetailJob?.let { job ->
        CompletedOrderDetailDialog(
            job = job,
            onDismiss = { showCompletedDetailJob = null },
            onSaveRating = { updatedJob -> viewModel.updateJob(updatedJob) }
        )
    }

    // Dialog 9: WhatsApp Inform Customer on Assignment (+30 min buffer)
    showAssignCustomerWhatsAppPopup?.let { (job, ranked, estTime) ->
        CustomerAssignWhatsAppDialog(
            job = job,
            expertName = ranked.expert.name,
            expertPhone = ranked.expert.phone,
            estimatedTimeText = estTime,
            onSendWhatsApp = {
                val msg = WhatsAppHelper.createCustomerAssignmentNotificationMessage(
                    customerName = job.customerName,
                    expertName = ranked.expert.name,
                    expertPhone = ranked.expert.phone,
                    serviceType = job.serviceType,
                    estimatedTimeText = estTime
                )
                WhatsAppHelper.sendWhatsAppDirectMessage(context, job.customerPhone, msg)
                viewModel.updateCustomerNotifiedOnAssign(job.id, true)
                showAssignCustomerWhatsAppPopup = null
            },
            onLater = {
                // As requested: Do NOT mark customer whatsapp sent!
                viewModel.updateCustomerNotifiedOnAssign(job.id, false)
                showAssignCustomerWhatsAppPopup = null
            }
        )
    }

    // Dialog 10: WhatsApp Inform Customer on Completion (Before Review)
    showCompletionCustomerWhatsAppJob?.let { job ->
        CustomerCompletionWhatsAppDialog(
            job = job,
            onSendWhatsApp = {
                val msg = WhatsAppHelper.createCompletionCustomerMessage(job.customerName)
                WhatsAppHelper.sendWhatsAppDirectMessage(context, job.customerPhone, msg)
                viewModel.updateCustomerNotifiedOnCompletion(job.id, true)
                showCompletionCustomerWhatsAppJob = null
            },
            onLater = {
                // As requested: Do NOT mark completion message sent!
                viewModel.updateCustomerNotifiedOnCompletion(job.id, false)
                showCompletionCustomerWhatsAppJob = null
            }
        )
    }

    // Dialog 11: Welcome message to newly added Expert
    showWelcomeExpertDialog?.let { (expert, isNewCreation) ->
        SendWelcomeExpertMessageDialog(
            expert = expert,
            isAlreadySent = !isNewCreation || expert.isWelcomeMessageSent,
            onSend = { messageText ->
                showWelcomeExpertDialog = null
                viewModel.updateWelcomeMessageSent(expert.id, true)
                WhatsAppHelper.sendWhatsAppDirectMessage(context, expert.phone, messageText)
            },
            onDismiss = {
                showWelcomeExpertDialog = null
            }
        )
    }

    // Dialog 11b: Edit Admin Profile Dialog
    if (showEditAdminProfileDialog) {
        EditAdminProfileDialog(
            initialName = adminName,
            initialPhone = adminPhone,
            initialRole = adminDesignation,
            initialPhotoUri = adminPhotoUri,
            isAdmin = isAdminUser,
            currentPassword = sessionManager.getUserPassword(),
            onUpdatePassword = { newPassword ->
                coroutineScope.launch {
                    val res = sessionManager.updatePassword(newPassword)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteAccount = if (isAdminUser) {
                {
                    coroutineScope.launch {
                        val res = sessionManager.deleteCurrentUserAccount()
                        if (res.isSuccess) {
                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            onLogout()
                        } else {
                            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else null,
            onSave = { newName, newPhone, newRole, newPhotoUri ->
                val finalDesignation = if (isAdminUser) newRole else adminDesignation
                val previousPhone = adminPhone
                if (isAdminUser) {
                    sessionManager.updateAdminProfile(newName, newPhone, finalDesignation, newPhotoUri)
                } else {
                    sessionManager.updateUserProfile(newName, newPhone, finalDesignation, newPhotoUri)
                }
                adminName = newName
                adminPhone = newPhone
                adminDesignation = finalDesignation
                adminPhotoUri = newPhotoUri
                coroutineScope.launch {
                    val res = syncManager.updateUserProfile(newPhone, newName, finalDesignation, newPhotoUri, oldPhone = previousPhone)
                    if (res.isFailure) {
                        val err = res.exceptionOrNull()
                        if (NetworkErrorHelper.isNetworkOrUnavailableError(err)) {
                            NetworkErrorHelper.showNetworkErrorToast(context)
                        } else {
                            Toast.makeText(context, "Profile save error: ${err?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showEditAdminProfileDialog = false
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showEditAdminProfileDialog = false }
        )
    }

    // Dialog 11b-2: User Management & Admin Control Center (Admin Only)
    if (showUserManagementDialog && isAdminUser) {
        com.example.ui.components.UserManagementDialog(
            allJobs = allJobs,
            allExperts = experts,
            allCategories = allCategories,
            onRestoreBackup = { restoredJobs, restoredExperts, restoredCategories ->
                viewModel.restoreBackupData(restoredJobs, restoredExperts, restoredCategories)
            },
            onDismiss = { showUserManagementDialog = false }
        )
    }

    // Requirement 9: Logout confirmation dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Confirm Logout", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of Hurifix Operations? You will need to login again to access your account.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        sessionManager.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 11b-3: Order Collision Protection Warning Dialog
    collisionWarningTarget?.let { target ->
        val currentUserId = sessionManager.getUserPhone().replace(Regex("[^0-9]"), "")
        val currentUserName = sessionManager.getUserName().ifBlank { "User" }
        val currentUserDesignation = sessionManager.getUserDesignationTag()

        AlertDialog(
            onDismissRequest = { collisionWarningTarget = null },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
            },
            title = {
                Text(text = "Order Currently Managed", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "This order is currently being managed by ${target.managerName} (${target.managerDesignation}). Are you sure you want to take over or edit this order?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val proceedAction = target.onProceed
                        val job = target.job
                        collisionWarningTarget = null
                        viewModel.takeoverOrder(
                            job = job,
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            currentUserDesignation = currentUserDesignation,
                            onDone = proceedAction
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Proceed & Edit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { collisionWarningTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 11c: Order Long Press Action Menu (Pending and Processing)
    activeLongPressJob?.let { job ->
        OrderLongPressActionDialog(
            job = job,
            canManageOrders = canManageOrders && !isViewOnly,
            onEditDetails = {
                if (canManageOrders && !isViewOnly) {
                    checkCollisionAndExecute(job) {
                        val targetJob = job
                        activeLongPressJob = null
                        editingCustomerJob = targetJob
                    }
                } else {
                    Toast.makeText(context, "Requires 'Manage & Dispatch Orders' permission to edit order details", Toast.LENGTH_SHORT).show()
                }
            },
            onUnassignExpert = {
                if (canManageOrders && !isViewOnly) {
                    checkCollisionAndExecute(job) {
                        activeLongPressJob = null
                        viewModel.unassignExpert(job)
                    }
                } else {
                    Toast.makeText(context, "Requires 'Manage & Dispatch Orders' permission to unassign experts", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { activeLongPressJob = null }
        )
    }

    // Dialog 11d: Edit Customer Order Details Dialog
    editingCustomerJob?.let { job ->
        val catNames = remember(allCategories) { allCategories.map { it.name } }
        EditCustomerOrderDialog(
            job = job,
            availableCategories = catNames,
            isAdmin = isAdminUser,
            onSave = { updatedJob ->
                viewModel.updateJob(updatedJob)
                editingCustomerJob = null
            },
            onDismiss = { editingCustomerJob = null }
        )
    }

    // Dialog 12: Universal Confirmation Popup on ANY Delete
    deleteTarget?.let { target ->
        val (title, message, confirmBtnText, onConfirm) = when (target) {
            is DeleteTarget.Job -> listOf(
                "Move Order #${target.job.id} to Recycle Bin?",
                "Order for ${target.job.customerName} will be moved to the Recycle Bin and stored safely for 30 days. You can restore it anytime.",
                "Move to Recycle Bin",
                { viewModel.deleteJob(target.job) }
            )
            is DeleteTarget.Expert -> listOf(
                "Move Expert to Recycle Bin?",
                "Expert '${target.expert.name}' (${target.expert.category}) will be moved to the Recycle Bin and stored safely for 30 days.",
                "Move to Recycle Bin",
                { viewModel.deleteExpert(target.expert) }
            )
            is DeleteTarget.Category -> listOf(
                "Delete Category '${target.category.name}'?",
                "Are you sure you want to delete category '${target.category.name}'? Existing experts in this category will not be deleted.",
                "Delete Category",
                { viewModel.deleteCategory(target.category) }
            )
        }
        UniversalDeleteConfirmationDialog(
            title = title as String,
            message = message as String,
            confirmButtonText = confirmBtnText as String,
            onConfirmDelete = {
                (onConfirm as () -> Unit).invoke()
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    // Dialog 13: Add New Category Dialog
    if (showAddNewCategoryDialog) {
        AddNewCategoryDialog(
            onAddCategory = { viewModel.addNewCategory(it) },
            onDismiss = { showAddNewCategoryDialog = false }
        )
    }

    // Dialog 14: Expert Work History Dialog (As explicitly requested)
    expertForWorkHistory?.let { expert ->
        com.example.ui.components.ExpertWorkHistoryDialog(
            expert = expert,
            allJobs = allJobs,
            onDismiss = { expertForWorkHistory = null }
        )
    }

    // Dialog 15: Recycle Bin Dialog (As explicitly requested)
    if (showRecycleBinDialog) {
        com.example.ui.components.RecycleBinDialog(
            deletedJobs = deletedJobs,
            deletedExperts = deletedExperts,
            onRestoreJob = { viewModel.restoreJobFromRecycleBin(it) },
            onDeleteJobPermanently = { viewModel.deleteJobPermanently(it) },
            onRestoreExpert = { viewModel.restoreExpertFromRecycleBin(it) },
            onDeleteExpertPermanently = { viewModel.deleteExpertPermanently(it) },
            onEmptyRecycleBin = { viewModel.emptyRecycleBin() },
            onDismiss = { showRecycleBinDialog = false }
        )
    }
}

/**
 * Customer Section: Has 2 Sub-Tabs:
 * 1. Dispatch Order (Fill new customer order)
 * 2. Orders (Has 4 status tabs: Pending, Processing, Completed, Cancelled)
 */
@Composable
private fun CustomerOrdersSection(
    viewModel: DispatchViewModel,
    allJobs: List<CustomerJobEntity>,
    currentSubTab: CustomerSubTab,
    currentOrderStatusTab: OrderStatusTab,
    canManageOrders: Boolean = true,
    canDeleteOrders: Boolean = true,
    isViewOnly: Boolean = false,
    onOpenWhatsAppParser: () -> Unit,
    onOrderSaved: (CustomerJobEntity) -> Unit,
    onOpenNearestExperts: (CustomerJobEntity) -> Unit,
    onCompleteOrCancelAction: (CustomerJobEntity, Boolean) -> Unit,
    onShowCompletedDetail: (CustomerJobEntity) -> Unit,
    onDeleteJob: (CustomerJobEntity) -> Unit,
    onLongPressOrder: (CustomerJobEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val subTabPagerState = rememberPagerState(
        initialPage = if (currentSubTab == CustomerSubTab.DISPATCH_ORDER) 0 else 1,
        pageCount = { 2 }
    )

    LaunchedEffect(subTabPagerState.currentPage) {
        val target = if (subTabPagerState.currentPage == 0) CustomerSubTab.DISPATCH_ORDER else CustomerSubTab.ORDERS
        if (target != currentSubTab) {
            viewModel.selectCustomerSubTab(target)
        }
    }

    LaunchedEffect(currentSubTab) {
        val page = if (currentSubTab == CustomerSubTab.DISPATCH_ORDER) 0 else 1
        if (subTabPagerState.currentPage != page) {
            subTabPagerState.animateScrollToPage(page)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub Tabs: Dispatch Order | Orders
        TabRow(
            selectedTabIndex = subTabPagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Tab(
                selected = subTabPagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch { subTabPagerState.animateScrollToPage(0) }
                },
                text = { Text("📝 Dispatch Order", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = subTabPagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch { subTabPagerState.animateScrollToPage(1) }
                },
                text = { Text("📋 Orders (${allJobs.size})", fontWeight = FontWeight.SemiBold) }
            )
        }

        HorizontalPager(
            state = subTabPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                DispatchOrderFormContent(
                    viewModel = viewModel,
                    canManageOrders = canManageOrders,
                    isViewOnly = isViewOnly,
                    onOpenWhatsAppParser = onOpenWhatsAppParser,
                    onOrderSaved = onOrderSaved
                )
            } else {
                OrdersListContent(
                    viewModel = viewModel,
                    allJobs = allJobs,
                    currentStatusTab = currentOrderStatusTab,
                    canManageOrders = canManageOrders && !isViewOnly,
                    canDeleteOrders = canDeleteOrders,
                    isViewOnly = isViewOnly,
                    onSelectStatusTab = { viewModel.selectOrderStatusTab(it) },
                    onOpenNearestExperts = onOpenNearestExperts,
                    onCompleteOrCancelAction = onCompleteOrCancelAction,
                    onShowCompletedDetail = onShowCompletedDetail,
                    onDeleteJob = onDeleteJob,
                    onLongPressOrder = onLongPressOrder
                )
            }
        }
    }
}

/**
 * Sub-Tab 1: Dispatch Order Form
 * Features: WhatsApp Lead Paste button, Free-form Service Type text box with matching autocomplete chips,
 * 10-Digit Mobile strict validation, Optional Address, Google Maps Link Coordinate auto-parsing,
 * Crisp borders, and Keyboard shift (imePadding).
 */
@Composable
private fun DispatchOrderFormContent(
    viewModel: DispatchViewModel,
    canManageOrders: Boolean = true,
    isViewOnly: Boolean = false,
    onOpenWhatsAppParser: () -> Unit,
    onOrderSaved: (CustomerJobEntity) -> Unit
) {
    val context = LocalContext.current
    val form by viewModel.customerForm.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val cleanPhone = remember(form.phone) { form.phone.filter { it.isDigit() }.take(10) }
    val isPhoneValid = cleanPhone.length == 10

    // Auto-complete / matching category chips as requested
    val matchingCategories = remember(form.serviceType, allCategories) {
        if (form.serviceType.isNotBlank()) {
            val set = linkedSetOf("Electrician", "Plumber")
            set.addAll(allCategories.map { it.name })
            set.filter {
                it.contains(form.serviceType.trim(), ignoreCase = true) &&
                !it.equals(form.serviceType.trim(), ignoreCase = true)
            }.take(5)
        } else emptyList()
    }

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showGpsDisabledDialog by remember { mutableStateOf(false) }
    var showConfirmSaveOrderDialog by remember { mutableStateOf(false) }
    var selectedCustomerMedia by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var driveUploadBusy by remember { mutableStateOf(false) }
    var driveSignInAction by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val uploadScope = rememberCoroutineScope()
    val customerMediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { selectedCustomerMedia = it }
    val driveSignInClient = remember {
        GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestScopes(Scope("https://www.googleapis.com/auth/drive.file")).build())
    }
    val driveAuthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account.email != "hurifixservices@gmail.com") {
                driveUploadBusy = false
                Toast.makeText(context, "Please select hurifixservices@gmail.com for Drive uploads.", Toast.LENGTH_LONG).show()
            } else {
                uploadScope.launch {
                    try {
                        val token = withContext(Dispatchers.IO) {
                            GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/drive.file")
                        }
                        driveSignInAction?.invoke(token)
                    } catch (e: Exception) {
                        driveUploadBusy = false
                        Toast.makeText(context, "Drive authorization failed: ${e.localizedMessage ?: "Try again"}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (_: Exception) {
            driveUploadBusy = false
            Toast.makeText(context, "Google Drive sign-in cancelled or failed.", Toast.LENGTH_LONG).show()
        }
    }

    fun saveCustomerOrderWithMedia(mediaUrls: List<String> = emptyList()) {
        val sessionManager = SessionManager(context)
        val cId = sessionManager.getUserPhone().replace(Regex("[^0-9]"), "")
        val cName = sessionManager.getUserName().ifBlank { "User" }
        val cDesig = sessionManager.getUserDesignationTag()
        viewModel.saveCustomerOrder(status = JobStatus.PENDING, createdById = cId, createdByName = cName, createdByDesignation = cDesig) { savedJob ->
            if (mediaUrls.isNotEmpty()) viewModel.updateJob(savedJob.copy(customerMediaUrls = mediaUrls.joinToString(",")))
            onOrderSaved(savedJob)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (!LocationHelper.isLocationEnabled(context)) {
                showGpsDisabledDialog = true
            } else {
                viewModel.fetchCurrentGps(context)
            }
        } else {
            showPermissionDeniedDialog = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚡ New Customer Dispatch Order",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fill details or paste WhatsApp lead below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        )

                        OutlinedButton(
                            onClick = onOpenWhatsAppParser,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("📋 Paste", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider()

                    // Customer Name
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input")
                    )

                    // Customer Phone - Strict 10 Digits Accept as requested
                    OutlinedTextField(
                        value = form.phone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(10)
                            viewModel.updatePhone(digits)
                        },
                        label = { Text("Customer Mobile Number (10 Digits) *") },
                        placeholder = { Text("10 Digit Mobile") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = form.phone.isNotBlank() && !isPhoneValid,
                        supportingText = {
                            if (form.phone.isNotBlank() && !isPhoneValid) {
                                Text("Must be exactly 10 digits (${cleanPhone.length}/10)", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Required 10-digit mobile (${cleanPhone.length}/10)")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input")
                    )

                    // Service Type: Free-form Text Box with auto-suggestion chips
                    OutlinedTextField(
                        value = form.serviceType,
                        onValueChange = { viewModel.updateServiceType(it) },
                        label = { Text("Service Type / Work Category *") },
                        placeholder = { Text("e.g. Electrician, Plumber, AC Repair...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_service_input")
                    )

                    // Matching Category Chips shown directly below as user types
                    if (matchingCategories.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            matchingCategories.forEach { catName ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { viewModel.updateServiceType(catName) }
                                ) {
                                    Text(
                                        text = "💡 $catName",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Problem / Issue Description
                    OutlinedTextField(
                        value = form.issueDescription,
                        onValueChange = { viewModel.updateIssue(it) },
                        label = { Text("Problem Description (Optional)") },
                        placeholder = { Text("e.g. Main switchboard tripping or tap leaking") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Address: Made OPTIONAL as explicitly requested
                    OutlinedTextField(
                        value = form.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = { Text("Customer Address (Optional)") },
                        placeholder = { Text("e.g. Flat 304, Green Heights (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_address_input")
                    )

                    // Coordinate Box (Compulsory & Extended)
                    val isMapsLink = LocationHelper.isGoogleMapsUrl(form.rawLocationInput)
                    val hasExtractedCoords = form.hasValidLocation && form.latitude != 0.0 && form.longitude != 0.0
                    OutlinedTextField(
                        value = form.rawLocationInput,
                        onValueChange = { viewModel.updateLocationInput(it) },
                        label = { Text("Coordinates / Maps Link *") },
                        placeholder = { Text("Lat, Lng or Google Maps link") },
                        isError = form.rawLocationInput.isNotBlank() && !form.hasValidLocation && !isMapsLink,
                        supportingText = {
                            if (hasExtractedCoords) {
                                Text("📍 Extracted: ${form.latitude}, ${form.longitude}", color = MaterialTheme.colorScheme.primary)
                            } else if (form.rawLocationInput.isNotBlank() && !form.hasValidLocation && !isMapsLink) {
                                Text("Enter valid Lat, Lng or Google Maps link (Compulsory)", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Coordinate box is compulsory *")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_gps_input")
                    )

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Customer Photos / Videos (Google Drive)", fontWeight = FontWeight.SemiBold)
                            Text(if (selectedCustomerMedia.isEmpty()) "Optional: attach photos or videos sent by the customer." else "${selectedCustomerMedia.size} media file(s) selected.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { customerMediaPicker.launch(arrayOf("image/*", "video/*")) }, modifier = Modifier.weight(1f)) { Text("Choose Photos/Videos") }
                                if (selectedCustomerMedia.isNotEmpty()) OutlinedButton(onClick = { selectedCustomerMedia = emptyList() }) { Text("Clear") }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Save Button - strictly requires 10 digits as requested, protected with HurifixConfirmDialog
                    Button(
                        onClick = {
                            showConfirmSaveOrderDialog = true
                        },
                        enabled = canManageOrders && !isViewOnly && !driveUploadBusy && form.name.isNotBlank() && isPhoneValid && form.serviceType.isNotBlank() && form.hasValidLocation,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_order_button")
                    ) {
                        Text(
                            text = if (driveUploadBusy) "Uploading customer media..."
                            else if (isViewOnly) "🔒 View Only Mode (Read Only)"
                            else if (!canManageOrders) "🔒 Dispatching Disabled by Admin"
                            else "Save Customer Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showConfirmSaveOrderDialog) {
        val cleanPhone = form.phone.filter { it.isDigit() }.take(10)
        com.example.ui.components.HurifixConfirmDialog(
            title = "Save & Dispatch Order?",
            message = "Are you sure you want to save and dispatch the order for customer ${form.name} ($cleanPhone) for service '${form.serviceType}'?",
            confirmText = "Yes, Save Order",
            icon = Icons.Default.CheckCircle,
            onConfirm = {
                showConfirmSaveOrderDialog = false
                if (selectedCustomerMedia.isEmpty()) {
                    saveCustomerOrderWithMedia()
                } else {
                    driveUploadBusy = true
                    driveSignInAction = { token ->
                        uploadScope.launch {
                            try {
                                val uploaded = com.example.util.GoogleDriveMediaHelper.uploadUris(
                                    context = context, accessToken = token, uris = selectedCustomerMedia,
                                    orderLabel = "Customer_${form.phone.filter { it.isDigit() }}_${System.currentTimeMillis()}", category = "Customer"
                                )
                                driveUploadBusy = false
                                selectedCustomerMedia = emptyList()
                                saveCustomerOrderWithMedia(uploaded.map { it.url })
                            } catch (e: Exception) {
                                driveUploadBusy = false
                                Toast.makeText(context, "Customer media upload failed; order was not saved: ${e.localizedMessage ?: "Try again"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    val existingDriveAccount = GoogleSignIn.getLastSignedInAccount(context)
                    if (existingDriveAccount != null && existingDriveAccount.email == "hurifixservices@gmail.com" && GoogleSignIn.hasPermissions(existingDriveAccount, Scope("https://www.googleapis.com/auth/drive.file"))) {
                        uploadScope.launch {
                            try {
                                val token = withContext(Dispatchers.IO) { GoogleAuthUtil.getToken(context, existingDriveAccount.account!!, "oauth2:https://www.googleapis.com/auth/drive.file") }
                                driveSignInAction?.invoke(token)
                            } catch (e: Exception) {
                                driveUploadBusy = false
                                Toast.makeText(context, "Drive authorization failed: ${e.localizedMessage ?: "Try again"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        driveAuthLauncher.launch(driveSignInClient.signInIntent)
                    }
                }
            },
            onDismiss = { showConfirmSaveOrderDialog = false }
        )
    }

    if (showPermissionDeniedDialog) {
        LocationPermissionDeniedDialog(
            onDismiss = { showPermissionDeniedDialog = false },
            onOpenSettings = {
                showPermissionDeniedDialog = false
                LocationHelper.openAppSettings(context)
            }
        )
    }

    if (showGpsDisabledDialog) {
        GpsDisabledDialog(
            onDismiss = { showGpsDisabledDialog = false },
            onOpenLocationSettings = {
                showGpsDisabledDialog = false
                LocationHelper.openLocationSettings(context)
            }
        )
    }
}

/**
 * Sub-Tab 2: Orders List with Horizontal Finger Swipe between Status Tabs!
 */
@Composable
private fun OrdersListContent(
    viewModel: DispatchViewModel,
    allJobs: List<CustomerJobEntity>,
    currentStatusTab: OrderStatusTab,
    canManageOrders: Boolean = true,
    canDeleteOrders: Boolean = true,
    isViewOnly: Boolean = false,
    onSelectStatusTab: (OrderStatusTab) -> Unit,
    onOpenNearestExperts: (CustomerJobEntity) -> Unit,
    onCompleteOrCancelAction: (CustomerJobEntity, Boolean) -> Unit,
    onShowCompletedDetail: (CustomerJobEntity) -> Unit,
    onDeleteJob: (CustomerJobEntity) -> Unit,
    onLongPressOrder: (CustomerJobEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf("ALL") }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    val statusTabs = remember { OrderStatusTab.entries }
    val pagerState = rememberPagerState(
        initialPage = currentStatusTab.ordinal,
        pageCount = { statusTabs.size }
    )

    // Synchronize pager swipe with selected tab
    LaunchedEffect(pagerState.currentPage) {
        val targetTab = statusTabs[pagerState.currentPage]
        if (targetTab != currentStatusTab) {
            onSelectStatusTab(targetTab)
        }
    }

    // Synchronize tab click with pager scroll
    LaunchedEffect(currentStatusTab) {
        if (pagerState.currentPage != currentStatusTab.ordinal) {
            pagerState.animateScrollToPage(currentStatusTab.ordinal)
        }
    }

    fun matchesDate(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        return when (selectedDateFilter) {
            "TODAY" -> {
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                timestamp >= cal.timeInMillis
            }
            "LAST_7_DAYS" -> {
                val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
                timestamp >= sevenDaysAgo
            }
            "THIS_MONTH" -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                timestamp >= cal.timeInMillis
            }
            "CUSTOM" -> {
                if (customStartDate != null && customEndDate != null) {
                    timestamp in customStartDate!!..customEndDate!!
                } else true
            }
            else -> true
        }
    }

    if (showCustomDatePicker) {
        CustomDateRangePickerDialog(
            currentStartDate = customStartDate,
            currentEndDate = customEndDate,
            onDismiss = { showCustomDatePicker = false },
            onApplyRange = { start, end ->
                customStartDate = start
                customEndDate = end
                selectedDateFilter = "CUSTOM"
                showCustomDatePicker = false
            }
        )
    }

    fun getTabColors(tab: OrderStatusTab): Triple<Color, Color, Color> {
        return when (tab) {
            OrderStatusTab.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFF59E0B))
            OrderStatusTab.PROCESSING -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), Color(0xFF3B82F6))
            OrderStatusTab.COMPLETED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFF22C55E))
            OrderStatusTab.CANCELLED -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Color(0xFFEF4444))
        }
    }

    fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = start.red + (stop.red - start.red) * f,
            green = start.green + (stop.green - start.green) * f,
            blue = start.blue + (stop.blue - start.blue) * f,
            alpha = start.alpha + (stop.alpha - start.alpha) * f
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Stationary Status Tabs Line with smooth sliding indicator box!
        // The text line stays firmly in place (does NOT slide horizontally).
        // Only the transparent box slides smoothly.
        // All 4 tabs fit across the display width so "Cancelled" never goes out of display!
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
                .height(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(3.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = statusTabs.size
            val tabWidth = totalWidth / tabCount

            val continuousPage = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, (tabCount - 1).toFloat())
            val indicatorOffsetX = tabWidth * continuousPage

            val baseIndex = continuousPage.toInt().coerceIn(0, tabCount - 1)
            val nextIndex = (baseIndex + 1).coerceAtMost(tabCount - 1)
            val fraction = (continuousPage - baseIndex).coerceIn(0f, 1f)

            val baseColors = getTabColors(statusTabs[baseIndex])
            val nextColors = getTabColors(statusTabs[nextIndex])
            val currentBoxBg = lerpColor(baseColors.first, nextColors.first, fraction)
            val currentBoxBorder = lerpColor(baseColors.third, nextColors.third, fraction)

            // Transparent Box (Slides smoothly under finger or on tab tap!)
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffsetX)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .background(color = currentBoxBg, shape = RoundedCornerShape(8.dp))
                    .border(width = 1.2.dp, color = currentBoxBorder, shape = RoundedCornerShape(8.dp))
            )

            // Stationary Text Row (All 4 tabs visible simultaneously, never slides off display)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                statusTabs.forEachIndexed { index, tab ->
                    val count = allJobs.count { it.status == tab.statusName }
                    val isSelected = pagerState.currentPage == index
                    val tabColors = getTabColors(tab)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSelectStatusTab(tab)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = if (totalWidth < 360.dp) 10.5.sp else 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) tabColors.second else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "($count)",
                                fontSize = if (totalWidth < 360.dp) 9.5.sp else 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                color = if (isSelected) tabColors.second else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Swipeable HorizontalPager allowing finger-slide between tabs
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageStatus = statusTabs[pageIndex]
            val jobsForThisPage = remember(allJobs, pageStatus, searchQuery, selectedDateFilter, customStartDate, customEndDate) {
                allJobs.filter { job ->
                    job.status == pageStatus.statusName &&
                            (searchQuery.isBlank() ||
                                    job.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                                    job.customerPhone.contains(searchQuery.trim()) ||
                                    job.serviceType.contains(searchQuery.trim(), ignoreCase = true) ||
                                    (job.assignedExpertName?.contains(searchQuery.trim(), ignoreCase = true) == true)) &&
                            matchesDate(job.createdAt)
                }
            }

            val listState = rememberLazyListState()
            val showScrollToTop by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Search Bar (Scrolls up with list!)
                    item(key = "search_bar") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, mobile, service...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                // 3. Date Filter Chips Row (Scrolls up with list!)
                item(key = "date_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedDateFilter == "ALL",
                            onClick = { selectedDateFilter = "ALL" },
                            label = { Text("All Time") }
                        )
                        FilterChip(
                            selected = selectedDateFilter == "TODAY",
                            onClick = { selectedDateFilter = "TODAY" },
                            label = { Text("Today") }
                        )
                        FilterChip(
                            selected = selectedDateFilter == "LAST_7_DAYS",
                            onClick = { selectedDateFilter = "LAST_7_DAYS" },
                            label = { Text("Last 7 Days") }
                        )
                        FilterChip(
                            selected = selectedDateFilter == "THIS_MONTH",
                            onClick = { selectedDateFilter = "THIS_MONTH" },
                            label = { Text("This Month") }
                        )
                        FilterChip(
                            selected = selectedDateFilter == "CUSTOM",
                            onClick = { showCustomDatePicker = true },
                            label = {
                                val labelText = if (selectedDateFilter == "CUSTOM" && customStartDate != null && customEndDate != null) {
                                    val df = SimpleDateFormat("dd/MM", Locale.getDefault())
                                    "📅 ${df.format(Date(customStartDate!!))} - ${df.format(Date(customEndDate!!))}"
                                } else "📅 Custom Range"
                                Text(labelText)
                            }
                        )
                    }
                }

                if (jobsForThisPage.isEmpty()) {
                    item(key = "empty_jobs") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (searchQuery.isNotBlank() || selectedDateFilter != "ALL") {
                                        "🔍 No matching records found."
                                    } else {
                                        when (pageStatus) {
                                            OrderStatusTab.PENDING -> "⏳ No pending orders found."
                                            OrderStatusTab.PROCESSING -> "⚙️ No orders currently in processing."
                                            OrderStatusTab.COMPLETED -> "✅ No completed orders found."
                                            OrderStatusTab.CANCELLED -> "❌ No cancelled orders found."
                                        }
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(jobsForThisPage, key = { it.id }) { job ->
                        OrderItemCard(
                            job = job,
                            currentStatus = pageStatus,
                            canManageOrders = canManageOrders,
                            canDeleteOrders = canDeleteOrders,
                            isViewOnly = isViewOnly,
                            onOpenNearestExperts = { onOpenNearestExperts(job) },
                            onCompleteAction = { onCompleteOrCancelAction(job, true) },
                            onCancelAction = { onCompleteOrCancelAction(job, false) },
                            onShowCompletedDetail = { onShowCompletedDetail(job) },
                            onDeleteJob = { onDeleteJob(job) },
                            onLongPress = {
                                if (pageStatus == OrderStatusTab.PENDING || pageStatus == OrderStatusTab.PROCESSING) {
                                    onLongPressOrder(job)
                                }
                            },
                            onUpdateExpertNotified = { jobId, sent -> viewModel.updateExpertNotified(jobId, sent) },
                            onUpdateCustomerNotified = { jobId, sent -> viewModel.updateCustomerNotifiedOnAssign(jobId, sent) },
                            onUpdateCompletionNotified = { jobId, sent -> viewModel.updateCustomerNotifiedOnCompletion(jobId, sent) }
                        )
                    }
                }
            }

            ScrollToTopButton(
                    visible = showScrollToTop,
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrderItemCard(
    job: CustomerJobEntity,
    currentStatus: OrderStatusTab,
    canManageOrders: Boolean = true,
    canDeleteOrders: Boolean = true,
    isViewOnly: Boolean = false,
    onOpenNearestExperts: () -> Unit,
    onCompleteAction: () -> Unit,
    onCancelAction: () -> Unit,
    onShowCompletedDetail: () -> Unit,
    onDeleteJob: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    onUpdateExpertNotified: (Long, Boolean) -> Unit,
    onUpdateCustomerNotified: (Long, Boolean) -> Unit,
    onUpdateCompletionNotified: (Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmTitle by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmButtonText by remember { mutableStateOf("Confirm") }
    var confirmIsDestructive by remember { mutableStateOf(false) }
    var confirmIcon by remember { mutableStateOf(Icons.Default.Warning) }
    var confirmAction by remember { mutableStateOf<() -> Unit>({}) }

    fun requestConfirm(
        title: String,
        message: String,
        buttonText: String = "Confirm",
        isDestructive: Boolean = false,
        icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Warning,
        action: () -> Unit
    ) {
        confirmTitle = title
        confirmMessage = message
        confirmButtonText = buttonText
        confirmIsDestructive = isDestructive
        confirmIcon = icon
        confirmAction = action
        showConfirmDialog = true
    }

    if (showConfirmDialog) {
        com.example.ui.components.HurifixConfirmDialog(
            title = confirmTitle,
            message = confirmMessage,
            confirmText = confirmButtonText,
            isDestructive = confirmIsDestructive,
            icon = confirmIcon,
            onConfirm = {
                showConfirmDialog = false
                confirmAction()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    val statusColor = when (job.status) {
        JobStatus.PENDING.name -> Color(0xFFF59E0B)
        JobStatus.PROCESSING.name -> Color(0xFF3B82F6)
        JobStatus.COMPLETED.name -> Color(0xFF16A34A)
        JobStatus.CANCELLED.name -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (currentStatus == OrderStatusTab.COMPLETED) {
                        onShowCompletedDetail()
                    }
                },
                onLongClick = {
                    onLongPress?.invoke()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Row: Order Number Badge (e.g. Order #001) & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Order #${job.orderNumberFormatted}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = job.status,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Customer Name & Phone
            Column {
                Text(
                    text = job.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "📞 ${job.customerPhone} • ${dateFormat.format(Date(job.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Details
            Text(
                text = "🛠 Service: ${job.serviceType}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (job.issueDescription.isNotBlank()) {
                Text(
                    text = "📝 Problem: ${job.issueDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "📍 Address: ${job.address.ifBlank { "Address not specified" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Assigned Expert Details (if in Processing or Completed)
            if (job.assignedExpertName != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Assigned Expert:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = job.assignedExpertName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                job.assignedExpertPhone?.let {
                                    Text(text = "📞 $it", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            job.assignedExpertPhone?.let { phone ->
                                IconButton(onClick = {
                                    requestConfirm(
                                        title = "Call Assigned Expert?",
                                        message = "Call expert ${job.assignedExpertName ?: "Expert"} at $phone?",
                                        buttonText = "Call Now",
                                        icon = Icons.Default.Phone,
                                        action = { WhatsAppHelper.openDialer(context, phone) }
                                    )
                                }) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // WhatsApp Dispatch Message Status for Expert (As requested)
                        if (currentStatus == OrderStatusTab.PROCESSING) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (job.isExpertNotified) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (job.isExpertNotified) "✅ Expert WhatsApp Sent" else "⚠️ WhatsApp message not sent yet",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (job.isExpertNotified) Color(0xFF15803D) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        job.assignedExpertPhone?.let { phone ->
                                            requestConfirm(
                                                title = if (job.isExpertNotified) "Send WhatsApp Again?" else "Send WhatsApp to Expert?",
                                                message = "Send job details and customer location to ${job.assignedExpertName ?: "Expert"} on WhatsApp?",
                                                buttonText = "Send via WhatsApp",
                                                icon = Icons.AutoMirrored.Filled.Send,
                                                action = {
                                                    val expertObj = ExpertEntity(
                                                        id = job.assignedExpertId ?: 0L,
                                                        name = job.assignedExpertName ?: "Expert",
                                                        phone = phone,
                                                        category = job.serviceType,
                                                        address = "",
                                                        latitude = 0.0,
                                                        longitude = 0.0
                                                    )
                                                    WhatsAppHelper.sendWhatsAppMessageToExpert(context, expertObj, job)
                                                    onUpdateExpertNotified(job.id, true)
                                                }
                                            )
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (job.isExpertNotified) "Send Again" else "Send to Expert",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Customer Assignment Notice Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (job.isCustomerNotifiedOnAssign) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (job.isCustomerNotifiedOnAssign) "✅ Customer WhatsApp Sent" else "⚠️ Customer WhatsApp pending",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (job.isCustomerNotifiedOnAssign) Color(0xFF15803D) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        requestConfirm(
                                            title = if (job.isCustomerNotifiedOnAssign) "Send WhatsApp Again?" else "Send WhatsApp to Customer?",
                                            message = "Send technician assignment notification to customer ${job.customerName} on WhatsApp?",
                                            buttonText = "Send via WhatsApp",
                                            icon = Icons.AutoMirrored.Filled.Send,
                                            action = {
                                                val estTime = WhatsAppHelper.calculateEstimatedArrivalTimeWithBuffer(job.distanceKmAtDispatch ?: 3.0)
                                                val msg = WhatsAppHelper.createCustomerAssignmentNotificationMessage(
                                                    customerName = job.customerName,
                                                    expertName = job.assignedExpertName ?: "Expert",
                                                    expertPhone = job.assignedExpertPhone ?: "",
                                                    serviceType = job.serviceType,
                                                    estimatedTimeText = estTime
                                                )
                                                WhatsAppHelper.sendWhatsAppDirectMessage(context, job.customerPhone, msg)
                                                onUpdateCustomerNotified(job.id, true)
                                            }
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (job.isCustomerNotifiedOnAssign) "Send Again" else "Send to Customer",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 10-Day Warranty Badge & Duration (for Completed Orders)
            if (job.status == JobStatus.COMPLETED.name) {
                val completedTime = job.completedAt ?: job.createdAt
                val daysPassed = ((System.currentTimeMillis() - completedTime) / (1000 * 60 * 60 * 24)).toInt()
                val isWarrantyValid = daysPassed <= 10
                val daysRemaining = maxOf(0, 10 - daysPassed)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF16A34A).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⏱ ${calculateTaskDuration(job.createdAt, job.completedAt)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = if (isWarrantyValid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isWarrantyValid) {
                                "🛡️ 10-Day Warranty: $daysRemaining Days Left"
                            } else {
                                "⚠️ Warranty Expired ($daysPassed days ago)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isWarrantyValid) Color(0xFF15803D) else Color(0xFFB91C1C),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Completion WhatsApp status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (job.isCustomerNotifiedOnCompletion) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (job.isCustomerNotifiedOnCompletion) "✅ Completion WhatsApp Sent" else "⚠️ Completion WhatsApp pending",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (job.isCustomerNotifiedOnCompletion) Color(0xFF15803D) else Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            requestConfirm(
                                title = if (job.isCustomerNotifiedOnCompletion) "Send Completion WhatsApp Again?" else "Send Completion WhatsApp?",
                                message = "Send 10-day warranty and feedback link to customer ${job.customerName} on WhatsApp?",
                                buttonText = "Send via WhatsApp",
                                icon = Icons.AutoMirrored.Filled.Send,
                                action = {
                                    val msg = WhatsAppHelper.createCompletionCustomerMessage(job.customerName)
                                    WhatsAppHelper.sendWhatsAppDirectMessage(context, job.customerPhone, msg)
                                    onUpdateCompletionNotified(job.id, true)
                                }
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (job.isCustomerNotifiedOnCompletion) "Send Again" else "Send to Customer",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Rating / Review display (if Completed or Cancelled)
            if (job.ratingGiven != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Rating Given:", style = MaterialTheme.typography.labelSmall)
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i <= job.ratingGiven) Color(0xFFF59E0B) else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "(${job.ratingGiven.toInt()}/5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                }
                job.reviewFeedback?.let {
                    Text(
                        text = "💬 Feedback: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💼 $managerText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons depending on Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (currentStatus) {
                    OrderStatusTab.PENDING -> {
                        Button(
                            onClick = onOpenNearestExperts,
                            enabled = canManageOrders && !isViewOnly,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!canManageOrders || isViewOnly) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Find Experts (Locked)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Text("🔍 Find Nearest Experts", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                requestConfirm(
                                    title = "Call Customer?",
                                    message = "Call customer ${job.customerName} at ${job.customerPhone}?",
                                    buttonText = "Call Now",
                                    icon = Icons.Default.Phone,
                                    action = { WhatsAppHelper.openDialer(context, job.customerPhone) }
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        if (canDeleteOrders && !isViewOnly) {
                            IconButton(onClick = onDeleteJob) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }

                    OrderStatusTab.PROCESSING -> {
                        Button(
                            onClick = {
                                requestConfirm(
                                    title = "Mark Order Complete?",
                                    message = "Are you sure you want to mark order #${job.orderNumberFormatted} for customer ${job.customerName} as COMPLETED?",
                                    buttonText = "Yes, Complete",
                                    icon = Icons.Default.CheckCircle,
                                    action = onCompleteAction
                                )
                            },
                            enabled = canManageOrders && !isViewOnly,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!canManageOrders || isViewOnly) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Complete (Locked)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Text("Mark Complete", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                requestConfirm(
                                    title = "Cancel Order?",
                                    message = "Are you sure you want to cancel order #${job.orderNumberFormatted} for ${job.customerName}?",
                                    buttonText = "Yes, Cancel Order",
                                    isDestructive = true,
                                    icon = Icons.Default.Warning,
                                    action = onCancelAction
                                )
                            },
                            enabled = canManageOrders && !isViewOnly,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!canManageOrders || isViewOnly) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cancel (Locked)", fontSize = 12.sp)
                            } else {
                                Text("Cancel Order")
                            }
                        }
                    }

                    OrderStatusTab.COMPLETED -> {
                        Button(
                            onClick = onShowCompletedDetail,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Full History", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                requestConfirm(
                                    title = "Call Customer?",
                                    message = "Call customer ${job.customerName} at ${job.customerPhone}?",
                                    buttonText = "Call Now",
                                    icon = Icons.Default.Phone,
                                    action = { WhatsAppHelper.openDialer(context, job.customerPhone) }
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call")
                        }

                        if (canDeleteOrders && !isViewOnly) {
                            IconButton(onClick = onDeleteJob) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }

                    OrderStatusTab.CANCELLED -> {
                        OutlinedButton(
                            onClick = {
                                requestConfirm(
                                    title = "Call Customer?",
                                    message = "Call customer ${job.customerName} at ${job.customerPhone}?",
                                    buttonText = "Call Now",
                                    icon = Icons.Default.Phone,
                                    action = { WhatsAppHelper.openDialer(context, job.customerPhone) }
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Call Customer")
                        }

                        if (canDeleteOrders && !isViewOnly) {
                            IconButton(onClick = onDeleteJob) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Experts Tab: Displays partner experts with category tabs!
 * User requirement: Default 2 tabs (Electrician, Plumber), creates new tab when new category added,
 * long press to delete custom category (with delete confirmation popup!).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpertsTabContent(
    experts: List<ExpertEntity>,
    categories: List<ExpertCategoryEntity>,
    canAddExperts: Boolean = true,
    isAdmin: Boolean = false,
    onAddNewCategory: (String) -> Unit,
    onDeleteCategory: (ExpertCategoryEntity) -> Unit,
    onEditExpert: (ExpertEntity) -> Unit,
    onDeleteExpert: (ExpertEntity) -> Unit,
    onToggleAvailability: (ExpertEntity, Boolean) -> Unit,
    onViewWorkHistory: (ExpertEntity) -> Unit,
    onSendWelcome: (ExpertEntity) -> Unit,
    onAddExpert: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Distinct category entities, ensuring Electrician and Plumber always appear first
    val displayCategories = remember(categories) {
        val catMap = linkedMapOf<String, ExpertCategoryEntity>()
        catMap["Electrician"] = ExpertCategoryEntity(name = "Electrician", isDefault = true)
        catMap["Plumber"] = ExpertCategoryEntity(name = "Plumber", isDefault = true)
        categories.forEach { cat ->
            catMap[cat.name] = cat
        }
        catMap.values.toList()
    }

    val categoryTabs = remember(displayCategories) {
        listOf("All") + displayCategories.map { it.name }
    }

    val categoryPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { categoryTabs.size }
    )

    if (showAddCategoryDialog) {
        AddNewCategoryDialog(
            onAddCategory = { newName ->
                onAddNewCategory(newName)
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    // Swipeable HorizontalPager between expert category tabs
    HorizontalPager(
        state = categoryPagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        val pageCategory = categoryTabs[pageIndex]
        val expertsForPage = remember(experts, pageCategory, searchQuery) {
            experts.filter { expert ->
                !expert.isDeleted &&
                (if (pageCategory == "All") true else expert.category.equals(pageCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() ||
                        expert.name.contains(searchQuery, ignoreCase = true) ||
                        expert.phone.contains(searchQuery, ignoreCase = true) ||
                        expert.address.contains(searchQuery, ignoreCase = true))
            }
        }

        val listState = rememberLazyListState()
        val isAtTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset <= 20
            }
        }
        val showScrollToTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Header: Title & Add Category Button (Scrolls up with list!)
                item(key = "experts_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Partner Experts (${experts.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${experts.count { it.isAvailable }} available for tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (canAddExperts) {
                            OutlinedButton(
                                onClick = { showAddCategoryDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Category", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. Category Tabs (Electrician, Plumber, + custom categories) - Click or Swipe! (Scrolls up with list!)
                item(key = "category_tabs") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryTabs.forEachIndexed { index, catName ->
                            val isSelected = categoryPagerState.currentPage == index
                            val count = if (catName == "All") experts.size else experts.count { it.category.equals(catName, ignoreCase = true) }
                            val catEntity = displayCategories.find { it.name.equals(catName, ignoreCase = true) }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = {
                                            coroutineScope.launch { categoryPagerState.animateScrollToPage(index) }
                                        },
                                        onLongClick = {
                                            if (catEntity != null && !catEntity.isDefault && catEntity.name != "Electrician" && catEntity.name != "Plumber") {
                                                onDeleteCategory(catEntity)
                                            } else if (catName != "All") {
                                                Toast.makeText(context, "$catName is a default category and cannot be deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$catName ($count)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (catEntity != null && !catEntity.isDefault && catEntity.name != "Electrician" && catEntity.name != "Plumber") {
                                        Text(
                                            text = "•",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Tip Text (Scrolls up with list!)
                item(key = "experts_tip") {
                    Text(
                        text = "Tip: Slide left/right to change category tabs. Long-press custom tab to delete.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 4. Search Field (Scrolls up with list!)
                item(key = "search_field") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, phone or area...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_experts_input")
                    )
                }

                if (expertsForPage.isEmpty()) {
                    item(key = "empty_experts") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No partner experts found in '$pageCategory'.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(expertsForPage, key = { it.id }) { expert ->
                        com.example.ui.components.ExpertCard(
                            expert = expert,
                            isAdmin = isAdmin,
                            onCall = { WhatsAppHelper.openDialer(context, expert.phone) },
                            onWhatsApp = {
                                WhatsAppHelper.openWhatsAppChatWithoutMessage(context, expert.phone)
                            },
                            onEdit = { onEditExpert(expert) },
                            onDelete = { onDeleteExpert(expert) },
                            onToggleAvailability = { onToggleAvailability(expert, it) },
                            onViewMap = {
                                WhatsAppHelper.openGoogleMaps(
                                    context = context,
                                    latitude = expert.latitude,
                                    longitude = expert.longitude,
                                    label = expert.name
                                )
                            },
                            onViewWorkHistory = { onViewWorkHistory(expert) },
                            onSendWelcome = { onSendWelcome(expert) },
                            onLongPress = { onEditExpert(expert) }
                        )
                    }
                }
            }

                // Action buttons:
                // 1. When at top: "Add Expert" button is shown.
                // 2. When scrolled down: "Add Expert" disappears ("gayab ho jay"), and "Scroll to Top" arrow appears.
                // 3. Clicking "Scroll to Top" scrolls to top, and when top is reached, "Add Expert" reappears!
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    if (canAddExperts) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isAtTop,
                            enter = fadeIn(tween(250)) + scaleIn(tween(250)),
                            exit = fadeOut(tween(200)) + scaleOut(tween(200))
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = onAddExpert,
                                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                text = { Text("Add Expert", fontWeight = FontWeight.Bold) },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            )
                        }
                    }

                    ScrollToTopButton(
                        visible = showScrollToTop,
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    )
                }
            }
        }
    }
