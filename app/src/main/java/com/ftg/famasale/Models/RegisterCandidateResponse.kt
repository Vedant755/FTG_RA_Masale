package com.ftg.famasale.Models

data class RegisterCandidateResponse(
    val message: String?,
    val status: Int?,
    val success: Boolean?,
    val data: CandidateDetails?
)
