package com.calculator.vault.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FileKindTest {

    @Test fun extensionsMapToTheirKind() {
        assertEquals(FileKind.PDF, FileKind.of("Tax_2025.PDF"))
        assertEquals(FileKind.WORD, FileKind.of("Report.docx"))
        assertEquals(FileKind.SPREADSHEET, FileKind.of("Budget.xlsx"))
        assertEquals(FileKind.SPREADSHEET, FileKind.of("export.csv"))
        assertEquals(FileKind.PRESENTATION, FileKind.of("Pitch.pptx"))
        assertEquals(FileKind.ARCHIVE, FileKind.of("backup.tar.gz"))
        assertEquals(FileKind.APK, FileKind.of("app.apk"))
        assertEquals(FileKind.EBOOK, FileKind.of("novel.epub"))
        assertEquals(FileKind.CODE, FileKind.of("config.json"))
        assertEquals(FileKind.OTHER, FileKind.of("firmware.bin"))
    }

    @Test fun mimeTypeDecidesWhenTheNameHasNoKnownExtension() {
        assertEquals(FileKind.PDF, FileKind.of("download", "application/pdf"))
        assertEquals(FileKind.WORD, FileKind.of("letter", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        assertEquals(FileKind.EBOOK, FileKind.of("book", "application/epub+zip"))
        assertEquals(FileKind.ARCHIVE, FileKind.of("bundle", "application/zip"))
        assertEquals(FileKind.TEXT, FileKind.of("readme", "text/plain"))
    }

    @Test fun badgeShowsTheRealExtensionWhenItFits() {
        assertEquals("PDF", FileKind.badgeLabel("Tax_2025.pdf"))
        assertEquals("XLSX", FileKind.badgeLabel("Budget.xlsx"))
        assertEquals("TXT", FileKind.badgeLabel("notes.markdown"))
        assertEquals("PDF", FileKind.badgeLabel("download", FileKind.PDF))
        assertEquals("FILE", FileKind.badgeLabel("no_extension"))
    }
}
