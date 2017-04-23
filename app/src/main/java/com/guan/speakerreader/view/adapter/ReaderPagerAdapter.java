package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.util.ContentController;
import com.guan.speakerreader.view.view.TextReaderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/4/4.
 */

public class ReaderPagerAdapter extends PagerAdapter implements View.OnClickListener{
    private ArrayList<WeakReference<View>> viewList;
    private ContentController contentController;
    private Context mContext;
    private String filePath;
    private Paint mPaint;
    private List<View> instantiatedViews;

    public void setmUpdateSeekBarController(UpdateSeekBarController mUpdateSeekBarController) {
        this.mUpdateSeekBarController = mUpdateSeekBarController;
    }

    private UpdateSeekBarController mUpdateSeekBarController;

    public void setmInnerViewOnClickedListener(InnerViewOnClickedListener mInnerViewOnClickedListener) {
        this.mInnerViewOnClickedListener = mInnerViewOnClickedListener;
    }

    private InnerViewOnClickedListener mInnerViewOnClickedListener;
    public ContentController getContentController() {
        return contentController;
    }

    public void setContentController(ContentController contentController) {
        this.contentController = contentController;
    }
    public ReaderPagerAdapter(Context mContext,String filePath,int totalWords,Paint paint) {
        this.mContext = mContext;
        this.filePath=filePath;
        this.mPaint = paint;
        viewList=new ArrayList<>();
        contentController=new ContentController(filePath,totalWords,this,mPaint);
        instantiatedViews=new ArrayList<>();
    }
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TextReaderView textReaderView;
        View view = null;
        if(viewList.size()>0){
            view=viewList.remove(0).get();
        }
        if(view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.page_layout,null);
        }
        textReaderView= (TextReaderView) view.findViewById(R.id.contentView);
        ((TextView)(view.findViewById(R.id.foot))).setText("第"+position+"页");
        textReaderView.setPosition(position);
        textReaderView.setmContentController(contentController);
        textReaderView.setmPaint(mPaint);
        instantiatedViews.add(view);
        container.addView(view);
        view.setOnClickListener(this);
//        textReaderView.invalidate();
        return view;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view= (View) object;
        container.removeView(view);
        view.setOnClickListener(null);
        instantiatedViews.remove(view);
        viewList.add(new WeakReference<>(view));
    }
    public void invalidateViews(){
        for(View childView:instantiatedViews){
            childView.invalidate();
        }
    }

    @Override
    public int getCount() {
        Log.e("getCount: ",String.valueOf(getTotalPage()));
        return getTotalPage();
    }
    private int getTotalPage() {
        return contentController.getPageCount();
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view ==object;
    }
    @Override
    public void onClick(View v) {
        mInnerViewOnClickedListener.onClicked();
    }
    public void updateSeekBar(int progress){
        mUpdateSeekBarController.upDate(progress);
    }
    public interface InnerViewOnClickedListener{
        void onClicked();
    }
    public interface UpdateSeekBarController {
      void upDate(int progress) ;
    }
}
