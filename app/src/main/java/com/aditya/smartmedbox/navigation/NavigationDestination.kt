package com.aditya.smartmedbox.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object Schedule : Screen

    @Serializable
    data object Medicines : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object Alerts : Screen

    @Serializable
    data object Settings : Screen
}

typealias NavigationDestination = Screen
