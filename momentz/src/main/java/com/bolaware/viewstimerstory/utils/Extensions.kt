package com.bolaware.viewstimerstory.utils

import android.content.Context
import android.util.TypedValue

fun Float.toPixel(context: Context): Int {
    val r = context.resources
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        r.displayMetrics
    ).toInt()
}