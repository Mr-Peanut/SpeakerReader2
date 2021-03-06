package com.guan.speakerreader.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guan.speakerreader.R;
import com.guan.speakerreader.adapter.ReadRecordAdapter;
import com.guan.speakerreader.database.RecordDatabaseHelper;

import java.io.File;

public class WelcomeActivity extends AppCompatActivity implements ReadRecordAdapter.ItemOnClickedListener, ReadRecordAdapter.ItemOnLongClickedListener {
    public final static String CHOOSE_FILE_ACTION = "FILE_CHOOSE";
    public final static String SCAN_FILES_ACTION = "FILE_SCAN";
    public static final int START_FROM_RECORD = 1;
    public static final int START_FROM_FILE = 0;
    public static final int START_FROM_SCREEN_CHANGE = 2;
    private final static String TABLE_NAME = "ReadRecord";
    private static final int REQUEST_CODE = 1;
    private static final String PACKAGE_URL_SCHEME = "package:";
    private RecyclerView recordList;
    private SQLiteOpenHelper recordDatabaseHelper;
    private ReadRecordAdapter readRecordAdapter;
    private RecordDBUpdateReceiver recordDBUpdateReceiver;
    private LinearLayout rootContainer;
    private TextView permissionStatue;
    private Button settingButton;
    private boolean getPermission = false;
    private Toast permissionToast;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity_layout);
        initStatueTextColor();
        initView();
        initData();
        initReceiver();
        //动态权限申请
        //        Log.e("version+get",String.valueOf(Build.VERSION.PREVIEW_SDK_INT));
//        Log.e("version+M",String.valueOf(Build.VERSION_CODES.M));
//        if(Build.VERSION.PREVIEW_SDK_INT>=Build.VERSION_CODES.M){
        permissionCheck();
