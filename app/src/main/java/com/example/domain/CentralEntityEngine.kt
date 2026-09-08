package com.example.domain

import com.example.data.model.*

data class DetectedEntityMatch(
    val entityType: EntityType,
    val entityId: Long,
    val entityName: String,
    val matchedWord: String,
    val startIndex: Int,
    val endIndex: Int,
    val snippet: String,
    val confidence: Float = 0.95f
)

data class DeletionImpactSummary(
    val entityType: EntityType,
    val entityId: Long,
    val entityTitle: String,
    val affectedScenesCount: Int,
    val affectedEventsCount: Int,
    val affectedDecisionsCount: Int,
    val affectedRelationshipsCount: Int,
    val affectedCausalEdgesCount: Int,
    val warningMessages: List<String>
)

data class UniverseGraphNode(
    val id: String, // e.g. "CHAR_1", "LOC_2", "EVT_3"
    val entityType: EntityType,
    val rawId: Long,
    val label: String,
    val subtitle: String,
    var x: Float,
    var y: Float,
    val radius: Float = 34f,
    val colorHex: String,
    val groupTag: String = ""
)

data class UniverseGraphEdge(
    val sourceNodeId: String,
    val targetNodeId: String,
    val label: String,
    val relationType: String,
    val isSuggested: Boolean = false,
    val intensity: Int = 3
)

data class UniverseGraphData(
    val nodes: List<UniverseGraphNode>,
    val edges: List<UniverseGraphEdge>
)

class CentralEntityEngine {

    /**
     * Scans any manuscript text or note for known entity references (Characters, Locations, Codex, Lexicon, Events).
     */
    fun scanTextForEntities(
        text: String,
        characters: List<Character>,
        locations: List<StoryLocation>,
        codexEntries: List<CodexEntry>,
        lexiconTerms: List<LexiconTerm> = emptyList(),
        events: List<StoryEvent> = emptyList()
    ): List<DetectedEntityMatch> {
        if (text.isBlank()) return emptyList()

        val matches = mutableListOf<DetectedEntityMatch>()

        // 1. Scan Characters
        for (char in characters) {
            val names = mutableListOf(char.name)
            if (char.aliases.isNotBlank()) {
                names.addAll(char.aliases.split(",").map { it.trim() }.filter { it.length > 1 })
            }
            for (name in names) {
                val pattern = "\\b${Regex.escape(name)}\\b".toRegex(RegexOption.IGNORE_CASE)
                for (match in pattern.findAll(text)) {
                    val snippet = extractSnippet(text, match.range.first, match.range.last)
                    matches.add(
                        DetectedEntityMatch(
                            entityType = EntityType.CHARACTER,
                            entityId = char.id,
                            entityName = char.name,
                            matchedWord = match.value,
                            startIndex = match.range.first,
                            endIndex = match.range.last + 1,
                            snippet = snippet,
                            confidence = if (name.equals(char.name, ignoreCase = true)) 1.0f else 0.88f
                        )
                    )
                }
            }
        }

        // 2. Scan Locations
        for (loc in locations) {
            val names = mutableListOf(loc.name)
            if (loc.aliases.isNotBlank()) {
                names.addAll(loc.aliases.split(",").map { it.trim() }.filter { it.length > 1 })
            }
            for (name in names) {
                val pattern = "\\b${Regex.escape(name)}\\b".toRegex(RegexOption.IGNORE_CASE)
                for (match in pattern.findAll(text)) {
                    val snippet = extractSnippet(text, match.range.first, match.range.last)
                    matches.add(
                        DetectedEntityMatch(
                            entityType = EntityType.LOCATION,
                            entityId = loc.id,
                            entityName = loc.name,
                            matchedWord = match.value,
                            startIndex = match.range.first,
                            endIndex = match.range.last + 1,
                            snippet = snippet,
                            confidence = 0.92f
                        )
                    )
                }
            }
        }

        // 3. Scan Codex Entries
        for (codex in codexEntries) {
            val pattern = "\\b${Regex.escape(codex.title)}\\b".toRegex(RegexOption.IGNORE_CASE)
            for (match in pattern.findAll(text)) {
                val snippet = extractSnippet(text, match.range.first, match.range.last)
                matches.add(
                    DetectedEntityMatch(
                        entityType = EntityType.CODEX,
                        entityId = codex.id,
                        entityName = codex.title,
                        matchedWord = match.value,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        snippet = snippet,
                        confidence = 0.90f
                    )
                )
            }
        }

        // 4. Scan Lexicon Terms
        for (term in lexiconTerms.filter { it.autoLinkEnabled }) {
            val pattern = "\\b${Regex.escape(term.term)}\\b".toRegex(RegexOption.IGNORE_CASE)
            for (match in pattern.findAll(text)) {
                val snippet = extractSnippet(text, match.range.first, match.range.last)
                matches.add(
                    DetectedEntityMatch(
                        entityType = EntityType.LEXICON,
                        entityId = term.id,
                        entityName = term.term,
                        matchedWord = match.value,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        snippet = snippet,
                        confidence = 0.95f
                    )
                )
            }
        }

        // 5. Scan Events
        for (evt in events) {
            val pattern = "\\b${Regex.escape(evt.title)}\\b".toRegex(RegexOption.IGNORE_CASE)
            for (match in pattern.findAll(text)) {
                val snippet = extractSnippet(text, match.range.first, match.range.last)
                matches.add(
                    DetectedEntityMatch(
                        entityType = EntityType.EVENT,
                        entityId = evt.id,
                        entityName = evt.title,
                        matchedWord = match.value,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1,
                        snippet = snippet,
                        confidence = 0.85f
                    )
                )
            }
        }

        // Remove overlapping duplicates prioritizing Characters > Locations > Codex > Lexicon > Events
        return matches.sortedWith(
            compareBy<DetectedEntityMatch> { it.startIndex }
                .thenBy { it.entityType.ordinal }
        )
    }

