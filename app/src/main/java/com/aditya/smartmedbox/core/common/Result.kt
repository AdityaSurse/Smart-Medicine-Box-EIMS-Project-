package com.aditya.smartmedbox.core.common

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>

    data class Error(
        val throwable: Throwable? = null,
        val message: String = throwable?.localizedMessage ?: "Unknown error"
    ) : Result<Nothing>

    data object Loading : Result<Nothing>
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (throwable: Throwable?, message: String) -> Unit): Result<T> {
    if (this is Result.Error) action(throwable, message)
    return this
}
