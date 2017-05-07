package com.guan.speakerreader.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.guan.speakerreader.view.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/3/11.
 */

public class ScanFileAdapter extends RecyclerView.Adapter<ScanFileAdapter.FileHolder> {
    private Context mContext;
    private ArrayList<File> files;
    private AsyncTask<Void, File, Void> scanFileTask;
    private ProcessBarController mProcessController;
    private boolean taskOn = false;
    private FileItemOnClickedListener mFileItemOnClickedListener;
    public ScanFileAdapter(Context mContext, ProcessBarController processBarController) {
        this.mContext = mContext;
        setMProcessController(processBarController);
        files = new ArrayList<>();
        getFiles();
    }
    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }


    public AsyncTask<Void, File, Void> getScanFileTask() {
        return scanFileTask;
    }

    public boolean isTaskOn() {
        return taskOn;
    }

    private void setTaskOn(boolean taskOn) {
        this.taskOn = taskOn;
    }

    private void setMProcessController(ProcessBarController mProcessController) {
        this.mProcessController = mProcessController;
    }

    public void setFileItemOnClickedListener(FileItemOnClickedListener mFileItemOnClickedListener) {
        this.mFileItemOnClickedListener = mFileItemOnClickedListener;
    }
    public ArrayList<File> getFlies(){
        return files;
    }

    private void getFiles() {
        if (scanFileTask == null || scanFileTask.isCancelled())
            scanFileTask = new AsyncTask<Void, File, Void>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                protected Void doInBackground(Void... params) {
                    setTaskOn(true);
                    String[] rootPaths = new FileUtil(mContext).getSDcardsRootByReflect();
                    for (String rootPath : rootPaths) {
                        listChildren(new File(rootPath));
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    setTaskOn(false);
                    mProcessController.dissProcessBar();
                    ScanFileAdapter.this.notifyDataSetChanged();
                    if (files.isEmpty()) {
                        mContext.sendBroadcast(new Intent("NO TXT FILE"));
                    }
                }
                @Override
                protected void onProgressUpdate(File... values) {
                    super.onProgressUpdate(values);
                    files.add(values[0]);
                    ScanFileAdapter.this.notifyDataSetChanged();
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    mProcessController.showProcessBar();
                }

                private void listChildren(File parent) {
                    File[] files = parent.listFiles();
                    if (files != null) {
                        for (File childFile : files) {
                            if (childFile.isFile()) {
                                if (childFile.getName().contains(".txt") || childFile.getName().contains(".TXT"))
                                    publishProgress(childFile);
                                if (this.isCancelled())
                                    return;
                            } else {
                                if (this.isCancelled())
                                    return;
                                listChildren(childFile);
                            }
                        }
                    }
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    setTaskOn(false);
                    ScanFileAdapter.this.notifyDataSetChanged();
                    mProcessController.dissProcessBar();
                }

            };
        scanFileTask.execute();
    }


    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View fileItemView = LayoutInflater.from(mContext).inflate(R.layout.filelist_item, parent, false);
        return new FileHolder(fileItemView);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, final int position) {
        holder.fileName.setText(files.get(position).getName());
        holder.fileIcon.setImageResource(R.mipmap.ic_launcher);
        holder.fileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               cancelScanTask();
                mFileItemOnClickedListener.onItemClicked(files.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public interface ProcessBarController {
        void showProcessBar();
        void dissProcessBar();
    }
    public void cancelScanTask(){
        if(scanFileTask!=null&&isTaskOn()){
            scanFileTask.cancel(true);
            taskOn=false;
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
