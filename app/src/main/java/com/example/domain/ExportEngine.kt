package com.example.domain

import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.data.model.Scene
import com.example.data.model.Series

data class ExportOptions(
    val format: ExportFormat = ExportFormat.MARKDOWN,
    val includeCover: Boolean = true,
    val authorName: String = "Author",
    val includeSceneTitles: Boolean = true,
    val includeWordCounts: Boolean = false
)

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    MARKDOWN("Markdown", "md", "text/markdown"),
    TXT("Plain Text", "txt", "text/plain"),
    EPUB("EPUB Document", "epub", "application/epub+zip"),
    PDF("PDF / HTML Print", "html", "text/html"),
    DOCX("Word Document (XML)", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    JSON("Veyronis Project JSON", "json", "application/json")
}

data class ExportResult(
    val fileName: String,
    val mimeType: String,
    val content: String,
    val wordCount: Int
)

class ExportEngine {

    fun exportBook(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): ExportResult {
        val totalWords = chapters.sumOf { ch ->
            (scenesByChapter[ch.id] ?: emptyList()).sumOf { it.wordCount }
        }

        val baseFileName = book.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")

        val outputContent = when (options.format) {
            ExportFormat.MARKDOWN -> generateMarkdown(series, book, chapters, scenesByChapter, options)
            ExportFormat.TXT -> generatePlainText(series, book, chapters, scenesByChapter, options)
            ExportFormat.PDF -> generateHtmlPrint(series, book, chapters, scenesByChapter, options)
            ExportFormat.EPUB -> generateEpubXhtmlPackage(series, book, chapters, scenesByChapter, options)
            ExportFormat.DOCX -> generateWordXml(series, book, chapters, scenesByChapter, options)
            ExportFormat.JSON -> generateProjectJson(series, book, chapters, scenesByChapter)
        }

        return ExportResult(
            fileName = "$baseFileName.${options.format.extension}",
            mimeType = options.format.mimeType,
            content = outputContent,
            wordCount = totalWords
        )
    }

