package com.example.model

data class SanskritMantra(
    val devanagari: String,
    val transliteration: String,
    val meaning: String,
    val source: String,
    val isHanumanChalisa: Boolean = false
)

object KendrikaranaWisdom {

    val allMantras: List<SanskritMantra> = listOf(
        // --- Hanuman Chalisa Core Focus Verses ---
        SanskritMantra(
            devanagari = "बुद्धिहीन तनु जानिके, सुमिरौ पवन-कुमार।\nबल बुद्धि विद्या देहु मोहिं, हरहु कलेश विकार॥",
            transliteration = "Buddhi-hīna tanu jānike, sumirau pavana-kumāra |\nBala buddhi vidyā dehu mohiṁ, harahu kalesha vikāra ||",
            meaning = "Mindful of our mortal limits, I meditate upon Hanuman. Bestow vigor, deep intellect, and clear wisdom; purge every distraction and mental affliction.",
            source = "Shri Hanuman Chalisa (Doha 2)",
            isHanumanChalisa = true
        ),
        SanskritMantra(
            devanagari = "महावीर विक्रम बजरङ्गी।\nकुमति निवार सुमति के सङ्गी॥",
            transliteration = "Mahāvīra vikrama bajaraṅgī |\nKumati nivāra sumati ke saṅgī ||",
            meaning = "O supreme champion of unbreakable resolve! Dispel scattered wanderings of the mind and guide my intellect toward one-pointed noble concentration.",
            source = "Shri Hanuman Chalisa (Chaupai 3)",
            isHanumanChalisa = true
        ),
        SanskritMantra(
            devanagari = "दुर्गम काज जगत के जेते।\nसुगम अनुग्रह तुम्हरे तेते॥",
            transliteration = "Durgama kāja jagata ke jete |\nSugama anugraha tumhare tete ||",
            meaning = "Every challenging task and steep academic mountain in this world becomes effortless through unwavering determination and grace.",
            source = "Shri Hanuman Chalisa (Chaupai 20)",
            isHanumanChalisa = true
        ),
        SanskritMantra(
            devanagari = "नासै रोग हरै सब पीरा।\nजपत निरन्तर हनुमत बीरा॥",
            transliteration = "Nāsai roga harai saba pīrā |\nJapata nirantara hanumata bīrā ||",
            meaning = "All restlessness, fatigue, and cognitive friction dissolve through steadfast, continuous concentration.",
            source = "Shri Hanuman Chalisa (Chaupai 25)",
            isHanumanChalisa = true
        ),
        SanskritMantra(
            devanagari = "संकट तें हनुमान छुड़ावै।\nमन क्रम वचन ध्यान जो लावै॥",
            transliteration = "Saṅkaṭa teṁ hanumāna chuṛāvai |\nMana krama vacana dhyāna jo lāvai ||",
            meaning = "Freed from all obstacles and cognitive turbulence is the student who harmonizes mind, work, and focus with pure commitment.",
            source = "Shri Hanuman Chalisa (Chaupai 26)",
            isHanumanChalisa = true
        ),

        // --- Vedic & Philosophical Focus Mantras ---
        SanskritMantra(
            devanagari = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात्॥",
            transliteration = "Oṁ bhūr bhuvaḥ svaḥ tat-savitur vareṇyaṁ bhargo devasya dhīmahi dhiyo yo naḥ pracodayāt",
            meaning = "We meditate on the supreme solar illumination that enlightens and inspires our intellect with profound clarity.",
            source = "Gayatri Mantra (Rigveda 3.62.10)"
        ),
        SanskritMantra(
            devanagari = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय।",
            transliteration = "Yogasthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya",
            meaning = "Anchored in inward stillness, carry out your purposeful work, detached from trivial distractions.",
            source = "Bhagavad Gita 2.48"
        ),
        SanskritMantra(
            devanagari = "अभ्यासेन तु कौन्तेय वैराग्येण च गृह्यते।",
            transliteration = "Abhyāsena tu kaunteya vairāgyeṇa ca gṛhyate",
            meaning = "Through steady, repeated immersion and detachment from impulses, the restless mind is mastered.",
            source = "Bhagavad Gita 6.35"
        ),
        SanskritMantra(
            devanagari = "एकं लक्ष्यम्, एकाग्रता च परमं बलम्।",
            transliteration = "Ekaṁ lakṣyam, ekāgratā ca paramaṁ balam",
            meaning = "One supreme goal. Single-pointed concentration is the highest potency.",
            source = "Sanskrit Concentration Sutra"
        ),
        SanskritMantra(
            devanagari = "योगश्चित्तवृत्तिनिरोधः।",
            transliteration = "Yogaś-citta-vṛtti-nirodhaḥ",
            meaning = "True focus is the mastery and tranquil convergence of mental fluctuations.",
            source = "Patanjali Yoga Sutras 1.2"
        ),
        SanskritMantra(
            devanagari = "उद्यमेन हि सिध्यन्ति कार्याणि न मनोरथैः।",
            transliteration = "Udyamena hi sidhyanti kāryāṇi na manorathaiḥ",
            meaning = "Triumphs and mastery are attained through diligent immersion, never through idle dreaming.",
            source = "Hitopadesha"
        )
    )

    fun getFilteredMantras(includeHanumanChalisa: Boolean): List<SanskritMantra> {
        return if (includeHanumanChalisa) {
            allMantras
        } else {
            allMantras.filter { !it.isHanumanChalisa }
        }
    }
}
