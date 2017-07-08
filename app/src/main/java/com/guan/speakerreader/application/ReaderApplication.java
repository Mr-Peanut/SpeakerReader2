package com.guan.speakerreader.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by guans on 2017/7/8.
 */

public class ReaderApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
