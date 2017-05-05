package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.graphics.Paint;
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

public class ReaderPagerAdapter implements View.OnClickListener{
    private ArrayList<WeakReference<View>> viewList;
    private ContentController contentController;
    private Context mContext;
    private String filePath;
    private Paint mPaint;
    private List<View> instantiatedViews;

    public void setUpdateSeekBarController(UpdateSeekBarController mUpdateSeekBarController) {
        this.mUpdateSeekBarController = mUpdateSeekBarController;
    }

    private UpdateSeekBarController mUpdateSeekBarController;

    public void setInnerViewOnClickedListener(InnerViewOnClickedListener mInnerViewOnClickedListener) {
        this.mInnerViewOnClickedListener = mInnerViewOnClickedListener;
    }

    private InnerViewOnClickedListener mInnerViewOnClickedListener;
    public ContentController getContentController() {
        return contentController;
    }

    public void setContentController(ContentController contentController) {
        this.contentController = contentController;
    }
    public ReaderPagerAdapter(Context mContext, String filePath, int totalWords, Paint paint) {
        this.mContext = mContext;
        this.filePath=filePath;
        this.mPaint = paint;
        viewList=new ArrayList<>();
        instantiatedViews=new ArrayList<>();
        contentController=new ContentController(filePath,totalWords,this,mPaint);
    }
    public Object instantiateLeftItem(ViewGroup container, int position) {
        final TextReaderView textReaderView;
        View view = null;
        if(viewList.size()>0){
            view=viewList.remove(0).get();
        }
        if(view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.page_layout,null);
        }
        textReaderView= (TextReaderView) view.findViewById(R.id.contentView);
        textReaderView.setPosition(position);
        textReaderView.setContentController(contentController);
        textReaderView.setPaint(mPaint);
        instantiatedViews.add(view);
        container.addView(view,0);
        view.setOnClickListener(this);
        textReaderView.invalidate();
        return view;
    }
    public Object instantiateRightItem(ViewGroup container, int position) {
        TextReaderView textReaderView;
        View view = null;
        if(viewList.size()>0){
            view=viewList.remove(0).get();
        }
        if(view==null){
            view= LayoutInflater.from(mContext).inflate(R.layout.page_layout,null);
        }
        textReaderView= (TextReaderView) view.findViewById(R.id.contentView);
        textReaderView.setPosition(position);
        textReaderView.setContentController(contentController);
        textReaderView.setPaint(mPaint);
        instantiatedViews.add(view);
        container.addView(view,-1);
        Log.e("add Right","add Right");
        view.setOnClickListener(this);
        textReaderView.invalidate();
        return view;
    }
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view= (View) object;
        container.removeView(view);
        view.setOnClickListener(null);
        instantiatedViews.remove(view);
        viewList.add(new WeakReference<>(view));
    }
    public void invalidateViews(){
        if(instantiatedViews.size()!=0){
            for(View childView:instantiatedViews){
                childView.invalidate();
            }
        }
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
