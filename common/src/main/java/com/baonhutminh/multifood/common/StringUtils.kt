package com.baonhutminh.multifood.common

object StringUtils {
    /**
     * Remove Vietnamese diacritics (dấu) và normalize string
     * Ví dụ: "Nguyễn Huệ" -> "nguyen hue", "Đà Nẵng" -> "da nang"
     */
    fun removeVietnameseDiacritics(str: String): String {
        val normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        val removed = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return removed.lowercase().trim()
    }
    
    /**
     * Normalize string để tìm kiếm: remove diacritics, lowercase, trim
     * Sử dụng cho tìm kiếm không phân biệt hoa/thường và có/không dấu
     */
    fun normalizeForSearch(str: String): String {
        return removeVietnameseDiacritics(str)
    }
    
    /**
     * Kiểm tra xem text có chứa query không (không phân biệt hoa/thường, có/không dấu)
     * Ví dụ: containsIgnoreCaseAndDiacritics("Nguyễn Huệ", "nguyen hue") -> true
     */
    fun containsIgnoreCaseAndDiacritics(text: String, query: String): Boolean {
        if (query.isBlank()) return true
        val normalizedText = normalizeForSearch(text)
        val normalizedQuery = normalizeForSearch(query)
        return normalizedText.contains(normalizedQuery)
    }
}



