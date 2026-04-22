package com.example.smartwastemanagementapp.model

enum class ReportModerationStatus(val dbValue: String) {
    PENDING_APPROVAL("pending_approval"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun from(value: String?): ReportModerationStatus {
            return entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) } ?: PENDING_APPROVAL
        }
    }
}

