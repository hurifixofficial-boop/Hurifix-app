package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CustomerJobEntity
import com.example.data.model.ExpertCategoryEntity
import com.example.data.model.ExpertEntity
import com.example.data.model.JobStatus
import com.example.data.model.RankedExpert
import com.example.data.repository.DispatchRepository
import com.example.util.GlobalLoadingManager
import com.example.util.LocationHelper
import com.example.util.NetworkErrorHelper
import com.example.util.toUserFriendlyMessage
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern

enum class MainTab(val title: String) {
    CUSTOMER_ORDERS("Customer Orders"),
    EXPERTS("Experts Directory")
}

enum class CustomerSubTab(val title: String) {
    DISPATCH_ORDER("Dispatch Order"),
    ORDERS("Orders")
}

enum class OrderStatusTab(val statusName: String, val label: String) {
    PENDING(JobStatus.PENDING.name, "Pending"),
    PROCESSING(JobStatus.PROCESSING.name, "Processing"),
    COMPLETED(JobStatus.COMPLETED.name, "Completed"),
    CANCELLED(JobStatus.CANCELLED.name, "Cancelled")
}

data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val serviceType: String = "",
    val issueDescription: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hasValidLocation: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val rawLocationInput: String = ""
)

class DispatchViewModel(private val repository: DispatchRepository) : ViewModel() {

    val allExperts: StateFlow<List<ExpertEntity>> = repository.allExperts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<CustomerJobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<ExpertCategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedJobs: StateFlow<List<CustomerJobEntity>> = repository.deletedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedExperts: StateFlow<List<ExpertEntity>> = repository.deletedExperts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customerForm = MutableStateFlow(CustomerFormState())
    val customerForm: StateFlow<CustomerFormState> = _customerForm.asStateFlow()

    private val _currentMainTab = MutableStateFlow(MainTab.CUSTOMER_ORDERS)
    val currentMainTab: StateFlow<MainTab> = _currentMainTab.asStateFlow()

    private val _currentCustomerSubTab = MutableStateFlow(CustomerSubTab.DISPATCH_ORDER)
    val currentCustomerSubTab: StateFlow<CustomerSubTab> = _currentCustomerSubTab.asStateFlow()

