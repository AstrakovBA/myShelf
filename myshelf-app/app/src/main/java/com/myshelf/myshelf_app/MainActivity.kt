package com.myshelf.myshelf_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myshelf.myshelf_app.presentation.navigation.AppNavigation
import com.myshelf.myshelf_app.presentation.settings.Theme
import com.myshelf.myshelf_app.presentation.viewmodel.SettingsViewModel
import com.myshelf.myshelf_app.presentation.viewmodel.ViewModelFactory
import com.myshelf.myshelf_app.ui.theme.MyShelfTheme
import com.myshelf.myshelf_app.util.LocaleManager
import com.myshelf.myshelf_app.util.StringResources

class MainActivity : ComponentActivity() {

    private val viewModelFactory by lazy {
        ViewModelFactory(applicationContext)
    }

    private val appVersion: String by lazy {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
    }

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleManager.getSavedLanguage(newBase)
        val localizedContext = LocaleManager.wrapContext(newBase, language)
        StringResources.init(localizedContext)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (theme) {
                Theme.Light -> false
                Theme.Dark -> true
                Theme.System -> systemDark
            }

            MyShelfTheme(darkTheme = darkTheme) {
                AppNavigation(
                    viewModelFactory = viewModelFactory,
                    settingsViewModel = settingsViewModel,
                    appVersion = appVersion,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
