package com.guan.speakerreader.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guan.speakerreader.R;
import com.guan.speakerreader.service.FileSearchService;
import com.guan.speakerreader.util.PathUtil;

import java.io.File;
import java.util.ArrayList;

public class SearchFileActivity extends AppCompatActivity {
    private ArrayList<File> resultList;
    private Button searchButton;
    private TextView searchStatue;
    private ProgressBar searchProgressBar;
    private EditText searchNameInput;
    private String[] sdCardsPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        initData();
        initView();
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initData() {
        resultList = new ArrayList<>();
        sdCardsPath = PathUtil.getSDCardPath(getApplicationContext());
    }

    private void initView() {
        searchNameInput = (EditText) findViewById(R.id.search_file_input);
        searchButton = (Button) findViewById(R.id.search_button);
        searchStatue = (TextView) findViewById(R.id.search_statue);
        searchProgressBar = (ProgressBar) findViewById(R.id.search_progressBar);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetName = searchNameInput.getText().toString().trim();
                if (targetName.length() != 0) {
                    searchProgressBar.setVisibility(View.VISIBLE);
                    for (String path : sdCardsPath) {
                        FileSearchService.startActionSearchFile(SearchFileActivity.this, path, targetName, resultList);
                    }
                } else {
                    Toast.makeText(SearchFileActivity.this, "请输入正确的文件名，不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class SearchUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    class SearchFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            searchProgressBar.setVisibility(View.INVISIBLE);
            searchStatue.setText("搜索完成");
        }
    }
}
