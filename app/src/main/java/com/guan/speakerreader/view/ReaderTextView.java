package com.guan.speakerreader.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;


/**
 * Created by guans on 2017/5/26.
 */

public class ReaderTextView extends android.support.v7.widget.AppCompatTextView {
    private TextPaint mPaint;
    private Context mContext;

    public ReaderTextView(Context context) {
        super(context);
        mContext = context;
        mPaint = getPaint();
    }

    public ReaderTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPaint = getPaint();
    }

    public void setContent(String content) {
        float showWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        float showHight = getHeight() - getPaddingTop() - getPaddingBottom();
    }
}
