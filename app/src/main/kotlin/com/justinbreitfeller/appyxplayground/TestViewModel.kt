package com.justinbreitfeller.appyxplayground

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject


@HiltViewModel
class TestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: MutableStateFlow<ViewState>

    init {
        val name: String = savedStateHandle[NAV_TARGET_NAME_ARG]!!
        state = MutableStateFlow(
            ViewState(
                name = name,
                savedMessage = savedStateHandle[MESSAGE_ARG],
            )
        )

        savedStateHandle[MESSAGE_ARG] = "Data from nav target $name"
    }

    data class ViewState(
        val name: String,
        val savedMessage: String?
    )

    override fun onCleared() {
        super.onCleared()
        println(state.value.name + "'s view model was cleared")
    }

    companion object {
        const val NAV_TARGET_NAME_ARG = "NAME"
        const val MESSAGE_ARG = "MESSAGE"
    }
}
