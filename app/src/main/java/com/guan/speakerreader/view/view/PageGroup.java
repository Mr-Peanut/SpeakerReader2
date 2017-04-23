package com.guan.speakerreader.view.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Scroller;

import com.guan.speakerreader.view.adapter.ReaderPagerAdapter2;
import com.guan.speakerreader.view.util.ContentController;

import java.util.ArrayList;
import java.util.List;

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

public class PageGroup extends ViewGroup {
    private int mTouchSlop;
    private float mXDown;
    private float mXMove;
    private float mXLastMove;
    private Scroller mScroller;
    private int leftBorder;
    private int rightBorder;
    private int childWidth;
    private boolean myChanged=false;



    private int onShowPosition=0;
    private ReaderPagerAdapter2 pagerAdapter;
    private boolean isFirstPage=true;
    private boolean isLastPage=true;
    private ViewPager.OnPageChangeListener mPageChangeListener;

    public PageGroup(Context context) {
        super(context);
    }

    public PageGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration=ViewConfiguration.get(context);
        mTouchSlop= ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mScroller=new Scroller(context);
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
        measureChildren(widthMeasureSpec,heightMeasureSpec);
//        View child;
//        for (int i = 0; i < getChildCount(); i++) {
//            child = getChildAt(i);
//            measureChild(child, widthMeasureSpec, heightMeasureSpec);
//        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.d("onLayout","not changed");
        if(changed||myChanged){
//            Log.d("onLayout","changed");
            int childCount=getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                //此处应该添加一个变量来记录上次滑动后的初始位置和结束位置
                child.layout(i*child.getMeasuredWidth()+leftBorder,0,(i+1)*child.getMeasuredWidth()+leftBorder,child.getMeasuredHeight());
//                Log.e("childLocation","第"+String.valueOf(i)+"页"+String.valueOf(i*child.getMeasuredWidth()+leftBorder)+"和"+String.valueOf((i+1)*child.getMeasuredWidth()+leftBorder));
            }
            if(getChildCount()!=0){
                if(childWidth==0)
                    childWidth=getChildAt(0).getMeasuredWidth();
                leftBorder=getChildAt(0).getLeft();
//            Log.e("leftBorder", String.valueOf(leftBorder));
                rightBorder=getChildAt(getChildCount()-1).getRight();
//            Log.e("rightBorder", String.valueOf(rightBorder));
            }
            myChanged=false;
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mXDown=ev.getRawX();
                mXLastMove=mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove=ev.getRawX();
                float diff=Math.abs(mXMove-mXDown);
                mXLastMove=mXMove;
                if(diff>mTouchSlop){
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
                mXMove=event.getRawX();
                int scrolledX= (int)((mXDown-mXLastMove)/10) ;
                if(getScrollX()+scrolledX< leftBorder){
                    scrollTo(leftBorder,0);
                    //添加向前页的逻辑
                    addLeftView();
                    return true;
                }else if(getScrollX()+childWidth+scrolledX>rightBorder){
                    scrollTo(rightBorder-childWidth,0);
                    //添加向后页的逻辑
                    addRightView();
                    return true;
                }
                scrollBy(scrolledX,0);
                mXLastMove=mXMove;
                break;
            case MotionEvent.ACTION_UP:
//                int targetIndex=(getScrollX()+getWidth()/2)/getWidth();
                //dx 为要移动的距离最终距离是getscroll+dx
//                int dx=targetIndex*getWidth()-getScrollX();
//                int dx=targetIndex*getWidth()-getScrollX();
                //
                int hasScroll=(getScrollX()>=0?getScrollX():-getScrollX())%getWidth();
                int dx;
                if(getScrollX()<=0){
                    if(hasScroll>=getWidth()/2)
                        dx=hasScroll-getWidth();
                    else
                        dx=hasScroll;
                }else {
                    if(hasScroll>=getWidth()/2)
                        dx=getWidth()-hasScroll;
                    else
                        dx=-hasScroll;
                }
                mScroller.startScroll(getScrollX(),0,dx,0);
                findPositionByScroll(getScrollX());
                mPageChangeListener.onPageSelected(onShowPosition);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void findPositionByScroll(int scrollX) {
        //通过滑动的位置判断当前页面
        //传入的为滑动距离
        //结果是当前页面
       onShowPosition=scrollX/getWidth();
    }

    @Override
    public void computeScroll() {
       if(mScroller.computeScrollOffset()){
           scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
           invalidate();
       }
    }
    private void addLeftView(){
        if(!isFirstPage){
//            viewRecycler.add(getChildAt(getChildCount()-1));
//            removeViewAt(getChildCount()-1);
//            View child=viewRecycler.remove(0);
            //chlid 内容修改后续添加adapter控制页面
            //修改child页面内容
            //获取child的新position
//            int newPositon=onshowposition-1;
//            addView(child,0);
            if(getChildCount()>=3)
            pagerAdapter.destroyItem(this,onShowPosition+2,getChildAt(getChildCount()-1));
            pagerAdapter.instantiateLeftItem(this,onShowPosition-1);
            leftBorder-=childWidth;
            rightBorder-=childWidth;
            myChanged=true;
            invalidate();
        }

    }
    private void addRightView(){
        if(!isLastPage){
//            viewRecycler.add(getChildAt(0));
//            removeViewAt(0);
//            View child=viewRecycler.remove(0);
//            //chlid 内容修改
//            addView(child,-1);
            if(getChildCount()>=3)
            pagerAdapter.destroyItem(this,onShowPosition-2,getChildAt(0));
            pagerAdapter.instantiateRightItem(this,onShowPosition+1);
            leftBorder+=childWidth;
            rightBorder+=childWidth;
            myChanged=true;
            invalidate();
        }
    }
    public void skipToChild(){
        //默认跳转到第二个child，除非要求跳转到第一个或者最后一个child
        //设置和刷新三个view的内容
        //更新了别忘了position是否要更新
        for(int i=0;i<getChildCount();i++){
            getChildAt(i).invalidate();
        }
    }

    public ReaderPagerAdapter2 getAdapter() {
        return pagerAdapter;
    }

    public void setAdapter(ReaderPagerAdapter2 pagerAdapter) {
        this.pagerAdapter = pagerAdapter;
        pagerAdapter.instantiateRightItem(this,0);
        invalidate();
    }
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mPageChangeListener=onPageChangeListener;
    }
}
