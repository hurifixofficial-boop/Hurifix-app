package com.example.util

import android.content.Context
import android.content.SharedPreferences

class OtpRateLimiter private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("otp_rate_limiter_prefs", Context.MODE_PRIVATE)

    fun checkCanRequestOtp(phone: String): Pair<Boolean, String?> {
        val now = System.currentTimeMillis()
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val keyCount = "count_$cleanPhone"
        val keyReset = "reset_$cleanPhone"

        val resetTime = prefs.getLong(keyReset, 0L)
        var count = prefs.getInt(keyCount, 0)

        if (now > resetTime) {
            count = 0
            prefs.edit().putInt(keyCount, 0).putLong(keyReset, now + 3600_000L).apply()
        }

        if (count >= 4) {
            val remainingMins = ((resetTime - now) / 60_000L).coerceAtLeast(1)
            return Pair(false, "Limit exceeded (Max 4 OTPs/hr). Try again in $remainingMins min.")
        }

        prefs.edit().putInt(keyCount, count + 1).apply()
        return Pair(true, null)
    }

    companion object {
        @Volatile
        private var instance: OtpRateLimiter? = null

        fun getInstance(context: Context): OtpRateLimiter {
            return instance ?: synchronized(this) {
                instance ?: OtpRateLimiter(context.applicationContext).also { instance = it }
            }
        }
    }
}
