package com.pulsehealth.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.pulsehealth.app.ui.PulseHealthApp
import com.pulsehealth.app.ui.theme.PulseTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseTheme { PulseHealthApp() }
        }
    }
}
