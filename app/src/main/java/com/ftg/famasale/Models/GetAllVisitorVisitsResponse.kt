package com.ftg.famasale.Models

data class GetAllVisitorVisitsResponse(
    val data: List<VisitorVisitDetails>?,
    val message: String?,
    val status: Int?,
    val success: Boolean?
)