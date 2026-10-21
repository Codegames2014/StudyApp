package com.example.data.model

enum class ArModelType {
    MOLECULAR_STRUCTURE,
    CRYSTAL_LATTICE,
    DNA_HELIX,
    GEOMETRIC_SOLID,
    OPTICS_RAY,
    PHYSICS_FIELD
}

data class ArNode3D(
    val id: String,
    val label: String,
    val symbol: String,
    val x: Float, // -1f to 1f normalized coordinates
    val y: Float,
    val z: Float,
    val colorHex: Long,
    val radius: Float = 0.12f,
    val charge: String = ""
)

data class ArBond3D(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val bondOrder: Int = 1, // 1 = Single, 2 = Double, 3 = Triple
    val colorHex: Long = 0xFF8E8EA0
)

data class Ar3dModel(
    val id: String,
    val title: String,
    val formulaOrSymbol: String,
    val subject: String,
    val chapterId: String,
    val description: String,
    val modelType: ArModelType,
    val nodes: List<ArNode3D>,
    val bonds: List<ArBond3D>,
    val ncertCitation: String,
    val funFact: String
)

object ArModelRegistry {
    // 1. Methane CH4 (Tetrahedral Molecular Structure - Class 10/11 Chemistry)
    val methane = Ar3dModel(
        id = "ar_ch4_methane",
        title = "Methane (CH₄) Molecule",
        formulaOrSymbol = "CH₄ (sp³ Hybridized)",
        subject = "Chemistry",
        chapterId = "cl10_sci_ch4",
        description = "Tetrahedral bond angle of 109.5°. Central Carbon atom covalently bonded to 4 Hydrogen atoms.",
        modelType = ArModelType.MOLECULAR_STRUCTURE,
        nodes = listOf(
            ArNode3D("c", "Carbon", "C", 0f, 0f, 0f, 0xFF374151, radius = 0.18f),
            ArNode3D("h1", "Hydrogen", "H", 0f, 0.45f, 0.32f, 0xFF38BDF8, radius = 0.11f),
            ArNode3D("h2", "Hydrogen", "H", 0.42f, -0.22f, 0.32f, 0xFF38BDF8, radius = 0.11f),
            ArNode3D("h3", "Hydrogen", "H", -0.42f, -0.22f, 0.32f, 0xFF38BDF8, radius = 0.11f),
            ArNode3D("h4", "Hydrogen", "H", 0f, 0f, -0.52f, 0xFF38BDF8, radius = 0.11f)
        ),
        bonds = listOf(
            ArBond3D("b1", "c", "h1", 1),
            ArBond3D("b2", "c", "h2", 1),
            ArBond3D("b3", "c", "h3", 1),
            ArBond3D("b4", "c", "h4", 1)
        ),
        ncertCitation = "NCERT Cl.10 Sci, Ch.4 (Carbon and its Compounds), p.58",
        funFact = "CBSE Board favorite: Bond angle is exactly 109°28' due to VSEPR electron repulsion minimization."
    )

    // 2. Water H2O (Bent/V-Shaped Molecule - Class 10/11)
    val water = Ar3dModel(
        id = "ar_h2o_water",
        title = "Water (H₂O) Bent Dipole",
        formulaOrSymbol = "H₂O (104.5° Bent)",
        subject = "Chemistry",
        chapterId = "cl10_sci_ch1",
        description = "Bent molecular geometry due to two lone pairs on Oxygen compressing the bond angle to 104.5°.",
        modelType = ArModelType.MOLECULAR_STRUCTURE,
        nodes = listOf(
            ArNode3D("o", "Oxygen", "O", 0f, 0.15f, 0f, 0xFFEF4444, radius = 0.20f, charge = "δ-"),
            ArNode3D("h1", "Hydrogen", "H", -0.38f, -0.25f, 0f, 0xFF38BDF8, radius = 0.12f, charge = "δ+"),
            ArNode3D("h2", "Hydrogen", "H", 0.38f, -0.25f, 0f, 0xFF38BDF8, radius = 0.12f, charge = "δ+")
        ),
        bonds = listOf(
            ArBond3D("b1", "o", "h1", 1, 0xFFFCA5A5),
            ArBond3D("b2", "o", "h2", 1, 0xFFFCA5A5)
        ),
        ncertCitation = "NCERT Cl.10 Sci, Ch.1 & Ch.3",
        funFact = "Strong hydrogen bonding gives water its uniquely high boiling point and makes ice float on water."
    )

