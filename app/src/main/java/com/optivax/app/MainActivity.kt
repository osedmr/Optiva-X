package com.optivax.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.optivax.app.core.Constants
import com.optivax.app.presentation.components.MediaConsentGate
import com.optivax.app.presentation.components.AppDataConsentGate
import com.optivax.app.core.notification.OptivaBannerController
import com.optivax.app.presentation.components.OptivaTopBannerHost
import com.optivax.app.presentation.navigation.OptivaFloatingBottomBar
import com.optivax.app.presentation.navigation.OptivaNavHost
import com.optivax.app.presentation.theme.DeepObsidian
import com.optivax.app.presentation.theme.OptivaXTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Android Activity hosting Edge-To-Edge Cyber Obsidian Theme,
 * Runtime Permission Handlers, Floating Navigation Bar, and Global Cyber Top Banner.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bannerController: OptivaBannerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Constants.TAG, "[MAIN] MainActivity onCreate initialized.")
        enableEdgeToEdge()

        setContent {
            OptivaXTheme {
                OptivaMainApp(bannerController = bannerController)
            }
        }
    }
}

@Composable
fun OptivaMainApp(
    bannerController: OptivaBannerController
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepObsidian
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MediaConsentGate {
                AppDataConsentGate {
                    // Main Navigation Graph
                    OptivaNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Floating Pill Navigation Bar
                    OptivaFloatingBottomBar(
                        navController = navController,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    // Global Animated Cyber Top Notification Banner
                    OptivaTopBannerHost(
                        bannerController = bannerController,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}
