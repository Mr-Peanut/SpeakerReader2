package com.guan.speakerreader.view.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guans on 2017/3/11.
 */

public class FileUtil {

    private Context mcontext;

    public FileUtil(Context context) {
        this.mcontext = context;
    }

    public File getFile(String fileType) {

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String[] getSDcardsRootByReflect() {
        List<String> filePaths = new ArrayList<>();
        try {
            StorageManager storageManager = (StorageManager) mcontext.getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            Object result = method.invoke(storageManager);
            if (result != null && result instanceof String[]) {
                String[] paths = (String[]) result;
                StatFs statFs;
                for (String path : paths) {
                    if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                        statFs = new StatFs(path);
                        if (statFs.getBlockCountLong() * statFs.getBlockSizeLong() != 0) {
                            filePaths.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            filePaths.add(path);
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public String[] getSDcardsRootByRuntime() {
        List<String> filePaths = new ArrayList<>();
        try {
            Runtime runtime = Runtime.getRuntime();
            InputStream inputStream = runtime.exec("mount").getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StatFs statFs;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("storage")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        statFs = new StatFs(path);
                        if (statFs.getBlockCountLong() * statFs.getBlockSizeLong() != 0) {
                            filePaths.add(path);
                        }
                    }
                }
            }
            inputStreamReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            filePaths.add(path);
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public String[] getSDcardsRootByEnviroment() {
        List<String> filePaths = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePaths.add(path);
        return filePaths.toArray(new String[filePaths.size()]);
    }


}
