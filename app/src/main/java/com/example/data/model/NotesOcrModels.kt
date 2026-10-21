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
) {
    val label: String get() = title
    val subtext: String? get() = subtitle

    constructor(id: String, title: String, subtitle: String?, conceptType: String, colorHex: Long) : this(
        id = id,
        title = title,
        subtitle = subtitle,
        conceptType = conceptType,
        colorHex = colorHex,
        children = emptyList()
    )

    constructor(id: String, label: String) : this(
        id = id,
        title = label,
        subtitle = null,
        conceptType = "CONCEPT",
        colorHex = 0xFF6366F1,
        children = emptyList()
    )

    constructor(id: String, label: String, children: List<MindMapNode>) : this(
        id = id,
        title = label,
        subtitle = null,
        conceptType = "CONCEPT",
        colorHex = 0xFF6366F1,
        children = children
    )

    constructor(id: String, label: String, subtext: String?, children: List<MindMapNode>) : this(
        id = id,
        title = label,
        subtitle = subtext,
        conceptType = "CONCEPT",
        colorHex = 0xFF6366F1,
        children = children
    )
}

data class HandwrittenNoteScanResult(
    val id: String,
    val scanTitle: String,
    val subject: String,
    val rawExtractedNotes: String,
    val flashcards: List<OcrFlashcard>,
    val mindMapRoot: MindMapNode,
    val timestamp: Long = System.currentTimeMillis()
)