    private fun generateMarkdown(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): String {
        val sb = StringBuilder()
        if (series != null) {
            sb.append("# ${series.title}\n\n")
        }
        sb.append("# ${book.title}\n")
        if (book.subtitle.isNotBlank()) {
            sb.append("### *${book.subtitle}*\n\n")
        }
        sb.append("**Author:** ${options.authorName}\n\n")
        sb.append("---\n\n")

        for (chapter in chapters.sortedBy { it.orderIndex }) {
            sb.append("## ${chapter.title}\n\n")
            val scenes = scenesByChapter[chapter.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for (scene in scenes) {
                if (options.includeSceneTitles) {
                    sb.append("### ${scene.title}\n\n")
                }
                sb.append("${scene.content.trim()}\n\n")
                sb.append("* * *\n\n")
            }
        }
        return sb.toString()
    }

    private fun generatePlainText(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): String {
        val sb = StringBuilder()
        if (series != null) {
            sb.append("${series.title.uppercase()}\n\n")
        }
        sb.append("${book.title.uppercase()}\n")
        if (book.subtitle.isNotBlank()) {
            sb.append("${book.subtitle}\n")
        }
        sb.append("By ${options.authorName}\n\n")
        sb.append("=========================================\n\n")

        for (chapter in chapters.sortedBy { it.orderIndex }) {
            sb.append("\n${chapter.title.uppercase()}\n")
            sb.append("-----------------------------------------\n\n")
            val scenes = scenesByChapter[chapter.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for (scene in scenes) {
                if (options.includeSceneTitles) {
                    sb.append("[${scene.title}]\n\n")
                }
                sb.append("${scene.content.trim()}\n\n\n")
            }
        }
        return sb.toString()
    }

    private fun generateHtmlPrint(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): String {
        val sb = StringBuilder()
        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>${book.title}</title>
                <style>
                    @page { size: A4; margin: 2.5cm; }
                    body {
                        font-family: 'Georgia', serif;
                        line-height: 1.8;
                        color: #1a1a1a;
                        background: #ffffff;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 2rem;
                    }
                    h1.book-title { font-size: 2.5rem; text-align: center; margin-top: 3rem; margin-bottom: 0.5rem; }
                    p.author { text-align: center; font-style: italic; margin-bottom: 4rem; color: #555; }
                    .chapter-break { page-break-before: always; margin-top: 4rem; }
                    h2.chapter-title { font-size: 1.8rem; border-bottom: 1px solid #ddd; padding-bottom: 0.5rem; margin-top: 2rem; }
                    h3.scene-title { font-size: 1.2rem; color: #444; margin-top: 1.5rem; }
                    p { text-indent: 1.5em; margin: 0.5em 0; text-align: justify; }
                    .scene-divider { text-align: center; margin: 2rem 0; font-size: 1.5rem; letter-spacing: 0.5rem; color: #888; }
                </style>
            </head>
            <body>
                <h1 class="book-title">${book.title}</h1>
                ${if (book.subtitle.isNotBlank()) "<p style='text-align:center;font-size:1.2rem;'>${book.subtitle}</p>" else ""}
                <p class="author">By ${options.authorName}</p>
        """.trimIndent())

        for (chapter in chapters.sortedBy { it.orderIndex }) {
            sb.append("<div class='chapter-break'>\n")
            sb.append("  <h2 class='chapter-title'>${chapter.title}</h2>\n")
            val scenes = scenesByChapter[chapter.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for (scene in scenes) {
                if (options.includeSceneTitles) {
                    sb.append("  <h3 class='scene-title'>${scene.title}</h3>\n")
                }
                val paragraphs = scene.content.split("\n\n").filter { it.isNotBlank() }
                for (p in paragraphs) {
                    sb.append("  <p>${p.replace("\n", "<br/>")}</p>\n")
                }
                sb.append("  <div class='scene-divider'>* * *</div>\n")
            }
            sb.append("</div>\n")
        }

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun generateEpubXhtmlPackage(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): String {
        // Generates clean standard OPF / XHTML manuscript format for EPUB readers
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xml:lang="en">
<head>
  <title>${book.title}</title>
  <style>
    body { font-family: serif; line-height: 1.6; margin: 1em; }
    h1 { text-align: center; }
    h2 { page-break-before: always; }
    p { text-indent: 1.5em; margin: 0; }
    .break { text-align: center; margin: 1em 0; }
  </style>
</head>
<body>
  <h1>${book.title}</h1>
  <p style="text-align:center;">By ${options.authorName}</p>
""")
        for (chapter in chapters.sortedBy { it.orderIndex }) {
            sb.append("  <h2>${chapter.title}</h2>\n")
            val scenes = scenesByChapter[chapter.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for (scene in scenes) {
                if (options.includeSceneTitles) {
                    sb.append("  <h3>${scene.title}</h3>\n")
                }
                val paragraphs = scene.content.split("\n\n").filter { it.isNotBlank() }
                for (p in paragraphs) {
                    sb.append("  <p>${p.replace("\n", "<br/>")}</p>\n")
                }
                sb.append("  <div class=\"break\">* * *</div>\n")
            }
        }
        sb.append("</body>\n</html>")
        return sb.toString()
    }

    private fun generateWordXml(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>,
        options: ExportOptions
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p><w:pPr><w:pStyle w:val="Title"/></w:pPr><w:r><w:t>${book.title}</w:t></w:r></w:p>
    <w:p><w:pPr><w:pStyle w:val="Subtitle"/></w:pPr><w:r><w:t>By ${options.authorName}</w:t></w:r></w:p>
""")
        for (chapter in chapters.sortedBy { it.orderIndex }) {
            sb.append("    <w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>${chapter.title}</w:t></w:r></w:p>\n")
            val scenes = scenesByChapter[chapter.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for (scene in scenes) {
                if (options.includeSceneTitles) {
                    sb.append("    <w:p><w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr><w:r><w:t>${scene.title}</w:t></w:r></w:p>\n")
                }
                for (p in scene.content.split("\n\n").filter { it.isNotBlank() }) {
                    sb.append("    <w:p><w:r><w:t>${p.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")}</w:t></w:r></w:p>\n")
                }
            }
        }
        sb.append("  </w:body>\n</w:document>")
        return sb.toString()
    }

    private fun generateProjectJson(
        series: Series?,
        book: Book,
        chapters: List<Chapter>,
        scenesByChapter: Map<Long, List<Scene>>
    ): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"project\": \"Veyronis\",\n")
        sb.append("  \"version\": \"1.0.0\",\n")
        sb.append("  \"series\": ${if (series != null) "\"${series.title}\"" else "null"},\n")
        sb.append("  \"book\": {\n")
        sb.append("    \"title\": \"${book.title}\",\n")
        sb.append("    \"subtitle\": \"${book.subtitle}\",\n")
        sb.append("    \"targetWordCount\": ${book.targetWordCount},\n")
        sb.append("    \"status\": \"${book.status}\"\n")
        sb.append("  },\n")
        sb.append("  \"chapters\": [\n")
        val chapterList = chapters.sortedBy { it.orderIndex }
        for ((idx, ch) in chapterList.withIndex()) {
            sb.append("    {\n")
            sb.append("      \"title\": \"${ch.title}\",\n")
            sb.append("      \"summary\": \"${ch.summary}\",\n")
            sb.append("      \"scenes\": [\n")
            val scenes = scenesByChapter[ch.id]?.sortedBy { it.orderIndex } ?: emptyList()
            for ((sIdx, sc) in scenes.withIndex()) {
                sb.append("        {\n")
                sb.append("          \"title\": \"${sc.title}\",\n")
                sb.append("          \"wordCount\": ${sc.wordCount},\n")
                sb.append("          \"content\": \"${sc.content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\"\n")
                sb.append("        }${if (sIdx < scenes.size - 1) "," else ""}\n")
            }
            sb.append("      ]\n")
            sb.append("    }${if (idx < chapterList.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}\n")
        return sb.toString()
    }
}
