package com.guan.speakerreader.bean;

/**
 * Created by shiqian.guan on 2017/5/9.
 */

public class ContentSearchResultValuePairs {
    private int position;
    private String preview;

    public ContentSearchResultValuePairs(int position, String preview) {
        this.position = position;
        this.preview = preview;
    }

    public int getPosition() {
        return position;
    }

    public String getPreview() {
        return preview;
    }
}
