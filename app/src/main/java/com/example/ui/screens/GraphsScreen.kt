package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CausalEdge
import com.example.data.model.StoryEvent
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryDialog
import com.example.ui.components.LuxuryGradientButton
import com.example.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class GraphMode {
    CAUSAL_EVENTS,
    CHARACTER_RELATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allEdges by viewModel.allCausalEdges.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allRelationships by viewModel.allRelationships.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var graphMode by remember { mutableStateOf(GraphMode.CAUSAL_EVENTS) }

    // Canvas Pan & Zoom State
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset(50f, 100f)) }

    var selectedEventId by remember { mutableStateOf<Long?>(null) }
    var showAddEdgeDialog by remember { mutableStateOf(false) }

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VeyronisBackground)
    ) {
        // Top Toolbar
        Surface(color = VeyronisPanel, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = graphMode == GraphMode.CAUSAL_EVENTS,
                        onClick = { graphMode = GraphMode.CAUSAL_EVENTS },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(Strings.get("graphs_mode_causal", language), style = MaterialTheme.typography.labelSmall)
                    }
                    SegmentedButton(
                        selected = graphMode == GraphMode.CHARACTER_RELATIONS,
                        onClick = { graphMode = GraphMode.CHARACTER_RELATIONS },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(Strings.get("graphs_mode_bonds", language), style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            scale = (scale * 1.25f).coerceAtMost(3.0f)
                        }
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = if (language == AppLanguage.ARABIC) "تكبير" else "Zoom In", tint = VeyronisTextSecondary)
                    }
                    IconButton(
                        onClick = {
                            scale = (scale / 1.25f).coerceAtLeast(0.4f)
                        }
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = if (language == AppLanguage.ARABIC) "تصغير" else "Zoom Out", tint = VeyronisTextSecondary)
                    }
                    IconButton(
                        onClick = {
                            scale = 1f
                            panOffset = Offset(50f, 100f)
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = if (language == AppLanguage.ARABIC) "إعادة تعيين العرض" else "Reset View", tint = VeyronisTextSecondary)
                    }
                    if (graphMode == GraphMode.CAUSAL_EVENTS) {
                        IconButton(
                            onClick = { showAddEdgeDialog = true },
                            modifier = Modifier.testTag("add_causal_edge_button")
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = Strings.get("graphs_link_events", language), tint = VeyronisPrimary)
                        }
                    }
                }
            }
        }

        // Interactive Graph Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(VeyronisBackground)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.4f, 3.0f)
                        panOffset += pan
                    }
                }
                .testTag("interactive_graph_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background subtle celestial grid lines
                val gridSize = 60f * scale
                val startX = (panOffset.x % gridSize)
                val startY = (panOffset.y % gridSize)
                var x = startX
                while (x < size.width) {
                    drawLine(
                        color = Color(0xFF141432),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += gridSize
                }
                var y = startY
                while (y < size.height) {
                    drawLine(
                        color = Color(0xFF141432),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridSize
                }

                if (graphMode == GraphMode.CAUSAL_EVENTS) {
                    val eventsMap = allEvents.associateBy { it.id }

                    // Draw Causal Edges
                    for (edge in allEdges) {
                        val source = eventsMap[edge.sourceEventId]
                        val target = eventsMap[edge.targetEventId]
                        if (source != null && target != null) {
                            val startPos = Offset(source.canvasPosX, source.canvasPosY) * scale + panOffset
                            val endPos = Offset(target.canvasPosX, target.canvasPosY) * scale + panOffset

                            val edgeColor = when (edge.relationshipType) {
                                "Causes" -> Color(0xFF38BDF8)
                                "Leads To" -> Color(0xFFFBBF24)
                                "Conflicts With" -> Color(0xFFF87171)
                                "Prevents" -> Color(0xFFEF4444)
                                "Creates" -> Color(0xFF34D399)
                                else -> Color(0xFFA78BFA)
                            }

                            // Curved path
                            val path = Path().apply {
                                moveTo(startPos.x, startPos.y)
                                val midX = (startPos.x + endPos.x) / 2
                                val midY = (startPos.y + endPos.y) / 2 - 20f * scale
                                quadraticTo(midX, midY, endPos.x, endPos.y)
                            }
                            drawPath(
                                path = path,
                                color = edgeColor.copy(alpha = 0.8f),
                                style = Stroke(width = 3f * scale)
                            )

                            // Arrowhead at target
                            val angle = atan2(endPos.y - startPos.y, endPos.x - startPos.x)
                            val arrowSize = 14f * scale
                            val arrowPath = Path().apply {
                                moveTo(endPos.x, endPos.y)
                                lineTo(
                                    endPos.x - arrowSize * cos(angle - Math.PI / 6).toFloat(),
                                    endPos.y - arrowSize * sin(angle - Math.PI / 6).toFloat()
                                )
                                lineTo(
                                    endPos.x - arrowSize * cos(angle + Math.PI / 6).toFloat(),
                                    endPos.y - arrowSize * sin(angle + Math.PI / 6).toFloat()
                                )
                                close()
                            }
                            drawPath(arrowPath, color = edgeColor)

                            // Edge Label
                            val midPoint = Offset((startPos.x + endPos.x) / 2, (startPos.y + endPos.y) / 2 - 15f * scale)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = edge.relationshipType,
                                topLeft = midPoint,
                                style = TextStyle(
                                    color = edgeColor,
                                    fontSize = (10 * scale).sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Draw Event Nodes
                    for (event in allEvents) {
                        val nodeCenter = Offset(event.canvasPosX, event.canvasPosY) * scale + panOffset
                        val nodeRadius = 24f * scale
                        val isSelected = event.id == selectedEventId

                        drawCircle(
                            color = if (isSelected) Color(0xFFE879F9) else Color(0xFF818CF8),
                            radius = nodeRadius,
                            center = nodeCenter
                        )
                        drawCircle(
                            color = Color(0xFF00001C),
                            radius = nodeRadius * 0.75f,
                            center = nodeCenter
                        )
                        // Label below node
                        drawText(
                            textMeasurer = textMeasurer,
                            text = event.title,
                            topLeft = Offset(nodeCenter.x - 40f * scale, nodeCenter.y + nodeRadius + 4f),
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (11 * scale).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                } else {
                    // Character Relationships Graph Mode
                    val chars = allCharacters
                    val count = chars.size.coerceAtLeast(1)
                    val center = Offset(size.width / 2, size.height / 2)
                    val orbitRadius = (160f * scale).coerceAtLeast(80f)

                    val positions = chars.mapIndexed { idx, char ->
                        val angle = (2 * Math.PI * idx / count).toFloat()
                        char.id to (center + Offset(cos(angle) * orbitRadius, sin(angle) * orbitRadius))
                    }.toMap()

                    // Draw relationships lines
                    for (rel in allRelationships) {
                        val p1 = positions[rel.sourceCharacterId]
                        val p2 = positions[rel.targetCharacterId]
                        if (p1 != null && p2 != null) {
                            drawLine(
                                color = Color(0xFFFBBF24).copy(alpha = 0.7f),
                                start = p1,
                                end = p2,
                                strokeWidth = 2.5f * scale
                            )
                            val mid = Offset((p1.x + p2.x) / 2, (p1.y + p2.y) / 2)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = rel.relationshipType,
                                topLeft = mid,
                                style = TextStyle(color = Color(0xFFFBBF24), fontSize = (10 * scale).sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Draw Character Nodes
                    for (char in chars) {
                        val pos = positions[char.id] ?: center
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 22f * scale,
                            center = pos
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = char.name,
                            topLeft = Offset(pos.x - 30f * scale, pos.y + 24f * scale),
                            style = TextStyle(color = Color.White, fontSize = (11 * scale).sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Legend / Causal Chain Info
        Surface(color = VeyronisPanel, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem(if (language == AppLanguage.ARABIC) "يسبب" else "Causes", Color(0xFF38BDF8))
                    LegendItem(if (language == AppLanguage.ARABIC) "يؤدي إلى" else "Leads To", Color(0xFFFBBF24))
                    LegendItem(if (language == AppLanguage.ARABIC) "يتعارض" else "Conflicts", Color(0xFFF87171))
                    LegendItem(if (language == AppLanguage.ARABIC) "ينشئ" else "Creates", Color(0xFF34D399))
                }
                Text(
                    text = if (language == AppLanguage.ARABIC) "اسحب وقرّب لاستكشاف السلاسل السببية" else "Pan & Zoom to explore causal chains",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTextMuted
                )
            }
        }
    }

    // Dialog: Add Causal Edge
    if (showAddEdgeDialog) {
        var sourceId by remember { mutableStateOf(allEvents.firstOrNull()?.id ?: 0L) }
        var targetId by remember { mutableStateOf(allEvents.getOrNull(1)?.id ?: 0L) }
        var relationType by remember { mutableStateOf("Causes") }

        LuxuryDialog(
            onDismissRequest = { showAddEdgeDialog = false },
            title = Strings.get("graphs_link_events", language),
            subtitle = if (language == AppLanguage.ARABIC) "إنشاء رابط سببي بين حدثين في السلسلة" else "Create causal dependency between events",
            icon = Icons.Default.Timeline,
            iconColor = LuxuryElectricCyan,
            actionButtons = {
                TextButton(onClick = { showAddEdgeDialog = false }) {
                    Text(Strings.get("cancel", language), color = LuxuryTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                LuxuryGradientButton(
                    text = if (language == AppLanguage.ARABIC) "ربط الأحداث" else "Connect Events",
                    onClick = {
                        if (sourceId != targetId && sourceId > 0 && targetId > 0) {
                            viewModel.addCausalEdge(sourceId, targetId, relationType)
                            showAddEdgeDialog = false
                        }
                    },
                    brush = LuxuryPrimaryGradient,
                    textColor = LuxuryVoidBackground,
                    enabled = sourceId != targetId && sourceId > 0 && targetId > 0
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "حدث المصدر (السبب):" else "Source Event (Cause):",
                    style = MaterialTheme.typography.labelMedium,
                    color = LuxuryElectricCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxurySurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (ev in allEvents) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sourceId = ev.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = sourceId == ev.id,
                                onClick = { sourceId = ev.id },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxuryElectricCyan)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(ev.title, color = LuxuryTextPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.ARABIC) "حدث الهدف (النتيجة):" else "Target Event (Effect):",
                    style = MaterialTheme.typography.labelMedium,
                    color = LuxuryAuroraViolet,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxurySurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (ev in allEvents) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetId = ev.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetId == ev.id,
                                onClick = { targetId = ev.id },
                                colors = RadioButtonDefaults.colors(selectedColor = LuxuryAuroraViolet)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(ev.title, color = LuxuryTextPrimary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = relationType,
                    onValueChange = { relationType = it },
                    label = { Text(if (language == AppLanguage.ARABIC) "نوع الرابط (Causes, Leads To, Conflicts With, Creates)" else "Relation Type") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryElectricCyan,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        unfocusedContainerColor = LuxurySurfaceElevated,
                        focusedContainerColor = LuxurySurfaceElevated
                    )
                )
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = VeyronisTextSecondary)
    }
}
