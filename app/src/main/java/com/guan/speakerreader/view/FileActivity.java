package com.guan.speakerreader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guan.speakerreader.R;
import com.guan.speakerreader.adapter.ChooseFileAdapter;
import com.guan.speakerreader.adapter.FileSearchResultAdapter;
import com.guan.speakerreader.adapter.ScanFileAdapter;
import com.guan.speakerreader.util.SearchAsyncTask;

import java.io.File;
import java.util.ArrayList;

public class FileActivity extends AppCompatActivity implements ScanFileAdapter.ProcessBarController, ScanFileAdapter.FileItemOnClickedListener, ChooseFileAdapter.FileItemOnClickedListener, SearchAsyncTask.ResultToShowTeller {
    private RecyclerView fileList;
    private ImageButton backButton;
    private ProgressBar scanProgress;
    private ScanFileAdapter scanFileAdapter;
    private ScanResultBroadcastReceiver scanResultReceiver;
    private ArrayList<File> results;
    private TextView fileSearch;
    private LinearLayout fileExplorerRootView;
    private PopupWindow searchPopupWindow;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fileexplorer_activity_layout);
        initView();
    }

    private void initScanResultBroadcastReceiver() {
        scanResultReceiver = new ScanResultBroadcastReceiver();
        registerReceiver(scanResultReceiver, new IntentFilter("NO TXT FILE"));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initView() {
        fileExplorerRootView = (LinearLayout) findViewById(R.id.file_explorer_root_view);
        fileList = (RecyclerView) findViewById(R.id.file_list);
        backButton = (ImageButton) findViewById(R.id.back_button);
        scanProgress = (ProgressBar) findViewById(R.id.scan_progressbar);
        fileSearch = (TextView) findViewById(R.id.file_search);
        fileList.setLayoutManager(new LinearLayoutManager(FileActivity.this, LinearLayout.VERTICAL, false));
        switch (getIntent().getAction()) {
            case WelcomeActivity.CHOOSE_FILE_ACTION:
                fileSearch.setVisibility(View.GONE);
                initChooseAdapter();
                break;
            case WelcomeActivity.SCAN_FILES_ACTION:
                initScanAdapter();
                fileSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initFileSearchPopupWindow();
                    }
                });
                break;
        }
    }

    private void initFileSearchPopupWindow() {
        searchPopupWindow = new PopupWindow(this);
        searchPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
//        searchPopupWindow.setHeight(file_explorer_root_view.getHeight());
        searchPopupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        searchPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        searchPopupWindow.setInputMethodMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        searchPopupWindow.setFocusable(true);
        final SearchAsyncTask[] searchAsyncTask = new SearchAsyncTask[1];
        View searchPopupView = LayoutInflater.from(this).inflate(R.layout.searchfilepopupwindow_view_layout, null);
        EditText searchNameInput = (EditText) searchPopupView.findViewById(R.id.searchNameInput);
        ListView resultList = (ListView) searchPopupView.findViewById(R.id.resultList);
        final ArrayList<File> searchResult = new ArrayList<>();
        final FileSearchResultAdapter fileSearchResultAdapter = new FileSearchResultAdapter(searchResult, this);
        resultList.setAdapter(fileSearchResultAdapter);
        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //取消异步操作
                if (searchAsyncTask[0] != null && !searchAsyncTask[0].isCancelled()) {
                    searchAsyncTask[0].cancel(true);
                }
                scanFileAdapter.cancelScanTask();
                FileActivity.this.onItemClicked(searchResult.get(position));
                searchPopupWindow.dismiss();
            }
        });
        results = scanFileAdapter.getFlies();
        searchNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchAsyncTask[0] != null && !searchAsyncTask[0].isCancelled()) {
                    searchAsyncTask[0].cancel(true);
                }
                searchAsyncTask[0] = new SearchAsyncTask(searchResult, results, fileSearchResultAdapter, scanFileAdapter);
                searchAsyncTask[0].setResultTeller(FileActivity.this);
                if (s != null) {
                    String target = String.valueOf(s);
                    if (!target.equals("") && !target.equals(" ") && s.length() != 0)
                        searchAsyncTask[0].execute(String.valueOf(s));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchPopupWindow.setContentView(searchPopupView);
        searchPopupWindow.showAtLocation(fileExplorerRootView, Gravity.TOP, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initChooseAdapter() {
        final ChooseFileAdapter chooseAdapter = new ChooseFileAdapter(this);
        chooseAdapter.setFileItemOnClickedListener(this);
        fileList.setAdapter(chooseAdapter);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAdapter.backToParent();
            }
        });
    }

    private void initScanAdapter() {
        scanFileAdapter = new ScanFileAdapter(this, this);
        scanFileAdapter.setFileItemOnClickedListener(this);
        initScanResultBroadcastReceiver();
        fileList.setAdapter(scanFileAdapter);
    }

    @Override
    public void onBackPressed() {
        if (searchPopupWindow != null && searchPopupWindow.isShowing()) {
            searchPopupWindow.dismiss();
            return;
        }
        if (scanFileAdapter != null) {
            AsyncTask<Void, File, Void> asyncTask = scanFileAdapter.getScanFileTask();
            if (scanFileAdapter.isTaskOn() && asyncTask != null && !asyncTask.isCancelled()) {
                asyncTask.cancel(true);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void showProcessBar() {
        scanProgress.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void disMissProcessBar() {
        scanProgress.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public void onItemClicked(File file) {
        if (file.getName().contains(".txt") || file.getName().contains(".TXT")) {
            Intent intent = new Intent(this, ReaderActivity.class);
            intent.putExtra("StartFlag", WelcomeActivity.START_FROM_FILE);
            intent.putExtra("FILEPATH", file.getAbsolutePath());
            startActivity(intent);
        } else {
            Toast.makeText(this, "不支持的文件类型", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (scanResultReceiver != null)
            unregisterReceiver(scanResultReceiver);
        super.onDestroy();
    }

    @Override
    public void noResult() {
        //当没有内容显示时增加无文件显示的状态
        LinearLayout searchPopupWindowView = (LinearLayout) (searchPopupWindow.getContentView().findViewById(R.id.searchPopupWindow));
        if (!(searchPopupWindowView.getChildAt(searchPopupWindowView.getChildCount() - 1) instanceof TextView)) {
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            textView.setLayoutParams(layoutParams);
            textView.setText("没有结果显示");
            searchPopupWindowView.addView(textView);
        }
    }

    @Override
    public void hasResult() {
        LinearLayout searchPopupWindowView = (LinearLayout) (searchPopupWindow.getContentView().findViewById(R.id.searchPopupWindow));
        if ((searchPopupWindowView.getChildAt(searchPopupWindowView.getChildCount() - 1) instanceof TextView)) {
            searchPopupWindowView.removeViewAt(searchPopupWindowView.getChildCount() - 1);
        }
    }

    @Override
    public void taskStart() {
        LinearLayout searchPopupWindowView = (LinearLayout) (searchPopupWindow.getContentView().findViewById(R.id.searchPopupWindow));
        LinearLayout taskStatueGroup = (LinearLayout) searchPopupWindowView.findViewById(R.id.taskStatueGroup);
        TextView taskStatue = (TextView) taskStatueGroup.findViewById(R.id.taskStatue);
        taskStatueGroup.setVisibility(View.VISIBLE);
        taskStatue.setText("文件搜索中，请稍后……");
    }

    @Override
    public void taskFinish() {
        LinearLayout searchPopupWindowView = (LinearLayout) (searchPopupWindow.getContentView().findViewById(R.id.searchPopupWindow));
        LinearLayout taskStatueGroup = (LinearLayout) searchPopupWindowView.findViewById(R.id.taskStatueGroup);
        TextView taskStatue = (TextView) taskStatueGroup.findViewById(R.id.taskStatue);
        ProgressBar taskProgressBar = (ProgressBar) taskStatueGroup.findViewById(R.id.taskProgressBar);
        taskProgressBar.setVisibility(View.INVISIBLE);
        taskStatueGroup.setVisibility(View.VISIBLE);
        taskStatue.setText("文件检索结束");
    }

    public class ScanResultBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(FileActivity.this, "没有扫描到任何文本", Toast.LENGTH_SHORT).show();
        }
    }
}
