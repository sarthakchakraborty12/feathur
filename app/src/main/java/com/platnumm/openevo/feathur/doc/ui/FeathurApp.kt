package com.platnumm.openevo.feathur.doc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import com.platnumm.openevo.feathur.doc.R
import com.platnumm.openevo.feathur.doc.data.*
import com.platnumm.openevo.feathur.doc.ui.theme.GoogleSansFlexFontFamily
import com.platnumm.openevo.feathur.doc.viewmodel.FeathurViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeathurApp(viewModel: FeathurViewModel) {
    val selectedUri by viewModel.selectedDocumentUri.collectAsState()
    val parsedDoc by viewModel.parsedDocument.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var isSettingsOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isSettingsOpen -> {
                SettingsScreen(viewModel = viewModel, onBack = { isSettingsOpen = false })
                BackHandler {
                    isSettingsOpen = false
                }
            }
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
                HomeScreen(viewModel = viewModel, onOpenSettings = { isSettingsOpen = true })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: FeathurViewModel, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val recents by viewModel.recentDocuments.collectAsState()
    var isIntroVisible by remember { mutableStateOf(true) }
    val fileLauncher = launcherForOpenFile { uri ->
        viewModel.openDocument(context, uri)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = GoogleSansFlexFontFamily,
                                fontSize = 28.sp,
                                fontWeight = FontWeight(605),
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { fileLauncher.launch(arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-powerpoint",
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
                                    text = "Feathur - Office Files Viewer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Lightweight offline file opener. Open Word, Excel, PowerPoint, and Text instantly without loading heavy suites.",
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
                                    FormatBadge("TXT", Color(0xFF64748B))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerContainer(
    viewModel: FeathurViewModel,
    uri: Uri,
    parsedDoc: ParsedDocument?,
    isLoading: Boolean,
    error: String?
) {
    val context = LocalContext.current
    val documentName by viewModel.documentName.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var searchActive by remember { mutableStateOf(false) }

    val searchMatchIndex by viewModel.searchMatchIndex.collectAsState()
    val searchMatchCount by viewModel.searchMatchCount.collectAsState()

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search inside document...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("in_document_search_field")
                        ,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    Text(
                                        text = "${if (searchMatchCount > 0) searchMatchIndex + 1 else 0} of $searchMatchCount",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.prevSearchMatch() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Prev Match")
                                    }
                                    IconButton(
                                        onClick = { viewModel.nextSearchMatch() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next Match")
                                    }
                                    IconButton(
                                        onClick = { viewModel.updateSearchQuery("") },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
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
                        val isPermissionDenial = remember(error) {
                            (error.contains("permission", ignoreCase = true) ||
                             error.contains("security", ignoreCase = true) ||
                             error.contains("denial", ignoreCase = true)) &&
                            !OfficeParsers.hasFilePermission(context)
                        }

                        if (isPermissionDenial) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Feathur does not need any permission to function, but if you need that the recently opened documents are still accessible after you close and reopen the app, you have to give feathur files permission.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                                Intent(
                                                    android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                            } else {
                                                Intent(
                                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                                                context.startActivity(intent)
                                            } catch (ex: Exception) {
                                                ex.printStackTrace()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Yes, grant file management permission")
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.closeDocument() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("No, I don't need it - back to home")
                                }
                            }
                        } else {
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
        is ParsedDocument.Word -> DocxViewer(doc, searchQuery, viewModel)
        is ParsedDocument.Excel -> XlsxViewer(doc, searchQuery, viewModel)
        is ParsedDocument.Slides -> PptxViewer(doc, searchQuery, viewModel)
        is ParsedDocument.Text -> PlainTextViewer(doc, searchQuery, viewModel)
    }
}

// 1. DOCX (Word Document) Screen
@Composable
fun DocxViewer(doc: ParsedDocument.Word, searchQuery: String, viewModel: FeathurViewModel) {
    if (doc.elements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No readable text found in this Word file.")
        }
        return
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val searchMatchIndex by viewModel.searchMatchIndex.collectAsState()

    val matches = remember(doc.elements, searchQuery) {
        val list = mutableListOf<Int>()
        if (searchQuery.isNotBlank()) {
            doc.elements.forEachIndexed { idx, elem ->
                val text = when (elem) {
                    is DocxElement.Paragraph -> elem.text
                    is DocxElement.Table -> elem.rows.flatten().joinToString(" ")
                }
                if (text.contains(searchQuery, ignoreCase = true)) {
                    list.add(idx)
                }
            }
        }
        list
    }

    LaunchedEffect(matches) {
        viewModel.setSearchMatchCount(matches.size)
    }

    LaunchedEffect(searchMatchIndex, matches) {
        if (matches.isNotEmpty() && searchMatchIndex in matches.indices) {
            listState.animateScrollToItem(matches[searchMatchIndex])
        }
    }

    androidx.compose.foundation.text.selection.SelectionContainer {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(doc.elements) { idx, elem ->
                val isCurrentMatch = remember(matches, searchMatchIndex, idx) {
                    matches.isNotEmpty() && searchMatchIndex in matches.indices && matches[searchMatchIndex] == idx
                }
                val borderModifier = if (isCurrentMatch) {
                    Modifier.border(2.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp)).padding(4.dp)
                } else Modifier
                
                Box(modifier = borderModifier) {
                    when (elem) {
                        is DocxElement.Paragraph -> {
                            val annotatedString = buildAnnotatedString {
                                if (elem.runs.isEmpty()) {
                                    val textVal = elem.text
                                    if (searchQuery.isNotBlank() && textVal.contains(searchQuery, ignoreCase = true)) {
                                        var startIdx = 0
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
                                    } else {
                                        append(textVal)
                                    }
                                } else {
                                    elem.runs.forEach { run ->
                                        val runText = run.text
                                        if (searchQuery.isNotBlank() && runText.contains(searchQuery, ignoreCase = true)) {
                                            var startIdx = 0
                                            while (true) {
                                                val matchIdx = runText.indexOf(searchQuery, startIdx, ignoreCase = true)
                                                if (matchIdx == -1) {
                                                    withStyle(
                                                        style = SpanStyle(
                                                            fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                                            fontStyle = if (run.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                                            fontSize = run.size.sp
                                                        )
                                                    ) {
                                                        append(runText.substring(startIdx))
                                                    }
                                                    break
                                                }
                                                if (matchIdx > startIdx) {
                                                    withStyle(
                                                        style = SpanStyle(
                                                            fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                                            fontStyle = if (run.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                                            fontSize = run.size.sp
                                                        )
                                                    ) {
                                                        append(runText.substring(startIdx, matchIdx))
                                                    }
                                                }
                                                withStyle(
                                                    style = SpanStyle(
                                                        fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                                        fontStyle = if (run.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                                        fontSize = run.size.sp,
                                                        background = Color(0xFFFDE047),
                                                        color = Color.Black
                                                    )
                                                ) {
                                                    append(runText.substring(matchIdx, matchIdx + searchQuery.length))
                                                }
                                                startIdx = matchIdx + searchQuery.length
                                            }
                                        } else {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontWeight = if (run.isBold) FontWeight.Bold else FontWeight.Normal,
                                                    fontStyle = if (run.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                                    fontSize = run.size.sp
                                                )
                                            ) {
                                                append(runText)
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        is DocxElement.Table -> {
                            TableRenderer(table = elem, searchQuery = searchQuery)
                        }
                    }
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

    val context = LocalContext.current
    val activeSheetIndex by viewModel.activeSheetIndex.collectAsState()
    val activeSheet = workbook.sheets.getOrNull(activeSheetIndex) ?: workbook.sheets.first()

    var selectedCell by remember(activeSheetIndex) { mutableStateOf<Pair<Int, Int>?>(null) }
    var fitCellsToDataSize by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(vertical = 4.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Checkbox(
                    checked = fitCellsToDataSize,
                    onCheckedChange = { fitCellsToDataSize = it }
                )
                Text(
                    text = "Fit cells",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

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
                SpreadsheetGrid(
                    sheet = activeSheet,
                    searchQuery = searchQuery,
                    viewModel = viewModel,
                    fitCellsToDataSize = fitCellsToDataSize,
                    selectedCell = selectedCell,
                    onCellClick = { r, c -> selectedCell = Pair(r, c) }
                )
            }
        }

        val clipboardManager = LocalClipboardManager.current
        val cellVal = remember(selectedCell, activeSheet) {
            if (selectedCell != null) {
                activeSheet.rows[selectedCell!!.first]?.get(selectedCell!!.second) ?: ""
            } else ""
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (selectedCell != null) "Cell ${getColLetter(selectedCell!!.second)}${selectedCell!!.first + 1}" else "No cell selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = cellVal.ifEmpty { "Empty cell" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (cellVal.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }

                if (selectedCell != null && cellVal.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(cellVal))
                            Toast.makeText(context, "Copied cell content", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Content",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpreadsheetGrid(
    sheet: ExcelSheet, 
    searchQuery: String,
    viewModel: FeathurViewModel,
    fitCellsToDataSize: Boolean,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit
) {
    val searchMatchIndex by viewModel.searchMatchIndex.collectAsState()
    
    val matches = remember(sheet, searchQuery) {
        val list = mutableListOf<Pair<Int, Int>>()
        if (searchQuery.isNotBlank()) {
            val maxCols = sheet.maxCol.coerceAtLeast(0)
            val maxRows = sheet.maxRow.coerceAtLeast(0)
            for (r in 0..maxRows) {
                for (c in 0..maxCols) {
                    val cellVal = sheet.rows[r]?.get(c) ?: ""
                    if (cellVal.contains(searchQuery, ignoreCase = true)) {
                        list.add(Pair(r, c))
                    }
                }
            }
        }
        list
    }

    LaunchedEffect(matches) {
        viewModel.setSearchMatchCount(matches.size)
    }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val density = LocalDensity.current

    val maxCols = sheet.maxCol.coerceAtLeast(1)
    val maxRows = sheet.maxRow.coerceAtLeast(1)

    val columnWidths = remember(sheet, fitCellsToDataSize) {
        val widths = mutableMapOf<Int, Int>()
        for (c in 0..maxCols) {
            if (fitCellsToDataSize) {
                var maxLen = 4
                for (r in 0..maxRows) {
                    val cellVal = sheet.rows[r]?.get(c) ?: ""
                    if (cellVal.length > maxLen) {
                        maxLen = cellVal.length
                    }
                }
                widths[c] = (maxLen * 8 + 24).coerceIn(80, 350)
            } else {
                widths[c] = 108
            }
        }
        widths
    }

    LaunchedEffect(searchMatchIndex, matches) {
        if (matches.isNotEmpty() && searchMatchIndex in matches.indices) {
            val (matchRow, matchCol) = matches[searchMatchIndex]
            val yOffset = with(density) { (matchRow * 38).dp.toPx() }
            var xOffsetSum = 0
            for (c in 0 until matchCol) {
                xOffsetSum += columnWidths[c] ?: 108
            }
            val xOffset = with(density) { xOffsetSum.dp.toPx() }

            verticalScrollState.animateScrollTo(yOffset.toInt())
            horizontalScrollState.animateScrollTo(xOffset.toInt())
            onCellClick(matchRow, matchCol)
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(42.dp)
                .verticalScroll(verticalScrollState)
        ) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )

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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
        ) {
            Row {
                for (c in 0..maxCols) {
                    val colWidth = columnWidths[c] ?: 108
                    Box(
                        modifier = Modifier
                            .width(colWidth.dp)
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

            for (r in 0..maxRows) {
                Row {
                    for (c in 0..maxCols) {
                        val cellVal = sheet.rows[r]?.get(c) ?: ""
                        val matchesSearch = remember(cellVal, searchQuery) {
                            searchQuery.isNotBlank() && cellVal.contains(searchQuery, ignoreCase = true)
                        }
                        val isSelected = selectedCell == Pair(r, c)
                        val isCurrentSearchMatch = remember(matches, searchMatchIndex, r, c) {
                            matches.isNotEmpty() && searchMatchIndex in matches.indices && matches[searchMatchIndex] == Pair(r, c)
                        }
                        val colWidth = columnWidths[c] ?: 108

                        Box(
                            modifier = Modifier
                                .width(colWidth.dp)
                                .height(38.dp)
                                .background(
                                    when {
                                        isCurrentSearchMatch -> Color(0xFFFDE047)
                                        matchesSearch -> Color(0xFFFEF08A)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = when {
                                        isSelected -> Color(0xFF107C41)
                                        matchesSearch -> Color(0xFFCA8A04)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                    }
                                )
                                .clickable { onCellClick(r, c) }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = cellVal,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrentSearchMatch || matchesSearch) Color.Black else MaterialTheme.colorScheme.onSurface,
                                maxLines = if (fitCellsToDataSize) 2 else 1,
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

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val searchMatchIndex by viewModel.searchMatchIndex.collectAsState()

    val matches = remember(doc.presentation.slides, searchQuery) {
        val list = mutableListOf<Int>()
        if (searchQuery.isNotBlank()) {
            doc.presentation.slides.forEachIndexed { idx, slide ->
                val text = slide.elements.mapNotNull {
                    if (it is SlideGraphicElement.TextBlock) it.text else null
                }.joinToString(" ")
                if (text.contains(searchQuery, ignoreCase = true)) {
                    list.add(idx)
                }
            }
        }
        list
    }

    LaunchedEffect(matches) {
        viewModel.setSearchMatchCount(matches.size)
    }

    LaunchedEffect(searchMatchIndex, matches) {
        if (matches.isNotEmpty() && searchMatchIndex in matches.indices) {
            listState.animateScrollToItem(matches[searchMatchIndex] + 1)
        }
    }

    LazyColumn(
        state = listState,
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
            GraphicalSlideCard(slide = slide, searchQuery = searchQuery)
        }
    }
}

@Composable
fun GraphicalSlideCard(slide: SlideItem, searchQuery: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GraphicalSlideItem(slide = slide, searchQuery = searchQuery)
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 8.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "${slide.index}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun GraphicalSlideItem(slide: SlideItem, searchQuery: String) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(slide.backgroundColor))
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        val positionalElements = slide.elements.filter { element ->
            when (element) {
                is SlideGraphicElement.ImageBlock -> true
                is SlideGraphicElement.TextBlock -> (element.x != 0f || element.y != 0f)
                is SlideGraphicElement.ShapeBlock -> (element.x != 0f || element.y != 0f)
            }
        }

        val nonPositionalTexts = slide.elements.filterIsInstance<SlideGraphicElement.TextBlock>()
            .filter { it.x == 0f && it.y == 0f }

        positionalElements.forEach { element ->
            when (element) {
                is SlideGraphicElement.ShapeBlock -> {
                    // Do not render vector background/decoration shapes to avoid overlapping actual content
                }
                is SlideGraphicElement.ImageBlock -> {
                    val bitmap = remember(element.bitmap) {
                        try {
                            element.bitmap.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                            modifier = Modifier
                                .offset(
                                    x = containerWidth * element.x,
                                    y = containerHeight * element.y
                                )
                                .size(
                                    width = containerWidth * element.width,
                                    height = containerHeight * element.height
                                )
                        )
                    }
                }
                is SlideGraphicElement.TextBlock -> {
                    val widthFraction = remember(element.width, element.x) {
                        if (element.width > 0.05f) element.width else (0.9f - element.x).coerceAtLeast(0.5f)
                    }
                    Box(
                        modifier = Modifier
                            .offset(
                                x = containerWidth * element.x,
                                y = containerHeight * element.y
                            )
                            .width(containerWidth * widthFraction)
                    ) {
                        HighlightText(
                            text = element.text,
                            query = searchQuery,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = (element.fontSize * (containerHeight.value / 400f)).coerceAtLeast(10f).sp,
                                fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (element.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                color = Color(element.textColor)
                            ),
                            color = Color(element.textColor)
                        )
                    }
                }
            }
        }

        if (nonPositionalTexts.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                nonPositionalTexts.forEach { element ->
                    HighlightText(
                        text = element.text,
                        query = searchQuery,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = (element.fontSize * (containerHeight.value / 350f)).coerceAtLeast(14f).sp,
                            fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (element.isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                            color = Color(element.textColor),
                            textAlign = TextAlign.Center
                        ),
                        color = Color(element.textColor),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HighlightText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        Text(text = text, style = style, color = color, modifier = modifier, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
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

    Text(text = highlightedString, style = style, modifier = modifier, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
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
            .background(Color.Black)
            .testTag("fullscreen_presentation_bg")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            ) {
                GraphicalSlideItem(slide = currentSlide, searchQuery = "")
            }
        }

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

// 6. Support for simple plain text documents (.txt fallback viewer)
@Composable
fun PlainTextViewer(doc: ParsedDocument.Text, searchQuery: String, viewModel: FeathurViewModel) {
    val lines = remember(doc.content) { doc.content.lines() }

    if (lines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No text content found.")
        }
        return
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val searchMatchIndex by viewModel.searchMatchIndex.collectAsState()

    val matches = remember(lines, searchQuery) {
        val list = mutableListOf<Int>()
        if (searchQuery.isNotBlank()) {
            lines.forEachIndexed { idx, line ->
                if (line.contains(searchQuery, ignoreCase = true)) {
                    list.add(idx)
                }
            }
        }
        list
    }

    LaunchedEffect(matches) {
        viewModel.setSearchMatchCount(matches.size)
    }

    LaunchedEffect(searchMatchIndex, matches) {
        if (matches.isNotEmpty() && searchMatchIndex in matches.indices) {
            listState.animateScrollToItem(matches[searchMatchIndex])
        }
    }

    androidx.compose.foundation.text.selection.SelectionContainer {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(lines) { idx, line ->
                val isCurrentMatch = remember(matches, searchMatchIndex, idx) {
                    matches.isNotEmpty() && searchMatchIndex in matches.indices && matches[searchMatchIndex] == idx
                }
                val borderModifier = if (isCurrentMatch) {
                    Modifier.border(2.dp, Color(0xFFFDE047), RoundedCornerShape(4.dp)).padding(4.dp)
                } else Modifier

                Box(modifier = borderModifier) {
                    HighlightText(
                        text = line,
                        query = searchQuery,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FeathurViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val darkModeSetting by viewModel.darkModeSetting.collectAsState()
    val themeSetting by viewModel.themeSetting.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Dark mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val modes = listOf("off" to "Off", "on" to "On", "adapt_device" to "Adapt device")
                        modes.forEach { (value, label) ->
                            FilterChip(
                                selected = darkModeSetting == value,
                                onClick = { viewModel.setDarkModeSetting(value) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val themes = listOf("device_wallpaper" to "Device wallpaper", "monochrome" to "Monochrome")
                        themes.forEach { (value, label) ->
                            FilterChip(
                                selected = themeSetting == value,
                                onClick = { viewModel.setThemeSetting(value) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "About and more",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = GoogleSansFlexFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight(605),
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "A simple, lightweight office files (.docx, .xlsx, .pptx etc.) viewer. Open all these file formats on your device without downloading full fldged heavy office apps. Why carry so much apps just for opening some files? Use feathur. Straightforward, simple, offline, ad-free and open source.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Connect with the creator (@sarthakchakraborty12) :",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val socials = listOf(
                        Triple("GitHub", "https://github.com/sarthakchakraborty12", Icons.Default.Code),
                        Triple("Behance", "https://behance.net/sarthakchakraborty12", Icons.Default.Palette),
                        Triple("LinkedIn", "https://linkedin.com/in/sarthakchakraborty12", Icons.Default.Work),
                        Triple("Instagram", "https://instagram.com/sarthouk", Icons.Default.CameraAlt)
                    )
                    socials.forEach { (name, url, icon) ->
                        OutlinedButton(
                            onClick = { uriHandler.openUri(url) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(icon, contentDescription = name, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Follow @platnummtech and @appbasket :",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val follow = listOf(
                        Triple("GitHub", "https://github.com/Platnumm", Icons.Default.Code),
                        Triple("LinkedIn", "https://www.linkedin.com/company/platnummtech", Icons.Default.Work),
                        Triple("Instagram", "https://instagram.com/app.basket", Icons.Default.CameraAlt)
                    )
                    follow.forEach { (name, url, icon) ->
                        OutlinedButton(
                            onClick = { uriHandler.openUri(url) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(icon, contentDescription = name, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
