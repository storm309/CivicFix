package com.example.smartwastemanagementapp.model

enum class AuthRole(val dbValue: String) {
    USER("user"),
    ADMIN("admin");

    companion object {
        fun from(value: String?): AuthRole {
            return entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) } ?: USER
        }
    }
}

