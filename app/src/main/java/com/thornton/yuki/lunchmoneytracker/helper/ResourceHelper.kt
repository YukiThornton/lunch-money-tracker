package com.thornton.yuki.lunchmoneytracker.helper

import android.content.Context
import android.util.TypedValue

class ResourceHelper {

    companion object {
        fun getThemeColor(context: Context, id: Int): Int {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(id, typedValue, true)
            return typedValue.data
        }
    }
}