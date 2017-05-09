package com.guan.speakerreader.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guan.speakerreader.bean.ContentSearchResultValuePairs;

import java.util.ArrayList;

/**
 * Created by guans on 2017/5/7.
 */

public class ContentSearchResultAdapter extends BaseAdapter {
    private ArrayList<ContentSearchResultValuePairs> resultValuePairsArrayList;
    private Context mContext;
    public ContentSearchResultAdapter(ArrayList<ContentSearchResultValuePairs> resultValuePairsArrayList, Context mContext) {
        this.resultValuePairsArrayList = resultValuePairsArrayList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return resultValuePairsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultValuePairsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder fileHolder;
        if (convertView == null) {
            fileHolder = new ViewHolder();
            convertView = new TextView(mContext);
            convertView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            convertView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            fileHolder.resultPreview = (TextView) convertView;
            convertView.setTag(fileHolder);
        } else {
            fileHolder = (ViewHolder) convertView.getTag();
            fileHolder.resultPreview.setText(resultValuePairsArrayList.get(position - 1).getPreview());
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView resultPreview;
    }
}
