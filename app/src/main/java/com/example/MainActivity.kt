package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppSection
import com.example.ui.VeyronisViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: VeyronisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VeyronisApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeyronisApp(viewModel: VeyronisViewModel) {
    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val warnings by viewModel.temporalWarnings.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = VeyronisPanel,
                drawerContentColor = VeyronisTextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = VeyronisPrimaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = VeyronisPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "VEYRONIS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = VeyronisTextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Worldbuilding & Novelist",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyronisTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = VeyronisSurfaceHighlight)
                Spacer(modifier = Modifier.height(8.dp))

                for (section in AppSection.values()) {
                    val isSelected = section == currentSection
                    NavigationDrawerItem(
                        icon = {
                            val icon = when (section) {
                                AppSection.DASHBOARD -> Icons.Default.Dashboard
                                AppSection.WRITER -> Icons.Default.Edit
                                AppSection.CHARACTERS -> Icons.Default.People
                                AppSection.CODEX -> Icons.Default.AutoStories
                                AppSection.TIMELINE -> Icons.Default.HourglassBottom
                                AppSection.EVENTS -> Icons.Default.Timeline
                                AppSection.GRAPHS -> Icons.Default.Hub
                                AppSection.WORLD_RULES -> Icons.Default.Rule
                                AppSection.SEARCH -> Icons.Default.Search
                                AppSection.SETTINGS -> Icons.Default.Settings
                            }
                            Icon(imageVector = icon, contentDescription = section.title)
                        },
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(section.title)
                                if (section == AppSection.WORLD_RULES && warnings.isNotEmpty()) {
                                    Surface(
                                        color = VeyronisWarning,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = warnings.size.toString(),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        selected = isSelected,
                        onClick = {
                            viewModel.navigateTo(section)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = VeyronisPrimaryContainer,
                            selectedTextColor = VeyronisTextPrimary,
                            selectedIconColor = VeyronisPrimary,
                            unselectedTextColor = VeyronisTextSecondary,
                            unselectedIconColor = VeyronisTextSecondary
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("nav_drawer_item_${section.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = VeyronisBackground,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentSection.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = VeyronisTextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppSection.SEARCH) },
                            modifier = Modifier.testTag("top_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Global Search",
                                tint = VeyronisTextPrimary
                            )
                        }
                        if (warnings.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.navigateTo(AppSection.WORLD_RULES) },
                                modifier = Modifier.testTag("top_warning_indicator_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = VeyronisWarning,
                                            contentColor = Color.Black
                                        ) {
                                            Text(warnings.size.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Temporal Warnings",
                                        tint = VeyronisWarning
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = VeyronisBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = VeyronisPanel,
                    contentColor = VeyronisTextPrimary
                ) {
                    val bottomItems = listOf(
                        AppSection.DASHBOARD to Icons.Default.Dashboard,
                        AppSection.WRITER to Icons.Default.Edit,
                        AppSection.CHARACTERS to Icons.Default.People,
                        AppSection.CODEX to Icons.Default.AutoStories,
                        AppSection.GRAPHS to Icons.Default.Hub,
                        AppSection.SETTINGS to Icons.Default.Settings
                    )

                    for ((section, icon) in bottomItems) {
                        val isSelected = currentSection == section
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(section) },
                            icon = {
                                Icon(imageVector = icon, contentDescription = section.title)
                            },
                            label = {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VeyronisPrimary,
                                selectedTextColor = VeyronisPrimary,
                                indicatorColor = VeyronisPrimaryContainer,
                                unselectedIconColor = VeyronisTextMuted,
                                unselectedTextColor = VeyronisTextMuted
                            ),
                            modifier = Modifier.testTag("bottom_nav_${section.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentSection) {
                    AppSection.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppSection.WRITER -> WriterScreen(viewModel = viewModel)
                    AppSection.CHARACTERS -> CharactersScreen(viewModel = viewModel)
                    AppSection.CODEX -> CodexScreen(viewModel = viewModel)
                    AppSection.TIMELINE -> TimelineScreen(viewModel = viewModel)
                    AppSection.EVENTS -> EventsScreen(viewModel = viewModel)
                    AppSection.GRAPHS -> GraphsScreen(viewModel = viewModel)
                    AppSection.WORLD_RULES -> WorldRulesScreen(viewModel = viewModel)
                    AppSection.SEARCH -> GlobalSearchScreen(viewModel = viewModel)
                    AppSection.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

