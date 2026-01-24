package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.ui.AppFromApi
import com.example.myapplication.ui.AuthActivity
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val repo = remember { AuthRepository(this) }
                var token by remember { mutableStateOf<String?>(null) }
                var checked by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    token = repo.getToken()
                    checked = true
                    if (token == null) {
                        startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                        finish()
                    }
                }

                if (checked && token != null) {
                    AppFromApi(
                        token = token!!,
                        baseUrl = "http://10.0.2.2:5000", //http://192.168.31.211:5000
                        onLogout = {
                            runBlocking { repo.logout() }
                            startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}
