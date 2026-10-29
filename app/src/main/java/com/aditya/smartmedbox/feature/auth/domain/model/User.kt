package com.aditya.smartmedbox.feature.auth.domain.model

data class User(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val linkedDeviceId: String? = "DG-4081"
)
