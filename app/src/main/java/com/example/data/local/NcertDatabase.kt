package com.example.data.local

import com.example.data.model.*

object NcertDatabase {

    fun getSubjectsForClass(cbseClass: Int): List<NcertSubject> {
        return when (cbseClass) {
            10 -> listOf(class10Science, class10Maths, class10SocialScience)
            12 -> listOf(class12Physics, class12Chemistry, class12Maths)
            9 -> listOf(class9Science, class9Maths)
            8 -> listOf(class8Science, class8Maths)
            else -> listOf(
                createGenericSubject(cbseClass, "Science & Environmental Explorations", 0xFF06B6D4),
                createGenericSubject(cbseClass, "Mathematics & Problem Solving", 0xFF6366F1),
                createGenericSubject(cbseClass, "Languages & English Literature", 0xFF8B5CF6)
            )
        }
    }

    fun getAllChaptersForClass(cbseClass: Int): List<NcertChapter> {
        return getSubjectsForClass(cbseClass).flatMap { it.chapters }
    }

    fun getChapterById(chapterId: String): NcertChapter? {
        val allClasses = listOf(10, 12, 9, 8, 7, 6, 11)
        for (c in allClasses) {
            val found = getAllChaptersForClass(c).find { it.id == chapterId }
            if (found != null) return found
        }
        return class10ChemicalReactions
    }

