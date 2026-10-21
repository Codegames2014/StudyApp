package com.example.data.model

enum class TutorLanguageTone(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val promptInstruction: String
) {
    STRICT_EXAMINER(
        id = "strict_examiner",
        title = "Strict English Examiner",
        subtitle = "Official CBSE marking rubric & exact keywords",
        emoji = "📋",
        promptInstruction = """
            Tone: Strict CBSE Board Examiner.
            Language: Pure formal English.
            Style: Strictly follow CBSE board marking schemes. Structure answers with:
            - Exact technical keywords and definitions.
            - Step-by-step point-wise marks allocation [Step 1: Formula (+1), Step 2: Substitution (+1), Step 3: Result (+1)].
            - Explicit warnings against common student mistakes and examiner penalty rules.
        """.trimIndent()
    ),

    FRIENDLY_HINGLISH(
        id = "friendly_hinglish",
        title = "Friendly Hinglish Partner",
        subtitle = "Casual desi analogies, easy recall tricks",
        emoji = "🤝",
        promptInstruction = """
            Tone: Ultra-friendly, relatable Indian study buddy/elder sibling (Bhaiya/Didi).
            Language: Natural conversational Hinglish (Hindi written in Roman script mixed seamlessly with English CBSE technical terms).
            Style: Explain tough concepts using everyday Indian cultural analogies (cricket, chai, traffic, Bollywood). Keep morale high with encouraging phrases like "Bhai tension mat le, simple logic hai", "Ekdum clear?", and memorable memory hacks.
        """.trimIndent()
    ),

    REGIONAL_BREAKDOWN(
        id = "regional_breakdown",
        title = "Regional Breakdown",
        subtitle = "Vernacular Hindi & local Indian languages",
        emoji = "🇮🇳",
        promptInstruction = """
            Tone: Patient, localized native language tutor.
            Language: Simple bilingual breakdown with Hindi / regional Indian terms alongside standard CBSE scientific terms.
            Style: Ground each scientific law or equation with intuitive mother-tongue explanations, ensuring foundational clarity before writing formal English definitions.
        """.trimIndent()
    );

    companion object {
        fun fromId(id: String): TutorLanguageTone {
            return entries.find { it.id == id } ?: FRIENDLY_HINGLISH
        }
    }
}
