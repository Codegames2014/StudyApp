package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.local.StudyFluidRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.HapticHelper
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Module 1: Revised Authentication Screen
 * Streamlined, uncluttered login screen strictly following Liquid Glass & Fluid Motion design.
 * Features:
 * - Header: StudyFluid logo & "CBSE NCERT Multi-Student Study Suite"
 * - Glassmorphic email & password sign-in form with primary "Sign In to Student Vault" button
 * - "OR CONNECT WITH" divider and Credential Manager "Sign in with Google" button
 * - Prominent glowing glassmorphic "Demo" button for instant entry into demo student profile
 * - STRICT UI REMOVALS: No sibling cards, no register tab toggle, no small text demo link
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    repository: StudyFluidRepository,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var emailInput by remember { mutableStateOf("aarav.sharma24@gmail.com") }
    var passwordInput by remember { mutableStateOf("study123") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

    fun triggerEmailAuth() {
        HapticHelper.triggerImpact(haptic)
        errorMessage = null
        isSigningIn = true

        val result = repository.authenticateWithEmailPassword(emailInput, passwordInput)
        isSigningIn = false
        result.onSuccess {
            onAuthSuccess()
        }.onFailure { e ->
            errorMessage = e.message ?: "Authentication failed. Please verify credentials."
            HapticHelper.triggerImpact(haptic)
        }
    }

    fun handleGoogleSignIn() {
        HapticHelper.triggerSelection(haptic)
        errorMessage = null
        isGoogleLoading = true

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            errorMessage = "Google Sign-In configuration missing: default_web_client_id not found"
            isGoogleLoading = false
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    repository.onGoogleSignInSuccess(
                        email = user?.email,
                        displayName = user?.displayName,
                        uid = user?.uid ?: "user_${System.currentTimeMillis()}"
                    )
                    isGoogleLoading = false
                    onAuthSuccess()
                } else {
                    errorMessage = "Unexpected credential returned from Google"
                    isGoogleLoading = false
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled: ${e.message}")
                isGoogleLoading = false
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                errorMessage = e.localizedMessage ?: "Google Sign-In failed"
                isGoogleLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryContainerIndigo,
                        LiquidBackground,
                        LiquidSurface
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 36.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Header: StudyFluid Branding Logo & Subtitle
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.25f),
                                        Color(0xFFEC4899).copy(alpha = 0.20f)
                                    )
                                )
                            )
                            .border(
                                1.8.dp,
                                Brush.linearGradient(
                                    listOf(LiquidGlowIndigo, LiquidGlowPink, LiquidGlowCyan)
                                ),
                                CircleShape
                            )
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.flow_cbse_logo),
                            contentDescription = "FlowCBSE Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "FlowCBSE",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.6.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CBSE NCERT Multi-Student Study Suite",
                        fontSize = 13.sp,
                        color = LiquidGlowCyan,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                }
            }

            // 2. Core Sign-In Section inside Glassmorphic Container
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_card"),
                    glowAccentColor = LiquidGlowIndigo
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Student Sign In",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Student Email") },
                            placeholder = { Text("aarav.sharma24@gmail.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = LiquidGlowCyan)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_email"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LiquidGlowIndigo,
                                unfocusedBorderColor = LiquidCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = LiquidGlowCyan,
                                unfocusedLabelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Enter password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = LiquidGlowCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    triggerEmailAuth()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_auth_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LiquidGlowIndigo,
                                unfocusedBorderColor = LiquidCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = LiquidGlowCyan,
                                unfocusedLabelColor = TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (errorMessage != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiquidGlowRose.copy(alpha = 0.15f))
                                    .border(1.dp, LiquidGlowRose.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = LiquidGlowRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = LiquidGlowRose,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Primary Sign-In Button
                        Button(
                            onClick = { triggerEmailAuth() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_auth_submit"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidGlowIndigo,
                                contentColor = Color.White
                            )
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In to Student Vault",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Divider: OR CONNECT WITH
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LiquidCardBorder)
                    Text(
                        text = "  OR CONNECT WITH  ",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LiquidCardBorder)
                }
            }

            // Google Sign-In Button
            item {
                Button(
                    onClick = { handleGoogleSignIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_google_signin"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGoogleLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    )
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1F2937)
                        )
                    } else {
                        Text(text = "🌐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }
            }

            // 3. Dedicated Demo Button: Prominent, Glowing Glassmorphic Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xD916161A), // #16161A with 85% opacity
                                    Color(0xF01A1829)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    LiquidGlowIndigo,
                                    LiquidGlowCyan,
                                    LiquidGlowIndigo
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            HapticHelper.triggerImpact(haptic)
                            repository.bypassAuthForDemo()
                            onAuthSuccess()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("btn_demo_vault")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                LiquidGlowIndigo.copy(alpha = 0.35f),
                                                LiquidGlowCyan.copy(alpha = 0.25f)
                                            )
                                        )
                                    )
                                    .border(1.dp, LiquidGlowCyan.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⚡", fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Demo",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LiquidGlowIndigo.copy(alpha = 0.3f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "INSTANT ACCESS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LiquidGlowCyan
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Explore pre-configured CBSE Class 10 student vault",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Enter Demo",
                            tint = LiquidGlowCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
