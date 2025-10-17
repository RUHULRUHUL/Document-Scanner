package com.bugbd.pdfocr.local_bd

import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KClass

class PreferenceManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_preferences"
    }

    // Generalized set() function for different data types
    fun <T> set(key: String, value: T) {
        with(preferences.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }

    // Generalized get() function with type inference
    fun <T : Any> get(key: String, defaultValue: T, clazz: KClass<T>): T {
        return when (clazz) {
            String::class -> preferences.getString(key, defaultValue as? String) as T
            Boolean::class -> preferences.getBoolean(key, defaultValue as Boolean) as T
            Int::class -> preferences.getInt(key, defaultValue as Int) as T
            Float::class -> preferences.getFloat(key, defaultValue as Float) as T
            Long::class -> preferences.getLong(key, defaultValue as Long) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}