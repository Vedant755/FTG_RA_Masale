package com.ftg.famasale.Models

data class EmployeeCheckOutResponse(
    val data: CheckedOutDetails?,
    val message: String?,
    val success: Boolean?
)
