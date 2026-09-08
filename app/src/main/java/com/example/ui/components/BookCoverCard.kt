package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

// Predefined thematic artistic gradient & preset styles for covers
val CoverGradients = listOf(
    // 0. Deep Cosmic Astral Cyan
    listOf(Color(0xFF07111E), Color(0xFF0F2642), Color(0xFF1E4976), Color(0xFF38BDF8)),
    // 1. Royal Celestial Gold & Void
    listOf(Color(0xFF120E04), Color(0xFF2C1F07), Color(0xFF5E420C), Color(0xFFFBBF24)),
    // 2. Neon Aurora & Cyber Violet
    listOf(Color(0xFF0F0826), Color(0xFF261250), Color(0xFF4C1D95), Color(0xFFA78BFA)),
    // 3. Mythic Crimson & Dark Obsidian
    listOf(Color(0xFF1A0507), Color(0xFF3B0C12), Color(0xFF701320), Color(0xFFFB7185)),
    // 4. Mystic Emerald & Ancient Rune
    listOf(Color(0xFF04140F), Color(0xFF0A2B20), Color(0xFF14533D), Color(0xFF34D399)),
    // 5. Velvet Amethyst & Starlight
    listOf(Color(0xFF140824), Color(0xFF2E104D), Color(0xFF581C87), Color(0xFFC084FC))
)

/**
 * Premium Luxury Book / Series Cover Card
 * Displays the artwork/image cleanly, or generative gradients with realistic spine effect and high legibility typography
 */
@Composable
fun BookCoverCard(
    coverUri: String?,
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = LuxuryPrimary,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    height: Dp = 200.dp,
    gradientIndex: Int = 0,
    contentScale: ContentScale = ContentScale.Crop,
    showSpine: Boolean = true
) {
    val gradientColors = CoverGradients[gradientIndex.mod(CoverGradients.size)]
    val context = LocalContext.current
    val isImageUri = !coverUri.isNullOrBlank() && (
            coverUri.startsWith("http://") ||
            coverUri.startsWith("https://") ||
            coverUri.startsWith("content://") ||
            coverUri.startsWith("file://")
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = LuxurySurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.6f), spotColor = LuxuryPrimary.copy(alpha = 0.25f))
            .border(1.2.dp, LuxurySurfaceHighlight, RoundedCornerShape(14.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isImageUri) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover of $title",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )

                // High-End Multi-Stop Dark Vignette Overlay for Title Readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.45f to Color.Black.copy(alpha = 0.2f),
                                0.75f to Color.Black.copy(alpha = 0.75f),
                                1.0f to Color.Black.copy(alpha = 0.95f)
                            )
                        )
                )
            } else {
                // Generative Artistic Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = gradientColors
                            )
                        )
                )

                // Ambient Radial Starburst
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                radius = 250f
                            )
                        )
                )
            }

            // Realistic Book Spine Visual Emboss Line
            if (showSpine) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.CenterStart)
                )
            }

            // Top Badge (e.g. SERIES / NOVEL / VOL 1)
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    color = badgeColor.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(bottomEnd = 10.dp, topStart = 14.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Book / Series Title & Subtitle prominently anchored at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title.ifBlank { "بدون عنوان" },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = LuxuryGold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Modern Interactive Cover Picker with Gallery File Picker, Presets, Crop/Fit Modes & URL Input
 */
@Composable
fun LuxuryCoverPicker(
    selectedCoverUri: String,
    onCoverSelected: (String) -> Unit,
    titleHint: String = "غلاف العمل / الكافر الفني",
    modifier: Modifier = Modifier
) {
    var isCustomUrlExpanded by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var selectedScaleMode by remember { mutableStateOf(ContentScale.Crop) }

    // Native Zero-Permission Android Photo Picker Contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onCoverSelected(uri.toString())
        }
    }

    val samplePresetCovers = listOf(
        "" to "خلفية فلكية (سديم فضائي)",
        "preset:gold" to "خلفية ذهبية (ملكية)",
        "preset:cyber" to "خلفية شفق (سايبر نيون)",
        "preset:crimson" to "خلفية حمم (قرمزي ملحمي)",
        "preset:emerald" to "خلفية غابة (زمرد وسحر)",
        "preset:purple" to "خلفية أثيرية (بنفسج)",
        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop" to "سديم ونجوم",
        "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop" to "قلعة ملحمية",
        "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop" to "بوابة العوالم",
        "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop" to "جبال الشفق"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LuxurySurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, LuxurySurfaceHighlight, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = LuxuryPrimaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = LuxuryPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = titleHint,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LuxuryTextPrimary
                    )
                    Text(
                        text = "اختر من معرض الهاتف أو استعمل أغلفة فنية مصممة",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary
                    )
                }
            }

            if (selectedCoverUri.isNotBlank()) {
                TextButton(
                    onClick = { onCoverSelected("") },
                    colors = ButtonDefaults.textButtonColors(contentColor = LuxuryWarning)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إزالة الغلاف", fontSize = 12.sp)
                }
            }
        }

        // Action Buttons Row (Pick from Phone + Enter URL)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Action: Choose from Phone Gallery
            LuxuryGradientButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                text = "اختيار صورة من الهاتف",
                icon = Icons.Default.PhotoLibrary,
                brush = LuxuryPrimaryGradient,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_cover_from_phone_btn")
            )

            // Secondary: Custom URL toggle
            LuxurySecondaryButton(
                onClick = { isCustomUrlExpanded = !isCustomUrlExpanded },
                text = if (isCustomUrlExpanded) "إغلاق الرابط" else "رابط خارجي",
                icon = Icons.Default.Link,
                modifier = Modifier.testTag("toggle_custom_url_cover_btn")
            )
        }

        // Expandable Custom URL Box
        AnimatedVisibility(
            visible = isCustomUrlExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = { customUrlInput = it },
                    placeholder = { Text("https://example.com/cover.jpg", color = LuxuryTextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryPrimary,
                        unfocusedBorderColor = LuxurySurfaceHighlight,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        focusedContainerColor = LuxurySurface,
                        unfocusedContainerColor = LuxurySurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            onCoverSelected(customUrlInput)
                            isCustomUrlExpanded = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxurySecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تطبيق", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Horizontal Preset Artworks
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "أو اختر قالباً فنياً جاهزاً:",
                style = MaterialTheme.typography.labelMedium,
                color = LuxuryTextMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                samplePresetCovers.forEachIndexed { index, (uri, label) ->
                    val isSelected = selectedCoverUri == uri
                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .height(126.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) LuxuryPrimary else LuxurySurfaceHighlight,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onCoverSelected(uri) }
                    ) {
                        BookCoverCard(
                            coverUri = uri,
                            title = label,
                            width = 86.dp,
                            height = 126.dp,
                            gradientIndex = index
                        )

                        if (isSelected) {
                            Surface(
                                color = LuxuryPrimary,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = LuxuryVoidBackground,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
