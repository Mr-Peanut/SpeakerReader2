package com.guan.speakerreader.view.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by guans on 2017/3/6.
 */

public class RecordDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    //建表字段id ，文件名，文件路径，预览（当前位置取10个字），总字数，上次阅读位置，上次阅读时间
    private final static String CREATE_TABLE = "create table ReadRecord(_id integer primary key autoincrement, filename text,filepath text, preview text,totalWords long, position long,updateTime long,formatPath text)";

    public RecordDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }

    public RecordDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    public void update(String tableName ,String filePath,String preview,long position){
        ContentValues values=new ContentValues();
        if(preview!=null)
            values.put("preview",preview);
        values.put("position",position);
        values.put("updateTime",System.currentTimeMillis());
        SQLiteDatabase recordDB= getWritableDatabase();
        recordDB.update(tableName,values,"filepath=?",new String[]{filePath});
        recordDB.close();
        mContext.sendBroadcast(new Intent("READ_RECORD_DB_UPDATE"));
    }
    public void insert(String tableName ,String fileName,String filePath,String preview,long totalWords,long position,String formatPath){
        ContentValues values=new ContentValues();
        values.put("filepath",filePath);
        values.put("preview",preview);
        values.put("filename",fileName);
        values.put("position",position);
        values.put("formatPath",formatPath);
        values.put("totalWords",totalWords);
        values.put("updateTime",System.currentTimeMillis());

        SQLiteDatabase recordDB= getWritableDatabase();
        recordDB.insert(tableName,null,values) ;
        recordDB.close();
        mContext.sendBroadcast(new Intent("READ_RECORD_DB_UPDATE"));
    }
}
