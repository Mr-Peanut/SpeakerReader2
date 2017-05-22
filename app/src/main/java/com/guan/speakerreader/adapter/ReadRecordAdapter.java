package com.guan.speakerreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.RecordItemGroup;

import java.io.File;

/**
 * Created by guans on 2017/3/6.
 */

public class ReadRecordAdapter extends RecyclerView.Adapter<ReadRecordAdapter.MHolder> {
    public final static String TABLE_NAME = "ReadRecord";
    private Context context;
    private SQLiteOpenHelper mHelper;
    private ItemOnClickedListener mItemOnClickedListener;
    private SQLiteDatabase mDatabase;
    private Cursor recordCursor;
    private DeleteItemOnClickedListener mDeleteItemOnClickedListener;
    private ItemOnLongClickedListener mItemOnLongClickedListener;
    private int openChildPosition = -1;

    public ReadRecordAdapter(Context context, SQLiteOpenHelper mHelper) {
        this.context = context;
        this.mHelper = mHelper;
        getData();
    }

    public int getOpenChildPosition() {
        return openChildPosition;
    }

    public void setOpenChildPosition(int openChildPosition) {
        this.openChildPosition = openChildPosition;
    }

    public void setItemOnLongClickedListener(ItemOnLongClickedListener mItemOnLongClickedListener) {
        this.mItemOnLongClickedListener = mItemOnLongClickedListener;
    }

    public void setDeleteItemOnClickedListener(DeleteItemOnClickedListener mDeleteItemOnClickedListener) {
        this.mDeleteItemOnClickedListener = mDeleteItemOnClickedListener;
    }

    public Cursor getRecordCursor() {
        if (recordCursor == null)
            getData();
        return recordCursor;
    }

    public void setItemOnClickedListener(ItemOnClickedListener mItemOnClickedListener) {
        this.mItemOnClickedListener = mItemOnClickedListener;
    }

    private void getData() {
        if (mDatabase == null)
            mDatabase = mHelper.getReadableDatabase();
        if (recordCursor == null)
            recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "updateTime DESC");
//        recordCursor.registerContentObserver(new ContentObserver(new Handler(context.getMainLooper())) {
//            @Override
//            public void onChange(boolean selfChange) {
//                super.onChange(selfChange);
//            }
//        });
        recordCursor.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                recordCursor.close();
                recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "updateTime DESC");
                recordCursor.registerDataSetObserver(this);
            }
        });
    }

    @Override
    public MHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MHolder(LayoutInflater.from(context).inflate(R.layout.readrecorditem_view_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(MHolder holder, final int position) {
        TextView itemContent = holder.item;
        final TextView deleteItemText = holder.deleteItem;
        recordCursor.moveToPosition(recordCursor.getCount() - position - 1);
        //记录的显示方法
        itemContent.setText(recordCursor.getInt(recordCursor.getColumnIndex("_id")) + " " + recordCursor.getString(recordCursor.getColumnIndex("filename")) + " " + recordCursor.getString(recordCursor.getColumnIndex("preview")));
        itemContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteItemText.getParent() != null) {
                    if (((RecordItemGroup) deleteItemText.getParent()).isDrawerOpen) {
                        ((RecordItemGroup) deleteItemText.getParent()).backTo0();
                        return;
                    }
                }
                mItemOnClickedListener.onRecordItemClick(position);
            }
        });
        itemContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mItemOnLongClickedListener.onItemLongClicked(position, v);
            }
        });
        deleteItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadRecordAdapter.this.deleteDataItem(position);
                if (deleteItemText.getParent() != null) {
                    ((RecordItemGroup) deleteItemText.getParent()).backTo0();
                }
//                mDeleteItemOnClickedListener.onDeleteClick(position);
            }
        });
    }

    public void deleteDataItem(int position) {
        recordCursor.moveToPosition(recordCursor.getCount() - position - 1);
        String formatString = recordCursor.getString(recordCursor.getColumnIndex("formatPath"));
        if (formatString != null) {
            File formatFile = new File(formatString);
            formatFile.delete();
            mDatabase.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(recordCursor.getLong(recordCursor.getColumnIndex("_id")))});
            notifyDataChanged();
        }
    }

    public void cleanAll() {
        //清除所有的format文件
        mDatabase.delete(TABLE_NAME, null, null);
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        recordCursor.close();
        recordCursor = mDatabase.query(TABLE_NAME, null, null, null, null, null, "updateTime DESC");
        ReadRecordAdapter.this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordCursor.getCount();
    }

    public interface ItemOnClickedListener {
        void onRecordItemClick(int position);
    }

    interface DeleteItemOnClickedListener {
        void onDeleteClick(int position);
    }

    public interface ItemOnLongClickedListener {
        boolean onItemLongClicked(int position, View view);
    }

    class MHolder extends RecyclerView.ViewHolder {
        TextView item;
        TextView deleteItem;

        MHolder(View itemView) {
            super(itemView);
            item = (TextView) itemView.findViewById(R.id.recordItem);
            deleteItem = (TextView) itemView.findViewById(R.id.deleteItem);
        }
    }
}