    private fun extractSnippet(text: String, start: Int, end: Int): String {
        val snippetStart = (start - 25).coerceAtLeast(0)
        val snippetEnd = (end + 35).coerceAtMost(text.length)
        val raw = text.substring(snippetStart, snippetEnd).replace("\n", " ").trim()
        return if (snippetStart > 0) "...$raw..." else "$raw..."
    }

    /**
     * Generates automatic relationship suggestions between a Scene and detected entities.
     */
    fun generateSuggestionsForScene(
        scene: Scene,
        matches: List<DetectedEntityMatch>,
        existingRelationships: List<UniversalRelationship>
    ): List<UniversalRelationship> {
        val suggestions = mutableListOf<UniversalRelationship>()

        // Group matches by unique target entity
        val distinctEntities = matches.distinctBy { Pair(it.entityType, it.entityId) }

        for (match in distinctEntities) {
            val isAlreadyLinked = existingRelationships.any {
                (it.sourceType == EntityType.SCENE.name && it.sourceId == scene.id &&
                 it.targetType == match.entityType.name && it.targetId == match.entityId &&
                 it.status != "Dismissed") ||
                (it.targetType == EntityType.SCENE.name && it.targetId == scene.id &&
                 it.sourceType == match.entityType.name && it.sourceId == match.entityId &&
                 it.status != "Dismissed")
            }

            if (!isAlreadyLinked) {
                val relationType = when (match.entityType) {
                    EntityType.CHARACTER -> "AppearsIn"
                    EntityType.LOCATION -> "LocatedAt"
                    EntityType.EVENT -> "DepictsEvent"
                    EntityType.CODEX -> "MentionsLore"
                    EntityType.LEXICON -> "UsesTerm"
                    else -> "RelatesTo"
                }

                suggestions.add(
                    UniversalRelationship(
                        sourceType = EntityType.SCENE.name,
                        sourceId = scene.id,
                        targetType = match.entityType.name,
                        targetId = match.entityId,
                        relationType = relationType,
                        status = "Suggested",
                        confidenceScore = match.confidence,
                        sourceTextSnippet = match.snippet,
                        notes = "Auto-detected reference: \"${match.matchedWord}\""
                    )
                )
            }
        }

        return suggestions
    }

