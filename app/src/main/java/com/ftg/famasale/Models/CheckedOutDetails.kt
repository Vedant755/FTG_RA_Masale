package com.ftg.famasale.Models

data class CheckedOutDetails(
    val createdAt: String?,
    val employee_id: Int?,
    val employee_visit_check_in: String?,
    val employee_visit_check_in_added_by: String?,
    val employee_visit_check_in_added_by_id: Int?,
    val employee_visit_check_out: String?,
    val employee_visit_check_out_added_by: String?,
    val employee_visit_check_out_added_by_id: Int?,
    val employee_visit_deleted: Boolean?,
    val employee_visit_id: Int?,
    val updatedAt: String?
)