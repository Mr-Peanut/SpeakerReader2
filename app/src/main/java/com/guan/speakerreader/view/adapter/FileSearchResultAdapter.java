package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guan.speakerreader.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by guans on 2017/5/7.
 */

public class FileSearchResultAdapter extends BaseAdapter{
    private ArrayList<File> resultFiles;
    private Context mContext;

    public FileSearchResultAdapter(ArrayList<File> resultFiles, Context mContext) {
        this.resultFiles = resultFiles;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return resultFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return resultFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder fileHolder;
        if(convertView==null ){
            fileHolder=new ViewHolder();
            convertView=LayoutInflater.from(mContext).inflate(R.layout.filelist_item,null);
            fileHolder.fileItemIcon= (ImageView) convertView.findViewById(R.id.fileItemIcon);
            fileHolder.fileItemName= (TextView) convertView.findViewById(R.id.fileItemName);
            convertView.setTag(fileHolder);
        }else {
            fileHolder= (ViewHolder) convertView.getTag();
            fileHolder.fileItemIcon.setImageResource(R.mipmap.ic_launcher_round);
            fileHolder.fileItemName.setText(resultFiles.get(position).getName());

        }

        return convertView;
    }
    static class ViewHolder{
        ImageView fileItemIcon;
        TextView fileItemName;
    }

}
