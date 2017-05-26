package com.guan.speakerreader.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.guan.speakerreader.util.SearchFileUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileSearchService extends IntentService {
    public static final String FILE_FIND_ACTION = "com.guan.speakerreader.FILE_FIND";
    public static final String SEARCH_TASK_FINISHED = "com.guan.speakerreader.SEARCH_TASK_FINISHED";
    public static final String STOP_ACTION = "com.guan.speakerreader.STOP_TASK";
    private static final String ACTION_SEARCH_FILE = "com.guan.speakerreader.action.ACTION_SEARCH_FILE";
    private static final String SEARCH_PATH = "com.guan.speakerreader.SEARCH_PATH";
    private static final String SEARCH_NAME = "com.guan.speakerreader.SEARCH_NAME";
    private static ArrayList<File> resultList;
    private static SearchFileUtil searchFileUtil;
    private Intent searchTaskIntent;
    private String taskPath;
    private TaskController taskController;
    public FileSearchService() {
        super("FileSearchService");
    }

    public static SearchFileUtil getSearchFileUtil() {
        return searchFileUtil;
    }

    public static void startActionSearchFile(Context context, String path, String name, ArrayList<File> resultList) {
        Intent intent = new Intent(context, FileSearchService.class);
        intent.setAction(ACTION_SEARCH_FILE);
        intent.putExtra(SEARCH_PATH, path);
        intent.putExtra(SEARCH_NAME, name);
        setResultList(resultList);
        context.startService(intent);
    }

    public static void setResultList(ArrayList<File> resultList) {
        FileSearchService.resultList = resultList;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        taskController = new TaskController();
        registerReceiver(taskController, new IntentFilter(STOP_ACTION));
    }

    @Override
    public void onDestroy() {
        if (taskController != null)
            unregisterReceiver(taskController);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEARCH_FILE.equals(action)) {
                final String path = intent.getStringExtra(SEARCH_PATH);
                final String name = intent.getStringExtra(SEARCH_NAME);
                handleActionSearchFile(path, name);
            }
        }
    }
    private void handleActionSearchFile(String path, final String name) {
        searchFileUtil = new SearchFileUtil();
        searchTaskIntent = new Intent(FILE_FIND_ACTION);
        searchFileUtil.setOnResultFindListener(new SearchFileUtil.OnResultFindListener() {
            @Override
            public void onFileFind(File resultFile) {
                resultList.add(resultFile);
                Log.e("findPath", resultFile.getAbsolutePath());
                sendBroadcast(searchTaskIntent);
            }
        });
        searchFileUtil.setSearchRequirement(new SearchFileUtil.SearchRequirement() {
            @Override
            public boolean isSatisfied(File file) {
                String fileName = file.getName();
                if (fileName.contains(".txt") || fileName.contains(".TXT")) {
                    if (fileName.contains(name))
                        return true;
                }
                return false;
            }
        });
        taskPath = path;
        searchFileUtil.searchFile(path);
        onTaskFinished();
    }

    private void onTaskFinished() {
        searchTaskIntent.setAction(SEARCH_TASK_FINISHED);
        searchTaskIntent.putExtra("TASK_TAG", taskPath);
        sendBroadcast(searchTaskIntent);
        setResultList(null);
        searchFileUtil = null;
        taskPath = null;
    }
    class TaskController extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                if (searchFileUtil != null) {
                    searchFileUtil.stopTask();
                    onTaskFinished();
                }
        }
    }
}
