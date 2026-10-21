package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.local.StudyFluidRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainNavigation
import com.example.ui.theme.LiquidBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var repository: StudyFluidRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = StudyFluidRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LiquidBackground
                ) {
                    val isAuthenticated by repository.isAuthenticated.collectAsState()
                    if (isAuthenticated) {
                        MainNavigation(repository = repository)
                    } else {
                        AuthScreen(
                            repository = repository,
                            onAuthSuccess = { }
                        )
                    }
                }
            }
        }
    }
}
