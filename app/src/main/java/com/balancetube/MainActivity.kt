package com.balancetube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.balancetube.ui.navigation.NavGraph
import com.balancetube.ui.navigation.Screen
import com.balancetube.ui.theme.BalanceTubeTheme
import com.balancetube.util.AuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BalanceTubeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (authManager.isSignedIn()) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(
                        navController = navController,
                        authManager = authManager,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
