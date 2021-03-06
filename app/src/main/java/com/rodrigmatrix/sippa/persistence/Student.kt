package com.rodrigmatrix.sippa.persistance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey
    var id: Int,
    var jsession: String,
    var responseHtml: String,
    var classSetHtml: String,
    var login: String,
    var password: String,
    var name: String,
    var matricula: String,
    var theme: String,
    var hasSavedData: Boolean,
    var lastUpdate: String
)

@Entity(tableName = "classes")
data class Class(
    @PrimaryKey
    var id: String,
    var name: String,
    var professorName: String,
    var professorEmail: String,
    var percentageAttendance: String,
    var credits: Int,
    var missed: Int,
    var totalAttendance: Int
)

@Entity(tableName = "news")
data class News(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var classId: String,
    var date: String,
    var content: String
)

@Entity(tableName = "grades")
class Grade(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var classId: String,
    var name: String,
    var grade: String
)

@Entity(tableName = "classPlan")
data class ClassPlan(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var classId: String,
    var date: String,
    var attendance: String,
    var planned: String,
    var diary: String
)

@Entity(tableName = "files")
data class File(
    @PrimaryKey
    var id: Int,
    var classId: String,
    var name: String
)

@Entity(tableName = "horasComplementares")
data class HoraComplementar(
    @PrimaryKey
    var id: Int,
    var name: String,
    var professor: String,
    var total: String
)