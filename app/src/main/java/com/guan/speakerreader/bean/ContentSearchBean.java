package com.guan.speakerreader.bean;

/**
 * Created by shiqian.guan on 2017/5/9.
 */

public class ContentSearchBean {
    private String targetPath;
    private String targetContent;
    private int totalWords;

    public ContentSearchBean(String targetPath, String targetContent, int totalWords) {
        this.targetPath = targetPath;
        this.targetContent = targetContent;
        this.totalWords = totalWords;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getTargetContent() {
        return targetContent;
    }

    public int getTotalWords() {
        return totalWords;
    }
}
