package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.util.Translations

@Composable
fun LoginScreen(
    state: UiState,
    onLogin: (identifier: String, pass: String) -> Unit,
    onSignUp: ((email: String, pass: String, fullName: String, phone: String?) -> Unit)? = null,
    onResendConfirmation: ((email: String) -> Unit)? = null,
    onUpdateSupabaseConfig: (url: String, key: String) -> Unit,
    onCheckServer: () -> Unit,
    onClearMessages: () -> Unit
) {
    val lang = state.language

    // ---------------------------------------------------------
    // LOGIN / REGISTER TAB
    // ---------------------------------------------------------
    var selectedTab by remember { mutableIntStateOf(0) }

    // ---------------------------------------------------------
    // LOGIN INPUTS
    // ---------------------------------------------------------
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // SIGN UP INPUTS
    // ---------------------------------------------------------
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupPasswordVisible by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // SERVER CONFIG DIALOG
    // ---------------------------------------------------------
    var showConfigDialog by remember { mutableStateOf(false) }

    var tempUrl by remember(state.supabaseUrl) {
        mutableStateOf(state.supabaseUrl)
    }

    var tempKey by remember(state.supabaseKey) {
        mutableStateOf(state.supabaseKey)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("login_screen")
    ) {

        // -----------------------------------------------------
        // SERVER SETTINGS BUTTON
        // -----------------------------------------------------
        IconButton(
            onClick = {
                showConfigDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .testTag("server_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "সার্ভার সেটিংস",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // -----------------------------------------------------
        // MAIN CONTENT
        // -----------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            // -------------------------------------------------
            // LOGO
            // -------------------------------------------------
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SF",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------
            // APP TITLE
            // -------------------------------------------------
            Text(
                text = Translations.getString("app_title", lang),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Translations.getString("login_sub", lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------
            // SUPABASE STATUS
            // -------------------------------------------------
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isSupabaseConnected) {
                        SuccessGreenContainer
                    } else {
                        ExpenseRedContainer
                    }
                ),
                modifier = Modifier
                    .clickable {
                        onCheckServer()
                    }
                    .testTag("supabase_status_badge")
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isSupabaseConnected) {
                                    SuccessGreen
                                } else {
                                    ExpenseRed
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (state.isSupabaseConnected) {
                            Translations.getString(
                                "supabase_cloud_connected",
                                lang
                            )
                        } else {
                            Translations.getString(
                                "supabase_cloud_error",
                                lang
                            )
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isSupabaseConnected) {
                            SuccessGreen
                        } else {
                            ExpenseRed
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (state.isSupabaseConnected) {
                            Icons.Default.CloudDone
                        } else {
                            Icons.Default.CloudOff
                        },
                        contentDescription = null,
                        tint = if (state.isSupabaseConnected) {
                            SuccessGreen
                        } else {
                            ExpenseRed
                        },
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------
            // SUCCESS MESSAGE
            // -------------------------------------------------
            if (!state.authSuccessMessage.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_success_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreenContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = state.authSuccessMessage,
                            color = SuccessGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------
            // ERROR MESSAGE
            // -------------------------------------------------
            if (!state.errorMessage.isNullOrEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_error_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ExpenseRedContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = state.errorMessage,
                            color = ExpenseRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )

                        // Email confirmation check
                        val isEmailUnconfirmed =
                            state.errorMessage.contains("ভেরিফাই") ||
                                    state.errorMessage.contains(
                                        "confirm",
                                        ignoreCase = true
                                    )

                        if (isEmailUnconfirmed) {

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Button(
                                onClick = {

                                    val targetEmail =
                                        if (selectedTab == 0) {
                                            loginIdentifier.trim()
                                        } else {
                                            signupEmail.trim()
                                        }

                                    if (targetEmail.contains("@")) {
                                        onResendConfirmation?.invoke(
                                            targetEmail
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExpenseRed
                                ),
                                modifier = Modifier.testTag(
                                    "resend_email_button"
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(6.dp)
                                )

                                Text(
                                    text = Translations.getString(
                                        "resend_email_btn",
                                        lang
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // -------------------------------------------------
            // LOGIN / REGISTER CARD
            // -------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column {

                    // -------------------------------------------------
                    // TABS
                    // -------------------------------------------------
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryBlue
                    ) {

                        // LOGIN TAB
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                onClearMessages()
                            },
                            text = {
                                Text(
                                    text = Translations.getString(
                                        "login_tab",
                                        lang
                                    ),
                                    fontWeight = if (selectedTab == 0) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag(
                                "tab_login"
                            )
                        )

                        // REGISTER TAB
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                onClearMessages()
                            },
                            text = {
                                Text(
                                    text = Translations.getString(
                                        "signup_tab",
                                        lang
                                    ),
                                    fontWeight = if (selectedTab == 1) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag(
                                "tab_signup"
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // =================================================
                        // LOGIN FORM
                        // =================================================
                        if (selectedTab == 0) {

                            OutlinedTextField(
                                value = loginIdentifier,
                                onValueChange = {
                                    loginIdentifier = it
                                },
                                label = {
                                    Text(
                                        Translations.getString(
                                            "email_phone",
                                            lang
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "login_identifier_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                },
                                label = {
                                    Text(
                                        Translations.getString(
                                            "password",
                                            lang
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            loginPasswordVisible =
                                                !loginPasswordVisible
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                if (loginPasswordVisible) {
                                                    Icons.Default.Visibility
                                                } else {
                                                    Icons.Default.VisibilityOff
                                                },
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation =
                                    if (loginPasswordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "login_password_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Button(
                                onClick = {
                                    if (
                                        loginIdentifier.isNotBlank() &&
                                        loginPassword.isNotBlank()
                                    ) {
                                        onLogin(
                                            loginIdentifier.trim(),
                                            loginPassword
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !state.isLoading
                            ) {

                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = Translations.getString(
                                            "login",
                                            lang
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            // Quick resend
                            TextButton(
                                onClick = {
                                    if (loginIdentifier.contains("@")) {
                                        onResendConfirmation?.invoke(
                                            loginIdentifier.trim()
                                        )
                                    }
                                },
                                modifier = Modifier.align(
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                Text(
                                    text = "ইমেইল ভেরিফিকেশন লিংক পাননি? পুনরায় পাঠান",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                        } else {

                            // =================================================
                            // SIGN UP FORM
                            // =================================================

                            OutlinedTextField(
                                value = signupName,
                                onValueChange = {
                                    signupName = it
                                },
                                label = {
                                    Text(
                                        Translations.getString(
                                            "full_name",
                                            lang
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "signup_name_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = signupEmail,
                                onValueChange = {
                                    signupEmail = it
                                },
                                label = {
                                    Text(
                                        "ইমেইল (Supabase Account)"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "signup_email_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = signupPassword,
                                onValueChange = {
                                    signupPassword = it
                                },
                                label = {
                                    Text(
                                        "${Translations.getString(
                                            "password",
                                            lang
                                        )} (কমপক্ষে ৬ অক্ষর)"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            signupPasswordVisible =
                                                !signupPasswordVisible
                                        }
                                    ) {
                                        Icon(
                                            imageVector =
                                                if (signupPasswordVisible) {
                                                    Icons.Default.Visibility
                                                } else {
                                                    Icons.Default.VisibilityOff
                                                },
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation =
                                    if (signupPasswordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "signup_password_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = signupPhone,
                                onValueChange = {
                                    signupPhone = it
                                },
                                label = {
                                    Text(
                                        Translations.getString(
                                            "phone_optional",
                                            lang
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(
                                        "signup_phone_input"
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Button(
                                onClick = {

                                    if (
                                        signupEmail.isNotBlank() &&
                                        signupPassword.isNotBlank() &&
                                        signupName.isNotBlank()
                                    ) {
                                        onSignUp?.invoke(
                                            signupEmail.trim(),
                                            signupPassword,
                                            signupName.trim(),
                                            signupPhone
                                                .trim()
                                                .takeIf {
                                                    it.isNotBlank()
                                                }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("signup_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !state.isLoading
                            ) {

                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = Translations.getString(
                                            "signup_btn",
                                            lang
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // =============================================================
    // SERVER CONFIG DIALOG
    // =============================================================
    if (showConfigDialog) {

        AlertDialog(
            onDismissRequest = {
                showConfigDialog = false
            },

            title = {
                Text(
                    text = Translations.getString(
                        "server_settings",
                        lang
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Supabase সংযোগ সেটিংস:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = {
                            tempUrl = it
                        },
                        label = {
                            Text("Supabase URL")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = {
                            tempKey = it
                        },
                        label = {
                            Text("Supabase API Key")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSupabaseConfig(
                            tempUrl,
                            tempKey
                        )
                        showConfigDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        Translations.getString(
                            "done",
                            lang
                        )
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showConfigDialog = false
                    }
                ) {
                    Text(
                        Translations.getString(
                            "cancel",
                            lang
                        )
                    )
                }
            }
        )
    }
}