    /**
     * Analyzes the systemic impact of deleting or majorly altering an entity.
     */
    fun analyzeDeletionImpact(
        entityType: EntityType,
        entityId: Long,
        entityTitle: String,
        scenes: List<Scene>,
        events: List<StoryEvent>,
        decisions: List<StoryDecision>,
        relationships: List<UniversalRelationship>,
        causalEdges: List<CausalEdge>,
        characters: List<Character>
    ): DeletionImpactSummary {
        val warnings = mutableListOf<String>()

        val directRels = relationships.filter {
            (it.sourceType == entityType.name && it.sourceId == entityId) ||
            (it.targetType == entityType.name && it.targetId == entityId)
        }

        var affectedScenes = 0
        var affectedEvents = 0
        var affectedDecisions = 0
        var affectedEdges = 0

        when (entityType) {
            EntityType.CHARACTER -> {
                affectedScenes = directRels.count { it.targetType == EntityType.SCENE.name || it.sourceType == EntityType.SCENE.name }
                affectedEvents = events.count { it.characterIds.split(",").map { id -> id.trim() }.contains(entityId.toString()) }
                affectedDecisions = decisions.count { it.characterId == entityId }
                if (affectedEvents > 0) warnings.add("Character is an active participant in $affectedEvents story events.")
                if (affectedDecisions > 0) warnings.add("Character has $affectedDecisions recorded pivotal decisions.")
                if (affectedScenes > 0) warnings.add("Character appears in $affectedScenes scenes across the manuscript.")
            }
            EntityType.EVENT -> {
                affectedEdges = causalEdges.count { it.sourceEventId == entityId || it.targetEventId == entityId }
                affectedDecisions = decisions.count { it.eventId == entityId }
                if (affectedEdges > 0) warnings.add("Event is part of $affectedEdges causal connections in the causal graph.")
                if (affectedDecisions > 0) warnings.add("Event is tied to $affectedDecisions character decisions.")
            }
            EntityType.LOCATION -> {
                affectedScenes = directRels.count { it.targetType == EntityType.SCENE.name || it.sourceType == EntityType.SCENE.name }
                affectedEvents = events.count { it.locationNames.contains(entityTitle, ignoreCase = true) }
                if (affectedEvents > 0) warnings.add("Location is the primary setting for $affectedEvents story events.")
                if (affectedScenes > 0) warnings.add("Location is visited in $affectedScenes scenes.")
            }
            else -> {}
        }

        return DeletionImpactSummary(
            entityType = entityType,
            entityId = entityId,
            entityTitle = entityTitle,
            affectedScenesCount = affectedScenes,
            affectedEventsCount = affectedEvents,
            affectedDecisionsCount = affectedDecisions,
            affectedRelationshipsCount = directRels.size,
            affectedCausalEdgesCount = affectedEdges,
            warningMessages = warnings
        )
    }

