package com.example.yolo_diary.database

import android.app.Application

class DiaryIntentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DiaryRepository.initialize(this)
    }
}