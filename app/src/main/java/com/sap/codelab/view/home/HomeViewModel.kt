package com.sap.codelab.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home Activity.
 */
internal class HomeViewModel : ViewModel() {

    private var isShowAll = false
    private val _memos: MutableStateFlow<List<Memo>> = MutableStateFlow(listOf())
    val memos: StateFlow<List<Memo>> = _memos

    private val _locationEnabled = MutableLiveData<Boolean>()
    val locationEnabled: LiveData<Boolean> get() = _locationEnabled

    /**
     * Loads all memos.
     */
    fun loadAllMemos() {
        isShowAll = true
        viewModelScope.launch(Dispatchers.Default) {
            _memos.value = Repository.getAll()
        }
    }

    /**
     * Loads all open (not done) memos.
     */
    fun loadOpenMemos() {
        isShowAll = false
        viewModelScope.launch(Dispatchers.Default) {
            _memos.value = Repository.getOpen()
        }
    }

    fun refreshMemos() {
        if (isShowAll) {
            loadAllMemos()
        } else {
            loadOpenMemos()
        }
    }

    /**
     * Updates the given memo, marking it as done if isChecked is true.
     */
    fun updateMemo(memo: Memo, isChecked: Boolean) {
        ScopeProvider.application.launch(Dispatchers.Default) {
            if (isChecked) {
                Repository.saveMemo(memo.copy(isDone = true))
            }
        }
    }

    /** Updates the LiveData with location enabled or disabled */
    fun updateLocationEnabledStatus(enabled: Boolean) {
        _locationEnabled.value = enabled
    }
}
