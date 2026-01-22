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
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DetailedDrawerExample()
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
                    modifier = Modifier.padding(horizontal = 16.dp)
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
            /*Text(
                text =
"""Контент вашего экрана 01
Контент вашего экрана 02
Контент вашего экрана 03
Контент вашего экрана 04
Контент вашего экрана 05
Контент вашего экрана 06
Контент вашего экрана 07
Контент вашего экрана 08
Контент вашего экрана 09
Контент вашего экрана 10
Контент вашего экрана 11
Контент вашего экрана 12
Контент вашего экрана 13
Контент вашего экрана 14
Контент вашего экрана 15
Контент вашего экрана 16
Контент вашего экрана 17
Контент вашего экрана 18
Контент вашего экрана 19
Контент вашего экрана 20
Контент вашего экрана 21
Контент вашего экрана 22
Контент вашего экрана 23
Контент вашего экрана 24
Контент вашего экрана 25
Контент вашего экрана 26
Контент вашего экрана 27
Контент вашего экрана 28
Контент вашего экрана 29
Контент вашего экрана 30
Контент вашего экрана 31
Контент вашего экрана 32
Контент вашего экрана 33
Контент вашего экрана 34
Контент вашего экрана 35
Контент вашего экрана 36
Контент вашего экрана 37
Контент вашего экрана 38
Контент вашего экрана 39
Контент вашего экрана 40
Контент вашего экрана 41
Контент вашего экрана 42
Контент вашего экрана 43
Контент вашего экрана 44
Контент вашего экрана 45
Контент вашего экрана 46
Контент вашего экрана 47
Контент вашего экрана 48
Контент вашего экрана 49
Контент вашего экрана 50
Контент вашего экрана 51
Контент вашего экрана 52
Контент вашего экрана 53
Контент вашего экрана 54
Контент вашего экрана 55
Контент вашего экрана 56
Контент вашего экрана 57
Контент вашего экрана 58
Контент вашего экрана 59
Контент вашего экрана 60""".trimMargin(),
                modifier = Modifier.padding(innerPadding)
                    .verticalScroll(scrollState)
            )*/
            Column(
                modifier = Modifier.padding(innerPadding)
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