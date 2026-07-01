package com.example.netmusicandroid.ui

sealed interface UiEvent {
    data class Toast(val message: String) : UiEvent
}
