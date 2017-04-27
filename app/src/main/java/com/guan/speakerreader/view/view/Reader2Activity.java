package com.guan.speakerreader.view.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.adapter.ReadRecordAdapter;
import com.guan.speakerreader.view.adapter.ReaderPagerAdapter2;
import com.guan.speakerreader.view.database.RecordDatabaseHelper;
import com.guan.speakerreader.view.util.TxtReader;

import java.io.File;
import java.io.IOException;

public class Reader2Activity extends AppCompatActivity implements ReaderPagerAdapter2.InnerViewOnClickedListener, ReaderPagerAdapter2.UpdateSeekBarController {
    private PageGroup contentPager;
    private String textPath;
    private ShowFinishedReceiver showFinishedReceiver;
    private int totalWords;
    private ProgressDialog getTotalWordsDialog;
    private ReaderPagerAdapter2 readerPagerAdapter;
    private SeekBar readerSeekBar;
    private TextView statusText;
    private PopupWindow settingWindow;
    private Paint textPaint;
    private String targetPath;
    private String storageCachePath;
    private int marked;
    private RecordDatabaseHelper recordDatabaseHelper;
    //
    private boolean notChosen =true;
    private boolean fromRecord=true;
    private Handler chooseHandler;
    private AlertDialog.Builder chooseDialog;
    //    private  PopupWindow settingWindow;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            contentPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVisible = true;
        setContentView(R.layout.activity_reader2);
        initDataBase();
        // Set up the user interaction to manually show or hide the system UI.
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        initPath();
        initBroadCast();
        initView();
        initPaint();
        Intent startIntent=getIntent();
        int startIntentFlag=startIntent.getIntExtra("StartFlag",0);
       if(startIntentFlag==WelcomeActivity.START_FROM_FILE){
//           Log.e("startFlag","Start from file");
           getTotalWords();
       }else{
//           Log.e("startFlag","Start from record");

           totalWords=startIntent.getIntExtra("totalWords",0);
//           Log.e("totalWords",String.valueOf(totalWords));
           targetPath=startIntent.getStringExtra("formatPath");
//           Log.e("targetPath",targetPath);
           textPath=startIntent.getStringExtra("FILEPATH");
//           Log.e("textPath",textPath);
           marked=startIntent.getIntExtra("position",0);
//           Log.e("marked",String.valueOf(marked));
           if(totalWords==0||targetPath==null||!new File(targetPath).exists()){
               //删除该条记录
               marked=0;
               totalWords=0;
               getTotalWords();
           }else {
               initAdapter();
           }
       }
    }
    private void initDataBase() {
        if (recordDatabaseHelper == null) {
            recordDatabaseHelper = new RecordDatabaseHelper(this, "recordDatabase", null, 1);
        }
    }

    private void initPaint() {
        if (textPaint == null) {
            textPaint = new Paint();
            //默认值
            textPaint.setTextSize(55.0f);
            textPaint.setColor(Color.BLACK);
            textPaint.setAntiAlias(true);
        }
    }

    private void getTotalWords() {
        //第一次阅读时执行，记录中要是有的话从记录中读取数据，不走这条逻辑
        if (totalWords == 0) {
            chooseHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                  //对话框询问
                    if(chooseDialog==null){
                        chooseDialog=new AlertDialog.Builder(Reader2Activity.this);
                        chooseDialog.setMessage("已经有该文件的阅读记录，是否从上次阅读的位置开始");
                        chooseDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fromRecord=true;
                                notChosen=false;
                            }
                        });
                        chooseDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fromRecord=false;
                                notChosen=false;
                            }
                        });
                        chooseDialog.setCancelable(false);
                    }
                    chooseDialog.show();
                }
            };
            //格式化文本，新建一个缓存文件，将源文件读取到缓存文件中，并替换掉当中的\r\n为\n（要新建一个工具类）
            //注意删除该条记录的时候要将缓存文件删除
            //在阅读记录生成前执行该逻辑
            AsyncTask<Void, Void, Integer> formatTask = new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    File originalFile = new File(textPath);
                    File resultFile = new File(storageCachePath + File.separator + originalFile.getName().replace(".txt", ""));
                    int totalWords;
                    if (resultFile.exists()) {
                        //进一步优化，当系统中有记录时弹出对话框，提示“系统中已经有记录是否要从记录中读取
                        targetPath = resultFile.getAbsolutePath();
                        Message chooseMSG=chooseHandler.obtainMessage();
                        chooseHandler.sendMessage(chooseMSG);
                        while (notChosen){
                        }
                        SQLiteDatabase recordDB = recordDatabaseHelper.getReadableDatabase();
                        Cursor recordCursor = recordDB.query(ReadRecordAdapter.TABLE_NAME, null, "formatPath=?", new String[]{targetPath}, null, null, null);
                        //注意int和long
                        recordCursor.moveToFirst();
                        totalWords = recordCursor.getInt(recordCursor.getColumnIndex("totalWords"));
                        if(fromRecord){
                            marked = recordCursor.getInt(recordCursor.getColumnIndex("position"));
                        }else {
                            marked=0;
                        }
                        recordCursor.close();

                        //设置对话框阻塞掉
                        return totalWords;
                    } else {
                        try {
                            resultFile.createNewFile();
                            targetPath = resultFile.getAbsolutePath();
                            totalWords = TxtReader.formatTxtFile(originalFile, resultFile);
                            marked = 0;
                            //下面的代码可以优化到一个方法中
                            Log.e("insertName",originalFile.getName());
                            Log.e("insert textPath",textPath);
                            Log.e("insert totalWords",String.valueOf(totalWords));
                            Log.e("insert targetPath",targetPath);
                            recordDatabaseHelper.insert(ReadRecordAdapter.TABLE_NAME, originalFile.getName(), textPath, null, totalWords, 0, targetPath);
                            return totalWords;
                        } catch (IOException e) {
                            e.printStackTrace();
                            resultFile.deleteOnExit();
                        }
                    }
                    //当无法建立格式化文件时，读取原始文件；
                    targetPath = textPath;
                    totalWords = TxtReader.getTotalWords(textPath);
                    recordDatabaseHelper.insert(ReadRecordAdapter.TABLE_NAME, originalFile.getName(), textPath, null, totalWords, 0, null);
                    return totalWords;
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    totalWords = integer;
                    initAdapter();
                    getTotalWordsDialog.dismiss();
                }

                @Override
                protected void onPreExecute() {
                    getTotalWordsDialog = new ProgressDialog(Reader2Activity.this);
                    getTotalWordsDialog.setMessage("正在读取文件，请稍后");
                    getTotalWordsDialog.show();
                    super.onPreExecute();
                }
            };
            formatTask.execute();
        }
    }

    private void initAdapter() {
        readerSeekBar.setMax(totalWords);
        readerPagerAdapter = new ReaderPagerAdapter2(this, targetPath, totalWords, textPaint);
        readerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
//                    readerPagerAdapter.getContentController().setPageCount(pageNumber);
                    //最后一页的逻辑
                    readerPagerAdapter.getContentController().reMeasure();
                    readerPagerAdapter.getContentController().setContentFromPage(contentPager.getOnShowPosition(), progress);
                    contentPager.skipToChild();
                }
                statusText.setText(String.valueOf(progress / totalWords * 100) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        readerPagerAdapter.setUpdateSeekBarController(this);
        readerPagerAdapter.setInnerViewOnClickedListener(this);
        contentPager.addOnPageChangeListener(new PageGroup.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                Log.e("pageChange",String.valueOf(position));
                readerPagerAdapter.getContentController().notifyPageChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (marked == 0) {
            readerPagerAdapter.getContentController().setContentFromPage(0, marked);
        } else {
            readerPagerAdapter.getContentController().setContentFromPage(contentPager.getOnShowPosition(), marked);
        }
        readerPagerAdapter.getContentController().setmPageGroup(contentPager);
        contentPager.setAdapter(readerPagerAdapter);
    }

    private void initPath() {
        textPath = getIntent().getStringExtra("FILEPATH");
        String appName;
        appName = String.valueOf(getApplicationContext().getPackageManager().getApplicationLabel(getApplicationInfo()));
        if (appName == null) {
            appName = getApplicationContext().getString(R.string.app_name);
        }
        File storageCache = new File(Environment.getExternalStorageDirectory() + File.separator + appName);
        if (!storageCache.exists()) {
            storageCache.mkdirs();
        }
        storageCachePath = storageCache.getAbsolutePath();
    }

    private void initView() {
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        contentPager = (PageGroup) findViewById(R.id.contentPager);
        readerSeekBar = (SeekBar) findViewById(R.id.readerSeekBar);
        statusText = (TextView) findViewById(R.id.statusText);
        Button settingMenu = (Button) findViewById(R.id.settingMenu);
        settingMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMenuView();
            }
        });
    }

    private void initMenuView() {
        if (settingWindow == null) {
            settingWindow = new PopupWindow(Reader2Activity.this);
            settingWindow.setHeight(contentPager.getHeight() / 2);
            settingWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            final View popuWindowsView = LayoutInflater.from(Reader2Activity.this).inflate(R.layout.readersetting_layout, null);
            //此处设计画笔菜单
            SeekBar lightAdjuster = (SeekBar) popuWindowsView.findViewById(R.id.lightAdjuster);
            CheckBox checkBox = (CheckBox) popuWindowsView.findViewById(R.id.fitSystemLightness);
            setScreenLightness(checkBox, lightAdjuster);
            Spinner textSizeSelector = (Spinner) popuWindowsView.findViewById(R.id.textSizeSpinner);
//            String[] textSize=getApplicationContext().getResources().getStringArray(R.array.textSize);
//            List<String> textSizeList= Arrays.asList(textSize);
//            textSizeSelector.setSelection(textSizeList.indexOf(String.valueOf(textPaint.getTextSize())));
            textSizeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    float textSize = Float.parseFloat((String) parent.getSelectedItem());
                    if (textSize != textPaint.getTextSize())
                        textPaint.setTextSize(textSize);
                    readerPagerAdapter.getContentController().reMeasure();
//                    readerPagerAdapter.getContentController().notifyPageChanged(contentPager.getCurrentItem());
                    readerPagerAdapter.invalidateViews();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            initBackgroundSelectedView(popuWindowsView);
            settingWindow.setContentView(popuWindowsView);
        }
        settingWindow.showAtLocation(mControlsView, Gravity.BOTTOM, 0, 0);
//        settingWindow.showAsDropDown(settingMenu);

    }

    private void setScreenLightness(final CheckBox checkBox, final SeekBar lightAdjuster) {
        lightAdjuster.setMax(255);
        lightAdjuster.setProgress(getSystemLightness());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    lightAdjuster.setProgress(getSystemLightness());
                    setLightness(lightAdjuster.getProgress());
                }
            }
        });
        //设置当跟随系统时无法拖动
        lightAdjuster.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (checkBox.isChecked())
                    return true;
                else
                    return false;
            }
        });
        lightAdjuster.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setLightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private int getSystemLightness() {
        int systemLightness = 0;
        ContentResolver contentResolver = getContentResolver();
        try {
            systemLightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //获取系统亮度当系统亮度为自动调节时，获取值为-1，此处应该怎样处理？
        if (systemLightness < 0)
            return -1 * systemLightness;
        return systemLightness;
    }

    private void setLightness(int progress) {
        WindowManager.LayoutParams windowManagerLayoutParams = getWindow().getAttributes();
        windowManagerLayoutParams.screenBrightness = (progress / 255f);
        getWindow().setAttributes(windowManagerLayoutParams);
    }


    private void initBackgroundSelectedView(View view) {
        BackgroundSelectorListener listener = new BackgroundSelectorListener();
        TextView black = (TextView) view.findViewById(R.id.black);
        TextView yellow = (TextView) view.findViewById(R.id.yellow);
        TextView white = (TextView) view.findViewById(R.id.white);
        black.setOnClickListener(listener);
        yellow.setOnClickListener(listener);
        white.setOnClickListener(listener);
    }

    private void initBroadCast() {
        showFinishedReceiver = new ShowFinishedReceiver();
        IntentFilter intentFilter = new IntentFilter("DRAW_FINISHED");
        registerReceiver(showFinishedReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        if (showFinishedReceiver != null)
            unregisterReceiver(showFinishedReceiver);
        //数据库添加位置记录
        //方法中发送广播让第一个activity 更新列表
        recordDatabaseHelper.update(ReadRecordAdapter.TABLE_NAME, textPath, null, readerPagerAdapter.getContentController().getOnShowStart());
//        ContentValues values=new ContentValues();
//        values.put("position",readerPagerAdapter.getContentController().getOnShowStart());
//        values.put("updateTime",System.currentTimeMillis());
//        SQLiteDatabase recordDB= recordDatabaseHelper.getWritableDatabase();
//        recordDB.update(ReadRecordAdapter.TABLE_NAME,values,"filepath=?",new String[]{textPath});
//        recordDB.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (settingWindow != null && settingWindow.isShowing()) {
            settingWindow.dismiss();
            return;
        }
//        recordDatabaseHelper.update(ReadRecordAdapter.TABLE_NAME,textPath,null,readerPagerAdapter.getContentController().getOnShowStart());
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        if (settingWindow != null && settingWindow.isShowing()) {
            settingWindow.dismiss();
        }
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        contentPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClicked() {
        toggle();
    }

    @Override
    public void upDate(int progress) {
        readerSeekBar.setProgress(progress);
        statusText.setText(progress / totalWords * 100 + "%");
    }

    class ShowFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

    private class BackgroundSelectorListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            contentPager.setBackground(v.getBackground());
            textPaint.setColor(((TextView) v).getPaint().getColor());
            contentPager.invalidate();
            Reader2Activity.this.hide();
        }
    }
}
