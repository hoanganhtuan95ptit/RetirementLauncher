package com.simple.launcher.retirement.utils

import java.text.Normalizer
import kotlin.text.iterator

/**
 * Tiện ích xử lý chuỗi tiếng Việt — bỏ dấu (normalize), so sánh không dấu.
 *
 * Dùng Unicode Normalizer NFD để tách dấu ra thành combining marks,
 * sau đó regex loại bỏ tất cả combining marks → kết quả là chuỗi không dấu.
 *
 * Ví dụ:
 *   "Nguyễn Văn Ân" → "Nguyen Van An"
 *   "Bác sĩ"        → "Bac si"
 */
object VietnameseStringUtils {

    // Regex khớp tất cả Unicode combining diacritical marks (U+0300 – U+036F)
    // plus combining marks supplement range
    private val DIACRITICS_REGEX = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    // Các ký tự đặc biệt tiếng Việt (đ/Đ) không bị NFD tách dấu → phải thay thủ công
    private val SPECIAL_CHARS = mapOf(
        'đ' to 'd',
        'Đ' to 'D'
    )

    /**
     * Bỏ toàn bộ dấu tiếng Việt, trả về chuỗi ASCII-friendly, giữ nguyên case.
     */
    fun removeDiacritics(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(SPECIAL_CHARS[c] ?: c)
        }
        val normalized = Normalizer.normalize(sb.toString(), Normalizer.Form.NFD)
        return DIACRITICS_REGEX.replace(normalized, "")
    }

    /**
     * So sánh `source` có chứa `query` hay không, hỗ trợ không dấu.
     *
     * @return `true` nếu `source` chứa `query` (case-insensitive, diacritics-insensitive).
     */
    fun containsIgnoreDiacritics(source: String, query: String): Boolean {
        // Fast path: so sánh trực tiếp trước (có dấu khớp có dấu)
        if (source.contains(query, ignoreCase = true)) return true
        // Slow path: bỏ dấu rồi so sánh
        val normalizedSource = removeDiacritics(source).lowercase()
        val normalizedQuery = removeDiacritics(query).lowercase()
        return normalizedSource.contains(normalizedQuery)
    }

    /**
     * Kiểm tra `source` có **bắt đầu** bằng `query` không (diacritics-insensitive).
     * Dùng để ưu tiên kết quả "khớp đầu" trước "chứa giữa/cuối".
     */
    fun startsWithIgnoreDiacritics(source: String, query: String): Boolean {
        if (source.startsWith(query, ignoreCase = true)) return true
        val normalizedSource = removeDiacritics(source).lowercase()
        val normalizedQuery = removeDiacritics(query).lowercase()
        return normalizedSource.startsWith(normalizedQuery)
    }

    /**
     * Kiểm tra `source` có **khớp chính xác** `query` không (diacritics-insensitive).
     */
    fun equalsIgnoreDiacritics(source: String, query: String): Boolean {
        if (source.equals(query, ignoreCase = true)) return true
        val normalizedSource = removeDiacritics(source).lowercase()
        val normalizedQuery = removeDiacritics(query).lowercase()
        return normalizedSource == normalizedQuery
    }
}