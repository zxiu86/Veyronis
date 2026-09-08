package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.AuroraBackgroundBox
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: VeyronisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MyApplicationTheme {
                    VeyronisApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeyronisApp(viewModel: VeyronisViewModel) {
    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val warnings by viewModel.temporalWarnings.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = LuxurySurface,
                drawerContentColor = LuxuryTextPrimary,
                modifier = Modifier
                    .width(320.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(LuxurySurfaceHighlight, Color.Transparent)
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .size(46.dp)
                                .background(LuxuryPrimaryGradient, RoundedCornerShape(14.dp))
                                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = LuxuryPrimary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LuxuryVoidBackground,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = Strings.get("app_title", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = LuxuryTextPrimary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = Strings.get("app_subtitle", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = LuxuryTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Language Switcher Inside Drawer
                    Surface(
                        color = LuxurySurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LuxurySurfaceHighlight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleLanguage() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = LuxuryPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "اللغة: العربية" else "Language: English",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LuxuryTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                color = LuxuryPrimaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, LuxuryPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "EN" else "عربي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuxuryPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = LuxurySurfaceHighlight)
                Spacer(modifier = Modifier.height(8.dp))

                for (section in AppSection.values()) {
                    val isSelected = section == currentSection
                    NavigationDrawerItem(
                        icon = {
                            val icon = when (section) {
                                AppSection.DASHBOARD -> Icons.Default.Dashboard
                                AppSection.WRITER -> Icons.Default.EditNote
                                AppSection.SETTINGS -> Icons.Default.Settings
                                AppSection.CHARACTERS -> Icons.Default.People
                                AppSection.CODEX -> Icons.Default.AutoStories
                                AppSection.TIMELINE -> Icons.Default.HourglassBottom
                                AppSection.EVENTS -> Icons.Default.Timeline
                                AppSection.GRAPHS -> Icons.Default.Hub
                                AppSection.WORLD_RULES -> Icons.Default.Rule
                                AppSection.SEARCH -> Icons.Default.Search
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = section.getLocalizedTitle(language),
                                tint = if (isSelected) LuxuryPrimary else LuxuryTextSecondary
                            )
                        },
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = section.getLocalizedTitle(language),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) LuxuryTextPrimary else LuxuryTextSecondary
                                )
                                if (section == AppSection.WORLD_RULES && warnings.isNotEmpty()) {
                                    Surface(
                                        color = LuxuryWarning,
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
                            selectedContainerColor = LuxuryPrimaryContainer.copy(alpha = 0.8f),
                            selectedTextColor = LuxuryTextPrimary,
                            selectedIconColor = LuxuryPrimary,
                            unselectedTextColor = LuxuryTextSecondary,
                            unselectedIconColor = LuxuryTextSecondary
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                            .testTag("nav_drawer_item_${section.name.lowercase()}")
                    )
                }
            }
        }
    ) {
        AuroraBackgroundBox {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentSection.getLocalizedTitle(language),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LuxuryTextPrimary,
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
                                    tint = LuxuryTextPrimary
                                )
                            }
                        },
                        actions = {
                            // Language toggle action in top bar
                            IconButton(
                                onClick = { viewModel.toggleLanguage() },
                                modifier = Modifier.testTag("top_language_toggle_button")
                            ) {
                                Surface(
                                    color = LuxurySurfaceElevated,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, LuxurySurfaceHighlight),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "EN" else "ع",
                                            color = LuxuryPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.navigateTo(AppSection.SEARCH) },
                                modifier = Modifier.testTag("top_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Global Search",
                                    tint = LuxuryTextPrimary
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
                                                containerColor = LuxuryWarning,
                                                contentColor = Color.Black
                                            ) {
                                                Text(warnings.size.toString())
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Temporal Warnings",
                                            tint = LuxuryWarning
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = LuxurySurface.copy(alpha = 0.85f)
                        )
                    )
                },
                bottomBar = {
                    // Ultra-Sleek Floating Glass Bottom Bar
                    Surface(
                        color = LuxurySurface.copy(alpha = 0.92f),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        border = BorderStroke(1.dp, LuxurySurfaceHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val footerItems = listOf(
                                Triple(AppSection.DASHBOARD, Icons.Default.Dashboard, Strings.get("nav_dashboard", language)),
                                Triple(AppSection.WRITER, Icons.Default.EditNote, Strings.get("nav_writer", language)),
                                Triple(AppSection.SETTINGS, Icons.Default.Settings, Strings.get("nav_settings", language))
                            )

                            for ((section, icon, label) in footerItems) {
                                val isSelected = currentSection == section
                                val animatedBg by animateColorAsState(
                                    targetValue = if (isSelected) LuxuryPrimaryContainer else Color.Transparent,
                                    animationSpec = tween(durationMillis = 220),
                                    label = "bottom_pill_bg"
                                )
                                val animatedIconColor by animateColorAsState(
                                    targetValue = if (isSelected) LuxuryPrimary else LuxuryTextMuted,
                                    animationSpec = tween(durationMillis = 220),
                                    label = "bottom_icon_color"
                                )
                                val animatedTextColor by animateColorAsState(
                                    targetValue = if (isSelected) LuxuryTextPrimary else LuxuryTextMuted,
                                    animationSpec = tween(durationMillis = 220),
                                    label = "bottom_text_color"
                                )

                                Surface(
                                    color = animatedBg,
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSelected) BorderStroke(1.dp, LuxuryPrimary.copy(alpha = 0.4f)) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.navigateTo(section) }
                                        .padding(horizontal = 4.dp)
                                        .testTag("bottom_nav_${section.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = animatedIconColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = animatedTextColor
                                            )
                                        }
                                    }
                                }
                            }
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
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello, $name!",
        color = LuxuryTextPrimary,
        modifier = modifier
    )
}
