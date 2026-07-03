package com.nobody.valurex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nobody.valurex.ui.navigation.ValurexNavGraph
import com.nobody.valurex.ui.theme.ValurexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialRoute = intent?.getStringExtra("navigate_to") 
            ?: intent?.getStringExtra("open_screen") 
            ?: "home"
        setContent {
            ValurexTheme { ValurexNavGraph(initialRoute = initialRoute) }
        }
    }
}
