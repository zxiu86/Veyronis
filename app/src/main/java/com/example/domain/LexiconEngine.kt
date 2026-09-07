package com.example.domain

import com.example.data.model.Character
import com.example.data.model.LexiconTerm

data class TermOccurrence(
    val term: LexiconTerm,
    val startIndex: Int,
    val endIndex: Int,
    val matchedWord: String
)

class LexiconEngine {

    /**
     * Finds occurrences of registered lexicon terms within the text.
     */
    fun findTermOccurrences(text: String, terms: List<LexiconTerm>): List<TermOccurrence> {
        if (text.isBlank() || terms.isEmpty()) return emptyList()

        val results = mutableListOf<TermOccurrence>()
        val sortedTerms = terms.filter { it.autoLinkEnabled }
            .sortedByDescending { it.term.length } // Match longest terms first

        for (term in sortedTerms) {
            val pattern = "\\b${Regex.escape(term.term)}\\b".toRegex(RegexOption.IGNORE_CASE)
            for (match in pattern.findAll(text)) {
                // Verify no overlap with existing found occurrences
                val overlaps = results.any { existing ->
                    match.range.first < existing.endIndex && match.range.last >= existing.startIndex
                }
                if (!overlaps) {
                    results.add(
                        TermOccurrence(
                            term = term,
                            startIndex = match.range.first,
                            endIndex = match.range.last + 1,
                            matchedWord = match.value
                        )
                    )
                }
            }
        }
        return results.sortedBy { it.startIndex }
    }

    /**
     * Automatically track character appearances throughout manuscripts.
     */
    fun countCharacterAppearances(text: String, character: Character): Int {
        if (text.isBlank()) return 0
        var count = 0
        val namesToSearch = mutableListOf(character.name)
        if (character.aliases.isNotBlank()) {
            namesToSearch.addAll(character.aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }

        for (name in namesToSearch) {
            val pattern = "\\b${Regex.escape(name)}\\b".toRegex(RegexOption.IGNORE_CASE)
            count += pattern.findAll(text).count()
        }
        return count
    }
}
