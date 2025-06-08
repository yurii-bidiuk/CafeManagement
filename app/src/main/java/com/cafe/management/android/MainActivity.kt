package com.cafe.management.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cafe.management.android.presentation.navigation.CafeNavHost
import com.cafe.management.android.presentation.theme.CafeManagementTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            CafeManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CafeManagementApp()
                }
            }
        }
    }
}

//@Composable
//fun CafeManagementApp() {
//    CafeNavHost()
//}