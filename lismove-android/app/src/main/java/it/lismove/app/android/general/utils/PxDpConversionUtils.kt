package it.lismove.app.android.general.utils

import android.content.Context

class PxDpConversionUtils {
    companion object{
        /**
         * This method converts dp unit to equivalent pixels, depending on device density.
         *
         * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
         * @param context Context to get resources and device specific display metrics
         * @return A float value to represent px equivalent to dp depending on device density
         */
        fun convertDpToPx(context: Context, dp: Float): Float {
            return dp * context.getResources().getDisplayMetrics().density
        }
    }
}