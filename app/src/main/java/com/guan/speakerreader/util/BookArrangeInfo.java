package com.guan.speakerreader.util;

import java.io.Serializable;
import java.util.List;

/**
 * Created by guans on 2017/3/21.
 */

public class BookArrangeInfo implements Serializable {
    private String bookName;
    private String bookPath;
    private List<Integer> recordsList;
    private int pageCount;

    public BookArrangeInfo(String bookName, String bookPath, List<Integer> recordsList, int pageCount) {
        this.bookName = bookName;
        this.bookPath = bookPath;
        this.recordsList = recordsList;
        this.pageCount = pageCount;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public List<Integer> getRecordsList() {
        return recordsList;
    }

    public void setRecordsList(List<Integer> recordsList) {
        this.recordsList = recordsList;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
