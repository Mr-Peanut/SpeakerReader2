package com.guan.speakerreader.view.view;

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
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.adapter.ChooseFileAdapter;
import com.guan.speakerreader.view.adapter.ScanFileAdapter;

import java.io.File;

public class FileActivity extends AppCompatActivity implements ScanFileAdapter.ProcessBarController, ScanFileAdapter.FileItemOnClickedListener, ChooseFileAdapter.FileItemOnClickedListener {
    private RecyclerView fileList;
    private ImageButton backButton;
    private ProgressBar scanProgress;
    private ScanFileAdapter scanFileAdapter;
    private ScanResultBroadcastReceiver scanResultReceiver;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fileexplorer);
        initView();
    }

    private void initScanResultBroadcastReceiver() {
        scanResultReceiver = new ScanResultBroadcastReceiver();
        registerReceiver(scanResultReceiver, new IntentFilter("NO TXT FILE"));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initView() {
        fileList = (RecyclerView) findViewById(R.id.fileList);
        backButton = (ImageButton) findViewById(R.id.backButton);
        scanProgress = (ProgressBar) findViewById(R.id.scanProgress);
        fileList.setLayoutManager(new LinearLayoutManager(FileActivity.this, LinearLayout.VERTICAL, false));
        switch (getIntent().getAction()) {
            case WelcomeActivity.CHOOSE_FILE_ACTION:
                initChooseAdapter();
                break;
            case WelcomeActivity.SCAN_FILES_ACTION:
                initScanAdapter();
                break;
        }
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
        scanFileAdapter.setmFileItemOnClickedListener(this);
        initScanResultBroadcastReceiver();
        fileList.setAdapter(scanFileAdapter);
    }

    @Override
    public void onBackPressed() {
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
    public void dissProcessBar() {
        scanProgress.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onItemClicked(File file) {
        if (file.getName().contains(".txt") || file.getName().contains(".TXT")) {
            Intent intent = new Intent(this, Reader2Activity.class);
            intent.putExtra("StartFlag",WelcomeActivity.START_FROM_FILE);
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

    public class ScanResultBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(FileActivity.this, "没有扫描到任何文本", Toast.LENGTH_SHORT).show();
        }
    }
}
