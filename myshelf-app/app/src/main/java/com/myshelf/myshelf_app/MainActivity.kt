package com.myshelf.myshelf_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.myshelf.myshelf_app.presentation.navigation.AppNavigation
import com.myshelf.myshelf_app.presentation.viewmodel.ViewModelFactory
import com.myshelf.myshelf_app.ui.theme.MyShelfTheme

class MainActivity : ComponentActivity() {

    private val viewModelFactory by lazy {
        ViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyShelfTheme {
                AppNavigation(
                    viewModelFactory = viewModelFactory,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
