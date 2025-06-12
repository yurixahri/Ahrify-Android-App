package com.yurixahri.ahrify.utils;

import android.content.Context;

public class UnitConverter {

    public static float dpToPx(float dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale;
    }

    public static float spToPx(float sp, Context context) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float ptToPx(float pt, Context context) {
        return pt * (1.0f / 0.75f);
    }

    public static float inToPx(float inch, Context context) {
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        return inch * dpi;
    }

    public static float mmToPx(float mm, Context context) {
        float inch = mm / 25.4f;
        return inToPx(inch, context);
    }
}
