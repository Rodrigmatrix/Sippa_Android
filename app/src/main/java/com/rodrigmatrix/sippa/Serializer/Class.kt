package com.rodrigmatrix.sippa.serializer

data class Class(
    var id: String,
    var name: String,
    var professor: String,
    var professorEmail: String,
    var period: String,
    var code: String,
    var grades: MutableList<Grade>,
    var news: MutableList<News>,
    var classPlan: MutableList<ClassPlan>,
    var files: MutableList<File>,
    var percentageAttendance: String,
    var attendance: Attendance,
    var credits: Int
)