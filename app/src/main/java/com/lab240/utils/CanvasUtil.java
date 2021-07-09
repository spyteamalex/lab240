package com.lab240.utils;

import android.graphics.Paint;
import android.graphics.Rect;

public class CanvasUtil {
    public static float getTextSizeForWidth(Paint paint, float desiredWidth,
                                            int len) {
        final float testTextSize = 48f;

        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(getRepeated("9", len), 0, len, bounds);

        return testTextSize * desiredWidth / bounds.width();
    }

    public static float getTextSizeForHeight(Paint paint, float desiredHeight,
                                             int len) {
        final float testTextSize = 48f;

        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(getRepeated("9", len), 0, len, bounds);

        return testTextSize * desiredHeight / bounds.height();
    }

    public static float getTextSizeForBounds(Paint paint, float desiredWidth, float desiredHeight,
                                             int len) {
        final float testTextSize = 48f;

        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(getRepeated("9", len), 0, len, bounds);

        return Math.min(testTextSize * desiredHeight / bounds.height(),
                testTextSize * desiredWidth / bounds.width());
    }

    private static String getRepeated(String s, int times){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < times; i++)
            sb.append(s);
        return sb.toString();
    }


}
