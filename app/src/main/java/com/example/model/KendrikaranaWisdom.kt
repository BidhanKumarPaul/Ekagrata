package com.example.model

data class SanskritMantra(
    val devanagari: String,
    val transliteration: String,
    val meaning: String,
    val source: String
)

object KendrikaranaWisdom {
    val sutras = listOf(
        SanskritMantra(
            devanagari = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।",
            transliteration = "Yogasthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya",
            meaning = "Established in deep centered stillness, perform your work, freed from attachment to distraction.",
            source = "Bhagavad Gita 2.48"
        ),
        SanskritMantra(
            devanagari = "एकं लक्ष्यम्, एकाग्रता च परमं बलम्।",
            transliteration = "Ekaṁ lakṣyam, ekāgratā ca paramaṁ balam",
            meaning = "One supreme goal. Unwavering one-pointed concentration is supreme power.",
            source = "Sanskrit Aphorism"
        ),
        SanskritMantra(
            devanagari = "मन एव मनुष्याणां कारणं बन्धमोक्षयोः।",
            transliteration = "Mana eva manuṣyāṇāṁ kāraṇaṁ bandha-mokṣayoḥ",
            meaning = "For humanity, the mind alone is the cause of scattered bondage or supreme mastery.",
            source = "Amritabindu Upanishad"
        ),
        SanskritMantra(
            devanagari = "अभ्यासेन तु कौन्तेय वैराग्येण च गृह्यते।",
            transliteration = "Abhyāsena tu kaunteya vairāgyeṇa ca gṛhyate",
            meaning = "Through steady intentional practice and stepping back from transient impulses, the mind is mastered.",
            source = "Bhagavad Gita 6.35"
        ),
        SanskritMantra(
            devanagari = "योगश्चित्तवृत्तिनिरोधः।",
            transliteration = "Yogaś-citta-vṛtti-nirodhaḥ",
            meaning = "True focus is the complete convergence and quieting of restless mental fluctuations.",
            source = "Patanjali Yoga Sutras 1.2"
        ),
        SanskritMantra(
            devanagari = "उद्यमेन हि सिध्यन्ति कार्याणि न मनोरथैः।",
            transliteration = "Udyamena hi sidhyanti kāryāṇi na manorathaiḥ",
            meaning = "Triumphs are attained through dedicated immersion and exertion, not by idle reverie.",
            source = "Hitopadesha"
        ),
        SanskritMantra(
            devanagari = "ज्ञानं ज्ञेयं परिज्ञाता त्रिविधा कर्मचोदना।",
            transliteration = "Jñānaṁ jñeyaṁ parijñātā tri-vidhā karma-codanā",
            meaning = "Knowledge, the object of study, and the focused learner form the tripartite impetus to action.",
            source = "Bhagavad Gita 18.18"
        )
    )

    fun getRandomSutra(): SanskritMantra = sutras.random()
}
