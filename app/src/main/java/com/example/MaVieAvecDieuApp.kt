package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class MaVieAvecDieuApp : Application() {
  override fun onCreate() {
    super.onCreate()
    if (FirebaseApp.getApps(this).isEmpty()) {
      FirebaseApp.initializeApp(this)
    }
  }
}
