package com.ftg.famasale.Models

data class EmployeeDetails(
    val department_id: Int?,                // When fetching employees by Department ID
    val employee_check_in_status: Boolean?, // When fetching all Employees
    val employee_gender: String?,
    val employee_id: Int?,
    val employee_name: String?,
    val employee_status: Boolean?,
    val employee_number: Int?,              // When getting all checked out vehicles
    val Department: DepartmentDetails       // When getting all checked out vehicles
)