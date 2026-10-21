package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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

    val profiles by repository.profiles.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }

    // Sign in fields
    var emailInput by remember { mutableStateOf("aarav.sharma24@gmail.com") }
    var passwordInput by remember { mutableStateOf("study123") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regClass by remember { mutableIntStateOf(10) }
    var regStream by remember { mutableStateOf("Science (PCM)") }
    var regSchool by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val streams = listOf("Science (PCM)", "Science (PCB)", "Commerce", "Humanities", "General Foundation")

    fun triggerEmailAuth() {
        HapticHelper.triggerImpact(haptic)
        errorMessage = null
        if (isRegisterMode) {
            val result = repository.registerStudentWithEmailPassword(
                name = regName,
                email = regEmail,
                pass = regPassword,
                cbseClass = regClass,
                stream = regStream,
                schoolName = regSchool,
                avatarEmoji = "🎓",
                colorHex = 0xFF6366F1
            )
            result.onSuccess {
                onAuthSuccess()
            }.onFailure { e ->
                errorMessage = e.message ?: "Registration failed"
                HapticHelper.triggerImpact(haptic)
            }
        } else {
            val result = repository.authenticateWithEmailPassword(emailInput, passwordInput)
            result.onSuccess {
                onAuthSuccess()
            }.onFailure { e ->
                errorMessage = e.message ?: "Authentication failed"
                HapticHelper.triggerImpact(haptic)
            }
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
                Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
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
            contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Logo
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(LiquidGlowIndigo, LiquidGlowCyan)
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌊", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "StudyFluid",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CBSE NCERT Multi-Student Study Suite",
                        fontSize = 13.sp,
                        color = LiquidGlowCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Authentication Mode Switcher (Sign In vs Register)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidSurfaceElevated)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isRegisterMode) LiquidGlowIndigo else Color.Transparent)
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                isRegisterMode = false
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Student Sign In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isRegisterMode) Color.White else TextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRegisterMode) LiquidGlowIndigo else Color.Transparent)
                            .clickable {
                                HapticHelper.triggerSelection(haptic)
                                isRegisterMode = true
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register Sibling / Profile",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRegisterMode) Color.White else TextMuted
                        )
                    }
                }
            }

            // Quick Student Profile Chips (when in sign in mode)
            if (!isRegisterMode && profiles.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Quick Account Select:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(profiles) { profile ->
                                val isSelected = emailInput.equals(profile.email, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) LiquidSurfaceElevated else LiquidSurfaceGlass)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(profile.avatarColorHex) else LiquidCardBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            HapticHelper.triggerSelection(haptic)
                                            emailInput = profile.email
                                            passwordInput = profile.password
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("quick_profile_${profile.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = profile.avatarEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = profile.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Class ${profile.cbseClass}",
                                                fontSize = 10.sp,
                                                color = Color(profile.avatarColorHex)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Form Fields Card
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
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!isRegisterMode) {
                            // Sign In Mode
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Student Email") },
                                placeholder = { Text("e.g. aarav.sharma24@gmail.com") },
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
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                placeholder = { Text("Enter student password") },
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
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        } else {
                            // Register Mode
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Student Full Name") },
                                placeholder = { Text("e.g. Diya Sharma") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = LiquidGlowCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reg_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Student / Parent Email") },
                                placeholder = { Text("e.g. diya.sharma@gmail.com") },
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
                                    .testTag("input_reg_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Create Password (min 4 chars)") },
                                placeholder = { Text("e.g. diya123") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = LiquidGlowCyan)
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reg_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Column {
                                Text(
                                    text = "Select CBSE Class (1 - 12)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items((1..12).toList()) { cls ->
                                        val isSel = cls == regClass
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) LiquidGlowIndigo else LiquidSurfaceElevated)
                                                .clickable {
                                                    HapticHelper.triggerSelection(haptic)
                                                    regClass = cls
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Cl. $cls",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            if (regClass >= 11) {
                                Column {
                                    Text(
                                        text = "Class 11/12 Academic Stream",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(streams) { stm ->
                                            val isSel = stm == regStream
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) LiquidGlowCyan else LiquidSurfaceElevated)
                                                    .clickable {
                                                        HapticHelper.triggerSelection(haptic)
                                                        regStream = stm
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = stm,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSel) Color.Black else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = regSchool,
                                onValueChange = { regSchool = it },
                                label = { Text("School Name") },
                                placeholder = { Text("e.g. Modern School") },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, tint = LiquidGlowCyan)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        triggerEmailAuth()
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reg_school"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LiquidGlowIndigo,
                                    unfocusedBorderColor = LiquidCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = LiquidGlowRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = { triggerEmailAuth() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag(if (isRegisterMode) "btn_reg_submit" else "btn_auth_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidGlowIndigo,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRegisterMode) "Create Student Profile" else "Sign In to Student Vault",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Divider / Or
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LiquidCardBorder)
                    Text(
                        text = "  OR CONNECT WITH  ",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = LiquidCardBorder)
                }
            }

            // Google Sign-In with Credential Manager
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

            // Quick Demo Explore Button
            item {
                TextButton(
                    onClick = {
                        HapticHelper.triggerSelection(haptic)
                        repository.bypassAuthForDemo()
                        onAuthSuccess()
                    },
                    modifier = Modifier.testTag("btn_guest_launch")
                ) {
                    Text(
                        text = "⚡ Instant Demo: Continue as Aarav (Class 10)",
                        color = LiquidGlowCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
