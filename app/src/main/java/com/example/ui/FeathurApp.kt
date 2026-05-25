package com.example.ui

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.*
import com.example.viewmodel.FeathurViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeathurApp(viewModel: FeathurViewModel) {
    val selectedUri by viewModel.selectedDocumentUri.collectAsState()
    val parsedDoc by viewModel.parsedDocument.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            selectedUri != null -> {
                DocumentViewerContainer(
                    viewModel = viewModel,
                    uri = selectedUri!!,
                    parsedDoc = parsedDoc,
                    isLoading = isLoading,
                    error = error
                )
            }
            else -> {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: FeathurViewModel) {
    val context = LocalContext.current
    val recents by viewModel.recentDocuments.collectAsState()
    var isIntroVisible by remember { mutableStateOf(true) }
    val fileLauncher = launcherForOpenFile { uri ->
        viewModel.openDocument(context, uri)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { fileLauncher.launch(arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint",
                    "image/svg+xml",
                    "text/plain"
                )) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Open file") },
                text = { Text("Open File") },
                modifier = Modifier.testTag("open_file_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vector feather logo
                Image(
                    painter = painterResource(id = R.drawable.ic_feathur_vector),
                    contentDescription = "Feathur Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .padding(8.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                )

                Text(
                    text = "Feathur",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Beautiful Expressive Intro Card (Material You / Material 3 standard)
            AnimatedVisibility(
                visible = isIntroVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Dismiss button in the top right corner
                        IconButton(
                            onClick = { isIntroVisible = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Large Expressive Icon container
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_feathur_vector),
                                    contentDescription = "Feathur Logo",
                                    modifier = Modifier.size(36.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1.0f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Feathur Office Opener",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Lightweight offline file opener. Open Word, Excel, PowerPoint, Text, and SVGs instantly without loading heavy suites.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )

                                // Compact Format Chips List
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FormatBadge("DOCX", Color(0xFF2B579A))
                                    FormatBadge("XLSX", Color(0xFF107C41))
                                    FormatBadge("PPTX", Color(0xFFD24726))
                                    FormatBadge("SVG", Color(0xFF7F44FC))
                                }
                            }
                        }
                    }
                }
            }

            // Document Upload File Picker Card (Direct tap interaction space)
            Card(
                onClick = { fileLauncher.launch(arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint",
                    "image/svg+xml",
                    "text/plain"
                )) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("upload_zone")
                ,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.5.dp, 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.UploadFile,
                            contentDescription = "Upload",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Browse or select office file",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Recent Documents Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Opened Files",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (recents.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.testTag("clear_history")
                    ) {
                        Text("Clear All")
                    }
                }
            }

            if (recents.isEmpty()) {
                // Empty State Viewport
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LibraryBooks,
                        contentDescription = "No recents",
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No documents opened recently",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // List of recents
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recents.forEach { doc ->
                        RecentFileRow(
                            doc = doc,
                            onClick = {
                                viewModel.openDocument(context, Uri.parse(doc.uriString))
                            },
                            onDelete = {
                                viewModel.deleteRecentDocument(doc.uriString)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormatBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RecentFileRow(
    doc: DocumentHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(doc.lastOpenedTimestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(doc.lastOpenedTimestamp))
    }

    val context = LocalContext.current
    val sizeStr = remember(doc.fileSize) {
        if (doc.fileSize > 0) Formatter.formatShortFileSize(context, doc.fileSize) else ""
    }

    val (icon, tint) = remember(doc.fileType) {
        when (doc.fileType.lowercase()) {
            "docx", "doc" -> Pair(Icons.Default.Description, Color(0xFF2B579A))
            "xlsx", "xls" -> Pair(Icons.Default.GridOn, Color(0xFF107C41))
            "pptx", "ppt" -> Pair(Icons.Default.Slideshow, Color(0xFFD24726))
            "svg" -> Pair(Icons.Default.Image, Color(0xFF7F44FC))
            "txt" -> Pair(Icons.Default.Article, Color(0xFF64748B))
            else -> Pair(Icons.Default.InsertDriveFile, Color(0xFF475569))
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_item_${doc.fileName.replace(" ", "_")}")
        ,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(44.dp)
                    .background(tint.copy(alpha = 0.1f), CircleShape)
                    .padding(10.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (sizeStr.isNotEmpty()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = sizeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_recent_${doc.fileName.replace(" ", "_")}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// Helper to open files
@Composable
fun launcherForOpenFile(onResult: (Uri) -> Unit) =
    androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                onResult(uri)
            }
        }
    )

// --- Document Viewer Container ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerContainer(
    viewModel: FeathurViewModel,
    uri: Uri,
    parsedDoc: ParsedDocument?,
    isLoading: Boolean,
    error: String?
) {
    val documentName by viewModel.documentName.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    // PowerPoint specific fullscreen logic
    val presentationMode by viewModel.presentationMode.collectAsState()

    BackHandler {
        if (presentationMode) {
            viewModel.togglePresentationMode(false)
        } else {
            viewModel.closeDocument()
        }
    }

    if (presentationMode && parsedDoc is ParsedDocument.Slides) {
        FullScreenPresentation(viewModel, parsedDoc.presentation)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeDocument() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = documentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Offline Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                actions = {
                    // Search toggle button (Office Docs support search)
                    if (parsedDoc !is ParsedDocument.Svg) {
                        IconButton(
                            onClick = { 
                                searchActive = !searchActive 
                                if (!searchActive) viewModel.updateSearchQuery("")
                            },
                            modifier = Modifier.testTag("search_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.SearchOff else Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                    if (parsedDoc is ParsedDocument.Slides) {
                        IconButton(onClick = { viewModel.togglePresentationMode(true) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Present Grid")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (searchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search inside document...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("in_document_search_field")
                    ,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Rendering document securely...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.closeDocument() }) {
                                Text("Back to Home")
                            }
                        }
                    }
                    parsedDoc != null -> {
                        DocumentContentPane(
                            doc = parsedDoc, 
                            searchQuery = searchQuery, 
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentContentPane(
    doc: ParsedDocument, 
    searchQuery: String, 
    viewModel: FeathurViewModel
) {
    when (doc) {
        is ParsedDocument.Word -> DocxViewer(doc, searchQuery)
        is ParsedDocument.Excel -> XlsxViewer(doc, searchQuery, viewModel)
        is ParsedDocument.Slides -> PptxViewer(doc, searchQuery, viewModel)
        is ParsedDocument.Svg -> SvgViewer(doc)
        is ParsedDocument.Text -> PlainTextViewer(doc, searchQuery)
    }
}

// --- Renderers ---

// 1. DOCX (Word Document) Screen
@Composable
fun DocxViewer(doc: ParsedDocument.Word, searchQuery: String) {
    if (doc.elements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No readable text found in this Word file.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(doc.elements) { elem ->
            when (elem) {
                is DocxElement.Paragraph -> {
                    val annotatedString = buildAnnotatedString {
                        if (elem.runs.isEmpty()) {
                            append(elem.text)
                        } else {
                            elem.runs.forEach { run ->
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                        fontStyle = if (run.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                        fontSize = run.size.sp
                                    )
                                ) {
                                    append(run.text)
                                }
                            }
                        }
                    }

                    // Perform search highlight
                    if (searchQuery.isNotBlank() && elem.text.contains(searchQuery, ignoreCase = true)) {
                        val highlightedString = buildAnnotatedString {
                            var startIdx = 0
                            val textVal = elem.text
                            while (true) {
                                val matchIdx = textVal.indexOf(searchQuery, startIdx, ignoreCase = true)
                                if (matchIdx == -1) {
                                    append(textVal.substring(startIdx))
                                    break
                                }
                                append(textVal.substring(startIdx, matchIdx))
                                withStyle(style = SpanStyle(background = Color(0xFFFDE047), color = Color.Black)) {
                                    append(textVal.substring(matchIdx, matchIdx + searchQuery.length))
                                }
                                startIdx = matchIdx + searchQuery.length
                            }
                        }
                        Text(
                            text = highlightedString,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is DocxElement.Table -> {
                    TableRenderer(table = elem, searchQuery = searchQuery)
                }
            }
        }
    }
}

@Composable
fun TableRenderer(table: DocxElement.Table, searchQuery: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            table.rows.forEachIndexed { rowIdx, rowCells ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowCells.forEach { cellText ->
                        Surface(
                            modifier = Modifier.widthIn(min = 100.dp, max = 240.dp),
                            color = if (rowIdx == 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                if (searchQuery.isNotBlank() && cellText.contains(searchQuery, ignoreCase = true)) {
                                    val highlightedString = buildAnnotatedString {
                                        var startIdx = 0
                                        while (true) {
                                            val matchIdx = cellText.indexOf(searchQuery, startIdx, ignoreCase = true)
                                            if (matchIdx == -1) {
                                                append(cellText.substring(startIdx))
                                                break
                                            }
                                            append(cellText.substring(startIdx, matchIdx))
                                            withStyle(style = SpanStyle(background = Color(0xFFFDE047), color = Color.Black)) {
                                                append(cellText.substring(matchIdx, matchIdx + searchQuery.length))
                                            }
                                            startIdx = matchIdx + searchQuery.length
                                        }
                                    }
                                    Text(
                                        text = highlightedString,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                } else {
                                    Text(
                                        text = cellText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                if (rowIdx < table.rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

// 2. XLSX (Excel Sheets Document) Renderer
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun XlsxViewer(
    doc: ParsedDocument.Excel, 
    searchQuery: String, 
    viewModel: FeathurViewModel
) {
    val workbook = doc.workbook
    if (workbook.sheets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No workbooks or sheets located inside this Excel file.")
        }
        return
    }

    val activeSheetIndex by viewModel.activeSheetIndex.collectAsState()
    val activeSheet = workbook.sheets.getOrNull(activeSheetIndex) ?: workbook.sheets.first()

    Column(modifier = Modifier.fillMaxSize()) {
        // Dynamic List of sheet names/tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(workbook.sheets) { index, sheet ->
                val isActive = index == activeSheetIndex
                FilterChip(
                    selected = isActive,
                    onClick = { viewModel.setActiveSheetIndex(index) },
                    label = { Text(sheet.name) },
                    modifier = Modifier.testTag("sheet_tab_$index")
                )
            }
        }

        // Real Interactive Excel-like Cells Canvas Grid Scroll Layout
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (activeSheet.rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This sheet is empty.")
                }
            } else {
                SpreadsheetGrid(sheet = activeSheet, searchQuery = searchQuery)
            }
        }
    }
}

@Composable
fun SpreadsheetGrid(sheet: ExcelSheet, searchQuery: String) {
    // Spreadsheet scroll handles
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    val maxCols = sheet.maxCol.coerceAtLeast(1)
    val maxRows = sheet.maxRow.coerceAtLeast(1)

    Row(modifier = Modifier.fillMaxSize()) {
        // Vertical Row Indexes Headers (Fixed 1, 2, 3 column)
        Column(
            modifier = Modifier
                .width(42.dp)
                .verticalScroll(verticalScrollState)
        ) {
            // Corner spacer for col labels header row
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

            // Number headers row elements
            for (r in 0..maxRows) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (r + 1).toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Grid canvas section (Both vert and horiz scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            // Horizontal Column Letters Label Headers (A, B, C...)
            Row {
                for (c in 0..maxCols) {
                    Box(
                        modifier = Modifier
                            .width(108.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getColLetter(c),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cell matrix layout cells row-by-row
            for (r in 0..maxRows) {
                Row {
                    for (c in 0..maxCols) {
                        val cellVal = sheet.rows[r]?.get(c) ?: ""
                        val matchesSearch = remember(cellVal, searchQuery) {
                            searchQuery.isNotBlank() && cellVal.contains(searchQuery, ignoreCase = true)
                        }

                        Box(
                            modifier = Modifier
                                .width(108.dp)
                                .height(38.dp)
                                .background(
                                    if (matchesSearch) Color(0xFFFEF08A) // Match highlight yellow
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = cellVal,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (matchesSearch) Color.Black else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getColLetter(colIdx: Int): String {
    var temp = colIdx
    val name = StringBuilder()
    while (temp >= 0) {
        name.insert(0, ('A' + (temp % 26)))
        temp = (temp / 26) - 1
    }
    return name.toString()
}

// 3. PPTX (Powerpoint Presentation) Renderer
@Composable
fun PptxViewer(
    doc: ParsedDocument.Slides, 
    searchQuery: String, 
    viewModel: FeathurViewModel
) {
    if (doc.presentation.slides.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No slides located inside this PPTX file.")
        }
        return
    }

    // Displays slides in a beautiful responsive scrolling grid list
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Button(
                onClick = { viewModel.togglePresentationMode(true) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("present_mode_initial_button")
            ) {
                Icon(Icons.Default.Slideshow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Full-Screen Slideshow")
            }
        }

        items(doc.presentation.slides) { slide ->
            SlideCard(slide = slide, searchQuery = searchQuery)
        }
    }
}

@Composable
fun SlideCard(slide: SlideItem, searchQuery: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            var titleText = "Slide ${slide.index}"
            val bulletItems = mutableListOf<String>()

            slide.elements.forEach { elem ->
                when (elem) {
                    is SlideElement.Title -> titleText = elem.text
                    is SlideElement.Body -> bulletItems.addAll(elem.bullets)
                    is SlideElement.TextBlock -> bulletItems.add(elem.text)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Title
                HighlightText(
                    text = titleText,
                    query = searchQuery,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Bullet contents
                if (bulletItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        bulletItems.take(4).forEach { bullet ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("•", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                HighlightText(
                                    text = bullet,
                                    query = searchQuery,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                        if (bulletItems.size > 4) {
                            Text("...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 14.dp))
                        }
                    }
                }
            }

            // Bottom index indicator
            Text(
                text = "${slide.index}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// Custom text search highlighter
@Composable
fun HighlightText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    maxLines: Int = Int.MAX_VALUE
) {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        Text(text = text, style = style, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
        return
    }

    val highlightedString = buildAnnotatedString {
        var startIdx = 0
        while (true) {
            val matchIdx = text.indexOf(query, startIdx, ignoreCase = true)
            if (matchIdx == -1) {
                append(text.substring(startIdx))
                break
            }
            append(text.substring(startIdx, matchIdx))
            withStyle(style = SpanStyle(background = Color(0xFFFDE047), color = Color.Black)) {
                append(text.substring(matchIdx, matchIdx + query.length))
            }
            startIdx = matchIdx + query.length
        }
    }

    Text(text = highlightedString, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

// 4. PPT/PPTX PPT Slideshow Presentation Mode Full-Screen Viewport
@Composable
fun FullScreenPresentation(viewModel: FeathurViewModel, presentation: SlideDocument) {
    val activeSlideIndex by viewModel.activeSlideIndex.collectAsState()
    val totalSlides = presentation.slides.size
    val currentSlide = presentation.slides.getOrNull(activeSlideIndex) ?: presentation.slides.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Strict sleek darkness themeing
            .testTag("fullscreen_presentation_bg")
    ) {
        // Render current slide zoomed/centered beautifully
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var slideTitle = "Slide ${currentSlide.index}"
            val slideBullets = mutableListOf<String>()

            currentSlide.elements.forEach { elem ->
                when (elem) {
                    is SlideElement.Title -> slideTitle = elem.text
                    is SlideElement.Body -> slideBullets.addAll(elem.bullets)
                    is SlideElement.TextBlock -> slideBullets.add(elem.text)
                }
            }

            // Large White display typography for presentation title
            Text(
                text = slideTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (slideBullets.isNotEmpty()) {
                Column(
                    modifier = Modifier.widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    slideBullets.forEach { bullet ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "•",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Left 30% Tap area gesture support (Prev Slide)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.3f)
                .align(Alignment.CenterStart)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (activeSlideIndex > 0) {
                            viewModel.setActiveSlideIndex(activeSlideIndex - 1)
                        }
                    }
                }
        )

        // Right 70% Tap area gesture support (Next Slide)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (activeSlideIndex < totalSlides - 1) {
                            viewModel.setActiveSlideIndex(activeSlideIndex + 1)
                        }
                    }
                }
        )

        // Bottom HUD control bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(18.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.togglePresentationMode(false) },
                modifier = Modifier.testTag("exit_presentation_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Screen",
                    tint = Color.White
                )
            }

            Text(
                text = "${activeSlideIndex + 1} of $totalSlides",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    enabled = activeSlideIndex > 0,
                    onClick = { viewModel.setActiveSlideIndex(activeSlideIndex - 1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Prev Slide",
                        tint = if (activeSlideIndex > 0) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }

                IconButton(
                    enabled = activeSlideIndex < totalSlides - 1,
                    onClick = { viewModel.setActiveSlideIndex(activeSlideIndex + 1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Slide",
                        tint = if (activeSlideIndex < totalSlides - 1) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// 5. SVG Viewer with dynamic panning, pinching, and zoom controls
@Composable
fun SvgViewer(doc: ParsedDocument.Svg) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Hardware accelerated vector representation using WebView loaded with transparent body
        // Allows zoom controls, page-views, scroll buffers out-of-the-box correctly.
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(0) // Transparent BG to align with current Material3 light/dark style
                }
            },
            update = { webView ->
                val html = """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=0.5, maximum-scale=5.0, user-scalable=yes">
                        <style>
                            html, body {
                                margin: 0;
                                padding: 0;
                                width: 100%;
                                height: 100%;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                background-color: transparent;
                            }
                            svg {
                                max-width: 100%;
                                max-height: 100%;
                                width: auto;
                                height: auto;
                            }
                        </style>
                    </head>
                    <body>
                        ${doc.rawSvgText}
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("svg_webview_canvas")
        )
    }
}

// 6. Support for simple plain text documents (.txt fallback viewer)
@Composable
fun PlainTextViewer(doc: ParsedDocument.Text, searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HighlightText(
            text = doc.content,
            query = searchQuery,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


