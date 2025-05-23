package com.rix.womblab.presentation.auth.register

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val profession: String,
    val specialization: String? = null,
    val workplace: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val wantsNewsletter: Boolean = true,
    val wantsNotifications: Boolean = true
)