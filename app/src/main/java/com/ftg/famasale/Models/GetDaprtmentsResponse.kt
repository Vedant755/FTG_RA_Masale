package com.ftg.famasale.Models

data class GetDaprtmentsResponse(
    val data: List<DepartmentDetails>?,
    val success: Boolean?,
    val total: Int?,
    val message: String?
)