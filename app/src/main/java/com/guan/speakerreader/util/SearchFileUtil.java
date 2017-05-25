package com.guan.speakerreader.util;

import java.io.File;

/**
 * Created by shiqian.guan on 2017/5/25.
 */

public class SearchFileUtil {
    private SearchRequirement mSearchRequirement;
    private OnResultFindListener mOnResultFindListener;
    private boolean run = true;
    public void setSearchRequirement(SearchRequirement mSearchRequirement) {
        this.mSearchRequirement = mSearchRequirement;
    }

    public void setOnResultFindListener(OnResultFindListener mOnResultFindListener) {
        this.mOnResultFindListener = mOnResultFindListener;
    }

    public void searchFile(File parentFile) {
        if (mOnResultFindListener == null || mSearchRequirement == null) {
            throw new UnsupportedOperationException("SearchRequirement or OnResultFindListener is not implemented");
        }
        if (!run)
            return;
        if (parentFile.isDirectory()) {
            File[] files = parentFile.listFiles();
            for (File file : files) {
                searchFile(file);
            }
        } else {
            if (mSearchRequirement.isSatisfied(parentFile)) {
                mOnResultFindListener.onFileFind(parentFile);
            }
        }
    }

    public void stopTask() {
        run = false;
    }
    public void searchFile(String filePath) {
        searchFile(new File(filePath));
    }

    public interface SearchRequirement {
        boolean isSatisfied(File file);
    }

    public interface OnResultFindListener {
        void onFileFind(File resultFile);
    }
}