    // --- CLASS 10 SCIENCE ---
    val class10ChemicalReactions = NcertChapter(
        id = "cl10_sci_ch1",
        chapterNumber = 1,
        title = "Chemical Reactions and Equations",
        subjectId = "cl10_sci",
        subjectName = "Science (Chemistry)",
        cbseClass = 10,
        estimatedReadMinutes = 18,
        summary = "Comprehensive breakdown of chemical transformations, balancing stoichiometric equations, redox mechanisms, exothermic vs endothermic reactions, and corrosion & rancidity prevention in daily life.",
        keyFormulasAndLaws = listOf(
            "Law of Conservation of Mass: Total mass of reactants = Total mass of products.",
            "Combination: A + B -> AB (e.g., CaO(s) + H₂O(l) -> Ca(OH)₂(aq) + Heat)",
            "Decomposition: Thermal (2FeSO₄ -> Fe₂O₃ + SO₂ + SO₃), Electrolytic (2H₂O -> 2H₂ + O₂), Photolytic (2AgCl -> 2Ag + Cl₂)",
            "Displacement: Fe + CuSO₄(aq) -> FeSO₄(aq) + Cu(s)",
            "Redox: Oxidation is Gain of Oxygen / Loss of Hydrogen; Reduction is Loss of Oxygen / Gain of Hydrogen."
        ),
        citations = listOf(
            NcertCitation(
                pageNumber = 6,
                sectionTitle = "1.1 Writing a Chemical Equation",
                excerpt = "A skeletal chemical equation represents a symbolic summary. We balance it using the hit-and-trial method because atoms are neither created nor destroyed according to Antoine Lavoisier's conservation principle.",
                citationBadge = "NCERT Cl.10 Sci, Ch.1, p.6"
            ),
            NcertCitation(
                pageNumber = 10,
                sectionTitle = "1.2 Types of Chemical Reactions: Exothermic & Quicklime",
                excerpt = "Calcium oxide reacts vigorously with water to produce slaked lime (calcium hydroxide) releasing a large amount of heat. Such reactions where heat is evolved are called exothermic chemical reactions.",
                citationBadge = "NCERT Cl.10 Sci, Ch.1, p.10"
            ),
            NcertCitation(
                pageNumber = 13,
                sectionTitle = "1.3 Effects of Oxidation: Corrosion and Rancidity",
                excerpt = "When fats and oils are oxidized, they become rancid and their smell and taste change. Usually antioxidants are added, and chips packets are flushed with nitrogen gas to prevent rancidity.",
                citationBadge = "NCERT Cl.10 Sci, Ch.1, p.13"
            )
        ),
        sampleBoardQuestions = listOf(
            BoardExamQuestion(
                year = "CBSE Board 2024 (3 Marks)",
                question = "A shiny brown-coloured element 'X' on heating in air becomes black in colour. Name element 'X' and the black coloured compound formed. Write a balanced chemical equation.",
                modelAnswer = "Element 'X' is Copper (Cu). The black compound formed is Copper(II) oxide (CuO).\nReaction: 2Cu(s) + O₂(g) --[Heat]--> 2CuO(s).",
                markingSchemeTip = "State element name (1 mark), compound name (1 mark), and balanced equation with states (1 mark)."
            ),
            BoardExamQuestion(
                year = "CBSE Board 2023 (2 Marks)",
                question = "Why does the colour of copper sulphate solution change when an iron nail is dipped in it?",
                modelAnswer = "Iron is more reactive than copper (placed higher in electrochemical activity series). Iron displaces copper from copper sulphate solution forming light green iron(II) sulphate (FeSO₄) while reddish-brown copper deposits on the nail.",
                markingSchemeTip = "Mention reactivity series displacement and color transition from blue to pale green."
            )
        ),
        studyGuide = ChapterStudyGuide(
            executiveSummary = "The foundational chapter for CBSE Class 10 Chemistry. Mastering balancing, thermal/photolytic decomposition, and redox identification guarantees 5-6 marks in Section A & B of the Board Exam.",
            coreConcepts = listOf(
                "Balancing chemical equations via atom counting tables",
                "Slaked lime formation and subsequent whitewash reaction with atmospheric CO₂",
                "Redox analysis: Identifying oxidizing vs reducing agents",
                "Electrolysis of water: 2:1 volume ratio of Hydrogen to Oxygen at cathode vs anode"
            ),
            examWatchouts = listOf(
                "Never change the chemical formula subscript to balance; only adjust coefficients!",
                "Cathode collects Hydrogen gas (double volume); Anode collects Oxygen gas.",
                "Always write physical state notations: (s), (l), (g), (aq) for maximum marks."
            ),
            historicalContextOrTimeline = listOf(
                "1789: Antoine Lavoisier publishes Law of Conservation of Mass",
                "1800: Voltaic pile enables first water electrolysis experiment",
                "NCERT Syllabus: Core chemical kinetics and equilibrium concepts"
            )
        ),
        flashcards = listOf(
            FlashCard("fc1", "What gas is released when dilute HCl reacts with Zinc granules?", "Hydrogen gas (H₂). Test: Burns with a characteristic 'pop' sound.", "Reactions", "NCERT Ch.1, p.4"),
            FlashCard("fc2", "Why is respiration considered an exothermic process?", "Glucose combines with inhaled oxygen in cells to release CO₂, water, and vital energy (ATP).", "Bio-Chemistry", "NCERT Ch.1, p.11"),
            FlashCard("fc3", "What happens when silver chloride is exposed to sunlight?", "Photolytic decomposition: White AgCl turns grey forming silver metal and chlorine gas (2AgCl -> 2Ag + Cl₂). Used in black & white photography.", "Photochemistry", "NCERT Ch.1, p.9"),
            FlashCard("fc4", "Define Rancidity and name the inert gas used in snack packaging.", "Oxidation of fats/oils causing foul odour and bad taste. Nitrogen gas is flushed to displace oxygen.", "Real-World Chemistry", "NCERT Ch.1, p.13")
        ),
        quizQuestions = listOf(
            QuizQuestion(
                id = "q1",
                question = "In the reaction: CuO + H₂ -> Cu + H₂O, which substance undergoes oxidation?",
                options = listOf("CuO", "H₂", "Cu", "H₂O"),
                correctIndex = 1,
                explanation = "Hydrogen (H₂) gains oxygen to become H₂O, hence Hydrogen is oxidized. CuO loses oxygen, so it is reduced.",
                ncertReference = "NCERT Page 12, Section 1.2.5 Redox"
            ),
            QuizQuestion(
                id = "q2",
                question = "During electrolysis of acidified water, what is the ratio of volumes of gas collected at cathode and anode?",
                options = listOf("1 : 1", "2 : 1", "1 : 2", "3 : 1"),
                correctIndex = 1,
                explanation = "Water formula is H₂O. At cathode, 2 volumes of H₂ gas collect; at anode, 1 volume of O₂ gas collects. Ratio is 2:1.",
                ncertReference = "NCERT Page 9, Activity 1.7"
            ),
            QuizQuestion(
                id = "q3",
                question = "Which of the following is an endothermic reaction?",
                options = listOf("Respiration", "Burning of natural gas", "Decomposition of calcium carbonate into CaO and CO₂", "Dissolution of quicklime in water"),
                correctIndex = 2,
                explanation = "Decomposition of limestone (CaCO₃) requires continuous external heating to form quicklime (CaO) and carbon dioxide, making it endothermic.",
                ncertReference = "NCERT Page 10"
            )
        ),
        podcastScript = listOf(
            PodcastDialogue("Aarav", "Concept Host", "Welcome back to StudyFluid's Deep Dive! Today Priya and I are unpacking CBSE Class 10 Chapter 1: Chemical Reactions and Equations. Priya, why does CBSE love testing the whitewash wall reaction?"),
            PodcastDialogue("Priya", "Exam Strategist", "Great question, Aarav! Because students mix up quicklime and slaked lime. When you add water to quicklime (CaO), it sizzles exothermically into slaked lime Ca(OH)₂. Then over 2-3 days, atmospheric CO₂ turns it into a shiny coat of calcium carbonate!"),
            PodcastDialogue("Aarav", "Concept Host", "Right, that's CaCO₃! And what about the decomposition of ferrous sulphate crystals? That's always a 3-marker on board exams."),
            PodcastDialogue("Priya", "Exam Strategist", "Spot on! Green FeSO₄·7H₂O loses water of crystallization when heated, turning white, and then decomposes into brown ferric oxide (Fe₂O₃) with the choking smell of burning sulphur: SO₂ and SO₃ gases."),
            PodcastDialogue("Aarav", "Concept Host", "Remember students: always write state symbols (s), (g), and balance your equations using Antoine Lavoisier's conservation of mass!")
        ),
        mindMapRoot = MindMapNode(
            id = "mm_root",
            label = "Chemical Reactions & Equations",
            subtext = "CBSE Class 10 Chemistry",
            children = listOf(
                MindMapNode(
                    id = "mm_types",
                    label = "Types of Reactions",
                    subtext = "Core Categories",
                    children = listOf(
                        MindMapNode("mm_comb", "Combination (CaO + H₂O -> Ca(OH)₂)"),
                        MindMapNode("mm_decomp", "Decomposition (Thermal, Electric, Light)"),
                        MindMapNode("mm_disp", "Displacement & Double Displacement (Precipitation)"),
                        MindMapNode("mm_redox", "Redox (Oxidation & Reduction simultaneously)")
                    )
                ),
                MindMapNode(
                    id = "mm_energy",
                    label = "Energy Changes",
                    subtext = "Enthalpy flow",
                    children = listOf(
                        MindMapNode("mm_exo", "Exothermic (Heat released e.g. Respiration)"),
                        MindMapNode("mm_endo", "Endothermic (Heat absorbed e.g. Photosynthesis)")
                    )
                ),
                MindMapNode(
                    id = "mm_daily",
                    label = "Real-World Effects",
                    children = listOf(
                        MindMapNode("mm_corrosion", "Corrosion (Rusting of iron, green copper coating)"),
                        MindMapNode("mm_rancidity", "Rancidity (Fat oxidation, prevented by N₂ gas)")
                    )
                )
            )
        )
    )

