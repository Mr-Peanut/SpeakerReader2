package com.guan.speakerreader.util;

import android.net.Uri;

/**
 * Created by shiqian.guan on 2017/5/26.
 */

public class Uri2Path {
    public static String filePath(Uri uri) {
        if (uri == null)
            return null;
        return uri.getPath();
    }
}
