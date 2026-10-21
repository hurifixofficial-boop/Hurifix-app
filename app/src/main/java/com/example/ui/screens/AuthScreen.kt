package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.model.HurifixUser
import com.example.ui.components.OtpVerificationDialog
import com.example.ui.components.SuccessAnimationOverlay
import com.example.util.NetworkErrorHelper
import com.example.util.OtpRateLimiter
import com.example.util.PhoneAuthManager
import com.example.util.SessionManager
import com.example.util.SoundHelper
import com.example.util.toUserFriendlyMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (name: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncManager = remember { FirestoreSyncManager.getInstance(context) }
    val rateLimiter = remember { OtpRateLimiter.getInstance(context) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var isOtpLoginMode by remember { mutableStateOf(false) } // false = Password Mode, true = OTP Mode
    var isOtpSent by remember { mutableStateOf(false) } // false = Ask mobile number first, true = OTP code sent
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // OTP Mode State
    var loginOtpInput by remember { mutableStateOf("") }
    var activeVerificationId by remember { mutableStateOf("") }
    var activeOtpCodeHint by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var resendTimerSeconds by remember { mutableIntStateOf(0) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Success Animation Overlay State
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var successOverlayTitle by remember { mutableStateOf("Authentication Successful!") }
    var successOverlaySubtitle by remember { mutableStateOf("Redirecting to portal...") }
    var pendingSuccessName by remember { mutableStateOf("") }

    LaunchedEffect(showSuccessOverlay) {
        if (showSuccessOverlay) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    // Password Reset / Forgot Password state
    var showForgotPasswordPhoneDialog by remember { mutableStateOf(false) }
    var forgotPasswordPhone by remember { mutableStateOf("") }
    var showForgotPasswordOtpModal by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPasswordConfirmInput by remember { mutableStateOf("") }
    var resetPasswordError by remember { mutableStateOf<String?>(null) }

    // Emergency Admin Recovery Dialog state
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryPhone by remember { mutableStateOf("") }
    var recoveryKey by remember { mutableStateOf("") }
    var lostPhoneToBlock by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }
    var isRecovering by remember { mutableStateOf(false) }

    // Countdown Timer Loop for Resend OTP
    LaunchedEffect(resendTimerSeconds) {
        if (resendTimerSeconds > 0) {
            delay(1000L)
            resendTimerSeconds -= 1
        }
    }

    // Sends OTP ONLY after verifying that the mobile number is registered in Firestore
    fun sendLoginOtpIfRegistered() {
        if (isSendingOtp || resendTimerSeconds > 0) {
            return
        }
        val clean = phone.replace(Regex("[^0-9]"), "")
        if (clean.length < 10) {
            return
        }
        isSendingOtp = true

        // Sync phone into AuthViewModel
        authViewModel.onPhoneChanged(clean)

        // Delegate pre-check & phone auth initiation to AuthViewModel
        authViewModel.verifyPhoneRegistrationAndSendOtp(
            context = context,
            activity = context as? Activity,
            onSuccess = {
                val state = authViewModel.uiState.value
                isSendingOtp = state.isSendingOtp
                isOtpSent = state.isOtpSent
                activeVerificationId = state.activeVerificationId
                activeOtpCodeHint = state.activeOtpCodeHint
                resendTimerSeconds = state.resendTimerSeconds
                errorMessage = null

                Toast.makeText(context, "✅ OTP Sent successfully to your mobile number!", Toast.LENGTH_LONG).show()
            },
            onError = { _ ->
                val state = authViewModel.uiState.value
                isSendingOtp = state.isSendingOtp
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(16.dp))

            // Logo & Title
            Image(
                painter = painterResource(id = R.drawable.hurifix_logo_exact_1790919761852),
                contentDescription = "Hurifix Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Hurifix",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Many Problems | One Solution",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "🔒 Secure Centralized Multi-User Portal",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (!isOtpLoginMode) "Login with Password" else "Login via Mobile OTP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input ->
                            phone = input.filter { it.isDigit() }.take(10)
                            errorMessage = null
                        },
                        label = { Text("Mobile Number (10 Digits) *") },
                        prefix = { Text("+91 ") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                        singleLine = true,
                        supportingText = {
                            if (phone.isNotEmpty() && phone.length < 10) {
                                Text("${phone.length}/10 digits", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isOtpLoginMode) {
                        // MODE 1: Standard Password Input Box
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password / PIN *") },
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Small text options row below password box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    // Switches view to OTP mode without sending OTP directly!
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    isOtpLoginMode = true
                                    isOtpSent = false
                                    errorMessage = null
                                }
                            ) {
                                Text(
                                    text = "📲 Login with OTP",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            TextButton(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    forgotPasswordPhone = phone
                                    resetPasswordError = null
                                    showForgotPasswordPhoneDialog = true
                                }
                            ) {
                                Text("Forgot Password?", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        // MODE 2: OTP Login Mode (Replaces Password Box)
                        if (isOtpSent) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                OutlinedTextField(
                                    value = loginOtpInput,
                                    onValueChange = { input ->
                                        loginOtpInput = input.filter { it.isDigit() }.take(6)
                                        errorMessage = null
                                        if (loginOtpInput.length == 6) {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    },
                                    label = { Text("Enter 6-Digit OTP Code *") },
                                    placeholder = { Text("123456") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }),
                                    singleLine = true,
                                    isError = errorMessage != null,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 3.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input_field")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            isOtpLoginMode = false
                                            errorMessage = null
                                        }
                                    ) {
                                        Text(
                                            text = "🔑 Login with Password",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    TextButton(
                                        onClick = { sendLoginOtpIfRegistered() },
                                        enabled = !isSendingOtp && resendTimerSeconds == 0
                                    ) {
                                        if (resendTimerSeconds > 0) {
                                            Text("Resend in ${resendTimerSeconds}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text("🔄 Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Link to go back to password mode before OTP is requested
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = {
                                        isOtpLoginMode = false
                                        errorMessage = null
                                    }
                                ) {
                                    Text(
                                        text = "🔑 Switch to Password Login",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    errorMessage?.let { error ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    successMessage?.let { success ->
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = success,
                                color = Color(0xFF15803D),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            SoundHelper.playSFX("click")

                            if (phone.length < 10) {
                                SoundHelper.playSFX("error")
                                errorMessage = "Please enter your 10-digits mobile number to login"
                                return@Button
                            }

                            if (!isOtpLoginMode) {
                                // Password Login execution
                                if (password.isBlank()) {
                                    SoundHelper.playSFX("error")
                                    errorMessage = "Please enter your password"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = sessionManager.login(phone, password)
                                    isLoading = false
                                    result.onSuccess { loggedInName ->
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        SoundHelper.playSFX("success")
                                        pendingSuccessName = loggedInName
                                        successOverlayTitle = "Authentication Successful!"
                                        successOverlaySubtitle = "Welcome back, $loggedInName"
                                        showSuccessOverlay = true
                                    }.onFailure { exc ->
                                        SoundHelper.playSFX("error")
                                        errorMessage = exc.toUserFriendlyMessage("No user found try again")
                                    }
                                }
                            } else {
                                // OTP Mode Execution
                                if (!isOtpSent) {
                                    // Step A: Send OTP (verifies registration first!)
                                    sendLoginOtpIfRegistered()
                                } else {
                                    // Step B: Verify 6-digit OTP code & Login
                                    if (loginOtpInput.length < 6) {
                                        errorMessage = "Please enter a valid 6-digit OTP code"
                                        SoundHelper.playSFX("error")
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null
                                    PhoneAuthManager.verifyOtp(
                                        context = context,
                                        phone = phone,
                                        verificationId = activeVerificationId.ifBlank { "FALLBACK:$activeOtpCodeHint" },
                                        inputCode = loginOtpInput,
                                        onSuccess = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            coroutineScope.launch {
                                                val result = sessionManager.loginWithOtp(phone)
                                                isLoading = false
                                                result.onSuccess { loggedInName ->
                                                    SoundHelper.playSFX("success")
                                                    pendingSuccessName = loggedInName
                                                    successOverlayTitle = "OTP Login Successful!"
                                                    successOverlaySubtitle = "Welcome back, $loggedInName"
                                                    showSuccessOverlay = true
                                                }.onFailure { exc ->
                                                    errorMessage = exc.toUserFriendlyMessage("No user found try again")
                                                }
                                            }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                            SoundHelper.playSFX("error")
                                        }
                                    )
                                }
                            }
                        },
                        enabled = !isLoading && !isSendingOtp,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading || isSendingOtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(if (isSendingOtp) "Checking number & sending OTP..." else "Verifying credentials...")
                        } else {
                            Text(
                                text = if (!isOtpLoginMode) "Login to Hurifix" else if (!isOtpSent) "Send OTP Code" else "Verify & Login with OTP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Notice about access & Emergency Recovery Option
            Text(
                text = "Notice: Only registered staff and admins can access Hurifix. Contact Admin to register your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Emergency Admin Recovery Secret Option
            TextButton(
                onClick = {
                    recoveryPhone = phone
                    recoveryKey = ""
                    lostPhoneToBlock = ""
                    recoveryError = null
                    showRecoveryDialog = true
                }
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Recover Admin Access (Secret Key)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Forgot Password Modal - Step 1: Mobile Phone Check
    if (showForgotPasswordPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordPhoneDialog = false },
            title = { Text("Reset Password via OTP", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter your 10-digit registered mobile number to receive a verification OTP code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = forgotPasswordPhone,
                        onValueChange = { input ->
                            forgotPasswordPhone = input.filter { it.isDigit() }.take(10)
                            resetPasswordError = null
                        },
                        label = { Text("Registered Mobile Number *") },
                        prefix = { Text("+91 ") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    resetPasswordError?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordPhone.length < 10) {
                            SoundHelper.playSFX("error")
                            resetPasswordError = "Please enter your 10-digits mobile number to login"
                            return@Button
                        }

                        coroutineScope.launch {
                            val userCheck = syncManager.loginWithOtp(forgotPasswordPhone)
                            userCheck.onSuccess {
                                showForgotPasswordPhoneDialog = false
                                showForgotPasswordOtpModal = true
                            }.onFailure {
                                SoundHelper.playSFX("error")
                                resetPasswordError = "No user found try again"
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send OTP Code", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotPasswordPhoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Forgot Password Modal - Step 2: OTP Verification
    if (showForgotPasswordOtpModal) {
        OtpVerificationDialog(
            phone = forgotPasswordPhone,
            purposeTitle = "Verify Password Reset OTP",
            purposeSubtitle = "Verify OTP sent to +91 $forgotPasswordPhone to reset password",
            onSuccess = {
                showForgotPasswordOtpModal = false
                newPasswordInput = ""
                newPasswordConfirmInput = ""
                resetPasswordError = null
                showResetPasswordDialog = true
            },
            onDismiss = { showForgotPasswordOtpModal = false }
        )
    }

    // Forgot Password Modal - Step 3: Set New Password
    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("Set New Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set a new login password for account: +91 $forgotPasswordPhone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = {
                            newPasswordInput = it
                            resetPasswordError = null
                        },
                        label = { Text("New Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPasswordConfirmInput,
                        onValueChange = {
                            newPasswordConfirmInput = it
                            resetPasswordError = null
                        },
                        label = { Text("Confirm New Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    resetPasswordError?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPasswordInput.isBlank() || newPasswordInput.length < 4) {
                            SoundHelper.playSFX("error")
                            resetPasswordError = "Password must be at least 4 characters long"
                            return@Button
                        }
                        if (newPasswordInput.trim() != newPasswordConfirmInput.trim()) {
                            SoundHelper.playSFX("error")
                            resetPasswordError = "Passwords do not match"
                            return@Button
                        }

                        coroutineScope.launch {
                            val res = syncManager.updateUserPassword(forgotPasswordPhone, newPasswordInput.trim())
                            if (res.isSuccess) {
                                SoundHelper.playSFX("success")
                                showResetPasswordDialog = false
                                Toast.makeText(context, "✅ Password updated successfully! Please login with your new password.", Toast.LENGTH_LONG).show()
                                phone = forgotPasswordPhone
                                password = newPasswordInput.trim()
                                isOtpLoginMode = false
                                isOtpSent = false
                            } else {
                                SoundHelper.playSFX("error")
                                resetPasswordError = "Failed to update password. Please try again."
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update Password", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Emergency Admin Recovery Dialog
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { if (!isRecovering) showRecoveryDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Admin Access Recovery", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter the Emergency Secret Recovery Key to promote your mobile number to Master Admin. If your previous phone is lost, enter it below to block its access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = recoveryPhone,
                        onValueChange = { input ->
                            recoveryPhone = input.filter { it.isDigit() }.take(10)
                            recoveryError = null
                        },
                        label = { Text("Your Mobile Number (10 Digits) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recoveryKey,
                        onValueChange = {
                            recoveryKey = it
                            recoveryError = null
                        },
                        label = { Text("Emergency Secret Key *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lostPhoneToBlock,
                        onValueChange = { input ->
                            lostPhoneToBlock = input.filter { it.isDigit() }.take(10)
                        },
                        label = { Text("Lost/Compromised Mobile to Block (Optional)") },
                        placeholder = { Text("10-digit number to revoke") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    recoveryError?.let { err ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoveryPhone.length < 10) {
                            recoveryError = "Please enter your 10-digits mobile number to login"
                            return@Button
                        }
                        if (recoveryKey.trim() != HurifixUser.EMERGENCY_RECOVERY_KEY) {
                            recoveryError = "Galat Secret Recovery Key! Access Denied."
                            return@Button
                        }

                        isRecovering = true
                        recoveryError = null
                        coroutineScope.launch {
                            val result = sessionManager.recoverAdminAccess(
                                phone = recoveryPhone,
                                secretKey = recoveryKey.trim(),
                                lostPhoneToBlock = lostPhoneToBlock.ifBlank { null }
                            )
                            isRecovering = false
                            result.onSuccess { adminName ->
                                showRecoveryDialog = false
                                onLoginSuccess(adminName)
                            }.onFailure { exc ->
                                recoveryError = exc.toUserFriendlyMessage("Recovery failed")
                            }
                        }
                    },
                    enabled = !isRecovering,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isRecovering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Promoting to Admin...")
                    } else {
                        Text("Grant Admin Access", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRecoveryDialog = false },
                    enabled = !isRecovering
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    SuccessAnimationOverlay(
        visible = showSuccessOverlay,
        title = successOverlayTitle,
        subtitle = successOverlaySubtitle,
        onFinished = {
            showSuccessOverlay = false
            onLoginSuccess(pendingSuccessName)
        }
    )
}