    val class10LifeProcesses = NcertChapter(
        id = "cl10_sci_ch6",
        chapterNumber = 6,
        title = "Life Processes",
        subjectId = "cl10_sci",
        subjectName = "Science (Biology)",
        cbseClass = 10,
        estimatedReadMinutes = 22,
        summary = "Comprehensive exploration of nutrition (autotrophic vs heterotrophic), human digestive system, respiration (aerobic vs anaerobic), double circulation in the human heart, and nephron excretion mechanics.",
        keyFormulasAndLaws = listOf(
            "Photosynthesis: 6CO₂ + 12H₂O + Chlorophyll + Sunlight -> C₆H₁₂O₆ + 6O₂ + 6H₂O",
            "Aerobic Breakdown: Glucose (6C) -> Pyruvate (3C) in cytoplasm -> CO₂ + H₂O + 38 ATP in mitochondria",
            "Anaerobic in Yeast: Pyruvate -> Ethanol + CO₂ + 2 ATP (Fermentation)",
            "Anaerobic in Human Muscle: Pyruvate -> Lactic Acid + Energy (causes muscle cramps)"
        ),
        citations = listOf(
            NcertCitation(
                pageNumber = 95,
                sectionTitle = "6.2 Autotrophic Nutrition & Stomata",
                excerpt = "Chloroplasts contain chlorophyll that absorbs light energy. Guard cells swell when water flows into them, causing stomatal pores to open for gaseous exchange.",
                citationBadge = "NCERT Cl.10 Sci, Ch.6, p.95"
            ),
            NcertCitation(
                pageNumber = 106,
                sectionTitle = "6.4 Double Circulation in Human Heart",
                excerpt = "Blood goes through the heart twice during each cycle in vertebrates. The left ventricle pumps oxygenated blood to body tissues via aorta, while right ventricle pumps deoxygenated blood to lungs via pulmonary artery.",
                citationBadge = "NCERT Cl.10 Sci, Ch.6, p.106"
            ),
            NcertCitation(
                pageNumber = 110,
                sectionTitle = "6.5 Excretion in Human Beings & Nephron",
                excerpt = "Each kidney contains approximately 1 million nephrons. Filtration occurs in the glomerulus inside Bowman's capsule, followed by selective reabsorption of glucose, amino acids, salts, and major water volume.",
                citationBadge = "NCERT Cl.10 Sci, Ch.6, p.110"
            )
        ),
        sampleBoardQuestions = listOf(
            BoardExamQuestion(
                year = "CBSE Board 2024 (5 Marks)",
                question = "Describe the pathway of oxygenated and deoxygenated blood in the human heart with the help of a schematic diagram. Why is double circulation necessary?",
                modelAnswer = "Double circulation separates oxygenated and deoxygenated blood. This prevents intermixing, ensuring highly efficient oxygen delivery required by warm-blooded mammals to constantly maintain their body temperature.",
                markingSchemeTip = "Draw 4 chambers, label bicuspid/tricuspid valves, pulmonary artery/vein, and explain separation advantage."
            )
        ),
        studyGuide = ChapterStudyGuide(
            executiveSummary = "Highest weightage chapter in Class 10 Biology (8-9 marks). Prioritize the 3 breakdown pathways of glucose, nephron structure, and bile juice emulsification.",
            coreConcepts = listOf(
                "Enzymes: Salivary amylase (starch to maltose), Pepsin (proteins in acidic stomach), Trypsin & Lipase (pancreatic juice)",
                "Xylem (unidirectional water transport via transpiration pull) vs Phloem (bidirectional translocation via ATP)",
                "Nephron: Glomerular ultrafiltration, tubular reabsorption, secretion"
            ),
            examWatchouts = listOf(
                "Never confuse Pulmonary Vein (carries oxygenated blood) with systemic veins!",
                "HCl provides acidic pH for pepsin and kills ingested pathogens.",
                "Emulsification: Bile salts convert large fat globules into tiny droplets for lipase action."
            ),
            historicalContextOrTimeline = listOf(
                "1628: William Harvey discovers systemic blood circulation",
                "1842: William Bowman details the nephron microcapsule",
                "NCERT Paradigm: Bioenergetics and homeostasis"
            )
        ),
        flashcards = listOf(
            FlashCard("fc_lp1", "Why do aquatic organisms have a faster breathing rate than terrestrial animals?", "Because dissolved oxygen in water is significantly lower than oxygen in the atmospheric air.", "Respiration", "NCERT Ch.6, p.103"),
            FlashCard("fc_lp2", "What is the structural and functional unit of the human kidney?", "Nephron (comprising Glomerulus, Bowman's capsule, Henle's loop, and collecting duct).", "Excretion", "NCERT Ch.6, p.110"),
            FlashCard("fc_lp3", "What is the role of hydrochloric acid (HCl) in our stomach?", "Kills ingested bacteria and creates an acidic medium (pH 1.5-2) necessary for enzyme pepsin to activate.", "Digestion", "NCERT Ch.6, p.99")
        ),
        quizQuestions = listOf(
            QuizQuestion(
                id = "q_lp1",
                question = "Which part of the alimentary canal receives bile from the liver and pancreatic juice from the pancreas?",
                options = listOf("Stomach", "Duodenum (Small Intestine)", "Large Intestine", "Oesophagus"),
                correctIndex = 1,
                explanation = "The small intestine (specifically the duodenum) receives secretions from both the liver (bile) and pancreas (pancreatic juice) via the hepatopancreatic duct.",
                ncertReference = "NCERT Page 100, Section 6.2.3"
            )
        ),
        podcastScript = listOf(
            PodcastDialogue("Aarav", "Concept Host", "Hello friends! In this StudyFluid Audio Overview, Priya and I are tackling Life Processes: The Big Four - Nutrition, Respiration, Transportation, and Excretion."),
            PodcastDialogue("Priya", "Exam Strategist", "Aarav, students always ask: how do I remember the three pathways of glucose breakdown?"),
            PodcastDialogue("Aarav", "Concept Host", "Simple! All pathways start with glycolysis in the cytoplasm making pyruvate. From there: aerobic in mitochondria gives 38 ATP; anaerobic in yeast makes ethanol; and muscle sprint gives lactic acid, which causes cramps!"),
            PodcastDialogue("Priya", "Exam Strategist", "Brilliant summary! And on excretion, remember that selective reabsorption in the nephron tubule ensures valuable amino acids, glucose, and water aren't wasted.")
        ),
        mindMapRoot = MindMapNode(
            id = "mm_lp_root",
            label = "Human Life Processes",
            subtext = "Class 10 Biology",
            children = listOf(
                MindMapNode("mm_nut", "Nutrition (Autotrophic, Digestive Enzymes, Villi)"),
                MindMapNode("mm_resp", "Respiration (Glycolysis, Aerobic vs Anaerobic)"),
                MindMapNode("mm_trans", "Circulation (Double Circulation, Heart, Xylem/Phloem)"),
                MindMapNode("mm_excr", "Excretion (Kidney, Glomerulus, Dialysis)")
            )
        )
    )

