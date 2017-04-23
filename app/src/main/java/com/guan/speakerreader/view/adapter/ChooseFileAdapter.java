package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guan.speakerreader.R;
import com.guan.speakerreader.view.util.PathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/3/12.
 */
public class ChooseFileAdapter extends RecyclerView.Adapter<ChooseFileAdapter.FileHolder> {
    private boolean isRoot = true;
    private List<PathUtil.VolumeInfo> volumeInfoList;
    private File parentFile;
    private File[] files;
    private Context mContext;
    private FileItemOnClickedListener mFileItemOnClickListener;
    private ArrayList<String> rootPaths;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ChooseFileAdapter(Context mContext) {
        this.mContext = mContext;
        volumeInfoList = PathUtil.getVolumeInfoList(mContext);
        rootPaths = new ArrayList<String>();
        for (PathUtil.VolumeInfo volumeInfo : volumeInfoList) {
            rootPaths.add(volumeInfo.getVolumePath());
        }
    }

    public void setFileItemOnClickedListener(FileItemOnClickedListener mFileItemOnClickedListener) {
        this.mFileItemOnClickListener = mFileItemOnClickedListener;
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View fileItemView = LayoutInflater.from(mContext).inflate(R.layout.filelist_item, parent, false);
        return new FileHolder(fileItemView);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, final int position) {
        if (!isRoot) {
            setFilePath(holder, position);
        } else {
            setRootPaths(holder, position);

        }
    }

    private void setRootPaths(FileHolder fileHolder, final int position) {
        fileHolder.fileName.setText(volumeInfoList.get(position).getVolumeName());
        fileHolder.fileIcon.setImageResource(R.mipmap.ic_launcher);
        fileHolder.fileItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                parentFile = new File(volumeInfoList.get(position).getVolumePath());
                files = parentFile.listFiles();
                isRoot = false;
                notifyDataSetChanged();
            }
        });

    }

    private void setFilePath(FileHolder fileHolder, final int position) {
        fileHolder.fileName.setText(files[position].getName());
        fileHolder.fileIcon.setImageResource(R.mipmap.ic_launcher);
        fileHolder.fileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (files[position].isDirectory()) {
                    parentFile = files[position];
                    files = parentFile.listFiles();
                    notifyDataSetChanged();
                } else {
                    mFileItemOnClickListener.onItemClicked(files[position]);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (!isRoot) {
            if (files == null) {
                //空文件夹
                return 0;
            } else {
                return files.length;
            }

        } else {
            return volumeInfoList.size();
        }
    }

    public void backToParent() {
        if (isRoot) {
            //直接是跟目录，不做反应，或者其他动作
            return;
        } else {
            //对返回的是否是根目录进行判断
            if (rootPaths.contains(parentFile.getAbsolutePath())) {
                isRoot = true;
                notifyDataSetChanged();
            } else {
                isRoot = false;
                parentFile = parentFile.getParentFile();
                files = parentFile.listFiles();
                notifyDataSetChanged();
            }
        }
    }

    public interface FileItemOnClickedListener {
        void onItemClicked(File file);
    }

    class FileHolder extends RecyclerView.ViewHolder {
        LinearLayout fileItem;
        TextView fileName;
        ImageView fileIcon;

        public FileHolder(View itemView) {
            super(itemView);
            fileItem = (LinearLayout) itemView.findViewById(R.id.fileItem);
            fileName = (TextView) itemView.findViewById(R.id.fileItemName);
            fileIcon = (ImageView) itemView.findViewById(R.id.fileItemIcon);
        }
    }
}
