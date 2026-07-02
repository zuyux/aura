package io.aura.android.domain.repository

import io.aura.android.domain.model.IncidentReport

interface IncidentReportRepository {
    suspend fun createLocalReport(report: IncidentReport)
}
