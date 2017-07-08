package com.guan.speakerreader.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by guans on 2017/7/8.
 */

public class RecordListView extends RecyclerView {
    private RecordItemGroup openedItem;

    public RecordListView(Context context) {
        super(context);
    }

    public RecordListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecordItemGroup getOpenedItem() {
        return openedItem;
    }

    public void setOpenedItem(RecordItemGroup openedItem) {
        this.openedItem = openedItem;
    }

    public void backTo0() {
        if (openedItem != null) {
            openedItem.backTo0();
        }
    }


}
