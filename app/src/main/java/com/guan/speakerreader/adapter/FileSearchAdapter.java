package com.guan.speakerreader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guan.speakerreader.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by guans on 2017/5/25.
 */

public class FileSearchAdapter extends RecyclerView.Adapter<FileSearchAdapter.SearchHolder> {
    private ArrayList<File> resultList;
    private Context mContext;
    private OnItemClickedListener mItemClickedListener;

    public FileSearchAdapter(ArrayList<File> resultList, Context mContext) {
        this.resultList = resultList;
        this.mContext = mContext;
    }

    @Override
    public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchHolder(LayoutInflater.from(mContext).inflate(R.layout.fileitem_view_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(SearchHolder holder, final int position) {
        holder.fileName.setText(resultList.get(position).getName());
        holder.fileIcon.setImageResource(R.mipmap.ic_launcher);
        holder.fileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickedListener.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public void setItemClickedListener(OnItemClickedListener mItemClickedListener) {
        this.mItemClickedListener = mItemClickedListener;
    }

    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

    class SearchHolder extends RecyclerView.ViewHolder {
        LinearLayout fileItem;
        TextView fileName;
        ImageView fileIcon;

        SearchHolder(View itemView) {
            super(itemView);
            fileItem = (LinearLayout) itemView.findViewById(R.id.file_item);
            fileName = (TextView) itemView.findViewById(R.id.file_item_name);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_item_icon);
        }
    }
}
