package com.example.yolo_diary

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Diary(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var completed: Boolean = false)
