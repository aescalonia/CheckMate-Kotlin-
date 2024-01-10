package com.project_checkmate

data class Task(
    val name: String,
    val dueDate: String,
    val category: String,
    val isDeleted: Boolean = false,
    val isCompleted: Boolean = false,
)