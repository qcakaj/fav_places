package com.ckj.fraktonjobapplicationexample.util


import android.content.Context
import androidx.core.content.edit
import com.ckj.fraktonjobapplicationexample.R

internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdated The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdated: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdated)
        }
}