    /**
     * Builds comprehensive universe graph data for visualization.
     */
    fun buildUniverseGraphData(
        characters: List<Character>,
        locations: List<StoryLocation>,
        events: List<StoryEvent>,
        codexEntries: List<CodexEntry>,
        universalRelationships: List<UniversalRelationship>,
        causalEdges: List<CausalEdge>,
        characterRelationships: List<CharacterRelationship>
    ): UniverseGraphData {
        val nodes = mutableListOf<UniverseGraphNode>()
        val edges = mutableListOf<UniverseGraphEdge>()

        // 1. Add Characters
        characters.forEachIndexed { index, char ->
            val angle = (index.toFloat() / (characters.size.coerceAtLeast(1))) * 2 * Math.PI.toFloat()
            val radius = 260f
            nodes.add(
                UniverseGraphNode(
                    id = "CHAR_${char.id}",
                    entityType = EntityType.CHARACTER,
                    rawId = char.id,
                    label = char.name,
                    subtitle = char.currentStatus,
                    x = (Math.cos(angle.toDouble()) * radius).toFloat() + 400f,
                    y = (Math.sin(angle.toDouble()) * radius).toFloat() + 350f,
                    radius = 32f,
                    colorHex = char.primaryColorHex.ifBlank { "#38BDF8" },
                    groupTag = char.origin.ifBlank { "Characters" }
                )
            )
        }

        // 2. Add Locations
        locations.forEachIndexed { index, loc ->
            val angle = (index.toFloat() / (locations.size.coerceAtLeast(1))) * 2 * Math.PI.toFloat() + 0.5f
            val radius = 420f
            nodes.add(
                UniverseGraphNode(
                    id = "LOC_${loc.id}",
                    entityType = EntityType.LOCATION,
                    rawId = loc.id,
                    label = loc.name,
                    subtitle = loc.type,
                    x = (Math.cos(angle.toDouble()) * radius).toFloat() + 400f,
                    y = (Math.sin(angle.toDouble()) * radius).toFloat() + 350f,
                    radius = 28f,
                    colorHex = "#10B981", // Emerald
                    groupTag = loc.realmOrWorld
                )
            )
        }

        // 3. Add Key Story Events
        events.forEachIndexed { index, evt ->
            val posX = if (evt.canvasPosX != 0f) evt.canvasPosX else (index * 140f + 100f)
            val posY = if (evt.canvasPosY != 0f) evt.canvasPosY else (index % 3 * 160f + 120f)
            nodes.add(
                UniverseGraphNode(
                    id = "EVT_${evt.id}",
                    entityType = EntityType.EVENT,
                    rawId = evt.id,
                    label = evt.title,
                    subtitle = "Epoch ${evt.cosmicTimestamp}",
                    x = posX,
                    y = posY,
                    radius = 30f,
                    colorHex = "#6366F1", // Indigo
                    groupTag = "Events"
                )
            )
        }

        // 4. Add Character Relationships
        for (rel in characterRelationships) {
            edges.add(
                UniverseGraphEdge(
                    sourceNodeId = "CHAR_${rel.sourceCharacterId}",
                    targetNodeId = "CHAR_${rel.targetCharacterId}",
                    label = rel.relationshipType,
                    relationType = rel.relationshipType,
                    isSuggested = false,
                    intensity = 4
                )
            )
        }

        // 5. Add Causal Edges
        for (edge in causalEdges) {
            edges.add(
                UniverseGraphEdge(
                    sourceNodeId = "EVT_${edge.sourceEventId}",
                    targetNodeId = "EVT_${edge.targetEventId}",
                    label = edge.relationshipType,
                    relationType = edge.relationshipType,
                    isSuggested = false,
                    intensity = 5
                )
            )
        }

        // 6. Add Universal Relationships (Confirmed & Suggested)
        for (rel in universalRelationships) {
            val srcPrefix = when (rel.sourceType) {
                EntityType.CHARACTER.name -> "CHAR_"
                EntityType.LOCATION.name -> "LOC_"
                EntityType.EVENT.name -> "EVT_"
                else -> null
            }
            val tgtPrefix = when (rel.targetType) {
                EntityType.CHARACTER.name -> "CHAR_"
                EntityType.LOCATION.name -> "LOC_"
                EntityType.EVENT.name -> "EVT_"
                else -> null
            }
            if (srcPrefix != null && tgtPrefix != null) {
                edges.add(
                    UniverseGraphEdge(
                        sourceNodeId = "$srcPrefix${rel.sourceId}",
                        targetNodeId = "$tgtPrefix${rel.targetId}",
                        label = rel.relationType,
                        relationType = rel.relationType,
                        isSuggested = rel.status == "Suggested",
                        intensity = rel.intensity
                    )
                )
            }
        }

        return UniverseGraphData(nodes = nodes, edges = edges)
    }
}