    private val _currentOrderStatusTab = MutableStateFlow(OrderStatusTab.PENDING)
    val currentOrderStatusTab: StateFlow<OrderStatusTab> = _currentOrderStatusTab.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Active customer job selected for finding nearest experts
    private val _activeJobForNearestExperts = MutableStateFlow<CustomerJobEntity?>(null)
    val activeJobForNearestExperts: StateFlow<CustomerJobEntity?> = _activeJobForNearestExperts.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.ensureDefaultCategoriesForCurrentUser()
                repository.purgeRecycleBinOlderThan30Days()
                repository.syncNow()
            } catch (_: Exception) {
            }
        }
    }

    fun onUserLoggedIn(phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        repository.setCurrentUser(cleanPhone)
        viewModelScope.launch {
            try {
                repository.ensureDefaultCategoriesForCurrentUser()
                repository.purgeRecycleBinOlderThan30Days()
                repository.syncNow()
            } catch (_: Exception) {
            }
        }
        _customerForm.value = CustomerFormState()
        _activeJobForNearestExperts.value = null
        _currentMainTab.value = MainTab.CUSTOMER_ORDERS
        _currentCustomerSubTab.value = CustomerSubTab.DISPATCH_ORDER
        _currentOrderStatusTab.value = OrderStatusTab.PENDING
    }

    fun onUserLoggedOut() {
        repository.setCurrentUser("")
        _customerForm.value = CustomerFormState()
        _activeJobForNearestExperts.value = null
        _currentMainTab.value = MainTab.CUSTOMER_ORDERS
        _currentCustomerSubTab.value = CustomerSubTab.DISPATCH_ORDER
        _currentOrderStatusTab.value = OrderStatusTab.PENDING
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.syncNow()
            } catch (_: Exception) {}
            kotlinx.coroutines.delay(500)
            _isRefreshing.value = false
            _statusMessage.value = "Data refreshed successfully! 🔄"
        }
    }

    fun selectMainTab(tab: MainTab) {
        _currentMainTab.value = tab
    }

    fun selectCustomerSubTab(subTab: CustomerSubTab) {
        _currentCustomerSubTab.value = subTab
    }

    fun selectOrderStatusTab(statusTab: OrderStatusTab) {
        _currentOrderStatusTab.value = statusTab
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun updateName(name: String) {
        _customerForm.value = _customerForm.value.copy(name = name)
    }

    fun updatePhone(phone: String) {
        _customerForm.value = _customerForm.value.copy(phone = phone)
    }

    fun updateServiceType(service: String) {
        _customerForm.value = _customerForm.value.copy(serviceType = service)
    }

    fun updateIssue(issue: String) {
        _customerForm.value = _customerForm.value.copy(issueDescription = issue)
    }

    fun updateAddress(address: String) {
        _customerForm.value = _customerForm.value.copy(address = address)
    }

    fun updateLocationInput(input: String) {
        val trimmed = input.trim()
        val parsed = LocationHelper.parseCoordinatesFromText(trimmed)

        if (parsed != null &&
            parsed.first.isFinite() && parsed.second.isFinite() &&
            parsed.first in -90.0..90.0 && parsed.second in -180.0..180.0
        ) {
            _customerForm.value = _customerForm.value.copy(
                rawLocationInput = input,
                latitude = parsed.first,
                longitude = parsed.second,
                hasValidLocation = true,
                isFetchingLocation = false
            )
        } else if (LocationHelper.isGoogleMapsUrl(trimmed)) {
            // URL remains in the input, but saving is blocked until coordinates resolve.
            _customerForm.value = _customerForm.value.copy(
                rawLocationInput = input,
                latitude = 0.0,
                longitude = 0.0,
                hasValidLocation = false,
                isFetchingLocation = true
            )

            viewModelScope.launch {
                val resolved = try {
                    LocationHelper.resolveAndExtractCoordinates(trimmed)
                } catch (_: Exception) {
                    null
                }

                // Do not let an older request overwrite a newer input.
                if (_customerForm.value.rawLocationInput != input) {
                    return@launch
                }

                if (resolved != null &&
                    resolved.first.isFinite() && resolved.second.isFinite() &&
                    resolved.first in -90.0..90.0 && resolved.second in -180.0..180.0
                ) {
                    _customerForm.value = _customerForm.value.copy(
                        latitude = resolved.first,
                        longitude = resolved.second,
                        hasValidLocation = true,
                        isFetchingLocation = false
                    )
                    _statusMessage.value = "Location fetched successfully!"
                } else {
                    _customerForm.value = _customerForm.value.copy(
                        hasValidLocation = false,
                        isFetchingLocation = false
                    )
                    _statusMessage.value =
                        "❌ Location fetch failed. Please check the Maps link."
                }
            }
        } else {
            _customerForm.value = _customerForm.value.copy(
                rawLocationInput = input,
                latitude = 0.0,
                longitude = 0.0,
                hasValidLocation = false,
                isFetchingLocation = false
            )
        }
    }

    fun setCoordinates(lat: Double, lng: Double, address: String? = null) {
        _customerForm.value = _customerForm.value.copy(
            latitude = lat,
            longitude = lng,
            rawLocationInput = "$lat, $lng",
            hasValidLocation = true,
            isFetchingLocation = false,
            address = address ?: _customerForm.value.address
        )
    }

    fun fetchCurrentGps(context: Context) {
        LocationHelper.fetchCurrentLocation(
            context = context,
            onSuccess = { loc ->
                setCoordinates(loc.latitude, loc.longitude)
                _statusMessage.value = "GPS Location set: ${loc.latitude}, ${loc.longitude}"
            },
            onError = { err ->
                _statusMessage.value = err
            }
        )
    }

    fun parseAndFillFromWhatsAppText(rawText: String) {
        var detectedName = ""
        var detectedPhone = ""
        var detectedService = ""
        var detectedIssue = ""
        var detectedAddress = ""

        val phonePattern = Pattern.compile("(?:\\+91|91|0)?[6-9]\\d{9}")
        val phoneMatcher = phonePattern.matcher(rawText)
        if (phoneMatcher.find()) {
            val fullPhone = phoneMatcher.group()
            detectedPhone = fullPhone.replace(Regex("^(\\+91|91|0)"), "")
        }

        val parsedCoords = LocationHelper.parseCoordinatesFromText(rawText)

        val lower = rawText.lowercase()
        when {
            lower.contains("ac") || lower.contains("air conditioner") || lower.contains("cooling") ->
                detectedService = "AC Service & Repair"
            lower.contains("fan") || lower.contains("switch") || lower.contains("electric") || lower.contains("wiring") || lower.contains("light") ->
                detectedService = "Electrician"
            lower.contains("fridge") || lower.contains("refrigerator") ->
                detectedService = "Refrigerator Repair"
            lower.contains("plumb") || lower.contains("tap") || lower.contains("pipe") || lower.contains("leak") || lower.contains("tank") ->
                detectedService = "Plumber"
            lower.contains("wash") || lower.contains("machine") ->
                detectedService = "Washing Machine Repair"
        }

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        for (line in lines) {
            val lineLower = line.lowercase()
            if (lineLower.startsWith("name:") || lineLower.startsWith("customer:")) {
                detectedName = line.substringAfter(":").trim()
            } else if (lineLower.startsWith("phone:") || lineLower.startsWith("mobile:") || lineLower.startsWith("number:")) {
                if (detectedPhone.isBlank()) {
                    detectedPhone = line.substringAfter(":").trim().replace(Regex("[^0-9]"), "")
                }
            } else if (lineLower.startsWith("address:") || lineLower.startsWith("location:")) {
                detectedAddress = line.substringAfter(":").trim()
            } else if (lineLower.startsWith("problem:") || lineLower.startsWith("issue:") || lineLower.startsWith("work:")) {
                detectedIssue = line.substringAfter(":").trim()
            }
        }

        if (detectedIssue.isBlank()) {
            detectedIssue = rawText.take(120)
        }

        _customerForm.value = _customerForm.value.copy(
            name = if (detectedName.isNotBlank()) detectedName else _customerForm.value.name,
            phone = if (detectedPhone.isNotBlank()) detectedPhone else _customerForm.value.phone,
            serviceType = if (detectedService.isNotBlank()) detectedService else _customerForm.value.serviceType,
            issueDescription = detectedIssue,
            address = if (detectedAddress.isNotBlank()) detectedAddress else _customerForm.value.address,
            latitude = parsedCoords?.first ?: _customerForm.value.latitude,
            longitude = parsedCoords?.second ?: _customerForm.value.longitude,
            rawLocationInput = parsedCoords?.let { "${it.first}, ${it.second}" } ?: _customerForm.value.rawLocationInput,
            hasValidLocation = parsedCoords != null || _customerForm.value.hasValidLocation,
            isFetchingLocation = false
        )
        _statusMessage.value = "WhatsApp lead parsed successfully!"
    }

    /**
     * Saves new customer order into database.
     * Duplicate check: blocks creation if an order for this customer mobile exists in the last 4 hours.
     * Address is completely optional as requested.
     */
    fun saveCustomerOrder(
        status: JobStatus = JobStatus.PENDING,
        createdById: String = "",
        createdByName: String = "",
        createdByDesignation: String = "",
        onComplete: (CustomerJobEntity) -> Unit
    ) {
        val form = _customerForm.value
        val cleanPhone = form.phone.replace(Regex("[^0-9]"), "")
        if (form.isFetchingLocation) {
            _statusMessage.value = "Please wait, fetching location..."
            return
        }
        if (!form.hasValidLocation ||
            !form.latitude.isFinite() || !form.longitude.isFinite() ||
            form.latitude !in -90.0..90.0 || form.longitude !in -180.0..180.0
        ) {
            _statusMessage.value =
                "❌ Valid coordinates required. Please check the location."
            return
        }
        val finalLat = form.latitude
        val finalLng = form.longitude

        val newJob = CustomerJobEntity(
            customerName = form.name.ifBlank { "Customer" },
            customerPhone = cleanPhone,
            serviceType = form.serviceType.ifBlank { "General Repair" },
            issueDescription = form.issueDescription.ifBlank { "Service requested" },
            address = form.address.ifBlank { "Address not specified" },
            latitude = finalLat,
            longitude = finalLng,
            status = status.name,
            created_by_user_id = createdById.ifBlank { null },
            created_by_user_name = createdByName.ifBlank { null },
            created_by_designation = createdByDesignation.ifBlank { null },
            managed_by_user_id = createdById.ifBlank { null },
            managed_by_user_name = createdByName.ifBlank { null },
            last_updated = System.currentTimeMillis()
        )

        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                if (cleanPhone.length >= 10) {
                    val isDuplicate = repository.checkDuplicateRecentOrder(cleanPhone)
                    if (isDuplicate) {
                        _statusMessage.value = "⚠️ Duplicate Order Blocked: An order for customer mobile '$cleanPhone' already exists within the last 4 hours!"
                        return@withLoading
                    }
                }

                val id = repository.insertJob(newJob)
                val insertedJob = newJob.copy(id = id)
                _customerForm.value = CustomerFormState() // Reset form
                onComplete(insertedJob)
            }
        }
    }

    fun openFindNearestExperts(job: CustomerJobEntity) {
        _activeJobForNearestExperts.value = job
    }

    fun closeFindNearestExperts() {
        _activeJobForNearestExperts.value = null
    }

    /**
     * Assigns selected expert to the given job using Firestore Real-Time Transaction Lock.
     * If two users assign simultaneously, only the first assignment succeeds.
     * Moves order to PROCESSING tab!
     */
    fun assignExpertToJob(
        job: CustomerJobEntity,
        ranked: RankedExpert,
        managedByUserId: String = "",
        managedByUserName: String = "",
        managedByDesignation: String = ""
    ) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                val result = repository.assignJobToExpertWithLock(
                    jobId = job.id,
                    expert = ranked.expert,
                    distanceKm = ranked.distanceKm,
                    managedByUserId = managedByUserId.ifBlank { null },
                    managedByUserName = managedByUserName.ifBlank { null },
                    managedByDesignation = managedByDesignation.ifBlank { null }
                )
                if (result.isSuccess) {
                    _activeJobForNearestExperts.value = null
                    _currentMainTab.value = MainTab.CUSTOMER_ORDERS
                    _currentCustomerSubTab.value = CustomerSubTab.ORDERS
                    _currentOrderStatusTab.value = OrderStatusTab.PROCESSING
                    _statusMessage.value = "Expert ${ranked.expert.name} assigned! Moved to Processing."
                } else {
                    _statusMessage.value = result.exceptionOrNull()?.toUserFriendlyMessage("Assignment conflict: order was already assigned!") ?: "Assignment conflict: order was already assigned!"
                }
            }
        }
    }

    fun takeoverOrder(
        job: CustomerJobEntity,
        currentUserId: String,
        currentUserName: String,
        currentUserDesignation: String = "",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.takeoverOrder(
                jobId = job.id,
                userId = currentUserId,
                userName = currentUserName,
                userDesignation = currentUserDesignation.ifBlank { null }
            )
            if (result.isSuccess) {
                _statusMessage.value = "You are now managing order #${job.id}"
                onDone()
            } else {
                _statusMessage.value = result.exceptionOrNull()?.toUserFriendlyMessage("Failed to takeover order") ?: "Failed to takeover order"
            }
        }
    }

    fun updateExpertNotified(jobId: Long, sent: Boolean) {
        viewModelScope.launch {
            repository.updateExpertNotified(jobId, sent)
        }
    }

    fun updateCustomerNotifiedOnAssign(jobId: Long, sent: Boolean) {
        viewModelScope.launch {
            repository.updateCustomerNotifiedOnAssign(jobId, sent)
        }
    }

    fun updateCustomerNotifiedOnCompletion(jobId: Long, sent: Boolean) {
        viewModelScope.launch {
            repository.updateCustomerNotifiedOnCompletion(jobId, sent)
        }
    }

    fun updateJob(job: CustomerJobEntity) {
        viewModelScope.launch {
            repository.updateJob(job)
            _statusMessage.value = "Order #${job.id} details updated successfully."
        }
    }

    fun unassignExpert(job: CustomerJobEntity) {
        viewModelScope.launch {
            repository.unassignExpertFromJob(job.id)
            _currentOrderStatusTab.value = OrderStatusTab.PENDING
            _statusMessage.value = "Expert unassigned. Order #${job.id} moved back to Pending."
        }
    }

    fun markMessageLaterDismissed(jobId: Long) {
        viewModelScope.launch {
            repository.updateMessageDismissedAt(jobId, System.currentTimeMillis())
        }
    }

    /**
     * Completes or cancels order and records expert review / rating.
     */
    fun completeOrCancelJobWithReview(
        job: CustomerJobEntity,
        isCompleted: Boolean,
        rating: Float,
        feedback: String?,
        beforeUrl: String? = null,
        afterUrl: String? = null
    ) {
        viewModelScope.launch {
            repository.completeOrCancelJobWithReview(
                jobId = job.id,
                expertId = job.assignedExpertId,
                isCompleted = isCompleted,
                rating = rating,
                feedback = feedback,
                beforeUrl = beforeUrl,
                afterUrl = afterUrl
            )

            _currentMainTab.value = MainTab.CUSTOMER_ORDERS
            _currentCustomerSubTab.value = CustomerSubTab.ORDERS
            _currentOrderStatusTab.value = if (isCompleted) OrderStatusTab.COMPLETED else OrderStatusTab.CANCELLED
            _statusMessage.value = if (isCompleted) {
                "Order #${job.id} marked Completed! Rating recorded."
            } else {
                "Order #${job.id} Cancelled."
            }
        }
    }

    fun getNearestExpertsForJob(job: CustomerJobEntity): List<RankedExpert> {
        val experts = allExperts.value
        return experts.map { expert ->
            val dist = LocationHelper.calculateDistanceKm(
                lat1 = job.latitude,
                lon1 = job.longitude,
                lat2 = expert.latitude,
                lon2 = expert.longitude
            )
            val time = LocationHelper.estimateTravelTimeMinutes(dist)
            RankedExpert(
                expert = expert,
                distanceKm = dist,
                travelTimeMinutes = time
            )
        }.sortedBy { it.distanceKm }
    }

    fun callExpert(context: Context, phone: String) {
        WhatsAppHelper.openDialer(context, phone)
    }

    fun callCustomer(context: Context, phone: String) {
        WhatsAppHelper.openDialer(context, phone)
    }

    fun saveNewExpert(expert: ExpertEntity, onSaved: (ExpertEntity) -> Unit = {}) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                val id = repository.insertExpert(expert)
                _statusMessage.value = "Expert ${expert.name} added successfully!"
                onSaved(expert.copy(id = id))
            }
        }
    }

    fun updateExpert(expert: ExpertEntity) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.updateExpert(expert)
                _statusMessage.value = "Expert ${expert.name} updated!"
            }
        }
    }

    fun deleteExpert(expert: ExpertEntity) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.moveExpertToRecycleBin(expert.id)
                _statusMessage.value = "Expert '${expert.name}' moved to Recycle Bin (Kept for 30 days)."
            }
        }
    }

    fun deleteJob(job: CustomerJobEntity) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.moveJobToRecycleBin(job.id)
                _statusMessage.value = "Order #${job.id} moved to Recycle Bin (Kept for 30 days)."
            }
        }
    }

    fun restoreJobFromRecycleBin(jobId: Long) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.restoreJobFromRecycleBin(jobId)
                _statusMessage.value = "Order #$jobId restored to active orders!"
            }
        }
    }

    fun deleteJobPermanently(jobId: Long) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.deleteJobPermanently(jobId)
                _statusMessage.value = "Order #$jobId deleted permanently."
            }
        }
    }

    fun restoreExpertFromRecycleBin(expertId: Long) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.restoreExpertFromRecycleBin(expertId)
                _statusMessage.value = "Expert restored to active experts!"
            }
        }
    }

    fun deleteExpertPermanently(expertId: Long) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.deleteExpertPermanently(expertId)
                _statusMessage.value = "Expert deleted permanently."
            }
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.clearRecycleBin()
                _statusMessage.value = "Recycle bin emptied!"
            }
        }
    }

    fun updateWelcomeMessageSent(expertId: Long, sent: Boolean) {
        viewModelScope.launch {
            repository.updateWelcomeMessageSent(expertId, sent)
            if (sent) {
                _statusMessage.value = "Welcome message sent status updated."
            }
        }
    }

    fun unassignExpert(jobId: Long) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.unassignExpertFromJob(jobId)
                _statusMessage.value = "Expert unassigned. Order moved back to Pending."
            }
        }
    }

    fun updateJobDetails(job: CustomerJobEntity) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.updateJob(job)
                _statusMessage.value = "Order #${job.id} details updated!"
            }
        }
    }

    fun addNewCategory(name: String) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.insertCategory(name)
                _statusMessage.value = "Category '$name' created!"
            }
        }
    }

    fun deleteCategory(category: ExpertCategoryEntity) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.deleteCategory(category)
                _statusMessage.value = "Category '${category.name}' deleted."
            }
        }
    }

    fun restoreBackupData(
        jobs: List<CustomerJobEntity>,
        experts: List<ExpertEntity>,
        categories: List<ExpertCategoryEntity>
    ) {
        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                repository.restoreDatabase(jobs, experts, categories)
                _statusMessage.value = "Backup successfully restored! (${jobs.size} orders, ${experts.size} experts)"
            }
        }
    }
}

class DispatchViewModelFactory(private val repository: DispatchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DispatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DispatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
