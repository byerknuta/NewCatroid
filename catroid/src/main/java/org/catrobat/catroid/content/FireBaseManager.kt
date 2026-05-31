package org.catrobat.catroid.content

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import org.catrobat.catroid.CatroidApplication

object FireBaseManager {
    private val isInitialized by lazy {
        if (FirebaseApp.getApps(CatroidApplication.getAppContext()).isEmpty()) {
            FirebaseApp.initializeApp(CatroidApplication.getAppContext())
        }
        true
    }

    private fun getDbRef(url: String, key: String): DatabaseReference {
        val init = isInitialized
        return FirebaseDatabase.getInstance(url).reference.child(key)
    }

    fun readFromDatabase(databaseUrl: String, key: String, callback: (String?) -> Unit) {
        getDbRef(databaseUrl, key).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.value?.toString() ?: "No data")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error reading data: ${error.message}", error.toException())
                callback(null)
            }
        })
    }

    fun writeToDatabase(databaseUrl: String, key: String, value: String) {
        getDbRef(databaseUrl, key).setValue(value)
            .addOnFailureListener { error ->
                Log.e("Firebase", "Error writing data: ${error.message}", error)
            }
    }

    fun deleteFromDatabase(databaseUrl: String, key: String) {
        getDbRef(databaseUrl, key).removeValue()
            .addOnFailureListener { error ->
                Log.e("Firebase", "Error deleting data: ${error.message}", error)
            }
    }
}