//        }
    }
    private void initStatueTextColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void permissionCheck() {
        Log.e("permissionCheck", "permissionCheck");
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            Log.e("permissionCheck", "permissionCheck");
        } else {
            getPermission = true;
            if (settingButton != null) {
                rootContainer.removeView(settingButton);
                settingButton = null;
            }
            if (permissionStatue != null) {
                rootContainer.removeView(permissionStatue);
                permissionStatue = null;
            }
        }
    }

    private void initReceiver() {
        if (recordDBUpdateReceiver == null) {
            recordDBUpdateReceiver = new RecordDBUpdateReceiver();
        }
        IntentFilter receiverIntentFilter = new IntentFilter("READ_RECORD_DB_UPDATE");
        registerReceiver(recordDBUpdateReceiver, receiverIntentFilter);
    }

    /*
    初始化数据
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initData() {
        if (recordDatabaseHelper == null) {
            recordDatabaseHelper = new RecordDatabaseHelper(this, "recordDatabase", null, 1);
        }
        if (readRecordAdapter == null) {
            readRecordAdapter = new ReadRecordAdapter(WelcomeActivity.this, recordDatabaseHelper);
            readRecordAdapter.setItemOnClickedListener(WelcomeActivity.this);
            readRecordAdapter.setItemOnLongClickedListener(WelcomeActivity.this);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(WelcomeActivity.this, LinearLayoutManager.VERTICAL, false);
            recordList.setLayoutManager(layoutManager);
            recordList.setAdapter(readRecordAdapter);
        }
    }
    /*
    初始化控件
     */
    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        Toolbar welcomeToolbar = (Toolbar) findViewById(R.id.welcome_toolbar);
        setSupportActionBar(welcomeToolbar);
        TextView searchFile = (TextView) findViewById(R.id.search_file);
        searchFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSearchFileActivityIntent = new Intent(WelcomeActivity.this, SearchFileActivity.class);
                startActivity(startSearchFileActivityIntent);
            }
        });
        permissionToast = Toast.makeText(this, "权限检查", Toast.LENGTH_SHORT);
        rootContainer = (LinearLayout) findViewById(R.id.root_container);
        TextView fileChoose = (TextView) findViewById(R.id.file_choose);
        TextView scanFiles = (TextView) findViewById(R.id.scan_files);
        recordList = (RecyclerView) findViewById(R.id.record_list);
        fileChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPermission) {
                    permissionToast.setText("请在设置中打开储存权限后再使用该功能");
                    permissionToast.show();
                    return;
                }
                Intent intent = new Intent(WelcomeActivity.this, FileActivity.class);
                intent.setAction(CHOOSE_FILE_ACTION);
                startActivity(intent);
            }
        });
        scanFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPermission) {
                    permissionToast.setText("请在设置中打开储存权限后再使用该功能");
                    permissionToast.show();
                    return;
                }
                Intent intent = new Intent(WelcomeActivity.this, FileActivity.class);
                intent.setAction(SCAN_FILES_ACTION);
                startActivity(intent);
            }
        });
        findViewById(R.id.goto_news).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "onResume");
    }

    @Override
    protected void onRestart() {
        permissionCheck();
        super.onRestart();
        Log.e("onRestart", "onRestart");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPermission = true;
                    if (settingButton != null) {
                        rootContainer.removeView(settingButton);
                        settingButton = null;
                    }
                    if (permissionStatue != null) {
                        rootContainer.removeView(permissionStatue);
                        permissionStatue = null;
                    }
                    break;
                } else {
                    getPermission = false;
//                    Toast.makeText(this,"没有同意储存权限无法使用该APP",Toast.LENGTH_SHORT).show();
                    permissionToast.setText("没有同意储存权限无法使用该APP");
                    permissionToast.show();
                    LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    if (permissionStatue == null) {
                        permissionStatue = new TextView(this);
                        permissionStatue.setLayoutParams(layoutParams);
                        permissionStatue.setText("储存权限被禁止，请在设置—应用—" + String.valueOf(getApplicationContext().getPackageManager().getApplicationLabel(getApplicationInfo()))
                                + "—权限—储存，打开储存权限后重新打开APP");
                        if (rootContainer != null) {
                            rootContainer.addView(permissionStatue);
                        }
                    }
                    if (settingButton == null) {
                        settingButton = new Button(this);
                        settingButton.setText("打开设置");
                        settingButton.setLayoutParams(layoutParams);
                        settingButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                settingIntent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
                                startActivity(settingIntent);
                            }
                        });
                        if (rootContainer != null) {
                            rootContainer.addView(settingButton);
                        }
                    }
                    break;
                }
        }
    }

    @Override
    protected void onDestroy() {
        if (recordDBUpdateReceiver != null)
            unregisterReceiver(recordDBUpdateReceiver);
        if (readRecordAdapter.getRecordCursor() != null && !readRecordAdapter.getRecordCursor().isClosed()) {
            readRecordAdapter.getRecordCursor().close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onItemLongClicked(final int position, View view) {
        if (!getPermission) {
            permissionToast.setText("请在设置中打开储存权限后再使用该功能");
            permissionToast.show();
            return true;
        }
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.record_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        readRecordAdapter.deleteDataItem(position);
                        break;
                    case R.id.open:
                        WelcomeActivity.this.openReaderActivity(position);
                        break;
                    case R.id.cleanAll:
                        readRecordAdapter.cleanAll();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
        return true;
    }

    private void openReaderActivity(int position) {
        Cursor cursor = readRecordAdapter.getRecordCursor();
        cursor.moveToPosition(cursor.getCount() - position - 1);
        String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            Toast.makeText(this, "原始文件不存在，记录被删除", Toast.LENGTH_SHORT).show();
            readRecordAdapter.deleteDataItem(position);
            return;
        }
        Intent intent = new Intent(WelcomeActivity.this, ReaderActivity.class);
        intent.putExtra("FILEPATH", filePath);
        intent.putExtra("totalWords", cursor.getInt(cursor.getColumnIndex("totalWords")));
        intent.putExtra("formatPath", cursor.getString(cursor.getColumnIndex("formatPath")));
        intent.putExtra("position", cursor.getInt(cursor.getColumnIndex("position")));
        intent.putExtra("StartFlag", START_FROM_RECORD);
        startActivity(intent);
    }

    @Override
    public void onRecordItemClick(int position) {
        //打开readerActivity
        //先检查原始文件是否存在，不存在删除该条记录
        if (!getPermission) {
            permissionToast.setText("请在设置中打开储存权限后再使用该功能");
            permissionToast.show();
            return;
        }
        openReaderActivity(position);
    }

    class RecordDBUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("WelcomeActivity:", "Receive BroadCast");
            readRecordAdapter.notifyDataChanged();
        }
    }
}
