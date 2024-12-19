package com.example.museumguide

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.museumguide.screens.MainViewModel
import com.example.museumguide.screens.MuseumsScreen
import com.example.museumguide.screens.ObjectInfoScreen
import com.example.museumguide.screens.ObjectsScreen
import com.example.museumguide.screens.WelcomeScreen
import com.example.museumguide.ui.theme.MuseumGuideTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuseumGuideTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(this)
                }
            }
        }
    }
}

@Composable
fun AppContent(context: Context) {
    var showWelcomeScreen by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        showWelcomeScreen = false
    }

    if (showWelcomeScreen) {
        WelcomeScreen()
    } else {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "museumsList") {
            composable("museumsList") {
                MuseumsScreen(navController)
            }
            composable("objectsList/{museumId}/{museumTitle}") { backStackEntry ->
                val museumId = backStackEntry.arguments?.getString("museumId")?.toInt()
                val museumTitle = backStackEntry.arguments?.getString("museumTitle")!!
                if (museumId != null) {
                    ObjectsScreen(museumId, museumTitle, navController)
                }
            }
            composable("objectInfo/{objectId}/{museumId}") { backStackEntry ->
                val objectId = backStackEntry.arguments?.getString("objectId")?.toInt()
                val museumId = backStackEntry.arguments?.getString("museumId")?.toInt()
                if (objectId != null) {
                    if (museumId != null) {
                        ObjectInfoScreen(museumId, objectId, context)
                    }
                }
            }
        }
    }
}



