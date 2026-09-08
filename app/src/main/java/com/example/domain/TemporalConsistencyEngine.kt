package com.example.domain

import com.example.data.model.*

enum class WarningSeverity {
    CRITICAL,
    WARNING,
    ADVISORY
}

data class TemporalWarning(
    val id: String,
    val title: String,
    val severity: WarningSeverity,
    val affectedEntityName: String,
    val description: String,
    val recommendation: String,
    val cosmicTimestamp: Double? = null
)

class TemporalConsistencyEngine {

    fun analyzeContinuity(
        characters: List<Character>,
        events: List<StoryEvent>,
        scenes: List<Scene>,
        timelines: List<Timeline>,
        rules: List<WorldRule>,
        edges: List<CausalEdge>,
        decisions: List<StoryDecision> = emptyList(),
        relationships: List<UniversalRelationship> = emptyList()
    ): List<TemporalWarning> {
        val warnings = mutableListOf<TemporalWarning>()

        val eventsById = events.associateBy { it.id }
        val charsById = characters.associateBy { it.id }

        // 1. Post-Death Appearance Contradiction
        for (character in characters) {
            val deathTime = character.deathCosmicTime
            if (character.isDeceased && deathTime != null) {
                // Check events where character is tagged
                for (event in events) {
                    val charIds = event.characterIds.split(",")
                        .mapNotNull { it.trim().toLongOrNull() }
                    if (character.id in charIds && event.cosmicTimestamp > deathTime) {
                        warnings.add(
                            TemporalWarning(
                                id = "post-death-${character.id}-${event.id}",
                                title = "Post-Mortem Existence Contradiction",
                                severity = WarningSeverity.CRITICAL,
                                affectedEntityName = character.name,
                                description = "Character '${character.name}' appears in Event '${event.title}' at Cosmic Year ${event.cosmicTimestamp}, which occurs after their recorded demise at Cosmic Year $deathTime.",
                                recommendation = "Verify if this is a time-displaced echo, an earlier timeline branch, or adjust the event date.",
                                cosmicTimestamp = event.cosmicTimestamp
                            )
                        )
                    }
                }
            }
        }

        // 2. Simultaneous Disjoint Location Contradiction
        // Group events by character and check overlapping cosmic times with different locations
        for (character in characters) {
            val charEvents = events.filter { ev ->
                ev.characterIds.split(",").mapNotNull { it.trim().toLongOrNull() }.contains(character.id)
            }.sortedBy { it.cosmicTimestamp }

            for (i in 0 until charEvents.size - 1) {
                val e1 = charEvents[i]
                val e2 = charEvents[i + 1]
                val e1End = e1.cosmicTimestamp + e1.durationYears
                if (e1End > e2.cosmicTimestamp) {
                    // Check if locations conflict
                    val loc1 = e1.locationNames.trim()
                    val loc2 = e2.locationNames.trim()
                    if (loc1.isNotEmpty() && loc2.isNotEmpty() && !loc1.equals(loc2, ignoreCase = true)) {
                        warnings.add(
                            TemporalWarning(
                                id = "bilocation-${character.id}-${e1.id}-${e2.id}",
                                title = "Bilocational Overlap Conflict",
                                severity = WarningSeverity.CRITICAL,
                                affectedEntityName = character.name,
                                description = "'${character.name}' is recorded simultaneously in '$loc1' (Event: '${e1.title}') and '$loc2' (Event: '${e2.title}') around Cosmic Year ${e2.cosmicTimestamp}.",
                                recommendation = "Adjust durations, establish quantum bilocation world rules, or shift event timelines.",
                                cosmicTimestamp = e2.cosmicTimestamp
                            )
                        )
                    }
                }
            }
        }

        // 3. Causal Edge Inversion (Cause occurring after Effect)
        for (edge in edges) {
            val source = eventsById[edge.sourceEventId]
            val target = eventsById[edge.targetEventId]
            if (source != null && target != null) {
                if (edge.relationshipType in listOf("Causes", "Leads To", "Depends On", "Creates")) {
                    if (source.cosmicTimestamp > target.cosmicTimestamp) {
                        warnings.add(
                            TemporalWarning(
                                id = "causal-inversion-${edge.id}",
                                title = "Inverted Causal Precedence",
                                severity = WarningSeverity.CRITICAL,
                                affectedEntityName = "${source.title} → ${target.title}",
                                description = "Event '${source.title}' (Cosmic ${source.cosmicTimestamp}) is designated as causing/leading to '${target.title}' (Cosmic ${target.cosmicTimestamp}), violating forward causality.",
                                recommendation = "Verify if closed-timelike curve physics are active, or re-order timestamps.",
                                cosmicTimestamp = source.cosmicTimestamp
                            )
                        )
                    }
                }
            }
        }

        // 4. Decision vs Event Chronology
        for (decision in decisions) {
            if (decision.eventId != null && decision.cosmicTimestamp != null) {
                val evt = eventsById[decision.eventId]
                if (evt != null && decision.cosmicTimestamp > evt.cosmicTimestamp + evt.durationYears) {
                    warnings.add(
                        TemporalWarning(
                            id = "decision-post-event-${decision.id}",
                            title = "Pivotal Decision Anachronism",
                            severity = WarningSeverity.WARNING,
                            affectedEntityName = decision.title,
                            description = "Decision '${decision.title}' is stamped at Cosmic ${decision.cosmicTimestamp}, which occurs after its associated Event '${evt.title}' (Cosmic ${evt.cosmicTimestamp}).",
                            recommendation = "Align decision cosmic timestamp prior to or within the event duration.",
                            cosmicTimestamp = decision.cosmicTimestamp
                        )
                    )
                }
            }
        }

        // 5. Timeline Offset & Dilated Branch Desync Advisory
        for (timeline in timelines) {
            if (!timeline.isCosmicPrime && timeline.divergencePointCosmic != null) {
                val timelineEvents = events.filter { it.timelineId == timeline.id }
                for (ev in timelineEvents) {
                    if (ev.cosmicTimestamp < timeline.divergencePointCosmic) {
                        warnings.add(
                            TemporalWarning(
                                id = "branch-premature-${timeline.id}-${ev.id}",
                                title = "Pre-Divergence Branch Event",
                                severity = WarningSeverity.WARNING,
                                affectedEntityName = timeline.name,
                                description = "Event '${ev.title}' in timeline '${timeline.name}' is scheduled at Cosmic ${ev.cosmicTimestamp}, before its branch point of ${timeline.divergencePointCosmic}.",
                                recommendation = "Move this event into the parent timeline or update the branch divergence point.",
                                cosmicTimestamp = ev.cosmicTimestamp
                            )
                        )
                    }
                }
            }
        }

        return warnings
    }
}
