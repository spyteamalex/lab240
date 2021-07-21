package com.lab240.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class ScrollLockingLayoutManager extends LinearLayoutManager {
    private boolean scrollH = true;

    public ScrollLockingLayoutManager setScrollable(boolean scroll) {
        this.scrollH = scroll;
        this.scrollV = scroll;
        return this;
    }

    public ScrollLockingLayoutManager setHorizontallyScrollable(boolean scrollH) {
        this.scrollH = scrollH;
        return this;
    }

    public ScrollLockingLayoutManager setVerticallyScrollable(boolean scrollV) {
        this.scrollV = scrollV;
        return this;
    }

    private boolean scrollV = true;

    public ScrollLockingLayoutManager(Context context) {
        super(context);
    }

    public ScrollLockingLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public ScrollLockingLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollH && super.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        System.out.println(scrollV);
        return scrollV && super.canScrollVertically();
    }
}
