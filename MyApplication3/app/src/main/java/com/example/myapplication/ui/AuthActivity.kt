package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.MainActivity
import com.example.myapplication.data.AuthApi
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AuthScreen(
                    baseUrl = "http://10.0.2.2:5000",
                    onAuthed = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthScreen(
    baseUrl: String,
    onAuthed: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { AuthRepository(ctx) }
    val api = remember(baseUrl) { AuthApi(baseUrl) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(Modifier.padding(16.dp).fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Вхід", style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                error = null
                                loading = true
                                try {
                                    val token = api.login(username.trim(), password)
                                    repo.saveToken(token)
                                    onAuthed()
                                } catch (t: Throwable) {
                                    error = t.message ?: t.toString()
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Увійти")
                    }
                }
            }
        }
    }
}
