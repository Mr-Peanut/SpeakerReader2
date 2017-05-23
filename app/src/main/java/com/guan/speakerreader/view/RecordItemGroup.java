package com.guan.speakerreader.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by guans on 2017/5/20.
 */

public class RecordItemGroup extends ViewGroup {
    public boolean isDrawerOpen = false;
    private int leftBorder;
    private int totalChildrenWidth;
    private int mTouchSlop;
    private float mXDown;
    private float mXMove;
    private float mXLastMove;
    private Scroller mScroller;
    private int hasScrolledX;
    public RecordItemGroup(Context context) {
        super(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mScroller = new Scroller(context);
    }

    public RecordItemGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed){
            for(int i = 0; i<getChildCount(); i++){
                View child=getChildAt(i);
               child.layout(totalChildrenWidth,0,totalChildrenWidth+child.getMeasuredWidth(),getMeasuredHeight());
                totalChildrenWidth+=child.getMeasuredWidth();
            }
            leftBorder=0;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                hasScrolledX=getScrollX();
                 return false;
            case MotionEvent.ACTION_MOVE:
                Log.e("intercept","intercept");
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                int scrolledX = (int) (mXDown - mXLastMove);
                if (hasScrolledX + scrolledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;
                }else if (hasScrolledX + scrolledX  >leftBorder+totalChildrenWidth-getMeasuredWidth() ) {
                    scrollTo(leftBorder+totalChildrenWidth-getMeasuredWidth(), 0);
                    return true;
                }
                scrollTo(hasScrolledX + scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                int hasScroll = getScrollX() % getMeasuredWidth();
                int dx;
                float fingerMove=event.getRawX()-mXDown;
                    if(fingerMove<0){
                        dx=totalChildrenWidth-getMeasuredWidth()-hasScroll;
                    }else {
                        dx=-hasScroll;
                    }
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
        isDrawerOpen = !(getScrollX() == 0);
        Log.e("isDrawerOpen", String.valueOf(isDrawerOpen));
    }
    public void backTo0(){
        scrollTo(0,0);
    }
}
