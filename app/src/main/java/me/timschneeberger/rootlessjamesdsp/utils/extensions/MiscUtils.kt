package me.timschneeberger.rootlessjamesdsp.utils.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.color.DynamicColors
import me.timschneeberger.rootlessjamesdsp.utils.SdkCheck
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import kotlin.math.*

fun String.asHtml(): Spanned = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)

fun Double.equalsDelta(other: Double, delta: Double = 0.000001): Boolean = abs(this/other - 1) < delta

fun Double.prettyNumberFormat(): String {
    if( this == 0.0 ) return "0"

    val prefix = if( this < 0 ) "-" else ""
    val num = abs(this)

    // figure out what group of suffixes we are in and scale the number
    val pow = floor(log10(num) /3).roundToInt()
    val base = num / 10.0.pow(pow * 3)

    // Using consistent rounding behavior, always rounding down since you want
    // 999999999 to show as 999.99M and not 1B
    val roundedDown = floor(base*100) /100.0

    // Convert the number to a string with up to 1 decimal place
    var baseStr = BigDecimal(roundedDown)
        .setScale(1, RoundingMode.HALF_EVEN)
        .toString()

    // Drop trailing zeros, then drop any trailing '.' if present
    baseStr = baseStr.dropLastWhile { it == '0' }.dropLastWhile { it == '.' }

    val suffixes = listOf("","k","M","B","T")

    return when {
        pow < suffixes.size -> "$prefix$baseStr${suffixes[pow]}"
        else -> "${prefix}∞"
    }
}

fun Boolean.toShort() = (if (this) 1 else 0).toShort()

val String.md5: ByteArray
    get() {
        return MessageDigest.getInstance("MD5").digest(this.toByteArray())
    }

private val isSamsung by lazy {
    Build.MANUFACTURER.equals("samsung", ignoreCase = true)
}

val isDynamicColorAvailable by lazy {
    DynamicColors.isDynamicColorAvailable() || (isSamsung && SdkCheck.isSnowCake)
}

fun View.setBackgroundFromAttribute(@AttrRes attrRes: Int) {
    val a = TypedValue()
    context.theme.resolveAttribute(attrRes, a, true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && a.isColorType) {
        setBackgroundColor(a.data)
    } else {
        background = ResourcesCompat.getDrawable(context.resources, a.resourceId, context.theme)
    }
}