    val class10LightOptics = NcertChapter(
        id = "cl10_sci_ch10",
        chapterNumber = 10,
        title = "Light – Reflection and Refraction",
        subjectId = "cl10_sci",
        subjectName = "Science (Physics)",
        cbseClass = 10,
        estimatedReadMinutes = 20,
        summary = "Laws of reflection, spherical mirrors (concave and convex), mirror formula, linear magnification, refraction, Snell's law, refractive index, thin lenses, and power of a lens.",
        keyFormulasAndLaws = listOf(
            "Mirror Formula: 1/f = 1/v + 1/u",
            "Lens Formula: 1/f = 1/v - 1/u",
            "Magnification (Mirror): m = -v/u = h'/h",
            "Magnification (Lens): m = +v/u = h'/h",
            "Snell's Law: sin(i) / sin(r) = Constant = n₂₁ (Refractive index)",
            "Power of Lens: P = 1 / f (in metres), unit is Dioptre (D)"
        ),
        citations = listOf(
            NcertCitation(
                pageNumber = 168,
                sectionTitle = "10.2 Spherical Mirrors & Sign Convention",
                excerpt = "According to New Cartesian Sign Convention, object is always placed to the left of the mirror (u is always negative). Distances measured in the direction of incident light are taken as positive.",
                citationBadge = "NCERT Cl.10 Sci, Ch.10, p.168"
            ),
            NcertCitation(
                pageNumber = 175,
                sectionTitle = "10.3 Refraction & Absolute Refractive Index",
                excerpt = "The absolute refractive index n = c / v, where c is speed of light in vacuum (3 × 10⁸ m/s) and v is speed of light in the medium.",
                citationBadge = "NCERT Cl.10 Sci, Ch.10, p.175"
            )
        ),
        sampleBoardQuestions = listOf(
            BoardExamQuestion(
                year = "CBSE Board 2024 (3 Marks)",
                question = "A convex lens of focal length 20 cm forms a real, inverted image at a distance of 40 cm. Find the object distance and magnification.",
                modelAnswer = "Given: f = +20 cm, v = +40 cm. By lens formula 1/f = 1/v - 1/u => 1/u = 1/v - 1/f = 1/40 - 1/20 = -1/40. Hence u = -40 cm. Magnification m = v/u = 40/(-40) = -1. Image is same size and at 2F₁.",
                markingSchemeTip = "State signs correctly (+ for real image in lens), step-by-step substitution, and conclude with image characteristics."
            )
        ),
        studyGuide = ChapterStudyGuide(
            executiveSummary = "Numerical powerhouse chapter! Sign convention errors cost students the most marks. Keep mirror formula vs lens formula signs crystal clear.",
            coreConcepts = listOf(
                "Concave mirror: Can form real inverted and virtual erect images (when object between F and P)",
                "Convex mirror: Always forms virtual, erect, and diminished images (used in rear-view vehicle mirrors)",
                "Refractive index and optical density vs mass density",
                "Combination of lenses: Total power P = P₁ + P₂"
            ),
            examWatchouts = listOf(
                "Never forget arrows on ray diagrams! Missing arrows forfeit 1 full mark.",
                "Power of convex lens is Positive (+); power of concave lens is Negative (-).",
                "Object distance 'u' is ALWAYS negative in Cartesian sign convention."
            ),
            historicalContextOrTimeline = listOf(
                "1621: Willebrord Snellius discovers laws of refraction",
                "1672: Isaac Newton conducts prism light spectrum experiments",
                "NCERT Geometric Optics principles"
            )
        ),
        flashcards = listOf(
            FlashCard("fc_opt1", "Why is a convex mirror preferred as a rear-view mirror in vehicles?", "It always produces an erect, diminished image and gives a much wider field of view.", "Mirrors", "NCERT Ch.10, p.167"),
            FlashCard("fc_opt2", "What is the SI unit of power of a lens?", "Dioptre (D). 1 Dioptre is the power of a lens with focal length of 1 metre.", "Lenses", "NCERT Ch.10, p.184"),
            FlashCard("fc_opt3", "State Snell's Law of Refraction.", "The ratio of sine of angle of incidence to sine of angle of refraction is constant for a given pair of media (sin i / sin r = constant).", "Refraction", "NCERT Ch.10, p.174")
        ),
        quizQuestions = listOf(
            QuizQuestion(
                id = "q_opt1",
                question = "Where should an object be placed in front of a convex lens to get a real image of the size of the object?",
                options = listOf("At the principal focus", "At twice the focal length (2F₁)", "At infinity", "Between optical centre and focus"),
                correctIndex = 1,
                explanation = "When placed at 2F₁, the refracted rays converge at 2F₂ on the opposite side, producing a real, inverted image of exactly the same size (m = -1).",
                ncertReference = "NCERT Page 179, Table 10.4"
            )
        ),
        podcastScript = listOf(
            PodcastDialogue("Aarav", "Concept Host", "Priya, ray diagrams scare a lot of Class 10 students. How can they conquer Light Reflection and Refraction?"),
            PodcastDialogue("Priya", "Exam Strategist", "Aarav, here is my golden rule: always draw the principal axis first, mark F and 2F symmetrically, and trace two rays: one parallel through focus, one through optical centre. And double check your signs!")
        ),
        mindMapRoot = MindMapNode(
            id = "mm_opt_root",
            label = "Geometric Optics (Light)",
            subtext = "Class 10 Physics",
            children = listOf(
                MindMapNode("mm_mirrors", "Spherical Mirrors (Concave, Convex, 1/f = 1/v + 1/u)"),
                MindMapNode("mm_refr", "Refraction & Snell's Law (sin i / sin r = n₂₁)"),
                MindMapNode("mm_lenses", "Lenses (Convex, Concave, 1/f = 1/v - 1/u)"),
                MindMapNode("mm_power", "Power of Lens (P = 1/f in m, Dioptres)")
            )
        )
    )

