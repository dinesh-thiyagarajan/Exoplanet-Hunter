package com.app.exoplanethunter.presentation.screens.planetlist

import androidx.annotation.StringRes
import com.app.exoplanethunter.R

/** User-selectable orderings for the planet list. */
enum class SortOption(@StringRes val labelRes: Int) {
    DEFAULT(R.string.sort_default),
    NEAREST(R.string.sort_nearest),
    LARGEST(R.string.sort_largest),
    EARTH_LIKE(R.string.sort_earth_like),
    NEWEST(R.string.sort_newest),
    NAME_AZ(R.string.sort_name_az)
}
