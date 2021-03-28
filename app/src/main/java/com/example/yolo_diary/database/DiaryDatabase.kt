package com.example.yolo_diary.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yolo_diary.Diary

@Database(entities = [ Diary::class ], version = 1)
@TypeConverters(DiaryTypeConverters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
}