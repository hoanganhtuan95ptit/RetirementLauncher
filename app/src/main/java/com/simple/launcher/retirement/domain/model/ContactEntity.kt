package com.simple.launcher.retirement.domain.model

data class ContactEntity(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)
