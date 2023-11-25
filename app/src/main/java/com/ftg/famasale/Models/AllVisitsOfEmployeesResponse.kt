package com.ftg.famasale.Models

data class AllVisitsOfEmployeesResponse(
    val data: List<EmployeeVisitDetails>?,
    val success: Boolean?,
    val total: Int?,
    val message: String?
)