    // --- CLASS 10 MATHS ---
    val class10QuadraticEquations = NcertChapter(
        id = "cl10_math_ch4",
        chapterNumber = 4,
        title = "Quadratic Equations",
        subjectId = "cl10_math",
        subjectName = "Mathematics",
        cbseClass = 10,
        estimatedReadMinutes = 16,
        summary = "Standard form ax² + bx + c = 0 (a ≠ 0), factorization by splitting middle term, quadratic formula derivation by Sridharacharya, nature of roots based on discriminant D = b² - 4ac, and word problem modeling.",
        keyFormulasAndLaws = listOf(
            "Standard Form: ax² + bx + c = 0, where a, b, c ∈ ℝ and a ≠ 0",
            "Quadratic Formula: x = [-b ± √(b² - 4ac)] / (2a)",
            "Discriminant: D = b² - 4ac",
            "If D > 0: Two distinct real roots",
            "If D = 0: Two equal real roots (x = -b / 2a)",
            "If D < 0: No real roots (complex conjugates)"
        ),
        citations = listOf(
            NcertCitation(
                pageNumber = 71,
                sectionTitle = "4.2 Quadratic Equations & Formulation",
                excerpt = "Any equation of the form p(x) = 0, where p(x) is a polynomial of degree 2, is a quadratic equation. Historically used by Babylonians to calculate areas of lands.",
                citationBadge = "NCERT Cl.10 Math, Ch.4, p.71"
            ),
            NcertCitation(
                pageNumber = 88,
                sectionTitle = "4.4 Nature of Roots & Discriminant",
                excerpt = "Since b² - 4ac determines whether the quadratic equation has real roots or not, b² - 4ac is called the discriminant of this quadratic equation.",
                citationBadge = "NCERT Cl.10 Math, Ch.4, p.88"
            )
        ),
        sampleBoardQuestions = listOf(
            BoardExamQuestion(
                year = "CBSE Board 2024 (3 Marks)",
                question = "Find the value of k for which the quadratic equation (k - 12)x² + 2(k - 12)x + 2 = 0 has equal real roots.",
                modelAnswer = "For equal roots, D = b² - 4ac = 0.\nHere a = (k - 12), b = 2(k - 12), c = 2.\nD = [2(k - 12)]² - 4(k - 12)(2) = 4(k - 12)² - 8(k - 12) = 0.\n4(k - 12)[(k - 12) - 2] = 0 => (k - 12)(k - 14) = 0.\nSince a ≠ 0, k cannot be 12. Hence k = 14.",
                markingSchemeTip = "State condition D = 0, reject k = 12 because coefficient 'a' cannot be zero, conclude k = 14."
            )
        ),
        studyGuide = ChapterStudyGuide(
            executiveSummary = "Guaranteed 4-5 marks on every CBSE Class 10 Math board paper. Word problems involving speed-distance-time, tap filling pipes, and consecutive integers are high probability.",
            coreConcepts = listOf(
                "Splitting the middle term technique for quick factorization",
                "Application of Quadratic formula for non-factorable roots",
                "Discriminant conditions (D > 0, D = 0, D < 0)"
            ),
            examWatchouts = listOf(
                "Always check if a ≠ 0 when solving for parameter k!",
                "In speed/time word problems, reject negative solutions with a clear reason."
            ),
            historicalContextOrTimeline = listOf(
                "628 CE: Brahmagupta provides explicit solution method in Brahma-sphuta-siddhanta",
                "1025 CE: Sridharacharya establishes the quadratic formula method"
            )
        ),
        flashcards = listOf(
            FlashCard("fc_quad1", "What is the condition for a quadratic equation ax² + bx + c = 0 to have two equal real roots?", "The discriminant must be zero: D = b² - 4ac = 0.", "Algebra", "NCERT Ch.4, p.88"),
            FlashCard("fc_quad2", "Write the Quadratic Formula (Sridharacharya's Rule).", "x = (-b ± √(b² - 4ac)) / (2a)", "Formula", "NCERT Ch.4, p.82")
        ),
        quizQuestions = listOf(
            QuizQuestion(
                id = "q_qd1",
                question = "The discriminant of the equation 2x² - 4x + 3 = 0 is:",
                options = listOf("-8", "8", "-20", "16"),
                correctIndex = 0,
                explanation = "D = b² - 4ac = (-4)² - 4(2)(3) = 16 - 24 = -8. Since D < 0, it has no real roots.",
                ncertReference = "NCERT Page 89, Ex 4.4"
            )
        ),
        podcastScript = listOf(
            PodcastDialogue("Aarav", "Concept Host", "Welcome back! Today Aarav and Priya are tackling Quadratic Equations. Priya, what is the #1 mistake students make with parameter 'k' questions?"),
            PodcastDialogue("Priya", "Exam Strategist", "Aarav, students find k = 12 and k = 14, and write both down! But if k = 12, then (k - 12)x² disappears and it is no longer quadratic! Always reject k = 12.")
        ),
        mindMapRoot = MindMapNode(
            id = "mm_qd_root",
            label = "Quadratic Equations",
            subtext = "Class 10 Mathematics",
            children = listOf(
                MindMapNode("mm_def", "Definition (ax² + bx + c = 0, a ≠ 0)"),
                MindMapNode("mm_sol", "Methods (Factorization, Quadratic Formula)"),
                MindMapNode("mm_roots", "Nature of Roots (D > 0, D = 0, D < 0)"),
                MindMapNode("mm_apps", "Word Problems (Speed/Distance, Work, Geometry)")
            )
        )
    )

