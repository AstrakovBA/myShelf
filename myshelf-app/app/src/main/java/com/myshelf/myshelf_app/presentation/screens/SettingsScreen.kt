package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.data.local.entity.UserLocal
import com.myshelf.myshelf_app.presentation.components.GuestModeTopBarTitle
import com.myshelf.myshelf_app.presentation.components.SettingsListTile
import com.myshelf.myshelf_app.presentation.components.guestModeTopAppBarColors
import com.myshelf.myshelf_app.presentation.components.SettingsSectionDivider
import com.myshelf.myshelf_app.presentation.components.SettingsSectionHeader
import com.myshelf.myshelf_app.presentation.settings.Theme
import com.myshelf.myshelf_app.presentation.viewmodel.AuthViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.SettingsViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    isGuestMode: Boolean = false,
    appVersion: String,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme by settingsViewModel.theme.collectAsStateWithLifecycle()
    val language by settingsViewModel.language.collectAsStateWithLifecycle()
    val isSyncing by settingsViewModel.isSyncing.collectAsStateWithLifecycle()
    val isClearingCache by settingsViewModel.isClearingCache.collectAsStateWithLifecycle()
    val isChangingPassword by settingsViewModel.isChangingPassword.collectAsStateWithLifecycle()
    val errorMessage by settingsViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by settingsViewModel.successMessage.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageReloadDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf<String?>(null) }

    val activity = LocalContext.current as ComponentActivity

    LaunchedEffect(Unit) {
        authViewModel.refreshCurrentUser()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearSuccessMessage()
            if (showPasswordDialog) showPasswordDialog = false
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            selectedTheme = theme,
            onThemeSelected = {
                settingsViewModel.setTheme(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            selectedLanguage = language,
            onLanguageSelected = { selected ->
                showLanguageDialog = false
                if (!selected.equals(language, ignoreCase = true)) {
                    pendingLanguage = selected
                    showLanguageReloadDialog = true
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showLanguageReloadDialog && pendingLanguage != null) {
        AlertDialog(
            onDismissRequest = {
                showLanguageReloadDialog = false
                pendingLanguage = null
            },
            title = { Text(stringResource(R.string.settings_language_reload_title)) },
            text = { Text(stringResource(R.string.settings_language_reload_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lang = pendingLanguage!!
                        showLanguageReloadDialog = false
                        pendingLanguage = null
                        settingsViewModel.applyLanguage(lang, activity)
                    }
                ) {
                    Text(stringResource(R.string.settings_language_reload_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLanguageReloadDialog = false
                        pendingLanguage = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            isLoading = isChangingPassword,
            onConfirm = { old, new, confirm ->
                settingsViewModel.changePassword(old, new, confirm)
            },
            onDismiss = { showPasswordDialog = false }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.settings_clear_cache)) },
            text = { Text(stringResource(R.string.settings_clear_cache_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        settingsViewModel.clearCache()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_clear_cache),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout)) },
            text = {
                Text(
                    stringResource(
                        if (isGuestMode) R.string.guest_logout_confirmation
                        else R.string.settings_logout_confirmation
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.settings_logout),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    GuestModeTopBarTitle(
                        title = stringResource(R.string.nav_settings),
                        isGuestMode = isGuestMode
                    )
                },
                colors = guestModeTopAppBarColors(isGuestMode)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (isGuestMode) {
                Text(
                    text = stringResource(R.string.guest_mode_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            SettingsSectionHeader(title = stringResource(R.string.settings_section_appearance))

            SettingsListTile(
                title = stringResource(R.string.settings_theme),
                trailingText = themeDisplayName(theme),
                leadingIcon = Icons.Default.Palette,
                onClick = { showThemeDialog = true }
            )

            SettingsListTile(
                title = stringResource(R.string.settings_language),
                trailingText = languageDisplayName(language = language),
                leadingIcon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )

            SettingsSectionDivider()

            if (!isGuestMode) {
                SettingsSectionHeader(title = stringResource(R.string.settings_section_account))

                SettingsListTile(
                    title = currentUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.settings_profile),
                    subtitle = currentUser?.email,
                    leadingContent = {
                        UserAvatar(user = currentUser)
                    },
                    showChevron = true,
                    onClick = onProfileClick
                )

                SettingsListTile(
                    title = stringResource(R.string.settings_change_password),
                    leadingIcon = Icons.Default.Lock,
                    showChevron = true,
                    onClick = { showPasswordDialog = true }
                )

                SettingsSectionDivider()
            } else {
                SettingsSectionHeader(title = stringResource(R.string.settings_section_account))

                SettingsListTile(
                    title = currentUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.guest_display_name),
                    subtitle = stringResource(R.string.guest_mode_warning),
                    leadingContent = {
                        UserAvatar(user = currentUser)
                    },
                    showChevron = false,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            SettingsSectionHeader(title = stringResource(R.string.settings_section_data))

            SettingsListTile(
                title = stringResource(R.string.settings_sync),
                leadingIcon = Icons.Default.Sync,
                enabled = !isSyncing,
                showChevron = false,
                onClick = { settingsViewModel.syncAll() }
            )

            if (isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            SettingsListTile(
                title = stringResource(R.string.settings_clear_cache),
                leadingIcon = Icons.Default.Delete,
                enabled = !isClearingCache,
                showChevron = false,
                onClick = { showClearCacheDialog = true }
            )

            SettingsSectionDivider()

            SettingsSectionHeader(title = stringResource(R.string.settings_section_app))

            SettingsListTile(
                title = stringResource(R.string.settings_about),
                subtitle = stringResource(R.string.settings_version, appVersion),
                leadingIcon = Icons.Default.Info,
                showChevron = false
            )

            SettingsListTile(
                title = stringResource(R.string.settings_logout),
                leadingIcon = Icons.AutoMirrored.Filled.Logout,
                titleColor = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error,
                showChevron = false,
                onClick = { showLogoutDialog = true }
            )
        }
    }
}

@Composable
private fun UserAvatar(user: UserLocal?) {
    val avatarUrl = user?.avatarUrl
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun themeDisplayName(theme: Theme): String = when (theme) {
    Theme.Light -> stringResource(R.string.settings_theme_light)
    Theme.Dark -> stringResource(R.string.settings_theme_dark)
    Theme.System -> stringResource(R.string.settings_theme_system)
}

@Composable
private fun languageDisplayName(language: String): String = when (language.lowercase()) {
    "ru" -> stringResource(R.string.settings_language_russian)
    "en" -> stringResource(R.string.settings_language_english)
    else -> language
}

@Composable
private fun ThemePickerDialog(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme_dialog_title)) },
        text = {
            Column {
                ThemeOption(
                    label = stringResource(R.string.settings_theme_light),
                    selected = selectedTheme == Theme.Light,
                    onClick = { onThemeSelected(Theme.Light) }
                )
                ThemeOption(
                    label = stringResource(R.string.settings_theme_dark),
                    selected = selectedTheme == Theme.Dark,
                    onClick = { onThemeSelected(Theme.Dark) }
                )
                ThemeOption(
                    label = stringResource(R.string.settings_theme_system),
                    selected = selectedTheme == Theme.System,
                    onClick = { onThemeSelected(Theme.System) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SettingsListTile(
        title = label,
        showChevron = false,
        onClick = onClick,
        leadingContent = {
            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@Composable
private fun LanguagePickerDialog(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column {
                ThemeOption(
                    label = stringResource(R.string.settings_language_russian),
                    selected = selectedLanguage.equals("ru", ignoreCase = true),
                    onClick = { onLanguageSelected("ru") }
                )
                ThemeOption(
                    label = stringResource(R.string.settings_language_english),
                    selected = selectedLanguage.equals("en", ignoreCase = true),
                    onClick = { onLanguageSelected("en") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_change_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text(stringResource(R.string.settings_old_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.settings_new_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.settings_confirm_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(oldPassword, newPassword, confirmPassword) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
