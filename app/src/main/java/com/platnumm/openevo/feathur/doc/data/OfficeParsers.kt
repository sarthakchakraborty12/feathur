package com.platnumm.openevo.feathur.doc.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

// --- Models for Word Document Viewer (.docx) ---
sealed class DocxElement {
    data class Paragraph(
        val text: String,
        val runs: List<TextRun>
    ) : DocxElement()

    data class Table(
        val rows: List<List<String>>
    ) : DocxElement()
}

data class TextRun(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val color: String? = null,
    val size: Float = 16f // sp
)

// --- Models for Spreadsheet Viewer (.xlsx) ---
data class ExcelWorkbook(
    val sheets: List<ExcelSheet>
)

data class ExcelSheet(
    val name: String,
    val rows: Map<Int, Map<Int, String>>, // row_idx -> col_idx -> cell_value String
    val maxRow: Int = 0,
    val maxCol: Int = 0
)

// --- Models for PowerPoint Presentation (.pptx) ---
data class SlideDocument(
    val slides: List<SlideItem>
)

data class SlideItem(
    val index: Int,
    val elements: List<SlideElement>
)

sealed class SlideElement {
    data class Title(val text: String) : SlideElement()
    data class Body(val bullets: List<String>) : SlideElement()
    data class TextBlock(val text: String) : SlideElement()
}

// --- High level parsed result ---
sealed class ParsedDocument {
    data class Word(val elements: List<DocxElement>) : ParsedDocument()
    data class Excel(val workbook: ExcelWorkbook) : ParsedDocument()
    data class Slides(val presentation: SlideDocument) : ParsedDocument()
    data class Svg(val rawSvgText: String) : ParsedDocument()
    data class Text(val content: String) : ParsedDocument()
}

object OfficeParsers {
    private const val TAG = "OfficeParsers"

    // Parse DOCX
    fun parseDocx(inputStream: InputStream): ParsedDocument.Word {
        val elements = mutableListOf<DocxElement>()
        try {
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    elements.addAll(readDocumentXml(zip))
                    break
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            zip.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing docx", e)
        }
        return ParsedDocument.Word(elements)
    }

