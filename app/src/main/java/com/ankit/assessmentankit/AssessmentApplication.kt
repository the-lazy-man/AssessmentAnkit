package com.ankit.assessmentankit

import android.app.Application
import com.ankit.assessmentankit.util.AppContainer

class AssessmentApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
