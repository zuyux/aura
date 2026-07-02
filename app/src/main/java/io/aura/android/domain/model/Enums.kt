package io.aura.android.domain.model

enum class IncidentType {
    THEFT,
    ATTEMPTED_THEFT,
    SUSPICIOUS_PERSON,
    VIOLENCE,
    HARASSMENT,
    ACCIDENT,
    DANGEROUS_AREA,
    OTHER,
}

enum class SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
}

enum class ReportStatus {
    DRAFT,
    PENDING_SYNC,
    SUBMITTED,
    UNDER_REVIEW,
    COMMUNITY_CONFIRMED,
    AUTHORITY_CONFIRMED,
    RESOLVED,
    DISMISSED,
}

enum class LocationPrecision {
    EXACT,
    APPROXIMATE,
    DISTRICT_ONLY,
}

enum class ReportVisibility {
    PRIVATE,
    TRUSTED_CONTACTS,
    COMMUNITY,
}

enum class EvidenceType {
    PHOTO,
    VIDEO,
    AUDIO,
}

enum class EvidenceVisibility {
    PRIVATE,
    REPORT_REVIEWERS,
    COMMUNITY,
}

enum class VerificationAction {
    ALSO_SEEN,
    SEEMS_FALSE,
    RESOLVED,
    HIDE_ALERT,
}

enum class AlertStatus {
    UNVERIFIED,
    COMMUNITY_CONFIRMED,
    AUTHORITY_CONFIRMED,
    RESOLVED,
    DISMISSED,
}

enum class SafetySessionStatus {
    ACTIVE,
    SOS_TRIGGERED,
    ENDED_SAFE,
    ENDED_UNKNOWN,
}

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE,
    UPLOAD,
}

enum class SyncPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL,
}

enum class SyncStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
}
