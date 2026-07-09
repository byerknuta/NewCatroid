/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2022 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
                    FirebaseApp.initializeApp(context)
                    Log.w("Firebase", "Fallback to default Firebase initialization.")
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
