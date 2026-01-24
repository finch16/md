package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.AppContent
import com.example.myapplication.model.MenuNode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerCourseApp(
    app: AppContent,
    onExitCourse: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentPageId by remember(app) {
        mutableStateOf(firstLeafPageId(app.menu) ?: app.pages.keys.firstOrNull())
    }

    val currentDoc = remember(app, currentPageId) { currentPageId?.let { app.pages[it] } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(app.appTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    DrawerMenuTree(
                        nodes = app.menu,
                        currentPageId = currentPageId,
                        onSelectPage = { pageId ->
                            currentPageId = pageId
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text(app.appTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = onExitCourse) { Text("Вийти") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (currentDoc == null) {
                    Text("Сторінку не знайдено")
                } else {
                    ContentScreen(
                        doc = currentDoc,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuTree(
    nodes: List<MenuNode>,
    currentPageId: String?,
    onSelectPage: (String) -> Unit
) {
    nodes.forEach { node ->
        DrawerMenuNode(
            node = node,
            currentPageId = currentPageId,
            onSelectPage = onSelectPage
        )
    }
}

@Composable
private fun DrawerMenuNode(
    node: MenuNode,
    currentPageId: String?,
    onSelectPage: (String) -> Unit
) {
    val children = node.children ?: emptyList()
    var expanded by remember { mutableStateOf(false) }

    if (children.isNotEmpty()) {
        NavigationDrawerItem(
            label = { Text(node.title) },
            selected = false,
            icon = {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            },
            onClick = { expanded = !expanded }
        )
        if (expanded) {
            Column(Modifier.padding(start = 16.dp)) {
                children.forEach { child ->
                    DrawerMenuNode(child, currentPageId, onSelectPage)
                }
            }
        }
    } else {
        val pid = node.pageId
        NavigationDrawerItem(
            label = { Text(node.title) },
            selected = (pid != null && pid == currentPageId),
            onClick = { if (pid != null) onSelectPage(pid) },
            modifier = Modifier.alpha(if (pid != null) 1f else 0.5f)
        )
    }
}

private fun firstLeafPageId(nodes: List<MenuNode>): String? {
    for (n in nodes) {
        val ch = n.children ?: emptyList()
        if (ch.isNotEmpty()) {
            val v = firstLeafPageId(ch)
            if (v != null) return v
        } else if (n.pageId != null) {
            return n.pageId
        }
    }
    return null
}
