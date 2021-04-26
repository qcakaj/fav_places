package com.ckj.fraktonjobapplicationexample.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ckj.fraktonjobapplicationexample.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random


fun Context.isLocationEnabled(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // This is a new method provided in API 28
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isLocationEnabled
    } else {
        // This was deprecated in API 28
        val mode: Int = Settings.Secure.getInt(
            this.contentResolver, Settings.Secure.LOCATION_MODE,
            Settings.Secure.LOCATION_MODE_OFF
        )
        mode != Settings.Secure.LOCATION_MODE_OFF
    }


}

fun Activity.bitmapDescriptorFromVector(
    vectorResId: Int? = R.drawable.ic_location_small
): BitmapDescriptor? {
    val vectorDrawable =
        vectorResId?.let { ContextCompat.getDrawable(this, it) }
    vectorDrawable!!.setBounds(
        0,
        0,
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight
    )
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}
fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Context.showDialog(
    title: String,
    body: String,
    onPositiveButton: () -> Unit,
    onNegativeButton: () -> Unit,
) {
    AlertDialog.Builder(this).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Ok") { dialog,_->
            onPositiveButton()
            dialog.cancel()
        }
        it.setNegativeButton("Cancel"){dialog,_->
            onNegativeButton()
            dialog.cancel()
        }
    }.create().show()
}

@ColorInt
fun randomColor(): Int {
    return Color.argb(128, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}
fun Activity.showAppDoesNotWorkWithoutLocationAlertDialog(isLocationOn: Boolean) {
    val title = getString(R.string.location_access)
    val message =
        if (isLocationOn) getString(R.string.location_access) else getString(R.string.turn_on_device_location)
    val positiveButtonTitle =
        if (isLocationOn) getString(R.string.give_permission) else getString(R.string.turn_on)
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(
        positiveButtonTitle
    ) { _, _ -> locationSourceSettingsIntent() }
    builder.setNegativeButton(
        getString(R.string.close_app)
    ) { dialogInterface, i ->
        finish()
        dialogInterface.cancel()
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.show()
}
private fun Activity.locationSourceSettingsIntent() {
    // Build intent that displays the App settings screen.
    val intent = Intent()
    intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}
