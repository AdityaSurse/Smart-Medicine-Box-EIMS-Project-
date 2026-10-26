package com.aditya.smartmedbox.feature.auth.domain.model

data class User(
    val uid: String,
    val email: String,
    val linkedDeviceId: String? = null
)
