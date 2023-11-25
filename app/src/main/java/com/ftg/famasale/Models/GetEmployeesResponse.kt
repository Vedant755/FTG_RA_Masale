package com.ftg.famasale.Models

data class GetEmployeesResponse(
    val data: List<EmployeeDetails>?,
    val success: Boolean?,
    val total: Int?,
    val message: String?
)