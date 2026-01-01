package com.lefa.thearchive.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lefa.thearchive.data.ArchiveRepository
import com.lefa.thearchive.data.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ArchiveRepository(application)

    val loadingState: StateFlow<ArchiveRepository.LoadingState> = repository.loadingState

    private val _searchResult = MutableStateFlow<SearchResult?>(null)
    val searchResult: StateFlow<SearchResult?> = _searchResult

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        _isSearching.value = true
        viewModelScope.launch {
            val result = repository.search(query)
            _searchResult.value = result
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResult.value = null
    }
}