    // --- SUBJECT COLLECTIONS ---
    val class10Science = NcertSubject(
        id = "cl10_sci",
        name = "Science (Physics, Chem, Bio)",
        iconName = "science",
        colorHex = 0xFF06B6D4,
        cbseClass = 10,
        chapters = listOf(class10ChemicalReactions, class10LifeProcesses, class10LightOptics)
    )

    val class10Maths = NcertSubject(
        id = "cl10_math",
        name = "Mathematics (Standard & Basic)",
        iconName = "calculate",
        colorHex = 0xFF6366F1,
        cbseClass = 10,
        chapters = listOf(class10QuadraticEquations)
    )

    val class10SocialScience = NcertSubject(
        id = "cl10_sst",
        name = "Social Science (Hist, Geo, Pol)",
        iconName = "public",
        colorHex = 0xFFF59E0B,
        cbseClass = 10,
        chapters = listOf(
            NcertChapter(
                id = "cl10_sst_ch1",
                chapterNumber = 1,
                title = "Nationalism in Europe & India",
                subjectId = "cl10_sst",
                subjectName = "Social Science (History)",
                cbseClass = 10,
                estimatedReadMinutes = 20,
                summary = "Rise of the nation-state, French Revolution ideals, Giuseppe Mazzini, unification of Germany and Italy, and Gandhi's Non-Cooperation and Civil Disobedience movements.",
                keyFormulasAndLaws = listOf(
                    "1789: French Revolution introduces sovereignty to people",
                    "1804: Napoleonic Code (Civil Code) abolishes feudal privileges",
                    "1919: Rowlatt Act and Jallianwala Bagh Massacre (13 April 1919)",
                    "1930: Dandi March and Salt Satyagraha initiation"
                ),
                citations = listOf(
                    NcertCitation(14, "Civil Disobedience Movement", "On 31 January 1930, Mahatma Gandhi sent a letter to Viceroy Irwin stating eleven demands. The most stirring was to abolish the salt tax.", "NCERT Cl.10 Hist, p.14")
                ),
                sampleBoardQuestions = emptyList(),
                studyGuide = ChapterStudyGuide(
                    executiveSummary = "Core history pillar covering modern democratic revolutions and India's anti-colonial freedom struggle.",
                    coreConcepts = listOf("Liberal Nationalism", "Simon Commission protests", "Poona Pact 1932"),
                    examWatchouts = listOf("Map pointing questions are frequently taken from this chapter (Nagpur, Madras sessions)."),
                    historicalContextOrTimeline = listOf("1915: Gandhi returns to India", "1920: Non-Cooperation adopted", "1931: Gandhi-Irwin Pact")
                ),
                flashcards = listOf(
                    FlashCard("fc_sst1", "Who was Frederic Sorrieu and what did he paint in 1848?", "A French artist who prepared a series of four prints visualizing a world made up of 'Democratic and Social Republics'.", "European History", "NCERT Hist, p.3")
                ),
                quizQuestions = emptyList(),
                podcastScript = emptyList(),
                mindMapRoot = MindMapNode("mm_sst", "Nationalism Movements")
            )
        )
    )

