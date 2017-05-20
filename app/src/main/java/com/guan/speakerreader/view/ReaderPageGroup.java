package com.guan.speakerreader.view;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.guan.speakerreader.adapter.ReaderPagerAdapter;
import com.guan.speakerreader.util.ContentController;

//import android.view.GestureDetector;

/**
 * Created by guans on 2017/4/20.
 */
/*
设置一个适配器adapter
往左滑的时候要是左边还有view，左边界往左移一个view，往右还有的话右边界往右移一个view
添加view后
难点：怎么在0的位置添加view
注意view的回收，一直保证最多有三个view，多余的view remove掉
 */
public class ReaderPageGroup extends ViewGroup {

    private int mTouchSlop;
    private float mXDown;
    private float mXMove;
    private float mXLastMove;
    private Scroller mScroller;
    private int leftBorder;
    private int rightBorder;
    private int childWidth;
    private boolean myChanged = false;
    private ContentController mContentController;
    private int onShowPosition = 0;
    private ReaderPagerAdapter pagerAdapter;
    private int hasScrolledX;
    private OnPageChangeListener mPageChangeListener;
    private boolean isFirstLayout = true;
//    private GestureDetector mGestureDetector;
//    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener;
    private SparseArray<View> viewHashMap;

    public ReaderPageGroup(Context context) {
        super(context);
    }

    public ReaderPageGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mScroller = new Scroller(context);
        viewHashMap = new SparseArray<>();
//        initGestureDetector();
    }
