package com.example.potholedetection_v1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.potholedetection_v1.screens.MainScreen
import com.example.potholedetection_v1.ui.theme.PotholeDetection_v1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PotholeDetection_v1Theme {
                val viewModel: PotholeViewModel = viewModel()
                MainScreen()
            }
        }
    }
}
