package com.aditya.smartmedbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme
import com.aditya.smartmedbox.navigation.SmartMedBoxAppScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SmartMedBoxTheme {
                SmartMedBoxAppScreen()
            }
        }
    }
}