    // 3. Benzene C6H6 (Resonance Planar Hexagon - Class 10/11/12)
    val benzene = Ar3dModel(
        id = "ar_c6h6_benzene",
        title = "Benzene (C₆H₆) Resonance Ring",
        formulaOrSymbol = "C₆H₆ (Aromatic)",
        subject = "Chemistry",
        chapterId = "cl10_sci_ch4",
        description = "Planar hexagonal ring of 6 Carbon atoms with delocalized pi-electrons sharing alternating double bonds.",
        modelType = ArModelType.MOLECULAR_STRUCTURE,
        nodes = listOf(
            ArNode3D("c1", "C1", "C", 0.45f, 0f, 0f, 0xFF374151, radius = 0.14f),
            ArNode3D("c2", "C2", "C", 0.225f, 0.39f, 0f, 0xFF374151, radius = 0.14f),
            ArNode3D("c3", "C3", "C", -0.225f, 0.39f, 0f, 0xFF374151, radius = 0.14f),
            ArNode3D("c4", "C4", "C", -0.45f, 0f, 0f, 0xFF374151, radius = 0.14f),
            ArNode3D("c5", "C5", "C", -0.225f, -0.39f, 0f, 0xFF374151, radius = 0.14f),
            ArNode3D("c6", "C6", "C", 0.225f, -0.39f, 0f, 0xFF374151, radius = 0.14f),
            // Hydrogens
            ArNode3D("h1", "H1", "H", 0.70f, 0f, 0f, 0xFF38BDF8, radius = 0.09f),
            ArNode3D("h2", "H2", "H", 0.35f, 0.60f, 0f, 0xFF38BDF8, radius = 0.09f),
            ArNode3D("h3", "H3", "H", -0.35f, 0.60f, 0f, 0xFF38BDF8, radius = 0.09f),
            ArNode3D("h4", "H4", "H", -0.70f, 0f, 0f, 0xFF38BDF8, radius = 0.09f),
            ArNode3D("h5", "H5", "H", -0.35f, -0.60f, 0f, 0xFF38BDF8, radius = 0.09f),
            ArNode3D("h6", "H6", "H", 0.35f, -0.60f, 0f, 0xFF38BDF8, radius = 0.09f)
        ),
        bonds = listOf(
            ArBond3D("b12", "c1", "c2", 2, 0xFFA78BFA),
            ArBond3D("b23", "c2", "c3", 1, 0xFFCBD5E1),
            ArBond3D("b34", "c3", "c4", 2, 0xFFA78BFA),
            ArBond3D("b45", "c4", "c5", 1, 0xFFCBD5E1),
            ArBond3D("b56", "c5", "c6", 2, 0xFFA78BFA),
            ArBond3D("b61", "c6", "c1", 1, 0xFFCBD5E1),
            ArBond3D("bh1", "c1", "h1", 1),
            ArBond3D("bh2", "c2", "h2", 1),
            ArBond3D("bh3", "c3", "h3", 1),
            ArBond3D("bh4", "c4", "h4", 1),
            ArBond3D("bh5", "c5", "h5", 1),
            ArBond3D("bh6", "c6", "h6", 1)
        ),
        ncertCitation = "NCERT Cl.10 Sci, Ch.4, p.66",
        funFact = "August Kekule famously envisioned this cyclic ring structure after dreaming of a snake seizing its own tail (Ouroboros)."
    )

    // 4. Sodium Chloride NaCl Crystal Unit Cell (Face-Centered Cubic)
    val nacl = Ar3dModel(
        id = "ar_nacl_lattice",
        title = "Sodium Chloride (NaCl) Ionic Lattice",
        formulaOrSymbol = "Na⁺ Cl⁻ (FCC Lattice)",
        subject = "Chemistry",
        chapterId = "cl10_sci_ch3",
        description = "Cubic closed pack lattice where each Na⁺ ion is surrounded octahedrally by 6 Cl⁻ ions.",
        modelType = ArModelType.CRYSTAL_LATTICE,
        nodes = listOf(
            ArNode3D("na1", "Na⁺", "Na", -0.35f, -0.35f, -0.35f, 0xFF818CF8, radius = 0.10f, charge = "+1"),
            ArNode3D("cl1", "Cl⁻", "Cl", 0.35f, -0.35f, -0.35f, 0xFF34D399, radius = 0.16f, charge = "-1"),
            ArNode3D("na2", "Na⁺", "Na", 0.35f, 0.35f, -0.35f, 0xFF818CF8, radius = 0.10f, charge = "+1"),
            ArNode3D("cl2", "Cl⁻", "Cl", -0.35f, 0.35f, -0.35f, 0xFF34D399, radius = 0.16f, charge = "-1"),
            ArNode3D("cl3", "Cl⁻", "Cl", -0.35f, -0.35f, 0.35f, 0xFF34D399, radius = 0.16f, charge = "-1"),
            ArNode3D("na3", "Na⁺", "Na", 0.35f, -0.35f, 0.35f, 0xFF818CF8, radius = 0.10f, charge = "+1"),
            ArNode3D("cl4", "Cl⁻", "Cl", 0.35f, 0.35f, 0.35f, 0xFF34D399, radius = 0.16f, charge = "-1"),
            ArNode3D("na4", "Na⁺", "Na", -0.35f, 0.35f, 0.35f, 0xFF818CF8, radius = 0.10f, charge = "+1")
        ),
        bonds = listOf(
            ArBond3D("c1", "na1", "cl1", 1),
            ArBond3D("c2", "cl1", "na2", 1),
            ArBond3D("c3", "na2", "cl2", 1),
            ArBond3D("c4", "cl2", "na1", 1),
            ArBond3D("c5", "cl3", "na3", 1),
            ArBond3D("c6", "na3", "cl4", 1),
            ArBond3D("c7", "cl4", "na4", 1),
            ArBond3D("c8", "na4", "cl3", 1),
            ArBond3D("c9", "na1", "cl3", 1),
            ArBond3D("c10", "cl1", "na3", 1),
            ArBond3D("c11", "na2", "cl4", 1),
            ArBond3D("c12", "cl2", "na4", 1)
        ),
        ncertCitation = "NCERT Cl.10 Sci, Ch.3 (Metals and Non-metals), p.48",
        funFact = "High electrostatic attraction between oppositely charged ions results in a very high melting point (801°C)."
    )

