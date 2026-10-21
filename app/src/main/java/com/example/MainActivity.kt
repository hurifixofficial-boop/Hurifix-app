package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.DispatchViewModel
import com.example.ui.DispatchViewModelFactory
import com.example.ui.components.GlobalLoadingOverlay
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.GlobalLoadingManager
import com.example.util.SessionManager
import com.example.util.SoundHelper

enum class AppDestination {
    SPLASH,
    AUTH,
    HOME
}

class MainActivity : ComponentActivity() {

    private val viewModel: DispatchViewModel by viewModels {
        val app = application as SevaMitraApplication
        DispatchViewModelFactory(app.repository)
    }

    private lateinit var sessionManager: SessionManager
    private var downX = 0f
    private var downY = 0f
    private var downAt = 0L

    /** Provides a subtle tap sound throughout the app without intercepting touch handling. */
    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downAt = android.os.SystemClock.elapsedRealtime()
            }
            android.view.MotionEvent.ACTION_UP -> {
                val dx = kotlin.math.abs(event.x - downX)
                val dy = kotlin.math.abs(event.y - downY)
                val duration = android.os.SystemClock.elapsedRealtime() - downAt
                val threshold = 12f * resources.displayMetrics.density
                if (dx < threshold && dy < threshold && duration < 350L) SoundHelper.playClick()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(sessionManager.isDarkModeEnabled()) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
                        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

                        androidx.compose.runtime.LaunchedEffect(currentDestination) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }

                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(durationMillis = 250)) + slideInVertically(
                                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                                    initialOffsetY = { 35 }
                                )).togetherWith(
                                    fadeOut(animationSpec = tween(durationMillis = 200))
                                )
                            },
                            label = "app_destination_transition"
                        ) { destination ->
                            when (destination) {
                                AppDestination.SPLASH -> {
                                    SplashScreen(
                                        onSplashFinished = {
                                            if (sessionManager.isLoggedIn() && sessionManager.getUserPhone().isNotBlank()) {
                                                viewModel.onUserLoggedIn(sessionManager.getUserPhone())
                                                currentDestination = AppDestination.HOME
                                            } else {
                                                viewModel.onUserLoggedOut()
                                                currentDestination = AppDestination.AUTH
                                            }
                                        }
                                    )
                                }

                                AppDestination.AUTH -> {
                                    AuthScreen(
                                        sessionManager = sessionManager,
                                        onLoginSuccess = {
                                            viewModel.onUserLoggedIn(sessionManager.getUserPhone())
                                            currentDestination = AppDestination.HOME
                                        }
                                    )
                                }

                                AppDestination.HOME -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        sessionManager = sessionManager,
                                        isDarkMode = isDarkMode,
                                        onToggleDarkMode = { newMode ->
                                            isDarkMode = newMode
                                            sessionManager.setDarkModeEnabled(newMode)
                                        },
                                        onLogout = {
                                            viewModel.onUserLoggedOut()
                                            currentDestination = AppDestination.AUTH
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val isGlobalLoading by GlobalLoadingManager.isLoading.collectAsState()
                    val globalLoadingMsg by GlobalLoadingManager.loadingMessage.collectAsState()
                    GlobalLoadingOverlay(
                        isLoading = isGlobalLoading,
                        message = globalLoadingMsg
                    )
                }
            }
        }
    }


}
