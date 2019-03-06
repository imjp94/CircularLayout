package com.imjp.circularlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CircularLayout extends ViewGroup {
    public final static String TAG = CircularLayout.class.getSimpleName();
    private final Rect rectTmp = new Rect();
    private final ArrayList<View> centerViews = new ArrayList<>();
    private final ArrayList<View> orbitViews = new ArrayList<>();
    private int maxCenterChildWidth = 0;
    private int maxCenterChildHeight = 0;
    private float maxChildWidth = 0;
    private float maxChildHeight = 0;
    private float diameter = 0;

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

        diameter = 0;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            diameter = MeasureSpec.getSize(widthMeasureSpec);
        }
        else {
            diameter = maxChildWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            diameter = Math.min(diameter, MeasureSpec.getSize(heightMeasureSpec));
        }
        else {
            diameter = Math.max(diameter, maxChildHeight);
        }

        float maxWidth = diameter + maxCenterChildWidth + maxChildWidth;
        float maxHeight = diameter + maxCenterChildHeight + maxChildHeight;

        setMeasuredDimension(
                resolveSizeAndState((int) maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState((int) maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT)
        );
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

        float maxWidth = rectTmp.width() - maxChildWidth * 2.0f;
        float maxHeight = rectTmp.height() - maxChildHeight * 2.0f;
        for (int i = 0; i < centerViews.size(); i++) {
            layoutCenterChild(i, cX, cY, maxWidth, maxHeight);
        }
        maxWidth = rectTmp.width() - maxChildWidth;
        maxHeight = rectTmp.height() - maxChildHeight;
        for (int i = 0; i < orbitViews.size(); i++) {
            layoutOrbitChild(i, cX, cY, maxWidth, maxHeight);
        }
    }

    private void layoutCenterChild(int i, float cX, float cY, float maxWidth, float maxHeight) {
        View child = centerViews.get(i);
        if (child.getVisibility() == GONE) return;
        layoutChild(
                child, (int) cX, (int) cY,
                (int) Math.min(child.getMeasuredWidth(), maxWidth),
                (int) Math.min(child.getMeasuredHeight(), maxHeight)
        );
    }

    private void layoutOrbitChild(int i, float cX, float cY, float maxWidth, float maxHeight) {
        View child = orbitViews.get(i);
        if (child.getVisibility() == GONE) return;
        float radiusX = Math.min((diameter + maxCenterChildWidth) / 2.0f, maxWidth / 2.0f);
        float radiusY = Math.min((diameter + maxCenterChildHeight) / 2.0f, maxHeight / 2.0f);
        float angle = getAngleInRadian(i) + offsetAngleInRadian();
        layoutChild(
                child,
                (int) Utils.xInCircle(cX, radiusX, angle),
                (int) Utils.yInCircle(cY, radiusY, angle),
                child.getMeasuredWidth(), child.getMeasuredHeight()
        );
    }

    private void layoutChild(View child, int x, int y, int width, int height) {
        int left = x - width / 2;
        int top = y - height / 2;
        int right = x + width / 2;
        int bottom = y + height / 2;
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
