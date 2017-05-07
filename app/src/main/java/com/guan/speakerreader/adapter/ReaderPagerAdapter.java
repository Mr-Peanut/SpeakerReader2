package com.guan.speakerreader.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.guan.speakerreader.R;
import com.guan.speakerreader.util.ContentController;
import com.guan.speakerreader.view.PageGroup;
import com.guan.speakerreader.view.TextReaderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/4/4.
 */

public class ReaderPagerAdapter implements View.OnClickListener {
    private ArrayList<WeakReference<View>> viewList;
    private ContentController contentController;
    private Context mContext;
    private String filePath;
    private Paint mPaint;
    private List<View> instantiatedViews;
    private UpdateSeekBarController mUpdateSeekBarController;
    private InnerViewOnClickedListener mInnerViewOnClickedListener;

    public ReaderPagerAdapter(Context mContext, String filePath, int totalWords, Paint paint) {
        this.mContext = mContext;
        this.filePath = filePath;
        this.mPaint = paint;
        viewList = new ArrayList<>();
        instantiatedViews = new ArrayList<>();
        contentController = new ContentController(filePath, totalWords, this, mPaint);
    }

    public void setUpdateSeekBarController(UpdateSeekBarController mUpdateSeekBarController) {
        this.mUpdateSeekBarController = mUpdateSeekBarController;
    }

    public void setInnerViewOnClickedListener(InnerViewOnClickedListener mInnerViewOnClickedListener) {
        this.mInnerViewOnClickedListener = mInnerViewOnClickedListener;
    }

    public ContentController getContentController() {
        return contentController;
    }

    public void setContentController(ContentController contentController) {
        this.contentController = contentController;
    }

    public Object instantiateLeftItem(PageGroup container, int position) {
        final TextReaderView textReaderView;
        View view = null;
        if (viewList.size() > 0) {
            view = viewList.remove(0).get();
        }
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.page_view_layout, null);
        }
        textReaderView = (TextReaderView) view.findViewById(R.id.content_view);
        textReaderView.setPosition(position);
        textReaderView.setContentController(contentController);
        textReaderView.setPaint(mPaint);
        instantiatedViews.add(view);
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        container.addView(view, 0);
        container.getViewHashMap().put(position, view);
        view.setOnClickListener(this);
        textReaderView.invalidate();
        return view;
    }

    public Object instantiateRightItem(PageGroup container, int position) {
        TextReaderView textReaderView;
        View view = null;
        if (viewList.size() > 0) {
            view = viewList.remove(0).get();
        }
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.page_view_layout, null);
        }
        textReaderView = (TextReaderView) view.findViewById(R.id.content_view);
        textReaderView.setPosition(position);
        textReaderView.setContentController(contentController);
        textReaderView.setPaint(mPaint);
        instantiatedViews.add(view);
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        container.addView(view, -1);
        container.getViewHashMap().put(position, view);
        Log.e("add Right", "add Right");
        view.setOnClickListener(this);
        textReaderView.invalidate();
        return view;
    }

    public void destroyItem(PageGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        view.setOnClickListener(null);
        instantiatedViews.remove(view);
        container.getViewHashMap().remove(position);
        viewList.add(new WeakReference<>(view));
    }

    public void invalidateViews() {
        if (instantiatedViews.size() != 0) {
            Log.e("invalidateViews", String.valueOf(instantiatedViews.size()));
            for (View childView : instantiatedViews) {
                childView.invalidate();
            }
        }
    }

    @Override
    public void onClick(View v) {
        mInnerViewOnClickedListener.onClicked();
    }

    public void updateSeekBar(int progress) {
        mUpdateSeekBarController.upDate(progress);
    }

    public interface InnerViewOnClickedListener {
        void onClicked();
    }

    public interface UpdateSeekBarController {
        void upDate(int progress);
    }
}
