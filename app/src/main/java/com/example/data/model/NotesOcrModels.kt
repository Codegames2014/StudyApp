package com.example.data.model

data class OcrFlashcard(
    val id: String,
    val frontQuestion: String,
    val backAnswer: String,
    val formulaOrEquation: String? = null,
    val ncertCitation: String,
    val difficulty: String = "Medium", // "Easy", "Medium", "Board High-Yield"
    val tags: List<String> = emptyList()
)

data class MindMapNode(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val conceptType: String = "CONCEPT", // "CORE", "LAW", "FORMULA", "EXAMPLE"
    val colorHex: Long = 0xFF6366F1,
    val children: List<MindMapNode> = emptyList()
)

data class HandwrittenNoteScanResult(
    val id: String,
    val scanTitle: String,
    val subject: String,
    val rawExtractedNotes: String,
    val flashcards: List<OcrFlashcard>,
    val mindMapRoot: MindMapNode,
    val timestamp: Long = System.currentTimeMillis()
)
