package com.aditya.smartmedbox.feature.onboarding.domain.model

data class OnboardingDevice(
    val id: String = "DG-4081",
    val name: String = "DoseGuard Box #DG-4081",
    val firmwareVersion: String = "v3.2",
    val pairingCode: String = "8820",
    val signalStrength: String = "Strong Signal",
    val isSignalLocked: Boolean = true,
    val isPaired: Boolean = false
)
