package com.example.data.local

import android.content.Context
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.data.local.room.StudyFluidRoomDatabase
import com.example.data.local.room.StudyVaultDao
import com.example.data.local.room.StudyVaultEntity
import com.example.data.local.room.VaultItemEntity
import com.example.data.local.room.MistakeDao
import com.example.data.local.room.MistakeEntity

class StudyFluidRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    // --- ROOM DATABASE (LOCAL VAULTS & MISTAKE VAULT) ---
    val roomDb = StudyFluidRoomDatabase.getDatabase(context)
    val studyVaultDao: StudyVaultDao = roomDb.studyVaultDao()
    val mistakeDao: MistakeDao = roomDb.mistakeDao()

    // --- TUTOR LANGUAGE & TONE (FEATURE 6) ---
    private val _activeTutorTone = MutableStateFlow<TutorLanguageTone>(TutorLanguageTone.FRIENDLY_HINGLISH)
    val activeTutorTone: StateFlow<TutorLanguageTone> = _activeTutorTone.asStateFlow()

    fun setTutorTone(tone: TutorLanguageTone) {
        _activeTutorTone.value = tone
    }

    // --- 1. PROFILES & ISOLATION ---
    private val _profiles = MutableStateFlow<List<StudentProfile>>(defaultProfiles)
    val profiles: StateFlow<List<StudentProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow<String>(defaultProfiles[0].id)
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    val activeProfile: StudentProfile
        get() = _profiles.value.find { it.id == _activeProfileId.value } ?: _profiles.value.first()

    // --- AUTHENTICATION STATE ---
    private val _isAuthenticated = MutableStateFlow<Boolean>(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        try {
            val fbUser = Firebase.auth.currentUser
            if (fbUser != null) {
                _isAuthenticated.value = true
                val matching = defaultProfiles.find { it.email.equals(fbUser.email, ignoreCase = true) }
                if (matching != null) {
                    _activeProfileId.value = matching.id
                }
            }
        } catch (_: Throwable) {
            // Firebase Auth not initialized or in local test environment
        }

        // Seed initial Room vaults for multi-student profiles if empty
        scope.launch(Dispatchers.IO) {
            try {
                val existing = studyVaultDao.getVaultById("vault_aarav_chem")
                if (existing == null) {
                    val chemVault = StudyVaultEntity(
                        vaultId = "vault_aarav_chem",
                        studentProfileId = defaultProfiles[0].id,
                        title = "CBSE Class 10 Chemistry Core",
                        category = "NCERT Chemistry",
                        description = "Chemical reactions, balancing tricks, redox formulas, and board tips",
                        iconEmoji = "⚗️",
                        colorHex = 0xFF6366F1,
                        itemCount = 2
                    )
                    studyVaultDao.insertVault(chemVault)
                    studyVaultDao.insertItem(
                        VaultItemEntity(
                            itemId = "item_aarav_chem_1",
                            vaultId = "vault_aarav_chem",
                            studentProfileId = defaultProfiles[0].id,
                            chapterId = "cl10_sci_ch1",
                            title = "Redox Balancing Hit-and-Trial Summary",
                            content = "Step 1: Balance atoms other than H and O.\nStep 2: Balance H atoms.\nStep 3: Balance O atoms.\nStep 4: Check conservation of mass.",
                            itemType = "NOTE",
                            ncertCitationTag = "NCERT Cl.10 Sci, Ch.1, p.6",
                            isPinned = true
                        )
                    )
                    studyVaultDao.insertItem(
                        VaultItemEntity(
                            itemId = "item_aarav_chem_2",
                            vaultId = "vault_aarav_chem",
                            studentProfileId = defaultProfiles[0].id,
                            chapterId = "cl10_sci_ch1",
                            title = "Corrosion & Antioxidant Prevention",
                            content = "Rusting formula: Fe2O3.xH2O. Rancidity prevented by nitrogen flushing in chip bags and refrigeration.",
                            itemType = "NOTE",
                            ncertCitationTag = "NCERT Cl.10 Sci, Ch.1, p.13",
                            isPinned = false
                        )
                    )
                    val bioVault = StudyVaultEntity(
                        vaultId = "vault_aarav_bio",
                        studentProfileId = defaultProfiles[0].id,
                        title = "Life Processes Master Vault",
                        category = "NCERT Biology",
                        description = "Nephron diagrams, double circulation, and ATP yield notes",
                        iconEmoji = "🫀",
                        colorHex = 0xFF06B6D4,
                        itemCount = 1
                    )
                    studyVaultDao.insertVault(bioVault)
                    studyVaultDao.insertItem(
                        VaultItemEntity(
                            itemId = "item_aarav_bio_1",
                            vaultId = "vault_aarav_bio",
                            studentProfileId = defaultProfiles[0].id,
                            chapterId = "cl10_sci_ch6",
                            title = "Three Pathways of Glucose Breakdown",
                            content = "Glucose -> Pyruvate in cytoplasm.\nAerobic: 38 ATP + CO2 + H2O in mitochondria.\nAnaerobic: Ethanol + CO2 + 2 ATP in yeast.\nMuscle cramps: Lactic acid + energy.",
                            itemType = "NOTE",
                            ncertCitationTag = "NCERT Cl.10 Sci, Ch.6, p.102",
                            isPinned = true
                        )
                    )

                    val diyaVault = StudyVaultEntity(
                        vaultId = "vault_diya_sci",
                        studentProfileId = defaultProfiles[1].id,
                        title = "Class 7 Science Explorer",
                        category = "NCERT Science",
                        description = "Plant nutrition, acids, bases & salts flashcards",
                        iconEmoji = "🌱",
                        colorHex = 0xFF10B981,
                        itemCount = 1
                    )
                    studyVaultDao.insertVault(diyaVault)
                    studyVaultDao.insertItem(
                        VaultItemEntity(
                            itemId = "item_diya_sci_1",
                            vaultId = "vault_diya_sci",
                            studentProfileId = defaultProfiles[1].id,
                            chapterId = "cl7_sci_ch1",
                            title = "Autotrophic vs Heterotrophic Nutrition",
                            content = "Green plants synthesize own food via chlorophyll and sunlight. Parasites like Cuscuta depend on host plants.",
                            itemType = "NOTE",
                            ncertCitationTag = "NCERT Cl.7 Sci, Ch.1",
                            isPinned = true
                        )
                    )
                }

                // Seed initial Mistake Vault entities for Weakness Radar
                val existingMistakes = mistakeDao.getMistakeById("mistake_aarav_redox")
                if (existingMistakes == null) {
                    mistakeDao.insertAll(
                        listOf(
                            MistakeEntity(
                                id = "mistake_aarav_redox",
                                studentProfileId = defaultProfiles[0].id,
                                chapterId = "cl10_sci_ch1",
                                subject = "Chemistry",
                                topic = "Redox Agent Identification",
                                questionText = "In CuO + H₂ ➔ Cu + H₂O, identify the oxidizing agent.",
                                studentAnswer = "Cu is the oxidizing agent",
                                correctAnswer = "CuO is the oxidizing agent",
                                explanation = "Oxidizing and reducing agents must be selected strictly from the reactant side only. CuO loses oxygen, hence it is reduced and acts as the oxidizing agent.",
                                mistakeCategory = "Conceptual Gap",
                                severityLevel = 3,
                                isMastered = false,
                                ncertCitation = "NCERT Cl.10 Sci, Ch.1, p.12"
                            ),
                            MistakeEntity(
                                id = "mistake_aarav_lens",
                                studentProfileId = defaultProfiles[0].id,
                                chapterId = "cl10_sci_ch10",
                                subject = "Physics",
                                topic = "Lens Sign Convention & Object Distance",
                                questionText = "Calculate focal length when object is 20cm before a convex lens and image is formed 40cm behind.",
                                studentAnswer = "Used u = +20cm instead of -20cm",
                                correctAnswer = "u = -20cm, v = +40cm, 1/f = 1/40 - 1/(-20) = 3/40, f = +13.33cm",
                                explanation = "Cartesian sign convention requires object distance 'u' to be strictly negative for all real lenses and mirrors.",
                                mistakeCategory = "Formula Recall",
                                severityLevel = 2,
                                isMastered = false,
                                ncertCitation = "NCERT Cl.10 Sci, Ch.10, p.182"
                            ),
                            MistakeEntity(
                                id = "mistake_aarav_bio",
                                studentProfileId = defaultProfiles[0].id,
                                chapterId = "cl10_sci_ch6",
                                subject = "Biology",
                                topic = "Muscle Anaerobic Respiration Product",
                                questionText = "Name the product of anaerobic respiration in human muscle during sudden athletics.",
                                studentAnswer = "Ethanol and Carbon Dioxide",
                                correctAnswer = "Lactic Acid and Energy",
                                explanation = "In human muscles during lack of oxygen, pyruvate converts to 3-carbon Lactic Acid, which causes muscle cramps. Yeast produces ethanol.",
                                mistakeCategory = "Conceptual Gap",
                                severityLevel = 2,
                                isMastered = false,
                                ncertCitation = "NCERT Cl.10 Sci, Ch.6, p.102"
                            ),
                            MistakeEntity(
                                id = "mistake_aarav_math",
                                studentProfileId = defaultProfiles[0].id,
                                chapterId = "cl10_math_ch4",
                                subject = "Mathematics",
                                topic = "Quadratic Discriminant Boundary Condition",
                                questionText = "Find k if kx² + 4x + 1 = 0 has two equal real roots.",
                                studentAnswer = "k = 4, but forgot to state k ≠ 0",
                                correctAnswer = "D = 16 - 4k = 0 ➔ k = 4 (and k ≠ 0 for valid quadratic)",
                                explanation = "CBSE Board examiners deduct 0.5 marks if candidate forgets to verify that coefficient of x² remains non-zero.",
                                mistakeCategory = "Calculation Slip",
                                severityLevel = 1,
                                isMastered = true,
                                ncertCitation = "NCERT Cl.10 Math, Ch.4, p.89"
                            ),
                            MistakeEntity(
                                id = "mistake_aarav_sst",
                                studentProfileId = defaultProfiles[0].id,
                                chapterId = "cl10_sst_ch1",
                                subject = "Social Science",
                                topic = "Rio Earth Summit & Agenda 21",
                                questionText = "State the main objective of Agenda 21 signed at Rio de Janeiro.",
                                studentAnswer = "Economic growth of developing nations",
                                correctAnswer = "Global sustainable development, combating environmental damage, poverty, disease through global co-operation.",
                                explanation = "Agenda 21 explicitly focuses on achieving global sustainable development through localized environmental initiatives.",
                                mistakeCategory = "Question Misread",
                                severityLevel = 1,
                                isMastered = false,
                                ncertCitation = "NCERT Cl.10 Geography, Ch.1, p.4"
                            )
                        )
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    // Isolated chat history per profile: profileId -> List<ChatMessage>
    private val _profileChats = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            defaultProfiles[0].id to listOf(
                ChatMessage(
                    id = "msg_init_aarav",
                    sender = MessageSender.FLUID_TUTOR,
                    text = "Namaste Aarav! 🙏 I'm your Socratic FluidTutor for CBSE Class 10. Let's conquer Chemical Reactions, Life Processes, or Quadratic Equations today. You can also scan any textbook equation with the Camera Doubt Scanner!",
                    citationBadges = listOf("NCERT Cl.10 Sci, Ch.1", "NCERT Cl.10 Math, Ch.4")
                )
            ),
            defaultProfiles[1].id to listOf(
                ChatMessage(
                    id = "msg_init_diya",
                    sender = MessageSender.FLUID_TUTOR,
                    text = "Hi Diya! 🌟 Welcome to your Class 7 study vault. We can explore Science, Math puzzles, or prepare for upcoming unit tests. Ask me anything!",
                    citationBadges = listOf("NCERT Cl.7 Foundation")
                )
            )
        )
    )
    val profileChats: StateFlow<Map<String, List<ChatMessage>>> = _profileChats.asStateFlow()

    // Isolated student notes per profile
    private val _notes = MutableStateFlow<List<StudentNote>>(
        listOf(
            StudentNote(
                id = "note_1",
                profileId = defaultProfiles[0].id,
                chapterId = "cl10_sci_ch1",
                title = "Redox Trick: OIL RIG & Zinc Test",
                content = "Oxidation Is Loss of electrons/gain of Oxygen; Reduction Is Gain of electrons/loss of Oxygen. Zn + H2SO4 produces H2 gas which burns with 'pop' sound.",
                ncertCitationTag = "NCERT Cl.10 Sci, Ch.1, p.4",
                isPinned = true
            ),
            StudentNote(
                id = "note_2",
                profileId = defaultProfiles[0].id,
                chapterId = "cl10_sci_ch6",
                title = "Bile Juice Secretion & Emulsification",
                content = "Bile has no digestive enzymes! Its role is solely to create alkaline pH and emulsify large lipid droplets into micro-micelles for lipase.",
                ncertCitationTag = "NCERT Cl.10 Sci, Ch.6, p.100",
                isPinned = false
            )
        )
    )
    val notes: StateFlow<List<StudentNote>> = _notes.asStateFlow()

    // --- 2. OFFLINE CHAPTER CACHE & VECTOR EMBEDDINGS ---
    private val _cachedChapterIds = MutableStateFlow<Set<String>>(setOf("cl10_sci_ch1"))
    val cachedChapterIds: StateFlow<Set<String>> = _cachedChapterIds.asStateFlow()

    // Download progress map: chapterId -> Float (0f to 1f)
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    fun isChapterOffline(chapterId: String): Boolean {
        return _cachedChapterIds.value.contains(chapterId)
    }

    fun downloadChapterForOffline(chapterId: String) {
        if (_cachedChapterIds.value.contains(chapterId)) return
        scope.launch {
            _downloadProgress.update { it + (chapterId to 0.15f) }
            delay(350)
            _downloadProgress.update { it + (chapterId to 0.45f) }
            delay(350)
            _downloadProgress.update { it + (chapterId to 0.85f) }
            delay(300)
            _cachedChapterIds.update { it + chapterId }
            _downloadProgress.update { it - chapterId }
            incrementStreakAndXp(25)
        }
    }

    fun removeChapterFromCache(chapterId: String) {
        _cachedChapterIds.update { it - chapterId }
    }

    // --- 3. GAMIFIED CBSE MASTERY & SKILL TREE ---
    private val _skillNodes = MutableStateFlow<List<SkillNode>>(initialSkillNodes)
    val skillNodes: StateFlow<List<SkillNode>> = _skillNodes.asStateFlow()

    private val _cosmeticThemes = MutableStateFlow<List<CosmeticTheme>>(defaultThemes)
    val cosmeticThemes: StateFlow<List<CosmeticTheme>> = _cosmeticThemes.asStateFlow()

    private val _activeThemeId = MutableStateFlow<String>("theme_indigo")
    val activeThemeId: StateFlow<String> = _activeThemeId.asStateFlow()

    fun completeSkillNode(nodeId: String) {
        _skillNodes.update { list ->
            list.map { node ->
                if (node.id == nodeId) {
                    node.copy(isMastered = true, progressPercent = 1.0f)
                } else node
            }
        }
        // Unlock connected nodes
        val targetNode = _skillNodes.value.find { it.id == nodeId }
        if (targetNode != null) {
            _skillNodes.update { list ->
                list.map { node ->
                    if (targetNode.connectedNodeIds.contains(node.id)) {
                        node.copy(isUnlocked = true)
                    } else node
                }
            }
        }
        incrementStreakAndXp(60)
        checkThemeUnlocks()
    }

    private fun checkThemeUnlocks() {
        val currentXp = activeProfile.knowledgePoints
        _cosmeticThemes.update { list ->
            list.map { theme ->
                if (!theme.isUnlocked && currentXp >= (theme.unlockLevel * 150)) {
                    theme.copy(isUnlocked = true)
                } else theme
            }
        }
    }

    fun setActiveTheme(themeId: String) {
        val target = _cosmeticThemes.value.find { it.id == themeId && it.isUnlocked } ?: return
        _activeThemeId.value = target.id
    }

    // --- 4. SPACED REPETITION PLANNER ---
    private val _revisionTasks = MutableStateFlow<List<RevisionTask>>(initialTasks)
    val revisionTasks: StateFlow<List<RevisionTask>> = _revisionTasks.asStateFlow()

    private val _examCountdowns = MutableStateFlow<List<ExamCountdown>>(
        listOf(
            ExamCountdown("exam_1", "CBSE Science Board Term Finals", "Science", 28, 98),
            ExamCountdown("exam_2", "Mathematics Pre-Board Olympiad", "Mathematics", 45, 95)
        )
    )
    val examCountdowns: StateFlow<List<ExamCountdown>> = _examCountdowns.asStateFlow()

    fun toggleTaskCompletion(taskId: String) {
        _revisionTasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val updatedState = !task.isCompleted
                    if (updatedState) {
                        incrementStreakAndXp(task.xpReward)
                    }
                    task.copy(isCompleted = updatedState)
                } else task
            }
        }
    }

    fun generateSpacedRepetitionPlanForChapter(chapterId: String) {
        val chapter = NcertDatabase.getChapterById(chapterId) ?: NcertDatabase.class10ChemicalReactions
        val newTasks = listOf(
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_1",
                profileId = _activeProfileId.value,
                title = "Day 1 Active Recall: ${chapter.title}",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                estimatedMinutes = 12,
                xpReward = 30
            ),
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_2",
                profileId = _activeProfileId.value,
                title = "Day 3 Reinforce: Board Exemplar MCQs",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                estimatedMinutes = 15,
                xpReward = 45
            ),
            RevisionTask(
                id = "plan_${System.currentTimeMillis()}_3",
                profileId = _activeProfileId.value,
                title = "Day 7 Long-Term Mastery: Formula Derivations",
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subjectName = chapter.subjectName,
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                estimatedMinutes = 20,
                xpReward = 60
            )
        )
        _revisionTasks.update { newTasks + it }
    }

    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        _revisionTasks.update { list ->
            if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
                return@update list
            }
            val mutable = list.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            mutable
        }
    }

    fun moveTaskToDay(taskId: String, newDayOffset: Int) {
        _revisionTasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    task.copy(dueDateOffsetDays = newDayOffset)
                } else task
            }
        }
    }

    fun deleteTask(taskId: String) {
        _revisionTasks.update { list ->
            list.filterNot { it.id == taskId }
        }
    }

    fun generateCustomSpacedSchedule(
        topic: String,
        subject: String,
        examDays: Int,
        dailyMinutes: Int,
        confidence: String
    ) {
        val chapterId = "cl10_sci_ch1"
        val timestamp = System.currentTimeMillis()
        val intensityFactor = when (confidence) {
            "Needs Work" -> 1.3f
            "Intermediate" -> 1.0f
            else -> 0.8f
        }

        val generated = listOf(
            RevisionTask(
                id = "ai_plan_${timestamp}_1",
                profileId = _activeProfileId.value,
                title = "Initial Spaced Study: $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                estimatedMinutes = (dailyMinutes * 0.4 * intensityFactor).toInt().coerceAtLeast(10),
                xpReward = 35
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_2",
                profileId = _activeProfileId.value,
                title = "NCERT Exemplar & Practice: $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                estimatedMinutes = (dailyMinutes * 0.5 * intensityFactor).toInt().coerceAtLeast(15),
                xpReward = 45
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_3",
                profileId = _activeProfileId.value,
                title = "SM-2 Interval Recall: Formula Derivation for $topic",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                estimatedMinutes = (dailyMinutes * 0.6).toInt().coerceAtLeast(15),
                xpReward = 50
            ),
            RevisionTask(
                id = "ai_plan_${timestamp}_4",
                profileId = _activeProfileId.value,
                title = "Long-Term Retention: Audio Overview & Timed Test",
                chapterId = chapterId,
                chapterTitle = topic,
                subjectName = subject,
                dueDateOffsetDays = if (examDays > 7) 7 else examDays - 1,
                taskType = RevisionTaskType.AUDIO_PODCAST_REVIEW,
                estimatedMinutes = dailyMinutes,
                xpReward = 75
            )
        )

        _revisionTasks.update { generated + it }
        incrementStreakAndXp(30)
    }

    // --- 5. SHARED SIBLING STUDY ROOM & NOTES SYNC ---
    private val _sharedStudyRoom = MutableStateFlow<SharedStudyRoom>(
        SharedStudyRoom(
            roomId = "room_sharma_siblings",
            roomName = "Sharma Sibling CBSE Lounge",
            activeChapterId = "cl10_sci_ch1",
            activeChapterTitle = "Chemical Reactions & Equations",
            sharedScratchpadText = "Aarav (Cl.10) & Diya (Cl.7) Shared Notes:\n• Antoine Lavoisier Law of Mass Conservation: Total mass before = Total mass after.\n• Diya: Remember for Science Unit Test - photosynthesis formula uses chlorophyll and solar photons!\n• Aarav: White AgCl turning grey in sunlight is photolytic decomposition.",
            isCoListeningPodcast = false,
            activePodcastDialogueIndex = 0,
            siblingMessages = listOf(
                SiblingChatMessage(
                    id = "sm_1",
                    senderProfileId = defaultProfiles[0].id,
                    senderName = "Aarav",
                    senderEmoji = "🚀",
                    text = "Diya, I added the photolysis equations to the shared scratchpad! Check it out."
                ),
                SiblingChatMessage(
                    id = "sm_2",
                    senderProfileId = defaultProfiles[1].id,
                    senderName = "Diya",
                    senderEmoji = "✨",
                    text = "Awesome bhaiya! Can we listen to the Deep Dive audio podcast together?"
                )
            )
        )
    )
    val sharedStudyRoom: StateFlow<SharedStudyRoom> = _sharedStudyRoom.asStateFlow()

    fun updateSharedScratchpad(text: String) {
        _sharedStudyRoom.update { it.copy(sharedScratchpadText = text) }
    }

    fun sendSiblingMessage(text: String, isSticker: Boolean = false) {
        val currentProfile = activeProfile
        val newMsg = SiblingChatMessage(
            id = "sm_${System.currentTimeMillis()}",
            senderProfileId = currentProfile.id,
            senderName = currentProfile.name.split(" ").firstOrNull() ?: currentProfile.name,
            senderEmoji = currentProfile.avatarEmoji,
            text = text,
            isStickerOrReaction = isSticker
        )
        _sharedStudyRoom.update {
            it.copy(siblingMessages = it.siblingMessages + newMsg)
        }
    }

    fun toggleCoListening() {
        _sharedStudyRoom.update {
            it.copy(isCoListeningPodcast = !it.isCoListeningPodcast)
        }
    }

    // --- STANDARD PROFILE & APP ACTIONS ---
    fun switchProfile(profileId: String) {
        val target = _profiles.value.find { it.id == profileId } ?: return
        _activeProfileId.value = target.id
    }

    fun authenticateWithEmailPassword(email: String, pass: String): Result<StudentProfile> {
        val cleanEmail = email.trim().lowercase()
        val prof = _profiles.value.find { it.email.lowercase() == cleanEmail }
            ?: return Result.failure(IllegalArgumentException("Student profile not found for $email"))

        if (prof.password != pass.trim()) {
            return Result.failure(IllegalArgumentException("Incorrect student password. Default is study123"))
        }

        _activeProfileId.value = prof.id
        _isAuthenticated.value = true
        return Result.success(prof)
    }

    fun registerStudentWithEmailPassword(
        name: String,
        email: String,
        pass: String,
        cbseClass: Int,
        stream: String,
        schoolName: String,
        avatarEmoji: String,
        colorHex: Long
    ): Result<StudentProfile> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid student email address."))
        }
        if (pass.trim().length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        if (_profiles.value.any { it.email.lowercase() == cleanEmail }) {
            return Result.failure(IllegalArgumentException("A profile with this email already exists."))
        }

        val newProfile = StudentProfile(
            id = "prof_${System.currentTimeMillis()}",
            name = name.trim().ifEmpty { "Student" },
            email = cleanEmail,
            cbseClass = cbseClass.coerceIn(1, 12),
            stream = if (cbseClass >= 11) stream else "General",
            schoolName = schoolName.trim().ifEmpty { "CBSE Affiliated School" },
            avatarEmoji = avatarEmoji,
            avatarColorHex = colorHex,
            studyStreakDays = 1,
            knowledgePoints = 120,
            bookmarkedChapterIds = emptySet(),
            isGoogleLinked = false,
            password = pass.trim()
        )
        _profiles.update { it + newProfile }
        _activeProfileId.value = newProfile.id
        _isAuthenticated.value = true

        val welcomeMsg = ChatMessage(
            id = "msg_welcome_${newProfile.id}",
            sender = MessageSender.FLUID_TUTOR,
            text = "Welcome to StudyFluid, ${newProfile.name}! 🎉 Your Class ${newProfile.cbseClass} NCERT vault and spaced revision schedules are now initialized.",
            citationBadges = listOf("NCERT Cl.${newProfile.cbseClass}")
        )
        _profileChats.update { current ->
            current + (newProfile.id to listOf(welcomeMsg))
        }

        return Result.success(newProfile)
    }

    fun onGoogleSignInSuccess(email: String?, displayName: String?, uid: String) {
        val cleanEmail = email?.trim()?.lowercase().orEmpty()
        val matching = _profiles.value.find { it.email.lowercase() == cleanEmail }
        if (matching != null) {
            _profiles.update { list ->
                list.map { if (it.id == matching.id) it.copy(isGoogleLinked = true, firebaseUid = uid) else it }
            }
            _activeProfileId.value = matching.id
        } else {
            val newProfile = StudentProfile(
                id = "prof_${System.currentTimeMillis()}",
                name = displayName?.ifBlank { "CBSE Scholar" } ?: "CBSE Scholar",
                email = cleanEmail.ifEmpty { "student_${uid.take(6)}@studyfluid.cbse" },
                cbseClass = 10,
                stream = "General",
                schoolName = "Delhi Public School",
                avatarEmoji = "🚀",
                avatarColorHex = 0xFF6366F1,
                studyStreakDays = 1,
                knowledgePoints = 150,
                bookmarkedChapterIds = emptySet(),
                isGoogleLinked = true,
                firebaseUid = uid,
                password = "study123"
            )
            _profiles.update { it + newProfile }
            _activeProfileId.value = newProfile.id
        }
        _isAuthenticated.value = true
    }

    fun bypassAuthForDemo(profileId: String? = null) {
        if (profileId != null) {
            _activeProfileId.value = profileId
        }
        _isAuthenticated.value = true
    }

    fun signOutStudent() {
        try {
            Firebase.auth.signOut()
        } catch (_: Throwable) {}
        _isAuthenticated.value = false
    }

    // --- ROOM DATABASE LOCAL VAULT OPERATIONS ---
    fun getRoomVaultsForActiveProfile(): Flow<List<StudyVaultEntity>> {
        return studyVaultDao.getVaultsForProfile(_activeProfileId.value)
    }

    fun getRoomItemsForVault(vaultId: String): Flow<List<VaultItemEntity>> {
        return studyVaultDao.getItemsForVault(vaultId)
    }

    suspend fun createRoomVault(
        title: String,
        category: String,
        description: String,
        emoji: String = "📁",
        colorHex: Long = 0xFF6366F1
    ): StudyVaultEntity {
        val vault = StudyVaultEntity(
            vaultId = "vault_${System.currentTimeMillis()}",
            studentProfileId = _activeProfileId.value,
            title = title.trim().ifEmpty { "My Study Vault" },
            category = category.trim().ifEmpty { "General CBSE" },
            description = description.trim(),
            iconEmoji = emoji,
            colorHex = colorHex
        )
        studyVaultDao.insertVault(vault)
        return vault
    }

    suspend fun addRoomVaultItem(
        vaultId: String,
        title: String,
        content: String,
        itemType: String = "NOTE",
        chapterId: String? = null,
        citationTag: String? = null
    ): VaultItemEntity {
        val item = VaultItemEntity(
            itemId = "item_${System.currentTimeMillis()}",
            vaultId = vaultId,
            studentProfileId = _activeProfileId.value,
            chapterId = chapterId,
            title = title.trim().ifEmpty { "Study Note" },
            content = content.trim(),
            itemType = itemType,
            ncertCitationTag = citationTag
        )
        studyVaultDao.insertItem(item)
        val vault = studyVaultDao.getVaultById(vaultId)
        if (vault != null) {
            studyVaultDao.updateVault(vault.copy(itemCount = vault.itemCount + 1, updatedAt = System.currentTimeMillis()))
        }
        return item
    }

    suspend fun deleteRoomVault(vault: StudyVaultEntity) {
        studyVaultDao.deleteVault(vault)
    }

    suspend fun deleteRoomVaultItem(item: VaultItemEntity) {
        studyVaultDao.deleteItem(item)
        val vault = studyVaultDao.getVaultById(item.vaultId)
        if (vault != null && vault.itemCount > 0) {
            studyVaultDao.updateVault(vault.copy(itemCount = vault.itemCount - 1, updatedAt = System.currentTimeMillis()))
        }
    }

    // --- FEATURE 4: ADAPTIVE MISTAKE VAULT & WEAKNESS RADAR ---
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMistakesForActiveProfile(): Flow<List<MistakeEntity>> {
        return _activeProfileId.flatMapLatest { id -> mistakeDao.getAllMistakesForProfile(id) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUnmasteredMistakesForActiveProfile(): Flow<List<MistakeEntity>> {
        return _activeProfileId.flatMapLatest { id -> mistakeDao.getUnmasteredMistakesForProfile(id) }
    }

    suspend fun recordMistake(
        chapterId: String,
        subject: String,
        topic: String,
        questionText: String,
        studentAnswer: String,
        correctAnswer: String,
        explanation: String,
        mistakeCategory: String = "Conceptual Gap",
        ncertCitation: String = ""
    ): MistakeEntity {
        val mistake = MistakeEntity(
            id = "mistake_${System.currentTimeMillis()}",
            studentProfileId = _activeProfileId.value,
            chapterId = chapterId,
            subject = subject,
            topic = topic,
            questionText = questionText,
            studentAnswer = studentAnswer,
            correctAnswer = correctAnswer,
            explanation = explanation,
            mistakeCategory = mistakeCategory,
            ncertCitation = ncertCitation
        )
        mistakeDao.insertMistake(mistake)
        return mistake
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTopWeakChaptersForActiveProfile(): Flow<List<com.example.data.local.room.ChapterErrorSummary>> {
        return _activeProfileId.flatMapLatest { id -> mistakeDao.getTopWeakChapters(id) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubjectWiseErrorAggregation(): Flow<List<com.example.data.local.room.SubjectErrorSummary>> {
        return _activeProfileId.flatMapLatest { id -> mistakeDao.getSubjectWiseErrorAggregation(id) }
    }

    suspend fun healMistake(mistakeId: String) {
        mistakeDao.markMistakeMastered(mistakeId)
        incrementStreakAndXp(40)
    }

    suspend fun incrementMistakeError(mistakeId: String) {
        mistakeDao.incrementErrorCount(mistakeId)
    }

    suspend fun toggleMistakeMastered(mistakeId: String, isMastered: Boolean) {
        mistakeDao.setMasteredStatus(mistakeId, isMastered)
        if (isMastered) {
            incrementStreakAndXp(40)
        }
    }

    // --- FEATURE 5: CBSE EXAM BOARD PAPER BLUEPRINT SIMULATOR ---
    fun getMockExamPaperForActiveProfile(): CbseExamPaper {
        val cbseClass = activeProfile.cbseClass
        return CbseExamPaper(
            id = "cbse_mock_${cbseClass}_2026",
            title = "CBSE Class $cbseClass Science Official Board Mock Paper 2026",
            subject = "Science",
            cbseClass = cbseClass,
            totalMarks = 80,
            durationMinutes = 180,
            generalInstructions = listOf(
                "This question paper consists of 39 questions in 5 sections (A, B, C, D, and E).",
                "Section A comprises 20 MCQs carrying 1 mark each.",
                "Section B comprises 6 Short Answer type questions carrying 2 marks each.",
                "Section C comprises 7 Short Answer type questions carrying 3 marks each.",
                "Section D comprises 3 Long Answer type questions carrying 5 marks each.",
                "Section E comprises 3 source-based/case-based integrated units carrying 4 marks each."
            ),
            questions = listOf(
                // Section A
                ExamQuestion(
                    id = "q1",
                    questionNumber = 1,
                    section = ExamSectionType.SECTION_A,
                    questionText = "Which of the following represents a thermal decomposition reaction?",
                    marks = 1,
                    options = listOf(
                        "Option 1: 2H₂ + O₂ ➔ 2H₂O",
                        "Option 2: 2FeSO₄(s) ➔ Fe₂O₃(s) + SO₂(g) + SO₃(g)",
                        "Option 3: Zn + CuSO₄ ➔ ZnSO₄ + Cu",
                        "Option 4: CaO + H₂O ➔ Ca(OH)₂"
                    ),
                    correctOptionIndex = 1,
                    sampleSolution = "Heating ferrous sulphate crystals gives ferric oxide, sulphur dioxide and sulphur trioxide.",
                    markingSchemeRubric = "Correct option (b): 1 Mark"
                ),
                ExamQuestion(
                    id = "q2",
                    questionNumber = 2,
                    section = ExamSectionType.SECTION_A,
                    questionText = "The focal length of a concave lens is 15 cm. At what distance should the object be placed so that it forms an image at 10 cm from the lens?",
                    marks = 1,
                    options = listOf(
                        "Option 1: -30 cm",
                        "Option 2: -15 cm",
                        "Option 3: +30 cm",
                        "Option 4: -10 cm"
                    ),
                    correctOptionIndex = 0,
                    sampleSolution = "1/f = 1/v - 1/u. For concave lens f = -15, v = -10. 1/u = 1/(-10) - 1/(-15) = -1/30 => u = -30 cm.",
                    markingSchemeRubric = "Correct option (a): 1 Mark"
                ),
                ExamQuestion(
                    id = "q3",
                    questionNumber = 3,
                    section = ExamSectionType.SECTION_A,
                    questionText = "Which substance is oxidized in: MnO₂ + 4HCl ➔ MnCl₂ + 2H₂O + Cl₂?",
                    marks = 1,
                    options = listOf(
                        "Option 1: MnO₂",
                        "Option 2: HCl",
                        "Option 3: MnCl₂",
                        "Option 4: Cl₂"
                    ),
                    correctOptionIndex = 1,
                    sampleSolution = "HCl loses hydrogen to form Cl₂, hence HCl is oxidized.",
                    markingSchemeRubric = "Correct option (b): 1 Mark"
                ),
                // Section B
                ExamQuestion(
                    id = "q21",
                    questionNumber = 21,
                    section = ExamSectionType.SECTION_B,
                    questionText = "State the reason why respiration is considered an exothermic reaction. Write the balanced chemical equation.",
                    marks = 2,
                    sampleSolution = "During digestion, food is broken down into glucose. Glucose combines with oxygen in the cells of our body and provides energy. Hence, respiration is an exothermic process.\nC₆H₁₂O₆ + 6O₂ ➔ 6CO₂ + 6H₂O + Energy (ATP)",
                    markingSchemeRubric = "Reason: 1 Mark. Balanced chemical equation with states: 1 Mark."
                ),
                ExamQuestion(
                    id = "q22",
                    questionNumber = 22,
                    section = ExamSectionType.SECTION_B,
                    questionText = "A ray of light enters from water to glass. State whether it bends towards or away from the normal. What happens to its wavelength and speed?",
                    marks = 2,
                    sampleSolution = "Glass is optically denser than water (n_glass ~ 1.5 > n_water ~ 1.33). Light bends towards the normal. Speed and wavelength both decrease while frequency remains constant.",
                    markingSchemeRubric = "Bending towards normal: 1 Mark. Speed and wavelength decrease: 1 Mark."
                ),
                // Section C
                ExamQuestion(
                    id = "q27",
                    questionNumber = 27,
                    section = ExamSectionType.SECTION_C,
                    questionText = "Explain the three pathways of breakdown of glucose in different living organisms with schematic flowchart and ATP yields.",
                    marks = 3,
                    sampleSolution = "1. Cytoplasm: Glucose (6-C) ➔ Pyruvate (3-C) + Energy.\n2. In Yeast (Anaerobic): Pyruvate ➔ Ethanol + CO₂ + 2 ATP.\n3. In Muscle cells (Lack of O₂): Pyruvate ➔ Lactic acid + Energy.\n4. In Mitochondria (Aerobic): Pyruvate + O₂ ➔ 6CO₂ + 6H₂O + 38 ATP.",
                    markingSchemeRubric = "Flowchart schematic: 1 Mark. Pathway differentiation: 1 Mark. Correct end products and ATP yield: 1 Mark."
                ),
                // Section D
                ExamQuestion(
                    id = "q34",
                    questionNumber = 34,
                    section = ExamSectionType.SECTION_D,
                    questionText = "(a) State Snell's law of refraction.\n(b) A 4 cm tall object is placed perpendicular to the principal axis of a convex lens of focal length 20 cm. The distance of the object from the lens is 15 cm. Find the nature, position and size of the image formed.",
                    marks = 5,
                    sampleSolution = "(a) Snell's law: The ratio of sine of angle of incidence to sine of angle of refraction is a constant for a given pair of media (sin i / sin r = constant = n₂₁).\n(b) h₁ = 4 cm, f = +20 cm, u = -15 cm.\n1/v - 1/u = 1/f => 1/v = 1/20 - 1/15 = -1/60 => v = -60 cm.\nImage formed at 60 cm in front of lens on same side as object.\nm = v/u = (-60)/(-15) = +4.\nh₂ = m * h₁ = 4 * 4 = +16 cm.\nNature: Virtual, erect, and magnified 4 times.",
                    markingSchemeRubric = "Snell's Law statement: 1 Mark. Formula stating: 1 Mark. Substitution and v = -60 cm: 1.5 Marks. Magnification & size h₂ = 16 cm: 1 Mark. Nature of image stated: 0.5 Mark."
                ),
                // Section E
                ExamQuestion(
                    id = "q37",
                    questionNumber = 37,
                    section = ExamSectionType.SECTION_E,
                    questionText = "Case Study: In electric power transmission, power loss in cables is governed by Joule's heating law (H = I²Rt). To minimize power losses, electric current is stepped up to high voltage.\n(i) State Joule's law of heating mathematically.\n(ii) Why is high voltage preferred over high current for long-distance grid transmission?\n(iii) An electric iron of resistance 20 Ω takes a current of 5 A. Calculate the heat developed in 30 seconds.",
                    marks = 4,
                    sampleSolution = "(i) H = I²Rt where I is current, R is resistance and t is time.\n(ii) For transmitting power P = VI, higher voltage allows lower current I = P/V. Since cable heat dissipation varies as I²R, reducing current significantly minimizes thermal power loss.\n(iii) H = (5)² * 20 * 30 = 25 * 600 = 15,000 Joules = 15 kJ.",
                    markingSchemeRubric = "(i) 1 Mark. (ii) 1.5 Marks. (iii) 1.5 Marks."
                )
            )
        )
    }

    fun addProfile(
        name: String,
        email: String,
        cbseClass: Int,
        stream: String,
        schoolName: String,
        avatarEmoji: String,
        colorHex: Long
    ) {
        val newProfile = StudentProfile(
            id = "prof_${System.currentTimeMillis()}",
            name = name.trim().ifEmpty { "Student" },
            email = email.trim().ifEmpty { "student@cbse.studyfluid.ai" },
            cbseClass = cbseClass.coerceIn(1, 12),
            stream = if (cbseClass >= 11) stream else "General",
            schoolName = schoolName.trim().ifEmpty { "CBSE Affiliated School" },
            avatarEmoji = avatarEmoji,
            avatarColorHex = colorHex,
            studyStreakDays = 1,
            knowledgePoints = 100,
            bookmarkedChapterIds = emptySet()
        )
        _profiles.update { it + newProfile }
        _activeProfileId.value = newProfile.id

        val welcomeMsg = ChatMessage(
            id = "msg_welcome_${newProfile.id}",
            sender = MessageSender.FLUID_TUTOR,
            text = "Welcome aboard, ${newProfile.name}! 🎉 I've calibrated your workspace for CBSE Class ${newProfile.cbseClass}. Use the Camera Doubt Scanner or offline vault anytime!",
            citationBadges = listOf("NCERT Cl.${newProfile.cbseClass}")
        )
        _profileChats.update { current ->
            current + (newProfile.id to listOf(welcomeMsg))
        }
    }

    fun updateActiveProfileDetails(name: String, cbseClass: Int, stream: String, schoolName: String) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    prof.copy(
                        name = name.trim().ifEmpty { prof.name },
                        cbseClass = cbseClass.coerceIn(1, 12),
                        stream = if (cbseClass >= 11) stream else "General",
                        schoolName = schoolName.trim().ifEmpty { prof.schoolName }
                    )
                } else prof
            }
        }
    }

    fun toggleBookmarkChapter(chapterId: String) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    val updated = if (prof.bookmarkedChapterIds.contains(chapterId)) {
                        prof.bookmarkedChapterIds - chapterId
                    } else {
                        prof.bookmarkedChapterIds + chapterId
                    }
                    prof.copy(bookmarkedChapterIds = updated)
                } else prof
            }
        }
    }

    fun addChatMessage(profileId: String, message: ChatMessage) {
        _profileChats.update { current ->
            val existing = current[profileId] ?: emptyList()
            current + (profileId to (existing + message))
        }
    }

    fun clearChat(profileId: String) {
        _profileChats.update { current ->
            current + (profileId to emptyList())
        }
    }

    fun addNote(chapterId: String, title: String, content: String, citationTag: String) {
        val newNote = StudentNote(
            id = "note_${System.currentTimeMillis()}",
            profileId = _activeProfileId.value,
            chapterId = chapterId,
            title = title,
            content = content,
            ncertCitationTag = citationTag
        )
        _notes.update { listOf(newNote) + it }
    }

    fun deleteNote(noteId: String) {
        _notes.update { it.filterNot { n -> n.id == noteId } }
    }

    fun incrementStreakAndXp(xpGain: Int = 25) {
        val currentId = _activeProfileId.value
        _profiles.update { list ->
            list.map { prof ->
                if (prof.id == currentId) {
                    prof.copy(
                        knowledgePoints = prof.knowledgePoints + xpGain
                    )
                } else prof
            }
        }
        checkThemeUnlocks()
    }

    companion object {
        val defaultProfiles = listOf(
            StudentProfile(
                id = "prof_aarav_cl10",
                name = "Aarav Sharma",
                email = "aarav.sharma24@gmail.com",
                cbseClass = 10,
                stream = "General",
                schoolName = "Delhi Public School, R.K. Puram",
                avatarEmoji = "🚀",
                avatarColorHex = 0xFF6366F1,
                studyStreakDays = 7,
                knowledgePoints = 640,
                bookmarkedChapterIds = setOf("cl10_sci_ch1", "cl10_sci_ch6")
            ),
            StudentProfile(
                id = "prof_diya_cl7",
                name = "Diya Sharma (Sibling)",
                email = "diya.sharma29@gmail.com",
                cbseClass = 7,
                stream = "General",
                schoolName = "Modern School, Vasant Vihar",
                avatarEmoji = "✨",
                avatarColorHex = 0xFF06B6D4,
                studyStreakDays = 4,
                knowledgePoints = 320,
                bookmarkedChapterIds = emptySet()
            )
        )

        val defaultThemes = listOf(
            CosmeticTheme("theme_indigo", "Cyber Indigo", 0xFF6366F1, 0xFF06B6D4, 1, true),
            CosmeticTheme("theme_emerald", "Aurora Emerald", 0xFF10B981, 0xFF06B6D4, 2, true),
            CosmeticTheme("theme_amber", "Solar Flare Amber", 0xFFF59E0B, 0xFFEF4444, 3, true),
            CosmeticTheme("theme_violet", "Nebula Violet", 0xFF8B5CF6, 0xFFEC4899, 4, false),
            CosmeticTheme("theme_rose", "Quantum Rose", 0xFFF43F5E, 0xFF8B5CF6, 5, false)
        )

        val initialSkillNodes = listOf(
            SkillNode(
                id = "node_chem_1",
                chapterId = "cl10_sci_ch1",
                title = "Chemical Equations & Mass Conservation",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 1,
                xpReward = 50,
                isUnlocked = true,
                isMastered = true,
                progressPercent = 1.0f,
                connectedNodeIds = listOf("node_chem_2", "node_bio_1"),
                iconEmoji = "⚗️"
            ),
            SkillNode(
                id = "node_chem_2",
                chapterId = "cl10_sci_ch1",
                title = "Redox & Whitewash Reactions",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 75,
                isUnlocked = true,
                isMastered = false,
                progressPercent = 0.65f,
                connectedNodeIds = listOf("node_chem_3"),
                iconEmoji = "🔥"
            ),
            SkillNode(
                id = "node_chem_3",
                chapterId = "cl10_sci_ch1",
                title = "Exemplar Board Mastery (Corrosion/Rancidity)",
                subjectName = "Chemistry",
                cbseClass = 10,
                nodeTier = 3,
                xpReward = 100,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = emptyList(),
                iconEmoji = "👑"
            ),
            SkillNode(
                id = "node_bio_1",
                chapterId = "cl10_sci_ch6",
                title = "Photosynthesis & Stomata Dynamics",
                subjectName = "Biology",
                cbseClass = 10,
                nodeTier = 1,
                xpReward = 50,
                isUnlocked = true,
                isMastered = true,
                progressPercent = 1.0f,
                connectedNodeIds = listOf("node_bio_2"),
                iconEmoji = "🌿"
            ),
            SkillNode(
                id = "node_bio_2",
                chapterId = "cl10_sci_ch6",
                title = "Double Circulation & Nephron Ultrafiltration",
                subjectName = "Biology",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 80,
                isUnlocked = true,
                isMastered = false,
                progressPercent = 0.4f,
                connectedNodeIds = listOf("node_opt_1"),
                iconEmoji = "🫀"
            ),
            SkillNode(
                id = "node_opt_1",
                chapterId = "cl10_sci_ch10",
                title = "Geometric Optics & Snell's Law",
                subjectName = "Physics",
                cbseClass = 10,
                nodeTier = 2,
                xpReward = 90,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = listOf("node_quad_1"),
                iconEmoji = "🔦"
            ),
            SkillNode(
                id = "node_quad_1",
                chapterId = "cl10_math_ch4",
                title = "Quadratic Discriminant & Sridharacharya",
                subjectName = "Mathematics",
                cbseClass = 10,
                nodeTier = 3,
                xpReward = 120,
                isUnlocked = false,
                isMastered = false,
                progressPercent = 0.0f,
                connectedNodeIds = emptyList(),
                iconEmoji = "📐"
            )
        )

        val initialTasks = listOf(
            RevisionTask(
                id = "task_today_1",
                profileId = "prof_aarav_cl10",
                title = "Daily Spaced Recall: Chemical Reactions (Ch.1)",
                chapterId = "cl10_sci_ch1",
                chapterTitle = "Chemical Reactions and Equations",
                subjectName = "Chemistry",
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.FLASHCARD_SPACED,
                isCompleted = false,
                estimatedMinutes = 10,
                xpReward = 25
            ),
            RevisionTask(
                id = "task_today_2",
                profileId = "prof_aarav_cl10",
                title = "Nephron Diagram & Double Circulation Quiz",
                chapterId = "cl10_sci_ch6",
                chapterTitle = "Life Processes",
                subjectName = "Biology",
                dueDateOffsetDays = 0,
                taskType = RevisionTaskType.BOARD_MCQ_QUIZ,
                isCompleted = true,
                estimatedMinutes = 15,
                xpReward = 35
            ),
            RevisionTask(
                id = "task_tomorrow_1",
                profileId = "prof_aarav_cl10",
                title = "Convex Lens Sign Convention Derivation",
                chapterId = "cl10_sci_ch10",
                chapterTitle = "Light – Reflection & Refraction",
                subjectName = "Physics",
                dueDateOffsetDays = 1,
                taskType = RevisionTaskType.FORMULA_REWRITE,
                isCompleted = false,
                estimatedMinutes = 20,
                xpReward = 40
            ),
            RevisionTask(
                id = "task_spaced_1",
                profileId = "prof_aarav_cl10",
                title = "Listen to Two-Host Deep Dive on Quadratic Equations",
                chapterId = "cl10_math_ch4",
                chapterTitle = "Quadratic Equations",
                subjectName = "Mathematics",
                dueDateOffsetDays = 3,
                taskType = RevisionTaskType.AUDIO_PODCAST_REVIEW,
                isCompleted = false,
                estimatedMinutes = 16,
                xpReward = 30
            )
        )
    }
}
