package com.pulsehealth.app

import android.app.Application
import com.pulsehealth.app.di.AppContainer
import com.pulsehealth.app.di.DefaultAppContainer

class PulseHealthApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
