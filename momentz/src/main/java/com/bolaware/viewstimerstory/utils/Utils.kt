package com.bolaware.viewstimerstory.utils

import android.content.Context
import android.view.View

/**
 * by acdprd | 14.11.2019.
 */
object Utils {
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else 24.toFloat().toPixel(context)
    }

    @JvmStatic
    fun fixRootTopPadding(root: View) {
        root.context?.let {
            val topPadding = getStatusBarHeight(it)
            root.setPadding(root.paddingLeft, topPadding, root.right, root.paddingBottom)
        }
    }
}