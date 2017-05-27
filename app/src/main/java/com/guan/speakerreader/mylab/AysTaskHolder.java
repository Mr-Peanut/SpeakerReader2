package com.guan.speakerreader.mylab;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by shiqian.guan on 2017/5/27.
 */

public abstract class AysTaskHolder {
    private static final String DATA_FRAGMENT_TAG = "DataFragment";

    public static void addTask(String taskTag, AsyncTask mAsyncTask, Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        DataFragment dataFragment = (DataFragment) fragmentManager.findFragmentByTag(DATA_FRAGMENT_TAG);
        if (dataFragment == null) {
            dataFragment = (DataFragment) DataFragment.instantiate(activity, DATA_FRAGMENT_TAG);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(dataFragment, DATA_FRAGMENT_TAG);
            fragmentTransaction.commit();
        }
        dataFragment.addAsyncTask(taskTag, mAsyncTask);
    }
    public static AsyncTask getAsyncTask(String taskTag, Activity activity) {
        AsyncTask result = null;
        FragmentManager fragmentManager = activity.getFragmentManager();
        DataFragment dataFragment = (DataFragment) fragmentManager.findFragmentByTag(DATA_FRAGMENT_TAG);
        if (dataFragment != null) {
            result = dataFragment.getAsyncTask(taskTag);
        }
        return result;
    }

    public static class DataFragment extends Fragment {
        private HashMap<String, AsyncTask> mAsyncTaskMap;

        public AsyncTask getAsyncTask(String taskTag) {
            return mAsyncTaskMap.get(taskTag);
        }

        public void addAsyncTask(String taskTag, AsyncTask mAsyncTask) {
            mAsyncTaskMap.put(taskTag, mAsyncTask);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mAsyncTaskMap = new HashMap<>();
        }
    }
}
