package com.guan.speakerreader.util;

import com.guan.speakerreader.bean.ContentSearchResultValuePairs;

import java.util.ArrayList;

/**
 * Created by shiqian.guan on 2017/5/8.
 */

public class StringSearcher {
    private ArrayList<ContentSearchResultValuePairs> resultParsList;
    private int checkLength;
    private int resultListLengthLimit;
    private OnContentFindListener mOnContentFindListener;
    public StringSearcher() {
        resultParsList = new ArrayList<>();
        resultListLengthLimit = 20;
        checkLength = 10000;
    }

    public void setOnContentFindListener(OnContentFindListener mOnContentFindListener) {
        this.mOnContentFindListener = mOnContentFindListener;
    }

    public ArrayList<ContentSearchResultValuePairs> getResultParsList() {
        return resultParsList;
    }

    public void setCheckLength(int checkLength) {
        this.checkLength = checkLength;
    }

    public void setResultListLengthLimit(int resultListLengthLimit) {
        this.resultListLengthLimit = resultListLengthLimit;
    }

    public void searchTarget(String filePath, String targetString, int totalWords) throws Exception {
        int position;
        int totalChecked = 0;
        int targetLength = targetString.length();
        while (totalChecked < totalWords) {
            String toCheck = TxtTaker.readerFromText(filePath, totalChecked, checkLength + targetLength - 1);
            if (toCheck == null)
                return;
            int startPosition = 0;
            while (startPosition < checkLength + targetLength - 1) {
                position = toCheck.indexOf(targetString, startPosition);
                if (position != -1) {
                    resultParsList.add(new ContentSearchResultValuePairs(totalChecked + position, toCheck.substring(Math.max(position - 10, 0), Math.min(position + targetLength + 10, toCheck.length()))));
                    if(mOnContentFindListener!=null)
                        mOnContentFindListener.onContentFind();
                    startPosition = position + targetLength;
                    if (resultParsList.size() >= resultListLengthLimit)
                        return;
                } else {
                    break;
                }
            }
            totalChecked += checkLength;
        }
    }
    interface OnContentFindListener{
        void onContentFind();
    }
}
