package com.example.smartwastemanagementapp.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val role: String = "user",
    val authProvider: String = "email",
    val impactPoints: Int = 0,
    val reportsSubmitted: Int = 0
)