    // --- CLASS 12 SUBJECTS ---
    val class12Physics = NcertSubject(
        id = "cl12_phy",
        name = "Physics (CBSE Senior Secondary)",
        iconName = "bolt",
        colorHex = 0xFF3B82F6,
        cbseClass = 12,
        chapters = listOf(
            NcertChapter(
                id = "cl12_phy_ch1",
                chapterNumber = 1,
                title = "Electric Charges and Fields",
                subjectId = "cl12_phy",
                subjectName = "Physics",
                cbseClass = 12,
                estimatedReadMinutes = 25,
                summary = "Coulomb's Law in vector form, electric field intensity due to dipole at axial and equatorial points, Gauss's Theorem and its applications to line charge and infinite plane sheet.",
                keyFormulasAndLaws = listOf(
                    "Coulomb's Law: F = (1 / 4πε₀) * (|q₁q₂| / r²)",
                    "Electric Dipole Moment: p = q * 2a (directed negative to positive)",
                    "Gauss's Law: ∮ E · dA = q_enclosed / ε₀",
                    "Electric Field of Dipole (Axial): E = (1 / 4πε₀) * (2p / r³)",
                    "Electric Field of Dipole (Equatorial): E = (1 / 4πε₀) * (p / r³)"
                ),
                citations = listOf(
                    NcertCitation(18, "Gauss's Law Applications", "Electric field due to an infinitely long straight uniformly charged wire is E = λ / (2πε₀r).", "NCERT Cl.12 Phy, Ch.1, p.18")
                ),
                sampleBoardQuestions = listOf(
                    BoardExamQuestion("CBSE 2024 (3 Marks)", "Derive the expression for electric field intensity at an axial point of an electric dipole.", "Derivation using Coulomb superposition yields E = 2pr / [4πε₀(r² - a²)²]. For short dipole, E = 2p / (4πε₀r³).", "Show vector directions explicitly.")
                ),
                studyGuide = ChapterStudyGuide(
                    executiveSummary = "Foundational chapter for Electrostatics (Unit 1, 16 marks combined with Potential & Capacitance).",
                    coreConcepts = listOf("Quantization of charge q = ne", "Electric flux Φ = E · A", "Gauss surfaces"),
                    examWatchouts = listOf("Always show vector arrows on Coulomb's law formulas!"),
                    historicalContextOrTimeline = listOf("1785: Charles-Augustin de Coulomb confirms inverse square force")
                ),
                flashcards = listOf(
                    FlashCard("fc_c12_1", "What is the net electric flux through a closed Gaussian surface enclosing no charge?", "Zero, according to Gauss's Law (∮ E · dA = 0).", "Electrostatics", "NCERT Cl.12, p.22")
                ),
                quizQuestions = listOf(
                    QuizQuestion("q_12_1", "The ratio of electric field due to a short dipole at an axial point to that at an equatorial point at the same distance is:", listOf("1 : 1", "2 : 1", "1 : 2", "4 : 1"), 1, "Axial field is 2p/(4πε₀r³), equatorial is p/(4πε₀r³). The ratio is 2 : 1.", "NCERT Ch.1")
                ),
                podcastScript = listOf(
                    PodcastDialogue("Aarav", "Concept Host", "Class 12 Physics begins with Gauss's Law! Priya, why is Gauss's Law so much simpler than integrating Coulomb's law?"),
                    PodcastDialogue("Priya", "Exam Strategist", "Because symmetry turns messy integrals into simple products! If you choose a cylindrical Gaussian surface for a wire, the flux through the curved surface gives E = λ/(2πε₀r) directly.")
                ),
                mindMapRoot = MindMapNode("mm_c12_phy", "Electrostatics")
            )
        )
    )

    val class12Chemistry = NcertSubject(
        id = "cl12_chem",
        name = "Chemistry (Senior Secondary)",
        iconName = "biotech",
        colorHex = 0xFF10B981,
        cbseClass = 12,
        chapters = listOf(
            NcertChapter(
                id = "cl12_chem_ch1",
                chapterNumber = 1,
                title = "Solutions & Colligative Properties",
                subjectId = "cl12_chem",
                subjectName = "Chemistry",
                cbseClass = 12,
                estimatedReadMinutes = 20,
                summary = "Raoult's Law for volatile solutes, ideal vs non-ideal solutions with positive/negative deviations, colligative properties (relative lowering of vapour pressure, elevation of boiling point, depression of freezing point, osmotic pressure), and Van't Hoff factor (i).",
                keyFormulasAndLaws = listOf(
                    "Raoult's Law: P₁ = x₁ * P₁°",
                    "Elevation of Boiling Point: ΔTb = i * Kb * m",
                    "Depression of Freezing Point: ΔTf = i * Kf * m",
                    "Osmotic Pressure: π = i * C * R * T",
                    "Van't Hoff factor: i = 1 + (n - 1)α (for dissociation)"
                ),
                citations = listOf(
                    NcertCitation(45, "Colligative Properties & Van't Hoff", "Properties that depend only on the number of solute particles irrespective of their nature are colligative properties.", "NCERT Cl.12 Chem, p.45")
                ),
                sampleBoardQuestions = emptyList(),
                studyGuide = ChapterStudyGuide(
                    executiveSummary = "High-scoring physical chemistry chapter (7 marks). Numericals on Van't Hoff factor 'i' are guaranteed.",
                    coreConcepts = listOf("Henry's Law", "Azeotropes", "Abnormal molar mass"),
                    examWatchouts = listOf("Never forget to calculate i for ionic electrolytes like NaCl (i ≈ 2) or K₂SO₄ (i ≈ 3)."),
                    historicalContextOrTimeline = listOf("1887: Jacobus van 't Hoff publishes laws of chemical equilibrium and osmotic pressure")
                ),
                flashcards = emptyList(),
                quizQuestions = emptyList(),
                podcastScript = emptyList(),
                mindMapRoot = MindMapNode("mm_c12_chem", "Solutions")
            )
        )
    )

