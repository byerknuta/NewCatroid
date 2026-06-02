package org.catrobat.catroid.utils

import android.content.Context
import org.catrobat.catroid.content.bricks.Brick

object FavoriteBricksManager {
    private const val PREFS_NAME = "favorite_bricks_prefs"
    private const val KEY_FAVORITES = "favorites_set"

    fun getFavoriteClassNames(context: Context): Set<String> {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(context: Context, brick: Brick): Boolean {
        return getFavoriteClassNames(context).contains(brick.javaClass.name)
    }

    fun addFavorite(context: Context, brick: Brick) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoriteClassNames(context).toMutableSet()
        favorites.add(brick.javaClass.name)
        sharedPrefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun removeFavorite(context: Context, brick: Brick) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val favorites = getFavoriteClassNames(context).toMutableSet()
        favorites.remove(brick.javaClass.name)
        sharedPrefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
}
