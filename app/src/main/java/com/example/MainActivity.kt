package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.romresigner.ui.MainScreen
import com.example.romresigner.ui.RomResignerViewModel
import com.example.ui.theme.RomResignerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RomResignerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RomResignerTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
