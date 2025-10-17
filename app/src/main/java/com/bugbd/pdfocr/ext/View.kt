package com.bugbd.pdfocr.ext

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

//fun showToast(context: Context?, message: String?) {
//    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//}

fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun navigate(action: NavDirections, fragment: Fragment) {
    fragment.findNavController().navigate(action)
}

fun popBackStack(fragment: Fragment) {
    fragment.findNavController().popBackStack()
}

fun setToolbarTitle(titleTxtView: TextView, title: String) {
    titleTxtView.text = title
}


fun setDarkLightThem(them: String) {
    when (them) {
        "dark" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        "light" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        else -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

fun setCheckedRadio(switch: SwitchMaterial, them: String) {
    when (them) {
        "dark" -> {
            switch.isChecked = true
        }

        "light" -> {
            switch.isChecked = false
        }
    }
}

fun setLocale(context: Context, languageCode: String,activity: Activity) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    activity.recreate()
}
fun showToast(context: Context,message: String?){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

