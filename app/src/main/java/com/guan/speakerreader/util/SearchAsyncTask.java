package com.guan.speakerreader.util;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;

import com.guan.speakerreader.adapter.ScanFileAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by guans on 2017/5/7.
 */

public class SearchAsyncTask extends AsyncTask<String, File, Void> {
    private ArrayList<File> searchResults;
    private ArrayList<File> results;
    private BaseAdapter searchAdapter;
    private ResultToShowTeller resultToShowTeller;
    private ScanFileAdapter mScanFileAdapter;

    public SearchAsyncTask(ArrayList<File> searchResults, ArrayList<File> results, BaseAdapter searchAdapter, ScanFileAdapter mScanFileAdapter) {
        this.searchResults = searchResults;
        this.results = results;
        this.searchAdapter = searchAdapter;
        this.mScanFileAdapter = mScanFileAdapter;
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
    }
    public void setResultTeller(ResultToShowTeller resultToShowTeller) {
        this.resultToShowTeller = resultToShowTeller;
    }

    @Override
    protected Void doInBackground(String... params) {
        int count = 0;
        for (int i = 0; i < results.size(); i++) {
            File file = results.get(i);
            String fileName = file.getName();
            if (fileName.contains(params[0])) {
                publishProgress(file);
            }
            count++;
//            Log.e("searchTast1", "taskOn");
        }
        while (mScanFileAdapter.isTaskOn() || count != results.size()) {
//            Log.e("count", String.valueOf(count));
//            Log.e("resultSize", String.valueOf(results.size()));
//            Log.e("isTaskOn", String.valueOf(mScanFileAdapter.isTaskOn()));
//            Log.e("searchTast2", "taskOn");
            for (int i = count; i < results.size(); i++) {
                File file = results.get(i);
                String fileName = file.getName();
                if (fileName.contains(params[0])) {
                    publishProgress(file);
                }
                count++;
//                Log.e("count", String.valueOf(count));
//                Log.e("resultSize", String.valueOf(results.size()));
//                Log.e("isTaskOn", String.valueOf(mScanFileAdapter.isTaskOn()));
            }
            if ((!mScanFileAdapter.isTaskOn()) && count == results.size()) {
                break;
            }
        }
//        Log.e("searchTast2", "taskOnfinish");
        return null;
    }

    @Override
    protected void onProgressUpdate(File... values) {
        searchResults.add(values[0]);
        searchAdapter.notifyDataSetChanged();
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        searchAdapter.notifyDataSetChanged();
        if (resultToShowTeller != null) {
            if (searchResults.size() == 0) {
                resultToShowTeller.noResult();
            } else {
                resultToShowTeller.hasResult();
            }
            resultToShowTeller.taskFinish();
        }
    }
    @Override
    protected void onCancelled(Void aVoid) {
        searchAdapter.notifyDataSetChanged();
        if (resultToShowTeller != null) {
            if (searchResults.size() == 0) {
                resultToShowTeller.noResult();
            } else {
                resultToShowTeller.hasResult();
            }
            resultToShowTeller.taskFinish();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        resultToShowTeller.taskStart();
    }

    public interface ResultToShowTeller {
        void noResult();
        void hasResult();
        void taskStart();
        void taskFinish();
    }
}

