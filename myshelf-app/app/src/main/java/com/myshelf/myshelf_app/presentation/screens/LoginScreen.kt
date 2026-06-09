package com.myshelf.myshelf_app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myshelf.myshelf_app.R
import com.myshelf.myshelf_app.presentation.auth.AuthState
import com.myshelf.myshelf_app.presentation.viewmodel.AuthViewModel
import com.myshelf.myshelf_app.util.AuthField
import com.myshelf.myshelf_app.util.StringResources
import com.myshelf.myshelf_app.util.Validator

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        val state = authState
        if (state is AuthState.Error) {
            snackbarHostState.showSnackbar(state.message)
            viewModel.clearAuthError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text(stringResource(R.string.field_email)) },
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text(stringResource(R.string.field_password)) },
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submitLogin(
                                email = email,
                                password = password,
                                isLoading = isLoading,
                                onValidationError = { emailErr, passErr ->
                                    emailError = emailErr
                                    passwordError = passErr
                                },
                                onSubmit = { viewModel.login(email.trim(), password) }
                            )
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )

                Button(
                    onClick = {
                        submitLogin(
                            email = email,
                            password = password,
                            isLoading = isLoading,
                            onValidationError = { emailErr, passErr ->
                                emailError = emailErr
                                passwordError = passErr
                            },
                            onSubmit = { viewModel.login(email.trim(), password) }
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.login_button))
                }

                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .clickable(enabled = !isLoading, onClick = onNavigateToRegister)
                )

                OutlinedButton(
                    onClick = { viewModel.loginAsGuest() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(stringResource(R.string.login_try_guest))
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

private fun submitLogin(
    email: String,
    password: String,
    isLoading: Boolean,
    onValidationError: (emailError: String?, passwordError: String?) -> Unit,
    onSubmit: () -> Unit
) {
    if (isLoading) return

    val errors = Validator.validateLoginInput(email, password)
    if (errors.isNotEmpty()) {
        var emailErr: String? = null
        var passErr: String? = null
        errors.forEach { error ->
            when (error.field) {
                AuthField.EMAIL -> emailErr = StringResources.getString(error.messageRes)
                AuthField.PASSWORD -> passErr = StringResources.getString(error.messageRes)
                AuthField.DISPLAY_NAME -> Unit
            }
        }
        onValidationError(emailErr, passErr)
        return
    }

    onSubmit()
}
