package com.example.data.model

enum class ExamSectionType(
    val sectionCode: String,
    val title: String,
    val marksPerQuestion: Int,
    val description: String
) {
    SECTION_A("A", "Multiple Choice Questions", 1, "20 MCQs testing factual recall and conceptual grounding"),
    SECTION_B("B", "Very Short Answer", 2, "6 questions requiring precise definitions or 2-step calculations"),
    SECTION_C("C", "Short Answer Type", 3, "7 questions requiring detailed explanations or chemical equations"),
    SECTION_D("D", "Long Answer Type", 5, "3 questions with internal choice requiring full derivations"),
    SECTION_E("E", "Case-Based Integrated Units", 4, "3 case studies assessing real-world concept application")
}

data class ExamQuestion(
    val id: String,
    val questionNumber: Int,
    val section: ExamSectionType,
    val questionText: String,
    val marks: Int,
    val options: List<String> = emptyList(), // For Section A
    val correctOptionIndex: Int? = null,
    val sampleSolution: String,
    val markingSchemeRubric: String, // Step-wise distribution (e.g. Formula: +1, Calculation: +1, Unit: +1)
    val diagramPrompt: String? = null
)

data class CbseExamPaper(
    val id: String,
    val title: String,
    val subject: String,
    val cbseClass: Int,
    val totalMarks: Int = 80,
    val durationMinutes: Int = 180, // 3 hours
    val generalInstructions: List<String>,
    val questions: List<ExamQuestion>
)

data class QuestionEvaluation(
    val questionNumber: Int,
    val maxMarks: Int,
    val marksAwarded: Float,
    val stepWiseFeedback: List<String>,
    val examinerRemarks: String,
    val deductionReasons: String? = null
)

data class ExamSubmissionResult(
    val paperId: String,
    val paperTitle: String,
    val totalScore: Float,
    val maxScore: Int,
    val percentage: Float,
    val grade: String, // A1, A2, B1, etc.
    val sectionScores: Map<String, Float>,
    val generalExaminerFeedback: String,
    val evaluations: List<QuestionEvaluation>,
    val completedAt: Long = System.currentTimeMillis()
)
