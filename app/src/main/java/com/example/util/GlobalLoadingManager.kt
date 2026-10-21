package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GlobalLoadingManager {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow("Loading...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun show(message: String = "Loading...") {
        _loadingMessage.value = message
        _isLoading.value = true
    }

    fun hide() {
        _isLoading.value = false
    }

    inline fun <T> withLoading(message: String = "Loading...", block: () -> T): T {
        show(message)
        return try {
            block()
        } finally {
            hide()
        }
    }
}
