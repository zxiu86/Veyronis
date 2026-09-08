package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * High-end Glow Step Pill for Writer Workflow navigation bar
 */
@Composable
fun LuxuryStepPill(
    number: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = when {
        isActive -> LuxuryPrimaryGradient
        isDone -> Brush.horizontalGradient(listOf(LuxurySuccessContainer, LuxurySurfaceElevated))
        else -> Brush.horizontalGradient(listOf(LuxurySurfaceElevated, LuxurySurface))
    }

    val textColor = when {
        isActive -> LuxuryVoidBackground
        isDone -> LuxurySuccess
        else -> LuxuryTextSecondary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isActive -> LuxuryVoidBackground.copy(alpha = 0.25f)
                    isDone -> LuxurySuccess.copy(alpha = 0.2f)
                    else -> LuxurySurfaceHighlight
                },
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isDone && !isActive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = LuxurySuccess,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = number.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                    }
                }
            }

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
