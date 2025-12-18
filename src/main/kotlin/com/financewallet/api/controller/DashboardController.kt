package com.financewallet.api.controller

import com.financewallet.api.dto.response.common.ApiResponse
import com.financewallet.api.dto.response.dashboard.DashboardResponse
import com.financewallet.api.dto.response.dashboard.StatisticsResponse
import com.financewallet.api.service.dashboard.DashboardService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {
    /**
     * Get dashboard overview
     * GET /api/dashboard
     */
    @GetMapping
    fun getDashboard(): ResponseEntity<ApiResponse<DashboardResponse>> {
        val dashboard = dashboardService.getDashboard()
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = dashboard,
                message = "Dashboard data retrieved successfully"
            )
        )
    }

    /**
     * Get statistics for date range
     * GET /api/dashboard/statistics?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/statistics")
    fun getStatistics(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?
    ): ResponseEntity<ApiResponse<StatisticsResponse>> {
        // Default to current month if not provided
        val today = LocalDate.now()
        val start = startDate ?: today.with(TemporalAdjusters.firstDayOfMonth())
        val end = endDate ?: today.with(TemporalAdjusters.lastDayOfMonth())

        val statistics = dashboardService.getStatistics(start, end)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = statistics,
                message = "Statistics retrieved successfully"
            )
        )
    }

    /**
     * Get this month's statistics
     * GET /api/dashboard/statistics/this-month
     */
    @GetMapping("/statistics/this-month")
    fun getThisMonthStatistics(): ResponseEntity<ApiResponse<StatisticsResponse>> {
        val today = LocalDate.now()
        val startDate = today.with(TemporalAdjusters.firstDayOfMonth())
        val endDate = today.with(TemporalAdjusters.lastDayOfMonth())

        val statistics = dashboardService.getStatistics(startDate, endDate)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = statistics,
                message = "This month statistics retrieved successfully"
            )
        )
    }

    /**
     * Get last month's statistics
     * GET /api/dashboard/statistics/last-month
     */
    @GetMapping("/statistics/last-month")
    @Tag(name = "Dashboard", description = "Dashboard management endpoints")
    fun getLastMonthStatistics(): ResponseEntity<ApiResponse<StatisticsResponse>> {
        val lastMonth = LocalDate.now().minusMonths(1)
        val startDate = lastMonth.with(TemporalAdjusters.firstDayOfMonth())
        val endDate = lastMonth.with(TemporalAdjusters.lastDayOfMonth())

        val statistics = dashboardService.getStatistics(startDate, endDate)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = statistics,
                message = "Last month statistics retrieved successfully"
            )
        )
    }

    /**
     * Get this year's statistics
     * GET /api/dashboard/statistics/this-year
     */
    @GetMapping("/statistics/this-year")
    fun getThisYearStatistics(): ResponseEntity<ApiResponse<StatisticsResponse>> {
        val today = LocalDate.now()
        val startDate = today.with(TemporalAdjusters.firstDayOfYear())
        val endDate = today.with(TemporalAdjusters.lastDayOfYear())

        val statistics = dashboardService.getStatistics(startDate, endDate)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = statistics,
                message = "This year statistics retrieved successfully"
            )
        )
    }
}