package com.platnumm.openevo.feathur.doc.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.platnumm.openevo.feathur.doc.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeathurViewModel(
    private val repository: DocumentHistoryRepository,
    context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("feathur_settings", Context.MODE_PRIVATE)

    private val _darkModeSetting = MutableStateFlow(sharedPrefs.getString("dark_mode", "adapt_device") ?: "adapt_device")
    val darkModeSetting: StateFlow<String> = _darkModeSetting

    private val _themeSetting = MutableStateFlow(sharedPrefs.getString("theme", "device_wallpaper") ?: "device_wallpaper")
    val themeSetting: StateFlow<String> = _themeSetting

    fun setDarkModeSetting(value: String) {
        _darkModeSetting.value = value
        sharedPrefs.edit().putString("dark_mode", value).apply()
    }

    fun setThemeSetting(value: String) {
        _themeSetting.value = value
        sharedPrefs.edit().putString("theme", value).apply()
    }

    private val _selectedDocumentUri = MutableStateFlow<Uri?>(null)
    val selectedDocumentUri: StateFlow<Uri?> = _selectedDocumentUri

    private val _parsedDocument = MutableStateFlow<ParsedDocument?>(null)
    val parsedDocument: StateFlow<ParsedDocument?> = _parsedDocument

    private val _documentName = MutableStateFlow("Document")
    val documentName: StateFlow<String> = _documentName

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchMatchIndex = MutableStateFlow(0)
    val searchMatchIndex: StateFlow<Int> = _searchMatchIndex

    private val _searchMatchCount = MutableStateFlow(0)
    val searchMatchCount: StateFlow<Int> = _searchMatchCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Excel Specific State
    private val _activeSheetIndex = MutableStateFlow(0)
    val activeSheetIndex: StateFlow<Int> = _activeSheetIndex

    // PPT Specific State
    private val _activeSlideIndex = MutableStateFlow(0)
    val activeSlideIndex: StateFlow<Int> = _activeSlideIndex

    private val _presentationMode = MutableStateFlow(false)
    val presentationMode: StateFlow<Boolean> = _presentationMode

    // Recents List
    val recentDocuments: StateFlow<List<DocumentHistory>> = repository.recentDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun openDocument(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                Log.d("FeathurViewModel", "Failed to take persistable URI permission: ${e.message}")
            }

            _isLoading.value = true
            _error.value = null
            _selectedDocumentUri.value = uri
            _searchQuery.value = ""
            _searchMatchIndex.value = 0
            _searchMatchCount.value = 0
            _activeSheetIndex.value = 0
            _activeSlideIndex.value = 0
            _presentationMode.value = false

            val name = OfficeParsers.getFileName(context, uri)
            _documentName.value = name

            try {
                // Perform parsing on Dispatchers.IO to maintain smooth UI threads!
                val parsed = withContext(Dispatchers.IO) {
                    OfficeParsers.parseUri(context, uri)
                }
                _parsedDocument.value = parsed

                // Core details for Save
                val size = withContext(Dispatchers.IO) {
                    OfficeParsers.getFileSize(context, uri)
                }

                val ext = name.substringAfterLast(".", "").lowercase()

                // Insert into Room
                viewModelScope.launch {
                    repository.insert(
                        DocumentHistory(
                            uriString = uri.toString(),
                            fileName = name,
                            fileSize = size,
                            fileType = ext,
                            lastOpenedTimestamp = System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                Log.e("FeathurViewModel", "Failed to parse document", e)
                _error.value = "Unable to open document: ${e.localizedMessage}. This file might be password protected, corrupt, or unsupported."
                _parsedDocument.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun closeDocument() {
        _selectedDocumentUri.value = null
        _parsedDocument.value = null
        _error.value = null
        _searchQuery.value = ""
        _searchMatchIndex.value = 0
        _searchMatchCount.value = 0
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _searchMatchIndex.value = 0
        _searchMatchCount.value = 0
    }

    fun setSearchMatchCount(count: Int) {
        _searchMatchCount.value = count
        if (_searchMatchIndex.value >= count) {
            _searchMatchIndex.value = if (count > 0) 0 else 0
        }
    }

    fun nextSearchMatch() {
        val count = _searchMatchCount.value
        if (count > 0) {
            _searchMatchIndex.value = (_searchMatchIndex.value + 1) % count
        }
    }

    fun prevSearchMatch() {
        val count = _searchMatchCount.value
        if (count > 0) {
            _searchMatchIndex.value = (_searchMatchIndex.value - 1 + count) % count
        }
    }

    fun setActiveSheetIndex(index: Int) {
        _activeSheetIndex.value = index
    }

    fun setActiveSlideIndex(index: Int) {
        _activeSlideIndex.value = index
    }

    fun togglePresentationMode(enabled: Boolean) {
        _presentationMode.value = enabled
    }

    fun deleteRecentDocument(uriString: String) {
        viewModelScope.launch {
            repository.delete(uriString)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

class FeathurViewModelFactory(
    private val repository: DocumentHistoryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeathurViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeathurViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
