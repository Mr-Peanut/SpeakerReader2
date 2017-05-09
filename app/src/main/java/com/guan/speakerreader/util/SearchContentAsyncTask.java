package com.guan.speakerreader.util;

import android.os.AsyncTask;

import com.guan.speakerreader.bean.ContentSearchBean;

/**
 * Created by guans on 2017/5/8.
 */

public class SearchContentAsyncTask extends AsyncTask<ContentSearchBean, Void, Void> {
    private StringSearcher stringSearcher;
    private ResultToShowTeller resultToShowTeller;

    public SearchContentAsyncTask(StringSearcher stringSearcher) {
        this.stringSearcher = stringSearcher;
    }

    public void setResultToShowTeller(ResultToShowTeller resultToShowTeller) {
        this.resultToShowTeller = resultToShowTeller;
    }

    @Override
    protected Void doInBackground(ContentSearchBean... params) {
        stringSearcher.setOnContentFindListener(new StringSearcher.OnContentFindListener() {
            @Override
            public void onContentFind() {
                publishProgress();
            }
        });
        try {
            //params[0] 文本路径，params[1]目标文本
            stringSearcher.searchTarget(params[0].getTargetPath(), params[0].getTargetContent(), params[0].getTotalWords());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        resultToShowTeller.taskUpdate();
    }

    @Override
    protected void onPreExecute() {
        if (resultToShowTeller == null)
            try {
                throw new Exception("resultToShowTeller must be instantiated ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        resultToShowTeller.taskStart();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
            resultToShowTeller.taskFinish();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        resultToShowTeller.taskCanceled();
    }

    public interface ResultToShowTeller {
        void taskStart();
        void taskFinish();

        void taskUpdate();

        void taskCanceled();
    }
}