    private fun readDocumentXml(inputStream: InputStream): List<DocxElement> {
        val list = mutableListOf<DocxElement>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType

            var inTable = false
            val currentTableRows = mutableListOf<List<String>>()
            var currentRowCells = mutableListOf<String>()
            var currentCellText = StringBuilder()

            var currentParagraphRuns = mutableListOf<TextRun>()
            var currentRunText = StringBuilder()
            var isBold = false
            var isItalic = false
            var inParagraph = false
            var inRun = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "tbl" -> {
                                inTable = true
                                currentTableRows.clear()
                            }
                            "tr" -> {
                                currentRowCells = mutableListOf()
                            }
                            "tc" -> {
                                currentCellText = StringBuilder()
                            }
                            "p" -> {
                                inParagraph = true
                                currentParagraphRuns = mutableListOf()
                            }
                            "r" -> {
                                inRun = true
                                currentRunText = StringBuilder()
                                isBold = false
                                isItalic = false
                            }
                            "b" -> {
                                isBold = true
                            }
                            "i" -> {
                                isItalic = true
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inRun) {
                            currentRunText.append(parser.text)
                        } else if (inTable && !inParagraph) {
                            currentCellText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "r" -> {
                                inRun = false
                                if (currentRunText.isNotEmpty()) {
                                    currentParagraphRuns.add(
                                        TextRun(
                                            text = currentRunText.toString(),
                                            isBold = isBold,
                                            isItalic = isItalic
                                        )
                                    )
                                }
                            }
                            "p" -> {
                                inParagraph = false
                                val fullText = currentParagraphRuns.joinToString("") { it.text }
                                if (fullText.isNotBlank()) {
                                    if (inTable) {
                                        currentCellText.append(fullText).append(" ")
                                    } else {
                                        list.add(DocxElement.Paragraph(fullText, currentParagraphRuns))
                                    }
                                }
                            }
                            "tc" -> {
                                currentRowCells.add(currentCellText.toString().trim())
                            }
                            "tr" -> {
                                if (currentRowCells.isNotEmpty()) {
                                    currentTableRows.add(currentRowCells)
                                }
                            }
                            "tbl" -> {
                                inTable = false
                                if (currentTableRows.isNotEmpty()) {
                                    list.add(DocxElement.Table(currentTableRows.toList()))
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in readDocumentXml", e)
        }
        return list
    }

    // Parse XLSX
    fun parseXlsx(inputStream: InputStream): ParsedDocument.Excel {
        val sheets = mutableListOf<ExcelSheet>()
        val sharedStrings = mutableListOf<String>()
        val sheetsToParse = mutableListOf<Pair<String, String>>() // (name, file_path)

        try {
            // First pass or double scanning requires a copy or multiple files, but in ZipInputStream we can parse in stream
            // Since we can scan zip file sequential, let's process sharedStrings first if possible.
            // But ZipInputStream stream is forward-only. A very elegant way is to do scan the zip entries.
            // We can memorize the files in memory or stream it. Since they are small, XML parsing sheets on the fly is highly efficient.
            // Let's do a double stream or cache worksheets in a Map of bytes of zip entries so we can read sharedStrings first, then sheets!
            val cachedEntries = mutableMapOf<String, ByteArray>()
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml" || 
                    entry.name == "xl/workbook.xml" || 
                    entry.name.startsWith("xl/worksheets/sheet")) {
                    cachedEntries[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            zip.close()

            // 1. Parse sharedStrings
            val sharedStringsBytes = cachedEntries["xl/sharedStrings.xml"]
            if (sharedStringsBytes != null) {
                sharedStrings.addAll(parseSharedStrings(sharedStringsBytes.inputStream()))
            }

            // 2. Parse workbook to get sheet names
            val workbookBytes = cachedEntries["xl/workbook.xml"]
            val sheetIdsToNames = mutableMapOf<String, String>() // rId or sheetId to name
            if (workbookBytes != null) {
                sheetIdsToNames.putAll(parseWorkbook(workbookBytes.inputStream()))
            }

            // 3. Parse sheets
            // Group and sort sheet entries
            val sheetEntries = cachedEntries.keys.filter { it.startsWith("xl/worksheets/sheet") }
                .sortedWith(compareBy { entryName -> 
                    entryName.filter { it.isDigit() }.toIntOrNull() ?: 0 
                })

            sheetEntries.forEachIndexed { index, entryName ->
                val sheetName = sheetIdsToNames[entryName] ?: sheetIdsToNames[(index + 1).toString()] ?: "Sheet ${index + 1}"
                val sheetBytes = cachedEntries[entryName]
                if (sheetBytes != null) {
                    val sheet = parseSingleSheet(sheetName, sheetBytes.inputStream(), sharedStrings)
                    sheets.add(sheet)
                }
            }

            if (sheets.isEmpty() && cachedEntries.isNotEmpty()) {
                // Fallback for sheets list
                sheets.add(ExcelSheet("Sheet 1", emptyMap(), 0, 0))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing xlsx", e)
        }

        return ParsedDocument.Excel(ExcelWorkbook(sheets))
    }

    private fun parseSharedStrings(inputStream: InputStream): List<String> {
        val list = mutableListOf<String>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType
            var currentString = StringBuilder()
            var inT = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "t") {
                            inT = true
                            currentString = StringBuilder()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inT) {
                            currentString.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag == "t") {
                            inT = false
                            list.add(currentString.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseSharedStrings", e)
        }
        return list
    }

    private fun parseWorkbook(inputStream: InputStream): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                    val name = parser.getAttributeValue(null, "name") ?: ""
                    val sheetId = parser.getAttributeValue(null, "sheetId") ?: ""
                    val rId = parser.getAttributeValue("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id") ?: ""
                    
                    if (name.isNotBlank()) {
                        map[sheetId] = name
                        if (rId.isNotBlank()) {
                            // Link both just in case
                            map["xl/worksheets/sheet$sheetId.xml"] = name
                            map["xl/worksheets/sheet${rId.replace("rId", "")}.xml"] = name
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseWorkbook", e)
        }
        return map
    }

    private fun parseSingleSheet(
        sheetName: String, 
        inputStream: InputStream, 
        sharedStrings: List<String>
    ): ExcelSheet {
        val rRows = mutableMapOf<Int, MutableMap<Int, String>>()
        var maxRow = 0
        var maxCol = 0

        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType
            var inCell = false
            var cellType = ""
            var currentVal = StringBuilder()
            var cellRef = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tag == "c") {
                            inCell = true
                            cellRef = parser.getAttributeValue(null, "r") ?: ""
                            cellType = parser.getAttributeValue(null, "t") ?: ""
                            currentVal = StringBuilder()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inCell) {
                            currentVal.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tag == "c") {
                            inCell = false
                            val cellStrValue = currentVal.toString().trim()
                            if (cellRef.isNotBlank() && cellStrValue.isNotBlank()) {
                                // Extract row and column index from reference (e.g. "B4" -> col 1, row 3)
                                val colIndices = cellRef.takeWhile { it.isLetter() }
                                val rowIndices = cellRef.dropWhile { it.isLetter() }

                                val colIdx = excelColToNumber(colIndices) - 1
                                val rowIdx = (rowIndices.toIntOrNull() ?: 1) - 1

                                if (colIdx >= 0 && rowIdx >= 0) {
                                    if (rowIdx > maxRow) maxRow = rowIdx
                                    if (colIdx > maxCol) maxCol = colIdx

                                    val finalVal = if (cellType == "s") {
                                        val stringIdx = cellStrValue.toIntOrNull() ?: -1
                                        if (stringIdx in sharedStrings.indices) {
                                            sharedStrings[stringIdx]
                                        } else {
                                            cellStrValue
                                        }
                                    } else {
                                        cellStrValue
                                    }

                                    val rowData = rRows.getOrPut(rowIdx) { mutableMapOf() }
                                    rowData[colIdx] = finalVal
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in parseSingleSheet", e)
        }

        // Clean-up sheet mapping
        val cleanRows = rRows.mapValues { it.value.toMap() }
        return ExcelSheet(sheetName, cleanRows, maxRow, maxCol)
    }

    private fun excelColToNumber(col: String): Int {
        var num = 0
        for (i in 0 until col.length) {
            num = num * 26 + (col[i] - 'A' + 1)
        }
        return num
    }

    // Parse PPTX (Slides)
    fun parsePptx(inputStream: InputStream): ParsedDocument.Slides {
        val list = mutableListOf<SlideItem>()
        try {
            val cachedEntries = mutableMapOf<String, ByteArray>()
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                    cachedEntries[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            zip.close()

            // Sort slides naturally slide1, slide2, slide10
            val sortedSlides = cachedEntries.keys.sortedWith(compareBy { entryName ->
                entryName.filter { it.isDigit() }.toIntOrNull() ?: 0
            })

            sortedSlides.forEachIndexed { index, entryName ->
                val slideBytes = cachedEntries[entryName]
                if (slideBytes != null) {
                    val elements = parseSingleSlide(slideBytes.inputStream())
                    list.add(SlideItem(index + 1, elements))
                }
            }

            if (list.isEmpty()) {
                list.add(SlideItem(1, listOf(SlideElement.Title("Empty Presentation"), SlideElement.TextBlock("No slides parsed"))))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing pptx", e)
        }
        return ParsedDocument.Slides(SlideDocument(list))
    }

    private fun parseSingleSlide(inputStream: InputStream): List<SlideElement> {
        val elements = mutableListOf<SlideElement>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            var eventType = parser.eventType

            var inTxtBody = false
            var inParagraph = false
            var inRun = false
            var currentRunText = StringBuilder()
            val slideTexts = mutableListOf<String>()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "txBody" -> inTxtBody = true
                            "p" -> {
                                if (inTxtBody) {
                                    inParagraph = true
                                }
                            }
                            "r" -> {
                                if (inParagraph) {
                                    inRun = true
                                    currentRunText = StringBuilder()
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inRun) {
                            currentRunText.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "r" -> {
                                inRun = false
                                val rText = currentRunText.toString().trim()
                                if (rText.isNotEmpty()) {
                                    slideTexts.add(rText)
                                }
                            }
                            "p" -> {
                                inParagraph = false
                            }
                            "txBody" -> {
                                inTxtBody = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }

            // Group found texts creatively into title and bodies
            if (slideTexts.isNotEmpty()) {
                val titleText = slideTexts.first()
                elements.add(SlideElement.Title(titleText))
                if (slideTexts.size > 1) {
                    elements.add(SlideElement.Body(slideTexts.drop(1)))
                }
            } else {
                elements.add(SlideElement.Title("Blank Slide"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in parseSingleSlide", e)
        }
        return elements
    }

    // Comprehensive parsing entry-point
    fun parseUri(context: Context, uri: Uri): ParsedDocument {
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, uri).lowercase()
        val inputStream = contentResolver.openInputStream(uri) 
            ?: throw IllegalArgumentException("Could not open file input stream")

        return when {
            fileName.endsWith(".docx") || fileName.endsWith(".doc") -> {
                parseDocx(inputStream)
            }
            fileName.endsWith(".xlsx") || fileName.endsWith(".xls") -> {
                parseXlsx(inputStream)
            }
            fileName.endsWith(".pptx") || fileName.endsWith(".ppt") -> {
                parsePptx(inputStream)
            }
            fileName.endsWith(".svg") -> {
                val rawText = inputStream.bufferedReader().use { it.readText() }
                ParsedDocument.Svg(rawText)
            }
            else -> {
                // Return as clear text fallback
                val lines = inputStream.bufferedReader().use { it.readText() }
                ParsedDocument.Text(lines)
            }
        }
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = "Document"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }
}
