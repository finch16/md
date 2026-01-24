package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.ContentApi
import com.example.myapplication.data.ContentRepository
import com.example.myapplication.data.CoursesApi
import com.example.myapplication.data.CoursesRepository
import com.example.myapplication.model.AppContent
import com.example.myapplication.model.CourseItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFromApi(
    token: String,
    baseUrl: String = "http://10.0.2.2:5000",
    onLogout: () -> Unit
) {
    val contentRepo = remember(baseUrl) { ContentRepository(ContentApi(baseUrl)) }
    val coursesRepo = remember(baseUrl) { CoursesRepository(CoursesApi(baseUrl)) }
    val scope = rememberCoroutineScope()

    var coursesState: LoadState<List<CourseItem>> by remember {
        mutableStateOf(LoadState.Loading)
    }

    var selectedCourseSlug by remember { mutableStateOf<String?>(null) }

    var contentState: LoadState<AppContent> by remember {
        mutableStateOf(LoadState.Loading)
    }

    LaunchedEffect(baseUrl, token) {
        coursesState = LoadState.Loading
        coursesState = try {
            val list = coursesRepo.getCourses(token)
            LoadState.Success(list)
        } catch (t: Throwable) {
            LoadState.Error(t)
        }
    }

    LaunchedEffect(selectedCourseSlug, token, baseUrl) {
        val slug = selectedCourseSlug ?: return@LaunchedEffect
        contentState = LoadState.Loading
        contentState = try {
            val data = contentRepo.getAppContent(
                courseSlug = slug,
                token = token,
                forceRefresh = true
            )
            LoadState.Success(data)
        } catch (t: Throwable) {
            LoadState.Error(t)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Курси") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (selectedCourseSlug == null) {
                when (val s = coursesState) {
                    is LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    is LoadState.Error -> Column(Modifier.padding(16.dp)) {
                        Text("Не вдалося завантажити курси", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(s.throwable.message ?: s.throwable.toString())
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            scope.launch {
                                coursesState = LoadState.Loading
                                coursesState = try {
                                    LoadState.Success(coursesRepo.getCourses(token))
                                } catch (t: Throwable) {
                                    LoadState.Error(t)
                                }
                            }
                        }) { Text("Спробувати знову") }
                    }

                    is LoadState.Success -> {
                        val courses = s.data
                        if (courses.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Немає доступних курсів")
                            }
                        } else {

                            LazyColumn(Modifier.fillMaxSize()) {
                                items(courses) { c ->
                                    ListItem(
                                        headlineContent = { Text(c.title) },
                                        supportingContent = { Text(c.slug) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedCourseSlug = c.slug }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                return@Box
            }

            when (val s = contentState) {
                is LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                is LoadState.Error -> Column(Modifier.padding(16.dp)) {
                    Text("Не вдалося завантажити контент", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(s.throwable.message ?: s.throwable.toString())
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Button(onClick = {
                            scope.launch {
                                val slug = selectedCourseSlug ?: return@launch
                                contentState = LoadState.Loading
                                contentState = try {
                                    LoadState.Success(
                                        contentRepo.getAppContent(
                                            courseSlug = slug,
                                            token = token,
                                            forceRefresh = true
                                        )
                                    )
                                } catch (t: Throwable) {
                                    LoadState.Error(t)
                                }
                            }
                        }) { Text("Повторити") }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(onClick = { selectedCourseSlug = null }) {
                            Text("Змінити курс")
                        }
                    }
                }

                is LoadState.Success -> {
                    DrawerCourseApp(
                        app = s.data,
                        onExitCourse = { selectedCourseSlug = null }
                    )
                }
            }
        }
    }
}
