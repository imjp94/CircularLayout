package com.imjp.circularlayout;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.ViewGroup;

public class Utils {
    public static Point pointInCircle(float cX, float cY, float r, float angle, Point point) {
        point.set((int) xInCircle(cX, r, angle), (int) yInCircle(cY, r, angle));
        return point;
    }

    public static Point pointInCircle(float cX, float cY, float r, float angle) {
        return pointInCircle(cX, cY, r, angle, new Point());
    }

    public static float xInCircle(float cX, float r, float angle) {
        return (float) (cX + r * Math.cos(angle));
    }

    public static float yInCircle(float cY, float r, float angle) {
        return (float) (cY + r * Math.sin(angle));
    }

    public static Rect rectOfLayout(ViewGroup layout, int left, int top, int right, int bottom, Rect rect) {
        rect.left = layout.getPaddingLeft();
        rect.top = layout.getPaddingTop();
        rect.right = right - left - layout.getPaddingRight();
        rect.bottom = bottom - top - layout.getPaddingBottom();
        return rect;
    }

    public static Rect rectOfLayout(ViewGroup layout, int left, int top, int right, int bottom) {
        return rectOfLayout(layout, left, top, right, bottom, new Rect());
    }
}
