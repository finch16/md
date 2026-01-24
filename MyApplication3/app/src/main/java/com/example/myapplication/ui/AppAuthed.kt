package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ContentApi
import com.example.myapplication.model.AppContent
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAuthed(
    baseUrl: String,
    courseSlug: String,
    token: String,
    onLogout: () -> Unit
) {
    val api = remember(baseUrl) { ContentApi(baseUrl) }
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf<LoadState<AppContent>>(LoadState.Loading) }

    // Загружаем контент при старте
    LaunchedEffect(baseUrl, courseSlug, token) {
        state = LoadState.Loading
        state = try {
            val data = api.fetchAppContent(courseSlug, token)
            LoadState.Success(data)
        } catch (t: Throwable) {
            LoadState.Error(t)
        }
    }

    when (val s = state) {
        is LoadState.Loading -> LoadingScreen()

        is LoadState.Error -> ErrorScreen(
            error = s.throwable,
            onRetry = {
                scope.launch {
                    state = LoadState.Loading
                    state = try {
                        val data = api.fetchAppContent(courseSlug, token)
                        LoadState.Success(data)
                    } catch (t: Throwable) {
                        LoadState.Error(t)
                    }
                }
            },
            onLogout = onLogout
        )

        is LoadState.Success -> DrawerScaffold(
            app = s.data,
            onLogout = onLogout
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
    error: Throwable,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Помилка завантаження", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            Text(
                error.message ?: error.toString(),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))

            Row {
                Button(onClick = onRetry) {
                    Text("Спробувати знову")
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(onClick = onLogout) {
                    Text("Вийти")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerScaffold(
    app: AppContent,
    onLogout: () -> Unit
) {

    val firstPageId = remember(app) { app.pages.keys.firstOrNull() }
    val doc = remember(app, firstPageId) { firstPageId?.let { app.pages[it] } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.appTitle) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (doc == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Курс не містить сторінок")
            }
        } else {
            ContentScreen(
                doc = doc,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
