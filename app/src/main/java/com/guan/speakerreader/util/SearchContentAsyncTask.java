package com.guan.speakerreader.util;

import android.icu.text.StringSearch;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by guans on 2017/5/8.
 */

public class SearchContentAsyncTask extends AsyncTask<String,Void,Void> {
    private BaseAdapter resultAdapter;
    private StringSearcher stringSearcher;
    private ResultToShowTeller resultToShowTeller;
    private String targetPath;
    public void setResultToShowTeller(ResultToShowTeller resultToShowTeller) {
        this.resultToShowTeller = resultToShowTeller;
    }

    public SearchContentAsyncTask(BaseAdapter resultAdapter, StringSearcher stringSearcher) {
        this.resultAdapter = resultAdapter;
        this.stringSearcher = stringSearcher;
    }

    @Override
    protected Void doInBackground(String... params) {
        stringSearcher.setOnContentFindListener(new StringSearcher.OnContentFindListener() {
            @Override
            public void onContentFind() {
                publishProgress();
            }
        });
        try {
            stringSearcher.searchTarget(targetPath,params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        resultAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(resultToShowTeller!=null)
            resultToShowTeller.taskStart();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        resultAdapter.notifyDataSetChanged();
        if(resultToShowTeller!=null)
            resultToShowTeller.taskFinish();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        resultAdapter.notifyDataSetChanged();
    }

    public interface ResultToShowTeller {
        void taskStart();
        void taskFinish();
    }
}
