package com.example.data.model

data class NcertSubject(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val cbseClass: Int,
    val chapters: List<NcertChapter>
)

data class NcertChapter(
    val id: String,
    val chapterNumber: Int,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val cbseClass: Int,
    val estimatedReadMinutes: Int,
    val summary: String,
    val keyFormulasAndLaws: List<String>,
    val citations: List<NcertCitation>,
    val sampleBoardQuestions: List<BoardExamQuestion>,
    val studyGuide: ChapterStudyGuide,
    val flashcards: List<FlashCard>,
    val quizQuestions: List<QuizQuestion>,
    val podcastScript: List<PodcastDialogue>,
    val mindMapRoot: MindMapNode
)

data class NcertCitation(
    val pageNumber: Int,
    val sectionTitle: String,
    val excerpt: String,
    val citationBadge: String // e.g. "NCERT Cl.10 Sci, Ch.1, p.14"
)

data class BoardExamQuestion(
    val year: String, // e.g. "CBSE Board 2024 (3 Marks)"
    val question: String,
    val modelAnswer: String,
    val markingSchemeTip: String
)

data class ChapterStudyGuide(
    val executiveSummary: String,
    val coreConcepts: List<String>,
    val examWatchouts: List<String>,
    val historicalContextOrTimeline: List<String>
)

data class FlashCard(
    val id: String,
    val frontPrompt: String,
    val backAnswer: String,
    val category: String,
    val ncertCitation: String
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val ncertReference: String
)

data class PodcastDialogue(
    val speaker: String, // "Aarav" or "Priya"
    val avatarRole: String, // "Concept Host" or "Exam Strategist"
    val text: String,
    val tone: String = "conversational"
)
