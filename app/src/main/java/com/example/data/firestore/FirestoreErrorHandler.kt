package com.example.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OperationType {
  CREATE, READ, UPDATE, DELETE, LIST
}

fun handleFirestoreError(
  exception: Exception,
  operation: OperationType,
  path: String
) {
  val errorCode = if (exception is FirebaseFirestoreException) {
    exception.code.name
  } else {
    "UNKNOWN"
  }

  val json = JSONObject().apply {
    put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
    put("operation", operation.name)
    put("path", path)
    put("code", errorCode)
    put("message", exception.localizedMessage ?: "Unknown error")
  }

  Log.e("FirestoreSecurity", json.toString())
}