    val class12Maths = NcertSubject(
        id = "cl12_math",
        name = "Mathematics (Calculus & Vectors)",
        iconName = "functions",
        colorHex = 0xFF8B5CF6,
        cbseClass = 12,
        chapters = emptyList()
    )

    // --- CLASS 9 SUBJECTS ---
    val class9Science = NcertSubject(
        id = "cl9_sci",
        name = "Science (Foundations)",
        iconName = "school",
        colorHex = 0xFF06B6D4,
        cbseClass = 9,
        chapters = listOf(
            NcertChapter(
                id = "cl9_sci_ch9",
                chapterNumber = 9,
                title = "Force and Laws of Motion",
                subjectId = "cl9_sci",
                subjectName = "Science",
                cbseClass = 9,
                estimatedReadMinutes = 18,
                summary = "Newton's three laws of motion, inertia and mass, momentum p = mv, mathematical formulation F = ma, and conservation of linear momentum in collisions.",
                keyFormulasAndLaws = listOf(
                    "First Law: Law of Inertia",
                    "Second Law: F = dp/dt = m * a",
                    "Third Law: Action = -Reaction",
                    "Conservation of Momentum: m₁u₁ + m₂u₂ = m₁v₁ + m₂v₂"
                ),
                citations = listOf(
                    NcertCitation(118, "Newton's Second Law", "The rate of change of momentum of an object is proportional to the applied unbalanced force in the direction of force.", "NCERT Cl.9 Sci, p.118")
                ),
                sampleBoardQuestions = emptyList(),
                studyGuide = ChapterStudyGuide(
                    executiveSummary = "Core mechanical foundation for CBSE secondary physics.",
                    coreConcepts = listOf("Inertia of rest, motion, and direction", "Recoil of a gun", "F = ma derivations"),
                    examWatchouts = listOf("Action and reaction never act on the same body! They act on different bodies."),
                    historicalContextOrTimeline = listOf("1687: Isaac Newton publishes Philosophiæ Naturalis Principia Mathematica")
                ),
                flashcards = listOf(
                    FlashCard("fc_cl9_1", "Why does a fielder pull his hands backward while catching a fast cricket ball?", "By pulling hands back, he increases the time of impact Δt, thereby reducing the rate of change of momentum and impact force (F = Δp/Δt).", "Impulse", "NCERT Cl.9, p.121")
                ),
                quizQuestions = emptyList(),
                podcastScript = emptyList(),
                mindMapRoot = MindMapNode("mm_cl9_sci", "Laws of Motion")
            )
        )
    )

    val class9Maths = NcertSubject(
        id = "cl9_math",
        name = "Mathematics (Polynomials & Geometry)",
        iconName = "calculate",
        colorHex = 0xFF6366F1,
        cbseClass = 9,
        chapters = emptyList()
    )

    val class8Science = NcertSubject(
        id = "cl8_sci",
        name = "Science (Middle School)",
        iconName = "menu_book",
        colorHex = 0xFF10B981,
        cbseClass = 8,
        chapters = emptyList()
    )

    val class8Maths = NcertSubject(
        id = "cl8_math",
        name = "Mathematics (Rational Numbers & Mensuration)",
        iconName = "calculate",
        colorHex = 0xFFF59E0B,
        cbseClass = 8,
        chapters = emptyList()
    )

    private fun createGenericSubject(cbseClass: Int, name: String, colorHex: Long): NcertSubject {
        return NcertSubject(
            id = "subj_cl_${cbseClass}_${name.take(4).lowercase()}",
            name = name,
            iconName = "auto_stories",
            colorHex = colorHex,
            cbseClass = cbseClass,
            chapters = listOf(
                NcertChapter(
                    id = "ch_cl_${cbseClass}_intro",
                    chapterNumber = 1,
                    title = "Foundations of $name (Class $cbseClass)",
                    subjectId = "subj_cl_${cbseClass}_${name.take(4).lowercase()}",
                    subjectName = name,
                    cbseClass = cbseClass,
                    estimatedReadMinutes = 15,
                    summary = "Official CBSE syllabus module for Class $cbseClass covering core concepts, NCERT textbook exercises, and interactive mastery aids.",
                    keyFormulasAndLaws = listOf("Standard CBSE Class $cbseClass curriculum guidelines and foundational definitions."),
                    citations = listOf(
                        NcertCitation(1, "Chapter Syllabus Overview", "Curriculum aligned with National Education Policy (NEP) and NCERT guidelines.", "NCERT Cl.$cbseClass")
                    ),
                    sampleBoardQuestions = emptyList(),
                    studyGuide = ChapterStudyGuide(
                        executiveSummary = "Comprehensive NCERT module for CBSE Class $cbseClass.",
                        coreConcepts = listOf("Core competency mastery", "Step-by-step NCERT solved examples"),
                        examWatchouts = listOf("Read questions carefully and show complete working steps."),
                        historicalContextOrTimeline = emptyList()
                    ),
                    flashcards = listOf(
                        FlashCard("fc_gen_1", "What is the key takeaway of this NCERT module?", "Consistent daily review and grounded NCERT textbook exercises guarantee academic excellence.", "General", "NCERT Cl.$cbseClass")
                    ),
                    quizQuestions = emptyList(),
                    podcastScript = emptyList(),
                    mindMapRoot = MindMapNode("mm_gen", "Class $cbseClass Curriculum")
                )
            )
        )
    }
}
