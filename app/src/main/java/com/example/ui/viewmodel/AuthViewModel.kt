package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.model.HurifixUser
import com.example.util.GlobalLoadingManager
import com.example.util.NetworkErrorHelper
import com.example.util.OtpRateLimiter
import com.example.util.PhoneAuthManager
import com.example.util.SessionManager
import com.example.util.toUserFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for AuthScreen and Phone Authentication.
 */
data class AuthUiState(
    val phone: String = "",
    val password: String = "",
    val isOtpLoginMode: Boolean = false,
    val isOtpSent: Boolean = false,
    val loginOtpInput: String = "",
    val activeVerificationId: String = "",
    val activeOtpCodeHint: String = "",
    val isSendingOtp: Boolean = false,
    val isLoading: Boolean = false,
    val resendTimerSeconds: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isPhoneRegistered: Boolean? = null,
    val user: HurifixUser? = null
)

/**
 * ViewModel managing Firebase Phone Authentication, Firestore user registration verification,
 * rate limiting, and password login flows.
 */
class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(newPhone: String) {
        val clean = newPhone.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(phone = clean, errorMessage = null, isPhoneRegistered = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onOtpInputChanged(newOtp: String) {
        val clean = newOtp.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(loginOtpInput = clean, errorMessage = null) }
    }

    fun toggleLoginMode() {
        _uiState.update {
            it.copy(
                isOtpLoginMode = !it.isOtpLoginMode,
                isOtpSent = false,
                loginOtpInput = "",
                errorMessage = null
            )
        }
    }

    fun setOtpLoginMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isOtpLoginMode = enabled,
                isOtpSent = false,
                loginOtpInput = "",
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Verifies if the entered mobile number is registered in Firestore before initiating the OTP request.
     */
    fun verifyPhoneRegistrationAndSendOtp(
        context: Context,
        activity: Activity?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_uiState.value.isSendingOtp) {
            return
        }

        val currentPhone = _uiState.value.phone.replace(Regex("[^0-9]"), "")
        if (currentPhone.length < 10) {
            val err = "Please enter your 10-digits mobile number to login"
            _uiState.update { it.copy(errorMessage = err) }
            onError(err)
            return
        }

        _uiState.update { it.copy(isSendingOtp = true, errorMessage = null) }

        viewModelScope.launch {
            val syncManager = FirestoreSyncManager.getInstance(context)
            val rateLimiter = OtpRateLimiter.getInstance(context)

            // Step 1: Verify if phone number is registered in Firestore / Local Cache
            val checkResult = syncManager.loginWithOtp(currentPhone)
            checkResult.onSuccess { user ->
                _uiState.update { it.copy(isPhoneRegistered = true, user = user) }

                // Step 2: Enforce Rate Limit (Max 4 OTPs per hour)
                val (canReq, rateErr) = rateLimiter.checkCanRequestOtp(currentPhone)
                if (!canReq) {
                    val msg = rateErr ?: "Limit exceeded. Try again in an hour."
                    _uiState.update { it.copy(isSendingOtp = false, errorMessage = msg) }
                    onError(msg)
                    return@launch
                }

                // Step 3: Trigger Phone Auth OTP Request
                PhoneAuthManager.sendOtp(
                    context = context,
                    activity = activity,
                    phone = currentPhone,
                    onCodeSent = { verId, codeHint ->
                        _uiState.update { state ->
                            state.copy(
                                isSendingOtp = false,
                                isOtpSent = true,
                                activeVerificationId = verId,
                                activeOtpCodeHint = codeHint,
                                resendTimerSeconds = 60
                            )
                        }
                        onSuccess()
                    },
                    onError = { sendErr ->
                        _uiState.update { it.copy(isSendingOtp = false, errorMessage = sendErr) }
                        onError(sendErr)
                    }
                )

            }.onFailure { exc ->
                // Mobile number is NOT registered in Firestore
                val msg = exc.toUserFriendlyMessage("No user found try again")
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        isPhoneRegistered = false,
                        errorMessage = msg
                    )
                }
                onError(msg)
            }
        }
    }

    /**
     * Verifies the entered OTP and completes login.
     */
    fun verifyOtpAndLogin(
        context: Context,
        sessionManager: SessionManager,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val state = _uiState.value
        val phone = state.phone
        val otpInput = state.loginOtpInput

        if (otpInput.length < 6) {
            val err = "Invalid OTP"
            _uiState.update { it.copy(errorMessage = err) }
            onError(err)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        PhoneAuthManager.verifyOtp(
            context = context,
            phone = phone,
            verificationId = state.activeVerificationId.ifBlank { "FALLBACK:${state.activeOtpCodeHint}" },
            inputCode = otpInput,
            onSuccess = {
                viewModelScope.launch {
                    val result = sessionManager.loginWithOtp(phone)
                    _uiState.update { it.copy(isLoading = false) }
                    result.onSuccess { name ->
                        onSuccess(name)
                    }.onFailure { exc ->
                        val msg = exc.toUserFriendlyMessage("No user found try again")
                        _uiState.update { it.copy(errorMessage = msg) }
                        onError(msg)
                    }
                }
            },
            onError = { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err) }
                onError(err)
            }
        )
    }

    /**
     * Standard Password Login.
     */
    fun loginWithPassword(
        sessionManager: SessionManager,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val state = _uiState.value
        val phone = state.phone
        val password = state.password

        if (phone.length < 10) {
            val err = "Please enter your 10-digits mobile number to login"
            _uiState.update { it.copy(errorMessage = err) }
            onError(err)
            return
        }

        if (password.isBlank()) {
            val err = "Please enter your password"
            _uiState.update { it.copy(errorMessage = err) }
            onError(err)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            GlobalLoadingManager.withLoading("Loading...") {
                val result = sessionManager.login(phone, password)
                _uiState.update { it.copy(isLoading = false) }
                result.onSuccess { name ->
                    onSuccess(name)
                }.onFailure { exc ->
                    val msg = exc.toUserFriendlyMessage("No user found try again")
                    _uiState.update { it.copy(errorMessage = msg) }
                    onError(msg)
                }
            }
        }
    }

    fun decrementTimer() {
        _uiState.update {
            if (it.resendTimerSeconds > 0) it.copy(resendTimerSeconds = it.resendTimerSeconds - 1) else it
        }
    }
}