    // 5. DNA Double Helix Segment (Class 10 Heredity & Evolution / Class 12 Biology)
    val dna = Ar3dModel(
        id = "ar_dna_helix",
        title = "DNA Double Helix Segment",
        formulaOrSymbol = "Watson-Crick B-DNA",
        subject = "Biology",
        chapterId = "cl10_sci_ch9",
        description = "Antiparallel right-handed double helix connected by complementary base pairs (A=T, G≡C).",
        modelType = ArModelType.DNA_HELIX,
        nodes = listOf(
            ArNode3D("s1", "Sugar-P", "S", -0.35f, -0.6f, -0.2f, 0xFF38BDF8, radius = 0.12f),
            ArNode3D("s2", "Sugar-P", "S", 0.35f, -0.6f, 0.2f, 0xFFEC4899, radius = 0.12f),
            ArNode3D("b1", "Adenine (A)", "A", -0.1f, -0.6f, -0.05f, 0xFFF59E0B, radius = 0.10f),
            ArNode3D("b2", "Thymine (T)", "T", 0.1f, -0.6f, 0.05f, 0xFF10B981, radius = 0.10f),

            ArNode3D("s3", "Sugar-P", "S", -0.45f, -0.2f, 0.1f, 0xFF38BDF8, radius = 0.12f),
            ArNode3D("s4", "Sugar-P", "S", 0.45f, -0.2f, -0.1f, 0xFFEC4899, radius = 0.12f),
            ArNode3D("b3", "Guanine (G)", "G", -0.15f, -0.2f, 0.03f, 0xFF8B5CF6, radius = 0.10f),
            ArNode3D("b4", "Cytosine (C)", "C", 0.15f, -0.2f, -0.03f, 0xFF06B6D4, radius = 0.10f),

            ArNode3D("s5", "Sugar-P", "S", -0.2f, 0.2f, 0.35f, 0xFF38BDF8, radius = 0.12f),
            ArNode3D("s6", "Sugar-P", "S", 0.2f, 0.2f, -0.35f, 0xFFEC4899, radius = 0.12f),
            ArNode3D("b5", "Thymine (T)", "T", -0.08f, 0.2f, 0.12f, 0xFF10B981, radius = 0.10f),
            ArNode3D("b6", "Adenine (A)", "A", 0.08f, 0.2f, -0.12f, 0xFFF59E0B, radius = 0.10f),

            ArNode3D("s7", "Sugar-P", "S", 0.15f, 0.6f, 0.4f, 0xFF38BDF8, radius = 0.12f),
            ArNode3D("s8", "Sugar-P", "S", -0.15f, 0.6f, -0.4f, 0xFFEC4899, radius = 0.12f),
            ArNode3D("b7", "Cytosine (C)", "C", 0.05f, 0.6f, 0.15f, 0xFF06B6D4, radius = 0.10f),
            ArNode3D("b8", "Guanine (G)", "G", -0.05f, 0.6f, -0.15f, 0xFF8B5CF6, radius = 0.10f)
        ),
        bonds = listOf(
            ArBond3D("d1", "s1", "s3", 1, 0xFF38BDF8),
            ArBond3D("d2", "s3", "s5", 1, 0xFF38BDF8),
            ArBond3D("d3", "s5", "s7", 1, 0xFF38BDF8),
            ArBond3D("d4", "s2", "s4", 1, 0xFFEC4899),
            ArBond3D("d5", "s4", "s6", 1, 0xFFEC4899),
            ArBond3D("d6", "s6", "s8", 1, 0xFFEC4899),
            // Base pairings
            ArBond3D("bp1", "b1", "b2", 2, 0xFFF59E0B),
            ArBond3D("bp2", "b3", "b4", 3, 0xFF8B5CF6),
            ArBond3D("bp3", "b5", "b6", 2, 0xFF10B981),
            ArBond3D("bp4", "b7", "b8", 3, 0xFF06B6D4)
        ),
        ncertCitation = "NCERT Cl.10 Sci, Ch.9 (Heredity and Evolution), p.142",
        funFact = "The helical pitch is 3.4 nm per turn with approximately 10 base pairs per helical revolution."
    )

    val allModels = listOf(methane, water, benzene, nacl, dna)

    fun getModelForChapter(chapterId: String): Ar3dModel {
        return allModels.find { it.chapterId == chapterId } ?: methane
    }
}
