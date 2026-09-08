package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Aurora Background Canvas: Ambient soft-glowing aurora lights behind dark canvas
 */
@Composable
fun AuroraBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryVoidBackground)
            .drawBehind {
                // Top-Right Soft Aurora Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1F38BDF8), Color(0x0C8B5CF6), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.12f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.width * 0.8f
                )
                // Bottom-Left Soft Aurora Orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x18A78BFA), Color(0x0638BDF8), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = size.width * 0.75f
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                    radius = size.width * 0.75f
                )
            },
        content = content
    )
}

/**
 * Glassmorphic Luxury Panel / Card with subtle gradient, gentle border highlight and soft shadow
 */
@Composable
fun LuxuryGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderColor: Color = LuxurySurfaceHighlight,
    glowColor: Color? = null,
    borderWidth: Dp = 1.dp,
    containerBrush: Brush = LuxuryPanelGradient,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else Modifier

    val effectiveBorderColor = if (glowColor != null) glowColor.copy(alpha = 0.35f) else borderColor

    Surface(
        shape = shape,
        color = Color.Transparent,
        shadowElevation = elevation,
        border = BorderStroke(borderWidth, effectiveBorderColor),
        modifier = modifier
            .then(clickModifier)
    ) {
        Column(
            modifier = Modifier
                .background(containerBrush)
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Luxury Primary Gradient Button with tactile press scale & subtle glow
 */
@Composable
fun LuxuryGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    brush: Brush = LuxuryPrimaryGradient,
    textColor: Color = Color(0xFF04111D),
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    elevation: Dp = 6.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1200f),
        label = "button_scale"
    )

    Surface(
        shape = shape,
        color = Color.Transparent,
        shadowElevation = if (enabled) elevation else 0.dp,
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (enabled) brush else Brush.horizontalGradient(
                        listOf(Color(0xFF334155), Color(0xFF1E293B))
                    )
                )
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) textColor else LuxuryTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) textColor else LuxuryTextMuted,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Luxury Secondary Glass Button with delicate highlight border
 */
@Composable
fun LuxurySecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    borderColor: Color = LuxuryPrimary.copy(alpha = 0.6f),
    shape: Shape = RoundedCornerShape(12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1200f),
        label = "btn_secondary_scale"
    )

    Surface(
        shape = shape,
        color = LuxurySurfaceElevated.copy(alpha = 0.85f),
        border = BorderStroke(1.2.dp, if (enabled) borderColor else LuxurySurfaceHighlight),
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) LuxuryPrimary else LuxuryTextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) LuxuryTextPrimary else LuxuryTextMuted,
                    fontSize = 13.sp
                )
            )
        }
    }
}

/**
 * Luxury Icon Action Button (Circular or Rounded)
 */
@Composable
fun LuxuryIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LuxuryPrimary,
    containerColor: Color = LuxurySurfaceElevated,
    borderColor: Color = LuxurySurfaceHighlight,
    size: Dp = 40.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(100),
        label = "icon_btn_scale"
    )

    Surface(
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/**
 * Ultra-Luxury Glassmorphic Popup / Dialog Container
 * Features multi-layer ambient aurora backdrops, luminous borders, animated entrance, and refined styling.
 */
@Composable
fun LuxuryDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = LuxuryPrimary,
    glowBrush: Brush = LuxuryPrimaryGradient,
    actionButtons: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.92f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            ),
            label = "dialog_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(180),
            label = "dialog_alpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = LuxuryVoidBackground.copy(alpha = 0.96f),
                shadowElevation = 24.dp,
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        listOf(iconColor.copy(alpha = 0.5f), LuxurySurfaceHighlight, iconColor.copy(alpha = 0.2f))
                    )
                ),
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .scale(scale)
                    .drawBehind {
                        // Subtle ambient top-right glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(iconColor.copy(alpha = 0.18f), Color.Transparent),
                                center = Offset(size.width * 0.85f, 0f),
                                radius = size.width * 0.7f
                            ),
                            center = Offset(size.width * 0.85f, 0f),
                            radius = size.width * 0.7f
                        )
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header with Icon Halo & Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(iconColor.copy(alpha = 0.12f), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 22.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = iconColor.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, iconColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LuxuryTextPrimary,
                                    fontSize = 18.sp
                                )
                            )
                            if (subtitle != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LuxuryTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LuxurySurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LuxuryTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = LuxurySurfaceHighlight.copy(alpha = 0.5f), thickness = 1.dp)

                    // Dialog Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 18.dp)
                    ) {
                        content()
                    }

                    // Bottom Action Bar
                    HorizontalDivider(color = LuxurySurfaceHighlight.copy(alpha = 0.3f), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LuxurySurface.copy(alpha = 0.6f))
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        content = actionButtons
                    )
                }
            }
        }
    }
}
