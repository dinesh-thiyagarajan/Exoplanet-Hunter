package com.app.exoplanethunter.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build

/** Helpers for the one-tap "pin widget to home screen" flow (API 26+, launcher-dependent). */
object WidgetPinHelper {

    /** True when the current launcher supports programmatic widget pinning. */
    fun isSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }

    /**
     * Ask the launcher to show its "add this widget?" dialog for the Planet-of-the-Day widget.
     * Returns false if pinning isn't supported.
     */
    fun requestPin(context: Context): Boolean {
        if (!isSupported(context)) return false
        val provider = ComponentName(context, PlanetOfDayWidget::class.java)
        return AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, null)
    }
}
