package com.example.yolo_diary.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.yolo_diary.Diary
import java.util.*

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary")
    fun getDiaries(): LiveData<List<Diary>>

    @Query("SELECT * FROM diary WHERE id=(:id)")
    fun getDiary(id: UUID): LiveData<Diary?>

    @Update
    fun updateDiary(diary: Diary)

    @Insert
    fun addDiary(diary: Diary)

    @Delete
    fun deleteDiary(diary: Diary)
}