package com.app.exoplanethunter.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.app.exoplanethunter.MainActivity
import com.app.exoplanethunter.R
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetDatabase
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Home-screen widget showing a deterministic "Planet of the Day". The planet is chosen by the
 * current day number so it stays stable for 24 hours and rolls over to a new one each day.
 * Tapping the widget opens that planet's detail screen.
 */
class PlanetOfDayWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // DB access must be off the main thread; keep the broadcast alive while we work.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val planet = pickPlanetOfDay(context)
                appWidgetIds.forEach { id ->
                    appWidgetManager.updateAppWidget(id, buildViews(context, planet))
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun pickPlanetOfDay(context: Context): ExoplanetEntity? {
        val dao = ExoplanetDatabase.getInstance(context).exoplanetDao()
        val count = dao.getPlanetCountOnce()
        if (count <= 0) return null
        val dayNumber = System.currentTimeMillis() / DAY_MILLIS
        val offset = (dayNumber % count).toInt()
        return dao.getPlanetAtOffset(offset)
    }

    private fun buildViews(context: Context, planet: ExoplanetEntity?): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_planet_of_day)

        if (planet == null) {
            views.setViewVisibility(R.id.widget_planet_icon, View.GONE)
            views.setTextViewText(R.id.widget_planet_name, context.getString(R.string.widget_empty))
            views.setTextViewText(R.id.widget_planet_subtitle, "")
            views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context, null))
            return views
        }

        val iconPx = (ICON_DP * context.resources.displayMetrics.density).toInt()
        val planetBitmap = PlanetBitmapRenderer.render(
            sizePx = iconPx,
            equilibriumTempK = planet.equilibriumTempK,
            radiusEarth = planet.planetRadiusEarth,
            massEarth = planet.planetMassEarth
        )
        views.setViewVisibility(R.id.widget_planet_icon, View.VISIBLE)
        views.setImageViewBitmap(R.id.widget_planet_icon, planetBitmap)
        views.setTextViewText(R.id.widget_planet_name, planet.planetName)
        views.setTextViewText(
            R.id.widget_planet_subtitle,
            context.getString(R.string.widget_subtitle, planet.hostName, planet.discoveryYear)
        )
        views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context, planet.id))
        return views
    }

    private fun launchIntent(context: Context, planetId: Long?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (planetId != null) putExtra(EXTRA_PLANET_ID, planetId)
        }
        return PendingIntent.getActivity(
            context,
            planetId?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val EXTRA_PLANET_ID = "extra_widget_planet_id"
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val ICON_DP = 56
    }
}