//    private void initGestureDetector(){
//        setLongClickable(true);
//        mSimpleOnGestureListener=new PageGroupOnGestureListener();
//        mGestureDetector=new GestureDetector(mContext,mSimpleOnGestureListener );
////        setOnTouchListener(new OnTouchListener() {
////            @Override
////            public boolean onTouch(View v, MotionEvent event) {
////                return mGestureDetector.onTouchEvent(event);
////            }
////        });
//    }

    public SparseArray<View> getViewHashMap() {
        return viewHashMap;
    }

    public int getOnShowPosition() {
        return onShowPosition;
    }

    public void setOnShowPosition(int onShowPosition) {
        this.onShowPosition = onShowPosition;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        if (mContentController != null && getChildAt(0) != null && mContentController.getShowWidth() != getChildAt(0).getMeasuredWidth()) {
            mContentController.setShowHeight(getChildAt(0).getMeasuredHeight());
            mContentController.setShowWidth(getChildAt(0).getMeasuredWidth());
            mContentController.initUtils();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("onLayout", String.valueOf(getChildCount()));
        if (changed || myChanged) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                //此处应该添加一个变量来记录上次滑动后的初始位置和结束位置
                child.layout(i * getMeasuredWidth() + leftBorder, 0, (i + 1) * getMeasuredWidth() + leftBorder, getMeasuredHeight());

            }
            if (getChildCount() != 0) {
                if (childWidth == 0 && childWidth != getChildAt(0).getMeasuredWidth())
                    childWidth = getChildAt(0).getMeasuredWidth();
                leftBorder = getChildAt(0).getRight() - getMeasuredWidth();
                rightBorder = getChildAt(getChildCount() - 1).getLeft() + getMeasuredWidth();
            }
            myChanged = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return mGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                hasScrolledX = getScrollX();
                return false;
            case MotionEvent.ACTION_MOVE:
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
//            if(event.getAction()==MotionEvent.ACTION_MOVE) {
//                mXMove = event.getRawX();
//                mXLastMove = mXMove;
//            }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
//                return mGestureDetector.onTouchEvent(event);
                mXMove = event.getRawX();
                int scrolledX = (int) (mXDown - mXLastMove);
                if (hasScrolledX + scrolledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;
                } else if (hasScrolledX + scrolledX + getMeasuredWidth() > rightBorder) {
                    scrollTo(rightBorder - getMeasuredWidth(), 0);
                    return true;
                }
                scrollTo(hasScrolledX + scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
//                int hasScroll = (getScrollX() >= 0 ? getScrollX() : -getScrollX()) % getMeasuredWidth();
//                int dx;
//                if (getScrollX() <= 0) {
//                    if (hasScroll >= getMeasuredWidth() *0.5)
//                        dx = hasScroll - getMeasuredWidth();
//                    else
//                        dx = hasScroll;
//                } else {
//                    if (hasScroll >= getMeasuredWidth() *0.5)
//                        dx = getMeasuredWidth() - hasScroll;
//                    else
//                        dx = -hasScroll;
//                }
                int hasScroll = (getScrollX() >= 0 ? getScrollX() : -getScrollX()) % getMeasuredWidth();
                int dx = 0;
                float fingerMove=event.getRawX()-mXDown;
                if(getScrollX()>0){
                    if(fingerMove<0){
                        if (getScrollX() != rightBorder - getMeasuredWidth())
                        dx=getMeasuredWidth()-hasScroll;
                    }else {
                        if (getScrollX() != leftBorder)
                            dx = -hasScroll;
                    }
                }else {
                    if(fingerMove<0){
                        if (getScrollX() != rightBorder - getMeasuredWidth()) {
                            dx = hasScroll;
                        }
                    }else {
                        if (getScrollX() != leftBorder) {
                            dx = hasScroll - getMeasuredWidth();
                        }
                    }
                }
                if (dx != 0) {
                    Log.e("dx", String.valueOf(dx));
                    mScroller.startScroll(getScrollX(), 0, dx, 0);
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void findPositionByScroll(int scrollX) {
        //通过滑动的位置判断当前页面
        //传入的为滑动距离
        //结果是当前页面
        onShowPosition = scrollX / getMeasuredWidth();
    }

    @Override
    public void computeScroll() {
        if (getChildCount() != 0) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                invalidate();
            } else if (mScroller.getCurrX() == getScrollX()) {
                findPositionByScroll(getScrollX());
                //刷新layout
                mPageChangeListener.onPageSelected(onShowPosition);
                if (getChildCount() == 1)
                    addRightView();
                if (!isFirstLayout)
                    flushLayout();
            }
        }
    }

    private void flushLayout() {
        if (getScrollX() == leftBorder) {
            addLeftView();
        } else if (getScrollX() + getMeasuredWidth() == rightBorder) {
            addRightView();
        }
    }

    private void addLeftView() {
        if (mContentController.getPageStart().indexOfKey(onShowPosition) >= 0 && mContentController.getPageStart().get(onShowPosition) > 0) {
            if (getChildCount() >= 3)
                pagerAdapter.destroyItem(this, onShowPosition + 2, getChildAt(getChildCount() - 1));
            pagerAdapter.instantiateLeftItem(this, onShowPosition - 1);
//            leftBorder-=childWidth;
            leftBorder -= getMeasuredWidth();
            if (rightBorder - leftBorder >= 4 * getMeasuredWidth())
//                rightBorder-=childWidth;
                rightBorder -= getMeasuredWidth();
            myChanged = true;
            Log.e("leftadd", "addLeft");
        }
    }
    private void addRightView() {
        if (mContentController.getPageEnd().indexOfKey(onShowPosition) >= 0 && mContentController.getPageEnd().get(onShowPosition) != -1 && mContentController.getPageEnd().get(onShowPosition) < mContentController.getTotalWords() - 1) {
            if (getChildCount() >= 3)
                pagerAdapter.destroyItem(this, onShowPosition - 2, getChildAt(0));
            pagerAdapter.instantiateRightItem(this, onShowPosition + 1);
//            rightBorder += childWidth;
            rightBorder += getMeasuredWidth();
            if (rightBorder - leftBorder >= 4 * getMeasuredWidth())
//            leftBorder += childWidth;
                leftBorder += getMeasuredWidth();
            myChanged = true;
            Log.e("rightadd", "addright");
        }
    }
    public void scrollToLeft(){
        if (getScrollX()!=leftBorder) {
            mScroller.startScroll(getScrollX(), 0, -getMeasuredWidth(), 0);
            invalidate();
        }
    }
    public void scrollToRight(){
        if (getScrollX()!=rightBorder-getMeasuredWidth()) {
            mScroller.startScroll(getScrollX(), 0, getMeasuredWidth(), 0);
            invalidate();
        }
    }

    public ReaderPagerAdapter getAdapter() {
        return pagerAdapter;
    }

    public void setAdapter(final ReaderPagerAdapter pagerAdapter) {
        this.pagerAdapter = pagerAdapter;
        mContentController = pagerAdapter.getContentController();
        pagerAdapter.instantiateRightItem(this, 0);
        getChildAt(0).post(new Runnable() {
            @Override
            public void run() {
//                if(mContentController.getPageEnd().get(onShowPosition)<mContentController.getTotalWords()&&PageGroup.this.getChildAt(1)==null){
//                    Log.e("runnable","add right");
//                    Log.e("onShowEnd",String.valueOf(mContentController.getPageEnd().get(onShowPosition)));
//                    Log.e("totalWords",String.valueOf(mContentController.getTotalWords()));
//                    pagerAdapter.instantiateRightItem(PageGroup.this,1);
//                    rightBorder+=childWidth;
//                }
                if (mContentController.getPageStart().get(onShowPosition) > 0) {
                    Log.e("runnable", "add left");
                    pagerAdapter.instantiateLeftItem(ReaderPageGroup.this, -1);
//                    leftBorder-=childWidth;
                    leftBorder -= getMeasuredWidth();
                }
                isFirstLayout = false;
                myChanged = true;
            }
        });
    }

    public void addOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mPageChangeListener = onPageChangeListener;
    }

    public void destroyRight() {
        Log.e("destoreyRight", "destoreyRight");
        if (getChildAt(getChildCount() - 1).getLeft() == getScrollX() + getMeasuredWidth() && getChildCount() > 1) {
            pagerAdapter.destroyItem(this, onShowPosition + 1, getChildAt(getChildCount() - 1));
//            rightBorder-=childWidth;
            rightBorder -= getMeasuredWidth();
            invalidate();
        }
    }

    public void destroyLeft() {
        Log.e("destoreyLeft", "destoreyLeft");
        if (getChildAt(0).getRight() == getScrollX() && getChildCount() != 1) {
            pagerAdapter.destroyItem(this, onShowPosition - 1, getChildAt(0));
//            leftBorder+=childWidth;
            leftBorder += getMeasuredWidth();
            invalidate();
        }
    }

    public View getCurrentView() {
        Log.e("getOnshow", String.valueOf(onShowPosition));
        return viewHashMap.get(onShowPosition);
    }

    interface OnPageChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }
