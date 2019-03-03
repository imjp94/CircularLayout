package com.imjp.circularlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CircularLayout extends ViewGroup {
    private final Rect rectTmp = new Rect();
    private int maxCenterChildWidth = 0;
    private int maxCenterChildHeight = 0;
    private float maxChildWidth = 0;
    private float maxChildHeight = 0;
    private float centerOffsetX = 0;
    private float centerOffsetY = 0;
    private final ArrayList<View> centerViews = new ArrayList<>();
    private final ArrayList<View> orbitViews = new ArrayList<>();

    public float radius = 0;
    public float offsetAngle = 0;

    public CircularLayout(Context context) {
        this(context, null);
    }

    public CircularLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) parseXmlAttrs(context, attrs);
    }

    private void parseXmlAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularLayout);
        radius = a.getDimension(R.styleable.CircularLayout_radius, 0);
        offsetAngle = a.getFloat(R.styleable.CircularLayout_offset_angle, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childState = 0;
        centerViews.clear();
        orbitViews.clear();
        for (int i = 0; i < getChildCount(); i++) {
            childState = measureCircleSize(i, widthMeasureSpec, heightMeasureSpec, childState);
        }
        centerOffsetX = maxChildWidth;
        centerOffsetY = maxChildHeight;
        float maxWidth = (maxChildWidth + radius * 2) + maxCenterChildWidth + (hasCenter() ? centerOffsetX : 0);
        float maxHeight = (maxChildHeight + radius * 2) + maxCenterChildHeight + (hasCenter() ? centerOffsetY : 0);
        setMeasuredDimension(
                resolveSizeAndState((int) maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState((int) maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT)
        );
        Log.d("CircularLayout", "Measure");
    }

    private int measureCircleSize(int i, int widthMeasureSpec, int heightMeasureSpec, int childState) {
        final View child = getChildAt(i);
        if (child.getVisibility() == GONE) return childState;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();
        if (lp.center) {
            measureCenterView(width, height);
            centerViews.add(child);
        }
        else {
            measureOrbitView(width, height);
            orbitViews.add(child);
        }
        return combineMeasuredStates(childState, child.getMeasuredState());
    }

    private void measureCenterView(int width, int height) {
        maxCenterChildWidth = Math.max(maxCenterChildWidth,  width);
        maxCenterChildHeight = Math.max(maxCenterChildHeight, height);
    }

    private void measureOrbitView(int width, int height) {
        maxChildWidth = Math.max(maxChildWidth, width);
        maxChildHeight = Math.max(maxChildHeight, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Utils.rectOfLayout(this, left, top, right, bottom, rectTmp);
        float cX = rectTmp.exactCenterX();
        float cY = rectTmp.exactCenterY();
        for (int i = 0; i < centerViews.size(); i++) {
            layoutCenterChild(i, cX, cY);
        }
        for (int i = 0; i < orbitViews.size(); i++) {
            layoutOrbitChild(i, cX, cY);
        }
        Log.d("CircularLayout", "Layout");
    }

    private void layoutCenterChild(int i, float cX, float cY) {
        View child = centerViews.get(i);
        if (child.getVisibility() == GONE) return;
        layoutChild(child, (int) cX, (int) cY);
    }

    private void layoutOrbitChild(int i, float cX, float cY) {
        View child = orbitViews.get(i);
        if (child.getVisibility() == GONE) return;
        float radiusOffsetX = (maxCenterChildWidth + (hasCenter() ? centerOffsetX : 0)) / 2.0f;
        float radiusOffsetY = (maxCenterChildHeight + (hasCenter() ? centerOffsetY : 0)) / 2.0f;
        float angle = getAngleInRadian(i) + offsetAngleInRadian();
        layoutChild(
                child,
                (int) Utils.xInCircle(cX, radius + radiusOffsetX, angle),
                (int) Utils.yInCircle(cY, radius + radiusOffsetY, angle)
        );
    }

    private void layoutChild(View child, int x, int y) {
        final int halfWidth = child.getMeasuredWidth() / 2;
        final int halfHeight = child.getMeasuredHeight() / 2;
        int left = x - halfWidth;
        int top = y - halfHeight;
        int right = x + halfWidth;
        int bottom = y + halfHeight;
        child.layout(left, top, right, bottom);
    }

    public float offsetAngleInRadian() {
        return (float) Math.toRadians(offsetAngle);
    }

    public float getAngle(int i) {
        return 360.0f * i / orbitViews.size();
    }

    public float getAngleInRadian(int i) {
        return (float) Math.toRadians(getAngle(i));
    }

    private boolean hasCenter() {
        return centerViews.size() > 0;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(getContext(), null);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public boolean center = false;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            parseXmlAttrs(context, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        private void parseXmlAttrs(Context context, AttributeSet attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularLayout_Layout);
            center = a.getBoolean(R.styleable.CircularLayout_Layout_center, false);
            a.recycle();
        }
    }
}
