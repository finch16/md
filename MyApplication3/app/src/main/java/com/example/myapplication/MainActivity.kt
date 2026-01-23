package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme

import com.example.myapplication.database.AppDatabase
import com.example.myapplication.entity.User

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DetailedDrawerExample()
            }
        }
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        lifecycleScope.launch {
            val newUser1 = User(name = "Max", photo = "")
            val newUser2 = User(name = "Ivan", photo = "")
            val u1Id = userDao.insert(newUser1)
            val u2Id = userDao.insert(newUser2)

            userDao.update(User(id = u1Id, name = "Max", photo = "photo/${u1Id}"))
            userDao.update(User(id = u2Id, name = "Ivan", photo = "photo/${u2Id}"))

            val allUsers = userDao.getAllUsers()
            for (user in allUsers) {
                println("Пользователь: ${user.name}")
            }
        }
    }
}

@Composable
fun CombinedScreen(
    title: @Composable () -> Unit,
    items: @Composable () -> Unit
) {
    var hasData by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    NavigationDrawerItem(
        label = title,
        selected = false,
        icon = { Icon(
            imageVector =
                if(hasData) { Icons.Filled.KeyboardArrowDown }
                else { Icons.Filled.KeyboardArrowRight },
            contentDescription = null
        ) },
        onClick = {
            scope.launch {
                hasData = !hasData
            }
        }
    )
    Column() {
        if(hasData) {
            items()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedDrawerExample() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val textPositions = remember { mutableMapOf<Int, Int>() }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text("Drawer Title", modifier = Modifier.padding(16.dp))
                    HorizontalDivider()

                    Text("Section 1", modifier = Modifier.padding(16.dp))
                    Column() {
                        NavigationDrawerItem(
                            label = { Text("Item 1") },
                            selected = false,
                            onClick = { scope.launch {
                                scrollState.scrollTo(textPositions[1]?:0)
                                drawerState.close()
                            } }
                        )
                        CombinedScreen(
                            title = { Text("Item 2") }
                        ) {
                            NavigationDrawerItem(
                                label = { Text("Item 3") },
                                selected = false,
                                onClick = { scope.launch {
                                    scrollState.scrollTo(textPositions[10]?:0)
                                    drawerState.close()
                                } }
                            )
                            NavigationDrawerItem(
                                label = { Text("Item 4") },
                                selected = false,
                                onClick = { scope.launch {
                                    scrollState.scrollTo(textPositions[47]?:0)
                                    drawerState.close()
                                } }
                            )
                            /*CombinedScreen(
                                title = { Text("Item 5") }
                            ) {*/
                                NavigationDrawerItem(
                                    label = { Text("Item 6") },
                                    selected = false,
                                    onClick = {  }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Item 7") },
                                    selected = false,
                                    onClick = {  }
                                )
                            //}
                        }
                        NavigationDrawerItem(
                            label = { Text("Item 8") },
                            selected = false,
                            onClick = {  }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Section 2", modifier = Modifier.padding(16.dp))
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        badge = { Text("20") },
                        onClick = {  }
                    )
                    NavigationDrawerItem(
                        label = { Text("Help and feedback") },
                        selected = false,
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        onClick = { },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text("Мій заголовок")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Меню"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Прогрес"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Назва файлу",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[1] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Заголовок 1",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[2] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Розділ 1\n/*купа тексту*/",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[3] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Заголовок 2",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[4] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Розділ 2\n/*купа тексту*/",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[5] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Заголовок 3",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[6] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Розділ 3\n/*купа тексту*/",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[7] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Заголовок 4",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[8] = coordinates.positionInParent().y.toInt()
                    }
                )
                Text(
                    text = "Розділ 4\n/*купа тексту*/",
                    modifier = Modifier.onPlaced{coordinates ->
                        textPositions[9] = coordinates.positionInParent().y.toInt()
                    }
                )
                (10..160).forEach { index ->
                    Text(
                        text = "Контент 2 вашего экрана ${index.toString().padStart(2, '0')}",
                        modifier = Modifier.onPlaced{coordinates ->
                            textPositions[index] = coordinates.positionInParent().y.toInt()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailedDrawerExamplePreview() {
    MyApplicationTheme {
        DetailedDrawerExample()
    }
}

@Composable
fun Card(
    progress: Int,
    title: @Composable () -> Unit
) {
    Column {
        title()
        Text(text = progress.toString())
    }
}

@Composable
fun Cards(
    loads: @Composable () -> Unit,
    notLoads: @Composable () -> Unit
) {
    Column {
        Text(text = "Курси")
        Column {
            loads()
        }
        Text(text = "Інші курси")
        Column {
            notLoads()
        }
    }
}

@Composable
fun CardsNotLoad(
    title: @Composable () -> Unit
) {
    Column {
        title()
        Button(onClick = {}) {
            Text(text = "Завантажити")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Screen2Preview() {
    MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Cards({
                    Card(10) { Text(text = "/*назва курсу 1*/") }
                    Card(17) { Text(text = "/*назва курсу 2*/") }
                }) {
                    CardsNotLoad { Text(text = "/*назва курсу 3*/") }
                    CardsNotLoad { Text(text = "/*назва курсу 4*/") }
                }
            }
        }
    }
}