//    private class PageGroupOnGestureListener extends GestureDetector.SimpleOnGestureListener{
//        PageGroupOnGestureListener() {
//            super();
//        }
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Log.e("onScroll",String.valueOf(distanceX));
//            if((distanceX<0&&getScrollX()!=leftBorder)||(distanceX>0&&getScrollX()!=rightBorder-getMeasuredWidth())){
//                ReaderPageGroup.this.scrollBy((int) distanceX,0);
//                ReaderPageGroup.this.invalidate();
//            }
//
////            int hasScroll = (getScrollX() >= 0 ? getScrollX() : -getScrollX()) % getMeasuredWidth();
////            int dx;
////            if (getScrollX() <= 0) {
////                if (hasScroll >= getMeasuredWidth() *0.5)
////                    dx = hasScroll - getMeasuredWidth();
////                else
////                    dx = hasScroll;
////            } else {
////                if (hasScroll >= getMeasuredWidth() *0.5)
////                    dx = getMeasuredWidth() - hasScroll;
////                else
////                    dx = -hasScroll;
////            }
////            mScroller.startScroll(getScrollX(), 0, dx, 0);
////            invalidate();
////            return super.onScroll(e1, e2, distanceX, distanceY);
//            return true;
//        }
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.e("onFling",String.valueOf(velocityX));
////            mXMove = e2.getRawX();
////            int scrolledX = (int) ((mXDown - mXLastMove));
////            if (hasScrolledX + scrolledX < leftBorder) {
////                scrollTo(leftBorder, 0);
////                return true;
////            } else if (hasScrolledX + scrolledX + getMeasuredWidth() > rightBorder) {
////                scrollTo(rightBorder - getMeasuredWidth(), 0);
////                return true;
////            }
////            scrollTo(hasScrolledX + scrolledX, 0);
////            mXLastMove = mXMove;
////            return super.onFling(e1, e2, velocityX, velocityY);
//            return true;
//        }
//
//        @Override
//        public boolean onDown(MotionEvent e) {
//            Log.e("onDown",String.valueOf(e.getRawX()));
//            mXDown = e.getRawX();
//            mXLastMove = mXDown;
//            hasScrolledX = getScrollX();
//            return false;
//        }
//
//    }
}
