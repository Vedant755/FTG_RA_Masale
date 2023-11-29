package com.ftg.famasale.Models

data class GetAllVacanciesResponse(
    val message: String?,
    val status: Int?,
    val success: Boolean?,
    val data: List<JobDetail>?
)
