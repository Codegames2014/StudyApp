package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import com.example.data.local.StudyFluidRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FlowLoadingScreen
import com.example.ui.screens.MainNavigation
import com.example.ui.screens.StudentSurveyScreen
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
                    var showLoadingScreen by remember { mutableStateOf(true) }

                    AnimatedContent(
                        targetState = showLoadingScreen,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(350)) + scaleIn(
                                initialScale = 0.95f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )).togetherWith(
                                fadeOut(animationSpec = tween(250)) + scaleOut(
                                    targetScale = 1.05f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            )
                        },
                        label = "StateDrivenLoadingToAuth"
                    ) { isLoading ->
                        if (isLoading) {
                            FlowLoadingScreen(
                                targetGrade = repository.activeProfile.cbseClass,
                                studentName = repository.activeProfile.name,
                                onLoadingComplete = { showLoadingScreen = false }
                            )
                        } else {
                            val isAuthenticated by repository.isAuthenticated.collectAsState()
                            if (isAuthenticated) {
                                val activeProfile = repository.activeProfile
                                if (!activeProfile.isOnboardingCompleted) {
                                    StudentSurveyScreen(
                                        repository = repository,
                                        onSurveyCompleted = {
                                            repository.markOnboardingCompleted(activeProfile.id)
                                        }
                                    )
                                } else {
                                    MainNavigation(repository = repository)
                                }
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
    }
}

