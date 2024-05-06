package com.dynamic.island.oasis.util

import android.content.Context
import android.os.Build
import java.util.Locale

class LockGuide {
    /***
     * Xiaomi
     */
    private val BRAND_XIAOMI = "xiaomi"
    private val BRAND_XIAOMI_POCO = "poco"
    private val BRAND_XIAOMI_REDMI = "redmi"


    /***
     * Samsung
     */
    private val BRAND_SAMSUNG = "samsung"

    /***
     * One plus
     */
    private val BRAND_ONE_PLUS = "oneplus"


    fun loadUrl()=when (Build.BRAND.lowercase(Locale.ROOT)) {
        BRAND_XIAOMI, BRAND_XIAOMI_POCO, BRAND_XIAOMI_REDMI -> "https://youtu.be/yrbrSVIk_cc?t=22"
        BRAND_SAMSUNG -> "https://youtu.be/l-d0NHxmXRs?t=19"
        BRAND_ONE_PLUS -> "https://youtu.be/ZhofWrdXqmo?t=31"
        else -> "https://youtu.be/jUHn3jD7Jno?t=134"
    }
}