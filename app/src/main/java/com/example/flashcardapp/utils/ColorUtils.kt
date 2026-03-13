package com.example.flashcardapp.utils

import android.content.Context
import androidx.annotation.AttrRes

/**
 * Extension function để lấy màu từ thuộc tính theme
 */
fun Context.getAttrColor(@AttrRes attrRes: Int, defaultColor: Int = 0xFF1F7AE0.toInt()): Int {
    val typedArray = this.theme.obtainStyledAttributes(intArrayOf(attrRes))
    val color = typedArray.getColor(0, defaultColor)
    typedArray.recycle()
    return color
}

/**
 * Lớp ColorPalette để quản lý toàn bộ bảng màu từ attrs
 */
class ColorPalette(private val context: Context) {

    private val colorCache = mutableMapOf<Int, Int>()

    // Gradient backgrounds
    val gradientBackground1: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.gradientBackground1)

    val gradientBackground2: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.gradientBackground2)

    // Main background
    val mainBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.mainBackground)

    // Component background
    val componentBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.componentBackground)

    // Text color
    val textColor: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.textColor)

    // Loading gradient
    val loadingBackground1: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.loadingBackground1)

    val loadingBackground2: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.loadingBackground2)

    // Button
    val buttonColor: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.buttonColor)

    // Bottom nav buttons
    val buttonBottomNavActive: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.buttonBottomNavActive)

    val buttonBottomNavInactive: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.buttonBottomNavInactive)

    // Sub title
    val subTitleColor: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.subTitleColor)

    // Icon colors
    val iconBlue: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconBlue)

    val iconRed: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconRed)

    val iconGreen: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconGreen)

    val iconYellow: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconYellow)

    val iconPurple: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconPurple)

    val iconGray: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconGray)

    // Icon background colors
    val iconBlueBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconBlueBackground)

    val iconRedBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconRedBackground)

    val iconGreenBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconGreenBackground)

    val iconYellowBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconYellowBackground)

    val iconPurpleBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconPurpleBackground)

    val iconGrayBackground: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.iconGrayBackground)

    // Nav bar
    val navBarActive: Int
        get() = getColorFromCache(com.example.flashcardapp.R.attr.navBarActive)

    /**
     * Lấy màu từ cache hoặc từ theme nếu chưa cache
     */
    private fun getColorFromCache(@AttrRes attrRes: Int): Int {
        return colorCache.getOrPut(attrRes) {
            context.getAttrColor(attrRes)
        }
    }

    /**
     * Xóa cache để tái tải màu (hữu ích khi theme thay đổi)
     */
    fun clearCache() {
        colorCache.clear()
    }
}

