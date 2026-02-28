package com.example.cs388project5

import android.app.Application

class BitFitApplication : Application() {
    val db by lazy { AppDatabase.getInstance(this) }
}
