package com.ftg.famasale.Models

data class EmployeeCheckInResponse(
    val data: CheckedInDetails?,
    val message: String?,
    val success: Boolean?
)