package com.example.yolo_diary

import androidx.lifecycle.ViewModel
import com.example.yolo_diary.database.DiaryRepository

class DiaryListViewModel : ViewModel() {

    private val diaryRepository = DiaryRepository.get()
    val diariesListLiveData = diaryRepository.getDiaries()

    fun addDiary(diary: Diary) {
        diaryRepository.addDiary(diary)
    }
}