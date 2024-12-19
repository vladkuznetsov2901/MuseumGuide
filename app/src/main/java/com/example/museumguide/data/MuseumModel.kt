package com.example.museumguide.data

data class MuseumModel(
    val departments: List<Department>
)

data class Department(
    val departmentId: Int,
    val displayName: String
)