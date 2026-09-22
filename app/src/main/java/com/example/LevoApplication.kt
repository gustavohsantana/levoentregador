package com.example

import android.app.Application
import com.example.data.local.DriverDatabase
import com.example.data.local.SessionManager
import com.example.data.repository.DriverRepository

class LevoApplication : Application() {

    lateinit var sessionManager: SessionManager
        private set

    lateinit var repository: DriverRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        repository = DriverRepository(this, sessionManager)
    }
}
