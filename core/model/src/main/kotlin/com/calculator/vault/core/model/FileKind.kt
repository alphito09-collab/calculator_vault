package com.calculator.vault.core.model

/** What a file is, so it gets a recognisable icon: judged by its extension, then its MIME type. */
enum class FileKind(
    /** Badge text when the real extension is missing or too long to fit. */
    val defaultLabel: String,
) {
    PDF("PDF"),
    WORD("DOC"),
    SPREADSHEET("XLS"),
    PRESENTATION("PPT"),
    TEXT("TXT"),
    CODE("CODE"),
    ARCHIVE("ZIP"),
    APK("APK"),
    EBOOK("BOOK"),
    IMAGE("IMG"),
    AUDIO("AUD"),
    VIDEO("VID"),
    CONTACT("VCF"),
    OTHER("FILE"),
    ;

    companion object {
        private const val MAX_BADGE_CHARS = 4

        fun of(fileName: String, mimeType: String? = null): FileKind {
            BY_EXTENSION[extensionOf(fileName)]?.let { return it }
            val mime = mimeType?.lowercase().orEmpty()
            return when {
                mime == "application/pdf" -> PDF
                mime == "application/vnd.android.package-archive" -> APK
                mime == "application/epub+zip" -> EBOOK
                mime.startsWith("image/") -> IMAGE
                mime.startsWith("audio/") -> AUDIO
                mime.startsWith("video/") -> VIDEO
                mime == "text/x-vcard" || mime == "text/vcard" -> CONTACT
                "wordprocessing" in mime || mime == "application/msword" || mime == "application/rtf" -> WORD
                "spreadsheet" in mime || "ms-excel" in mime || mime == "text/csv" -> SPREADSHEET
                "presentation" in mime || "ms-powerpoint" in mime -> PRESENTATION
                "json" in mime || "xml" in mime || "javascript" in mime -> CODE
                mime.startsWith("text/") -> TEXT
                "zip" in mime || "compressed" in mime || "x-tar" in mime || "x-rar" in mime || "x-7z" in mime -> ARCHIVE
                else -> OTHER
            }
        }

        /** "PDF", "DOCX", "XLSX"… the real extension when it fits the badge, else the kind's label. */
        fun badgeLabel(fileName: String, kind: FileKind = of(fileName)): String {
            val extension = extensionOf(fileName)
            return if (extension.length in 1..MAX_BADGE_CHARS && extension.all(Char::isLetterOrDigit)) {
                extension.uppercase()
            } else {
                kind.defaultLabel
            }
        }

        private fun extensionOf(fileName: String): String = fileName.substringAfterLast('.', "").lowercase()

        private val BY_EXTENSION: Map<String, FileKind> = buildMap {
            fun add(kind: FileKind, vararg extensions: String) = extensions.forEach { put(it, kind) }
            add(PDF, "pdf")
            add(WORD, "doc", "docx", "docm", "dot", "dotx", "odt", "rtf", "pages", "wpd")
            add(SPREADSHEET, "xls", "xlsx", "xlsm", "xlt", "xltx", "ods", "csv", "tsv", "numbers")
            add(PRESENTATION, "ppt", "pptx", "pptm", "pps", "ppsx", "odp", "key")
            add(TEXT, "txt", "md", "markdown", "log", "ini", "cfg", "conf")
            add(CODE, "json", "xml", "html", "htm", "css", "js", "ts", "kt", "kts", "java", "py", "c", "cpp", "h", "swift", "dart", "go", "rs", "php", "rb", "sh", "sql", "yml", "yaml", "gradle")
            add(ARCHIVE, "zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz", "iso")
            add(APK, "apk", "apks", "xapk", "aab")
            add(EBOOK, "epub", "mobi", "azw", "azw3", "fb2", "djvu")
            add(IMAGE, "jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "svg", "tif", "tiff", "raw", "dng")
            add(AUDIO, "mp3", "m4a", "aac", "wav", "flac", "ogg", "opus", "amr", "wma", "mid", "midi")
            add(VIDEO, "mp4", "mkv", "mov", "avi", "webm", "3gp", "m4v", "wmv", "flv", "mts")
            add(CONTACT, "vcf", "vcard")
        }
    }
}
