package com.guan.speakerreader.util;

import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.guan.speakerreader.adapter.ReaderPagerAdapter;
import com.guan.speakerreader.view.PageGroup;

/**
 * Created by guans on 2017/4/3.
 */

public class ContentController {
    private PageGroup mPageGroup;
    private int onShowPage;
    private String filePath;
    private SparseArray<String> pageContent;
    private SparseIntArray pageStart;
    private SparseIntArray pageEnd;
    private int onShowStart;
    private int onShowEnd;
    private MeasurePreUtil measurePreUtil;
    private PagesArrangeUtil pagesArrangeUtil;
    private int marked = 0;
    private int totalWords;
    private Paint mPaint;
    private float showHeight;
    private float showWidth;
    private ReaderPagerAdapter mAdapter2;
    //每次预加载的最大字数暂时设定为3000，后续可以根据情况调整
    private int takenWords = 3000;

    public ContentController(String filePath, int totalWords, ReaderPagerAdapter readerPagerAdapter, Paint mPaint) {
        this.filePath = filePath;
        this.totalWords = totalWords;
        Log.e("totalWordscons", String.valueOf(totalWords));
        this.mAdapter2 = readerPagerAdapter;
        this.mPaint = mPaint;
        pageContent = new SparseArray<>();
        pageStart = new SparseIntArray();
        pageEnd = new SparseIntArray();
        measurePreUtil = new MeasurePreUtil(mPaint, showHeight, showWidth);
        pagesArrangeUtil = new PagesArrangeUtil(filePath, mPaint, showWidth, showHeight);
    }

    public SparseIntArray getPageStart() {
        return pageStart;
    }

