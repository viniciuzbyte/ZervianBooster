package com.zervian.booster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zervian.booster.ui.navigation.ZervianNavHost
import com.zervian.booster.ui.theme.ZervianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZervianTheme {
                ZervianNavHost()
            }
        }
    }
}
