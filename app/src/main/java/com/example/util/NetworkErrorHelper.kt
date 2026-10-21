package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized utility for graceful network and offline error handling.
 * Detects network failures, DNS blocks, timeouts, and Firebase UNAVAILABLE exceptions,
 * and displays clean user-friendly Toast/Snackbar messages: 'Please check your internet connection'.
 */
object NetworkErrorHelper {
    private const val TAG = "NetworkErrorHelper"
    const val USER_FRIENDLY_NETWORK_ERROR = "Please check your internet connection"

    private val mainHandler by lazy {
        try {
            Handler(Looper.getMainLooper())
        } catch (_: Throwable) {
            null
        }
    }
    private var lastToastTime = 0L
    private var isCallbackRegistered = false

    /**
     * Determines whether a throwable is caused by network loss, DNS resolution failure,
     * timeout, or Firestore UNAVAILABLE errors.
     */
    fun isNetworkOrUnavailableError(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        var current: Throwable? = throwable
        var depth = 0
        while (current != null && depth < 10) {
            if (current is UnknownHostException ||
                current is ConnectException ||
                current is SocketTimeoutException ||
                current is SocketException
            ) {
                return true
            }

            if (current is FirebaseFirestoreException) {
                if (current.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                    current.code == FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
                ) {
                    return true
                }
            }

            val msg = (current.message ?: "").lowercase()
            if (msg.contains("unavailable") ||
                msg.contains("unable to resolve host") ||
                msg.contains("failed to connect") ||
                msg.contains("no route to host") ||
                msg.contains("network is unreachable") ||
                msg.contains("network error") ||
                msg.contains("connection reset") ||
                msg.contains("timeout") ||
                msg.contains("custom dns") ||
                msg.contains("dns") ||
                msg.contains("offline") ||
                msg.contains("econnrefused") ||
                msg.contains("enotfound") ||
                msg.contains("sslhandshakeexception") ||
                msg.contains("broken pipe")
            ) {
                return true
            }
            current = current.cause
            depth++
        }
        return false
    }

    /**
     * Debounced Toast to prevent spamming when multiple network calls fail simultaneously.
     */
    fun showNetworkErrorToast(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastToastTime > 2500L) {
            lastToastTime = now
            mainHandler?.post {
                try {
                    Toast.makeText(context.applicationContext, USER_FRIENDLY_NETWORK_ERROR, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.w(TAG, "Toast error: ${e.message}")
                }
            }
        }
    }

    /**
     * Replaces technical debug messages with 'Please check your internet connection' if the error is network-related.
     */
    fun formatErrorMessage(throwable: Throwable?, fallback: String = "An error occurred"): String {
        return if (isNetworkOrUnavailableError(throwable)) {
            USER_FRIENDLY_NETWORK_ERROR
        } else {
            val msg = throwable?.localizedMessage?.trim()
            if (!msg.isNullOrBlank()) msg else fallback
        }
    }

    /**
     * Registers a device-wide connectivity monitor to detect when network connectivity is lost.
     */
    fun registerNetworkMonitor(context: Context) {
        if (isCallbackRegistered) return
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    super.onLost(network)
                    showNetworkErrorToast(context)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    showNetworkErrorToast(context)
                }
            })
            isCallbackRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network callback: ${e.message}")
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            return true
        }
    }
}

fun Throwable?.isNetworkOrUnavailableError(): Boolean = NetworkErrorHelper.isNetworkOrUnavailableError(this)
fun Throwable?.toUserFriendlyMessage(fallback: String = "An error occurred"): String = NetworkErrorHelper.formatErrorMessage(this, fallback)
