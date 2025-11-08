package com.balancetube

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BalanceTubeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
