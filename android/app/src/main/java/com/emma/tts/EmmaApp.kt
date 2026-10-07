package com.emma.tts

import android.app.Application

class EmmaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: EmmaApp
            private set
    }
}
