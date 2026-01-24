package com.example.myapplication.ui

sealed class LoadState<out T> {
    data object Loading : LoadState<Nothing>()
    data class Success<T>(val data: T) : LoadState<T>()
    data class Error(val throwable: Throwable) : LoadState<Nothing>()
}
