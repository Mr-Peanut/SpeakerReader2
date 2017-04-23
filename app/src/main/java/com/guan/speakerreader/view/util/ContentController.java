package com.guan.speakerreader.view.util;

import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.guan.speakerreader.view.adapter.ReaderPagerAdapter2;

/**
 * Created by guans on 2017/4/3.
 */

public class ContentController {
    private int pageNumberOnShow;
    private String filePath;
    private SparseArray<String> pageContent;
    private SparseIntArray pageStart;
    private SparseIntArray pageEnd;
    private int onShowStart;
    private int onShowEnd;
    private MeasurePreUtil measurePreUtil;
    private PagesArrangeUtil pagesArrangeUtil;
    private int marked = 0;
    private int pageCount = 1;
    private int totalWords;
    private Paint mPaint;
    private float showHeight;
    private float showWidth;
    private ReaderPagerAdapter2 mAdapter2;
    //每次预加载的最大字数暂时设定为3000，后续可以根据情况调整
    private int takenWords=3000;

    public ContentController(String filePath, int totalWords, ReaderPagerAdapter2 readerPagerAdapter2, Paint mPaint) {
        this.filePath = filePath;
        this.totalWords = totalWords;
        this.mAdapter2 = readerPagerAdapter2;
        this.mPaint=mPaint;
        pageContent = new SparseArray<>();
        pageStart = new SparseIntArray();
        pageEnd = new SparseIntArray();
    }




    public int getPageCount() {
        return pageCount;
    }
    public void setOnShowStart(int onShowStart) {
        this.onShowStart = onShowStart;
    }

    public int getOnShowStart() {
        return onShowStart;
    }

    /*
    还要考虑到往前到第0页有字和到最后一页还有字要动态修改页面
     */

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
        pageContent.clear();
        pageStart.clear();
        pageEnd.clear();
    }

    //交给view调用
    public String getContent(int position) {
        if (pageContent.indexOfKey(position) >= 0) {
            return pageContent.get(position);
        } else {
            try {
                //marked 的位置，当position为0时，marked=0，当position为其他数时默认为进度条拖动的位置
                String content = TxtReader.readerFromText(filePath, marked, takenWords);
                onShowStart = marked;
                content = measureContent(content);
                pageContent.put(position, content);
                onShowEnd = marked + content.length() - 1;
                //逻辑可能出错了
                pageStart.put(position, onShowStart);
                pageEnd.put(position, onShowEnd);
                if (onShowEnd >= totalWords) {
                    //标记右边还有
                    Log.e("getContent position: ", String.valueOf(position + 1));
                }
                if (onShowEnd < totalWords) {
                    //重复加出错当重画时出错
                    pageCount++;
//                    mAdapter.notifyDataSetChanged();
                    getContentNextShow(position);
                }
                if (onShowStart > 0)
                    getContentPreShow(position);
                return content;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void getContentNextShow(int position) {
        if (pageContent.indexOfKey(position + 1) < 0) {
            if (pageStart.indexOfKey(position + 1) >= 0 && pageEnd.indexOfKey(position + 1) >= 0) {
                try {
                    pageContent.put(position + 1, TxtReader.readerFromText(filePath, pageStart.get(position + 1), pageEnd.get(position + 1) - pageStart.get(position + 1) + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //对content start和end进行赋值修改
                try {
                    String content = TxtReader.readerFromText(filePath, onShowEnd + 1, takenWords);
                    if (content == null) {
                        //到头了右边没了
                        return;
                    }
                    content = measureContent(content);
                    //对content start和end进行赋值修改
                    pageContent.put(position + 1, content);
                    pageStart.put(position + 1, onShowEnd + 1);
                    pageEnd.put(position + 1, onShowEnd + content.length());
                    //当取完后一页还有字，页码加1加重复了
                    if (onShowEnd + content.length() < totalWords) {
                        pageCount++;
//                        mAdapter.notifyDataSetChanged();
                    }
                    //这边添加逻辑或者判断，当页面到最后一页时还有内容就继续添加页数
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (position >= 2)
                pageContent.delete(position - 2);
        }
    }

    private void getContentPreShow(int position) {
        if(position==0||onShowStart==0){
            //通知不让滑动
            //可以通知view
            return;
        }
        if (pageContent.indexOfKey(position - 1) < 0 && position >= 1) {
            //这一段也有可以优化当已经测量过直接取用测量的
            if (pageStart.indexOfKey(position - 1) >= 0 && pageEnd.indexOfKey(position - 1) >= 0) {
                try {
                    pageContent.put(position - 1, TxtReader.readerFromText(filePath, pageStart.get(position - 1), pageEnd.get(position - 1) - pageStart.get(position - 1) + 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String content;
                    if (pageStart.indexOfKey(position) >= 0) {
                        content = TxtReader.readerFromTextPre(filePath, pageStart.get(position) - takenWords, takenWords);
                    } else {
                        content = TxtReader.readerFromTextPre(filePath, onShowStart - takenWords, takenWords);
                    }
                    content = measurePreContent(content);
                    pageContent.put(position - 1, content);
                    pageStart.put(position - 1, onShowStart - content.length());
                    pageEnd.put(position - 1, onShowStart - 1);
                    if (position - 1 == 0 && onShowStart - content.length() > 0) {
                        //前面还有字，要做调整，第0页变成第1页，相关的三个记录的list要重新初始化,而且要修改pagecount
                        //难道要新建一个Adapter？参照C++的拷贝构造，重新构造一个Adapter，前面
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        pageContent.delete(position + 2);
        pageStart.delete(position + 2);
        pageEnd.delete(position + 2);
    }

    private String measureContent(String content) {
        //通过功能类measure util进行测量计算
        return pagesArrangeUtil.measurePage(content);
    }

    private String measurePreContent(String content) {
        //通过功能类measure util进行测量
        return measurePreUtil.prePageContentLength(content);
    }

    private void setPageNumber(int position) {
        pageNumberOnShow = position;
    }

    public void notifyPageChanged(int position) {
        if (position == 0) {
            setMarked(0);
        }//此处有大问题的，没有同步更新marked和onShow start的值因此每次取用字数都是从0开始取的
        // 的位置，思路新建一个表，从表中取值
        //从前往后选这个方法可以，但是如果从当中选值的话，这样做不行
        if (pageStart.indexOfKey(position) >= 0 && pageEnd.indexOfKey(position) >= 0) {
            onShowStart = pageStart.get(position);
            onShowEnd = pageEnd.get(position);
            setPageNumber(position);
            getContentNextShow(position);
        } else {
            getContent(position);
        }
        getContentPreShow(position);
        mAdapter2.updateSeekBar(onShowStart);
        marked = onShowStart;
        //把更新后的位置通知给seekbar可以通过handler实现或者广播，或者一个接口
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }

    //下面方法设置为从当中某一页打开
    public void setContentFromPage(int pageNumberOnShow, int marked) {
        setMarked(marked);
        notifyPageChanged(pageNumberOnShow);
    }

    public void reMeasure() {
        pageContent.clear();
        pageStart.clear();
        pageEnd.clear();
    }
    /*
    动态调整页面：
    1、当从0开始往后翻，只添加最后一页
    2、当从当中位置往前翻的时候，set当前页面为倒数第一页（向前加交给control解决，总页数大于当前字数/最小页字数，当页数向前翻到底的时候即当前页开始字为0的时候
    设置当前页为第0页（selected0？）
     */
}
