package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PhoneAuthManager {
    private const val TAG = "PhoneAuthManager"
    private const val WEBHOOK_URL = "https://trigger.macrodroid.com/cac6ee5b-e2d8-4b7e-9050-5aa8f9cf8305/hurifix_otp"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Store active OTPs per phone number
    private val activeOtps = java.util.concurrent.ConcurrentHashMap<String, String>()

    @Volatile
    private var lastRequestTime = 0L
    @Volatile
    private var lastRequestedPhone = ""

    fun sendOtp(
        context: Context,
        activity: Activity?,
        phone: String,
        onCodeSent: (verificationId: String, codeHint: String) -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").takeLast(10)
        if (cleanPhone.length != 10) {
            onError("Please enter a valid 10-digit mobile number")
            return
        }

        // Prevent duplicate calls triggered at the exact same moment
        synchronized(this) {
            val now = System.currentTimeMillis()
            if (cleanPhone == lastRequestedPhone && (now - lastRequestTime) < 2000L) {
                Log.w(TAG, "Duplicate sendOtp call suppressed within 2 seconds for phone $cleanPhone")
                return
            }
            lastRequestTime = now
            lastRequestedPhone = cleanPhone
        }

        // Always generate a fresh dynamic 6-digit OTP
        val generatedOtp = (100000..999999).random().toString()
        activeOtps[cleanPhone] = generatedOtp
        val verificationId = "VER_${System.currentTimeMillis()}"

        // Trigger MacroDroid Webhook in background with dynamic OTP across all possible parameter names & casings
        GlobalScope.launch(Dispatchers.IO) {
            val encodedMessage = java.net.URLEncoder.encode("Your Hurifix OTP is $generatedOtp", "UTF-8")
            val queryString = StringBuilder()
                .append("otp=").append(generatedOtp)
                .append("&OTP=").append(generatedOtp)
                .append("&Otp=").append(generatedOtp)
                .append("&code=").append(generatedOtp)
                .append("&CODE=").append(generatedOtp)
                .append("&Code=").append(generatedOtp)
                .append("&hurifix_otp=").append(generatedOtp)
                .append("&hurifixOtp=").append(generatedOtp)
                .append("&HURIFIX_OTP=").append(generatedOtp)
                .append("&pin=").append(generatedOtp)
                .append("&PIN=").append(generatedOtp)
                .append("&phone=").append(cleanPhone)
                .append("&Phone=").append(cleanPhone)
                .append("&PHONE=").append(cleanPhone)
                .append("&mobile=").append(cleanPhone)
                .append("&Mobile=").append(cleanPhone)
                .append("&MOBILE=").append(cleanPhone)
                .append("&number=").append(cleanPhone)
                .append("&Number=").append(cleanPhone)
                .append("&msg=").append(generatedOtp)
                .append("&MSG=").append(generatedOtp)
                .append("&message=").append(encodedMessage)
                .append("&sms=").append(encodedMessage)
                .toString()

            val fullUrl = "$WEBHOOK_URL?$queryString"

            val jsonPayload = """
                {
                  "otp": "$generatedOtp",
                  "OTP": "$generatedOtp",
                  "Otp": "$generatedOtp",
                  "code": "$generatedOtp",
                  "CODE": "$generatedOtp",
                  "Code": "$generatedOtp",
                  "hurifix_otp": "$generatedOtp",
                  "hurifixOtp": "$generatedOtp",
                  "pin": "$generatedOtp",
                  "PIN": "$generatedOtp",
                  "phone": "$cleanPhone",
                  "Phone": "$cleanPhone",
                  "PHONE": "$cleanPhone",
                  "mobile": "$cleanPhone",
                  "Mobile": "$cleanPhone",
                  "number": "$cleanPhone",
                  "message": "Your Hurifix OTP is $generatedOtp",
                  "msg": "$generatedOtp"
                }
            """.trimIndent()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toRequestBody(mediaType)

            // Send EXACTLY ONE single HTTP POST request with query params + JSON payload
            try {
                val postRequest = Request.Builder()
                    .url(fullUrl)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                client.newCall(postRequest).execute().use { response ->
                    Log.d(TAG, "MacroDroid webhook single delivery (code ${response.code}) with dynamic OTP: $generatedOtp for phone: $cleanPhone")
                }
            } catch (e: Exception) {
                Log.e(TAG, "MacroDroid webhook failed: ${e.message}", e)
            }
        }

        // Notify UI that code has been generated and sent
        onCodeSent(verificationId, generatedOtp)
    }

    fun verifyOtp(
        context: Context,
        phone: String,
        verificationId: String,
        inputCode: String,
        onSuccess: () -> Unit,
        onError: (errorMessage: String) -> Unit
    ) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").takeLast(10)
        val expectedOtp = activeOtps[cleanPhone]
            ?: if (verificationId.startsWith("FALLBACK:")) verificationId.removePrefix("FALLBACK:") else null

        val cleanInput = inputCode.trim()

        if (cleanInput.length == 6) {
            if (expectedOtp != null && cleanInput == expectedOtp.trim()) {
                activeOtps.remove(cleanPhone)
                onSuccess()
            } else {
                onError("Wrong OTP! Please enter the correct 6-digit OTP code.")
            }
        } else {
            onError("Please enter a valid 6-digit OTP code.")
        }
    }
}
