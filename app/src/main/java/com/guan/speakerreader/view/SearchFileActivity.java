package com.guan.speakerreader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guan.speakerreader.R;
import com.guan.speakerreader.adapter.FileSearchAdapter;
import com.guan.speakerreader.service.FileSearchService;
import com.guan.speakerreader.util.PathUtil;

import java.io.File;
import java.util.ArrayList;

public class SearchFileActivity extends AppCompatActivity implements FileSearchAdapter.OnItemClickedListener {
    private ArrayList<File> resultList;
    private TextView searchStatue;
    private ProgressBar searchProgressBar;
    private EditText searchNameInput;
    private String[] sdCardsPath;
    private BroadcastReceiver searchUpdateReceiver;
    private BroadcastReceiver searchFinishedReceiver;
    private FileSearchAdapter mFileSearchAdapter;
    private RecyclerView resultViewList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_file);
        initData();
        initView();
        initReceiver();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initReceiver() {
        searchUpdateReceiver = new SearchUpdateReceiver();
        searchFinishedReceiver = new SearchFinishedReceiver();
        registerReceiver(searchUpdateReceiver, new IntentFilter(FileSearchService.FILE_FIND_ACTION));
        registerReceiver(searchFinishedReceiver, new IntentFilter(FileSearchService.SEARCH_TASK_FINISHED));
    }

    private void initData() {
        resultList = new ArrayList<>();
        sdCardsPath = PathUtil.getSDCardPath(getApplicationContext());
        mFileSearchAdapter = new FileSearchAdapter(resultList, SearchFileActivity.this);
        mFileSearchAdapter.setItemClickedListener(this);

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchNameInput = (EditText) findViewById(R.id.search_file_input);
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchStatue = (TextView) findViewById(R.id.search_statue);
        searchProgressBar = (ProgressBar) findViewById(R.id.search_progressBar);
        resultViewList = (RecyclerView) findViewById(R.id.file_search_result);
        resultViewList.setLayoutManager(new LinearLayoutManager(SearchFileActivity.this, LinearLayoutManager.VERTICAL, false));
        resultViewList.setAdapter(mFileSearchAdapter);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultList.clear();
                mFileSearchAdapter.notifyDataSetChanged();
                String targetName = searchNameInput.getText().toString().trim();
                if (targetName.length() != 0) {
                    searchProgressBar.setVisibility(View.VISIBLE);
                    searchStatue.setText("搜索中。。。");
                    FileSearchService.startActionSearchFile(SearchFileActivity.this, sdCardsPath, targetName, resultList);
                } else {
                    Toast.makeText(SearchFileActivity.this, "请输入正确的文件名，不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (searchUpdateReceiver != null)
            unregisterReceiver(searchUpdateReceiver);
        if (searchFinishedReceiver != null)
            unregisterReceiver(searchFinishedReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (FileSearchService.getSearchFileUtil() != null && FileSearchService.getSearchFileUtil().isRun()) {
            sendBroadcast(new Intent(FileSearchService.STOP_ACTION));
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onItemClicked(int position) {
        sendBroadcast(new Intent(FileSearchService.STOP_ACTION));
        Intent intent = new Intent(this, ReaderActivity.class);
        intent.putExtra("StartFlag", WelcomeActivity.START_FROM_FILE);
        intent.putExtra("FILEPATH", resultList.get(position).getAbsolutePath());
        startActivity(intent);
    }

    class SearchUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mFileSearchAdapter.notifyDataSetChanged();
        }
    }

    class SearchFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            searchProgressBar.setVisibility(View.INVISIBLE);
            searchStatue.setText("搜索完成");
            if (resultList.size() == 0) {
                searchStatue.append("，无结果显示");
            }
            mFileSearchAdapter.notifyDataSetChanged();
        }
    }
}