    public SparseIntArray getPageEnd() {
        return pageEnd;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getOnShowStart() {
        return onShowStart;
    }

    public void setOnShowStart(int onShowStart) {
        this.onShowStart = onShowStart;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public float getShowHeight() {
        return showHeight;
    }

    public void setShowHeight(float showHeight) {
        this.showHeight = showHeight;
    }

    public float getShowWidth() {
        return showWidth;
    }

    public void setShowWidth(float showWidth) {
        this.showWidth = showWidth;
    }


    public void initUtils() {
        if (measurePreUtil == null) {
            measurePreUtil = new MeasurePreUtil(mPaint, showHeight, showWidth);
        } else {
            measurePreUtil.setShowHeight(showHeight);
            measurePreUtil.setShowWidth(showWidth);
        }
        if (pagesArrangeUtil == null) {
            pagesArrangeUtil = new PagesArrangeUtil(filePath, mPaint, showWidth, showHeight);
        } else {
            pagesArrangeUtil.setShowHeight(showHeight);
            pagesArrangeUtil.setShowWidth(showWidth);
        }
        //当尺寸没有具体变化时不要清理
        reMeasure();
    }

    public int getMarked() {
        return marked;
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }

    private void fillContentList(int position) {
        try {
            //marked 的位置，当position为0时，marked=0，当position为其他数时默认为进度条拖动的位置
            String content = TxtTaker.readerFromText(filePath, marked, takenWords);
            onShowStart = marked;
            content = measureContent(content);
//            Log.e("contentList",content);
            pageContent.put(position, content);
//            if(content.length()!=0)
//                return;
            onShowEnd = marked + content.length() - 1;
            //逻辑可能出错了
            pageStart.put(position, onShowStart);
            pageEnd.put(position, onShowEnd);
//                if (onShowEnd >= totalWords) {
//                    //标记右边还有
//                    Log.e("getContent position: ", String.valueOf(position + 1));
//                }
            if (onShowEnd != -1 && onShowEnd < totalWords - 1) {
                getContentNextShow(position);
            }
            if (onShowStart > 0)
                getContentPreShow(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //交给view调用
    public String getContent(int position) {
//        onShowPage=position;
        if (pageContent.indexOfKey(position) < 0) {
            fillContentList(position);
        }
        return pageContent.get(position);
    }

    private void getContentNextShow(int position) {
        if (pageContent.indexOfKey(position + 1) < 0) {
            if (pageStart.indexOfKey(position + 1) >= 0 && pageEnd.indexOfKey(position + 1) >= 0) {
                try {
                    pageContent.put(position + 1, TxtTaker.readerFromText(filePath, pageStart.get(position + 1), pageEnd.get(position + 1) - pageStart.get(position + 1) + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //对content start和end进行赋值修
                try {
                    String content = TxtTaker.readerFromText(filePath, onShowEnd + 1, takenWords);
                    Log.e("contentBeforeMeasure", String.valueOf(content.length()));
                    content = measureContent(content);
                    Log.e("contentAfterMeasure", String.valueOf(content.length()));
                    //对content start和end进行赋值修改
                    pageContent.put(position + 1, content);
//                    Log.e("contentListNext",content);
                    pageStart.put(position + 1, onShowEnd + 1);
                    pageEnd.put(position + 1, onShowEnd + content.length());
                    //当取完后一页还有字，页码加1加重复了
                    //这边添加逻辑或者判断，当页面到最后一页时还有内容就继续添加页数
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pageContent.delete(position - 2);
        }
    }

    private void getContentPreShow(int position) {
        if (pageContent.indexOfKey(position - 1) < 0) {
            //这一段也有可以优化当已经测量过直接取用测量的
            if (pageStart.indexOfKey(position - 1) >= 0 && pageEnd.indexOfKey(position - 1) >= 0) {
                try {
                    pageContent.put(position - 1, TxtTaker.readerFromText(filePath, pageStart.get(position - 1), pageEnd.get(position - 1) - pageStart.get(position - 1) + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String content;
                    if (pageStart.indexOfKey(position) >= 0) {
                        content = TxtTaker.readerFromTextPre(filePath, pageStart.get(position) - takenWords, takenWords);
                    } else {
                        content = TxtTaker.readerFromTextPre(filePath, onShowStart - takenWords, takenWords);
                    }
                    content = measurePreContent(content);
                    pageContent.put(position - 1, content);
                    pageStart.put(position - 1, onShowStart - content.length());
                    pageEnd.put(position - 1, onShowStart - 1);
                    Log.e("endpre" + String.valueOf(position - 1) + "page", String.valueOf(onShowStart - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        pageContent.delete(position + 2);
    }

    private String measureContent(String content) {
        //通过功能类measure util进行测量计算
//        Log.e("pagesArrangeUtil",String.valueOf(pagesArrangeUtil==null));
        return pagesArrangeUtil.measurePage(content);
    }

    private String measurePreContent(String content) {
        //通过功能类measure util进行测量
        return measurePreUtil.prePageContentLength(content);
    }

    public void notifyPageChanged(int position) {
        onShowPage = position;
        if (pageStart.indexOfKey(position) >= 0 && pageEnd.indexOfKey(position) >= 0) {
            onShowStart = pageStart.get(position);
            onShowEnd = pageEnd.get(position);
            if (onShowEnd < totalWords - 1 && onShowEnd != -1)
                getContentNextShow(position);
            if (onShowStart > 0)
                getContentPreShow(position);
        } else {
            fillContentList(position);
        }
        mAdapter2.updateSeekBar(onShowStart);
        marked = onShowStart;
        //把更新后的位置通知给seekbar可以通过handler实现或者广播，或者一个接口
    }

    //下面方法设置为从当中某一页打开
    public void setContentFromPage(int pageNumberOnShow, int marked) {
        setMarked(marked);
        if (getShowWidth() == 0)
            return;
        onShowPage = pageNumberOnShow;
        reMeasure();
//        notifyPageChanged(pageNumberOnShow);
    }

    public void reMeasure() {
        pageContent.clear();
        pageStart.clear();
        pageEnd.clear();
        Log.e("remeasure", "remeasure");
        fillContentList(onShowPage);
        Log.e("onShowEndRemeasur", String.valueOf(onShowEnd));
        Log.e("totalwords", String.valueOf(totalWords));
        if (onShowEnd >= totalWords - 1 && mPageGroup != null) {
            //删除最后一个view
            Log.e("detory", "detoryRight");
            mPageGroup.destroyRight();
        }
        if (onShowStart == 0 && mPageGroup != null) {
            Log.e("detory", "detoryLeft");
            mPageGroup.destroyLeft();
        }
    }

    public void setPageGroup(PageGroup mPageGroup) {
        this.mPageGroup = mPageGroup;
    }
    /*
    动态调整页面：
    1、当从0开始往后翻，只添加最后一页
    2、当从当中位置往前翻的时候，set当前页面为倒数第一页（向前加交给control解决，总页数大于当前字数/最小页字数，当页数向前翻到底的时候即当前页开始字为0的时候
    设置当前页为第0页（selected0？）
     */
}
