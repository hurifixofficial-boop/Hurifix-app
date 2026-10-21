package com.example.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Tracks whether the application is currently active in the foreground.
 * Used to suppress notifications when the app is open and being used on device.
 */
object AppLifecycleTracker {
    @Volatile
    private var resumedActivitiesCount = 0

    val isAppInForeground: Boolean
        get() = resumedActivitiesCount > 0

    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                resumedActivitiesCount++
            }

            override fun onActivityPaused(activity: Activity) {
                resumedActivitiesCount = (resumedActivitiesCount - 1).coerceAtLeast(0)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
