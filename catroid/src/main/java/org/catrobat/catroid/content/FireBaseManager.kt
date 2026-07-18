package org.catrobat.catroid.content

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import org.catrobat.catroid.CatroidApplication

object FireBaseManager {
    private val isInitialized by lazy {
        val context = CatroidApplication.getAppContext()
        if (context != null && FirebaseApp.getApps(context).isEmpty()) {
            try {
                val res = context.resources
                val packageName = context.packageName

                val googleAppIdId = res.getIdentifier("google_app_id", "string", packageName)
                val googleApiKeyId = res.getIdentifier("google_api_key", "string", packageName)
                val projectIdId = res.getIdentifier("project_id", "string", packageName)
                val databaseUrlId = res.getIdentifier("firebase_database_url", "string", packageName)

                if (googleAppIdId != 0 && googleApiKeyId != 0) {
                    val appId = res.getString(googleAppIdId)
                    val apiKey = res.getString(googleApiKeyId)
                    val projectId = if (projectIdId != 0) res.getString(projectIdId) else null
                    val dbUrl = if (databaseUrlId != 0) res.getString(databaseUrlId) else null

                    val builder = FirebaseOptions.Builder()
                        .setApplicationId(appId)
                        .setApiKey(apiKey)

                    if (projectId != null) builder.setProjectId(projectId)
                    if (dbUrl != null) builder.setDatabaseUrl(dbUrl)

                    val options = builder.build()
                    FirebaseApp.initializeApp(context, options)
                    Log.i("Firebase", "Firebase initialized dynamically with explicit options successfully!")
                } else {
                    val dummyOptions = FirebaseOptions.Builder()
                        .setApplicationId("1:1234567890:android:abcdef12345678")
                        .setApiKey("AIzaSyDummyApiKeyPlaceholder123456789")
                        .setProjectId("dummy-project-id")
                        .build()
                    FirebaseApp.initializeApp(context, dummyOptions)
                    Log.w("Firebase", "Firebase initialized dynamically with dummy options (no resources found).")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Failed to initialize Firebase dynamically", e)
                try {
                    FirebaseApp.initializeApp(context)
                } catch (ignored: Exception) {}
            }
        }
        true
    }

    private fun getDbRef(urlOrId: String, key: String): DatabaseReference {
        val init = isInitialized

        val trimmedInput = urlOrId.trim()
        if (trimmedInput.isEmpty()) {
            throw IllegalArgumentException("Database ID or URL cannot be empty")
        }

        val formattedUrl = if (trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://")) {
            trimmedInput
        } else {
            "https://$trimmedInput-default-rtdb.firebaseio.com/"
        }

        return FirebaseDatabase.getInstance(formattedUrl).reference.child(key)
    }

    fun readFromDatabase(databaseUrl: String, key: String, callback: (String?) -> Unit) {
        try {
            getDbRef(databaseUrl, key).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.value?.toString() ?: "No data")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error reading data: ${error.message}", error.toException())
                    callback(null)
                }
            })
        } catch (e: Exception) {
            Log.e("Firebase", "Database read execution failed", e)
            callback(null)
        }
    }

    fun writeToDatabase(databaseUrl: String, key: String, value: String) {
        try {
            getDbRef(databaseUrl, key).setValue(value)
                .addOnFailureListener { error ->
                    Log.e("Firebase", "Error writing data: ${error.message}", error)
                }
        } catch (e: Exception) {
            Log.e("Firebase", "Database write execution failed", e)
        }
    }

    fun deleteFromDatabase(databaseUrl: String, key: String) {
        try {
            getDbRef(databaseUrl, key).removeValue()
                .addOnFailureListener { error ->
                    Log.e("Firebase", "Error deleting data: ${error.message}", error)
                }
        } catch (e: Exception) {
            Log.e("Firebase", "Database delete execution failed", e)
        }
    }
}
