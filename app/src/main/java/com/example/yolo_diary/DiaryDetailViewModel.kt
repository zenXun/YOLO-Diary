package com.example.yolo_diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.yolo_diary.database.DiaryRepository
import java.util.*

class DiaryDetailViewModel() : ViewModel() {

    private val diaryRepository = DiaryRepository.get()
    private val diaryIdLiveData = MutableLiveData<UUID>()

    var diaryLiveData: LiveData<Diary?> =
        Transformations.switchMap(diaryIdLiveData) {
            diaryRepository.getDiary(it)
        }

    fun loadDiary(diaryId: UUID) {
        diaryIdLiveData.value = diaryId
    }

    fun saveDiary(diary: Diary) {
        diaryRepository.updateDiary(diary)
    }
}