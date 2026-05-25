package com.example.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeathurViewModel(private val repository: DocumentHistoryRepository) : ViewModel() {

    private val _selectedDocumentUri = MutableStateFlow<Uri?>(null)
    val selectedDocumentUri: StateFlow<Uri?> = _selectedDocumentUri

    private val _parsedDocument = MutableStateFlow<ParsedDocument?>(null)
    val parsedDocument: StateFlow<ParsedDocument?> = _parsedDocument

    private val _documentName = MutableStateFlow("Document")
    val documentName: StateFlow<String> = _documentName

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

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
            _isLoading.value = true
            _error.value = null
            _selectedDocumentUri.value = uri
            _searchQuery.value = ""
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
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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

class FeathurViewModelFactory(private val repository: DocumentHistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeathurViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeathurViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
