package com.ftg.famasale.Models

data class VisitorVisitDetails(
    val Employee: Employee?,
    val createdAt: String?,
    val employee_check_out: String?,
    val employee_id: Int?,
    val updatedAt: String?,
    val visitor_address: String?,
    val visitor_check_in: String?,
    val visitor_check_in_added_by: String?,
    val visitor_check_in_added_by_id: Int?,
    val visitor_check_out: String?,
    val visitor_check_out_added_by: String?,
    val visitor_check_out_added_by_id: Int?,
    val visitor_deleted: Boolean?,             // When fetching all visits
    val visitor_description: String?,
    val visitor_gender: String?,
    val visitor_id: Int?,
    val visitor_mobile: Long?,
    val visitor_name: String?,
    val visitor_status: String?
)