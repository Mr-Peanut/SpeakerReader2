package com.guan.speakerreader.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathUtil {
    @TargetApi(24)
    public static String[] getSDcardPath(Context context) {
        List<String> pathList = new ArrayList<String>();
        try {
            Method getPath = getPathMethod();
            StorageVolume[] volumes = getStorageVolumes(context);
            for (StorageVolume volume : volumes) {
                if (volume.getState().equals("mounted")) {
                    String volumePath = (String) getPath.invoke(volume);
                    pathList.add(volumePath);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            String volumePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            pathList.add(volumePath);
        }

        return pathList.toArray(new String[pathList.size()]);
    }

    @TargetApi(24)
    public static HashMap<String, String> getSDcardStatu(Context context) {
        HashMap<String, String> volumeMap = new HashMap<String, String>();
        try {
            Method getPath = getPathMethod();
            StorageVolume[] volumes = getStorageVolumes(context);
            for (StorageVolume volume : volumes) {
                if (volume.getState().equals("mounted")) {
                    String volumePath = (String) getPath.invoke(volume);
                    System.err.println(volume.getState() + " " + volume.toString());
                    if (!volume.isRemovable())
                        volumeMap.put("内部储存" + volumePath.substring(volumePath.lastIndexOf(File.separator) + 1), volumePath);
                    else {
                        volumeMap.put("外置储存" + volumePath.substring(volumePath.lastIndexOf(File.separator) + 1), volumePath);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            File storageDirectory = Environment.getExternalStorageDirectory();
            volumeMap.put(storageDirectory.getName(), storageDirectory.getAbsolutePath());
        }

        return volumeMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<VolumeInfo> getVolumeInfoList(Context context) {
        List<VolumeInfo> volumeInfoList = new ArrayList<>();
        try {
            StorageVolume[] volumes = getStorageVolumes(context);
            Method getPath = getPathMethod();
            for (StorageVolume volume : volumes) {
                if (volume.getState().equals("mounted")) {
                    VolumeInfo volumeInfo = new VolumeInfo();
                    if (!volume.isRemovable()) {
                        volumeInfo.setVolumeType("本地储存");
                    } else {
                        volumeInfo.setVolumeType("外置SD卡");
                    }
                    String volumePath = (String) getPath.invoke(volume);
                    volumeInfo.setVolumePath(volumePath);
                    volumeInfo.setVolumeName(volumePath.substring(volumePath.lastIndexOf(File.separator) + 1));
                    volumeInfoList.add(volumeInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            File rootFile = Environment.getExternalStorageDirectory();
            VolumeInfo volumeInfo = new VolumeInfo();
            volumeInfo.setVolumeName(rootFile.getName());
            volumeInfo.setVolumePath(rootFile.getAbsolutePath());
            volumeInfo.setVolumeType("默认储存");
        }
        return volumeInfoList;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static StorageVolume[] getStorageVolumes(Context context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        StorageManager storageManager = (StorageManager) context.getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
        Method getVolumeList = StorageManager.class.getMethod("getVolumeList");
        getVolumeList.setAccessible(true);
        Object volumeResulet = getVolumeList.invoke(storageManager);
        StorageVolume[] volumes = (StorageVolume[]) volumeResulet;
        return volumes;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Method getPathMethod() throws NoSuchMethodException {
        Method getPath = StorageVolume.class.getMethod("getPath");
        getPath.setAccessible(true);
        return getPath;
    }

    public static class VolumeInfo {
        private String volumeName;
        private String volumePath;
        private String volumeType;

        public String getVolumeName() {
            return volumeName;
        }

        public void setVolumeName(String volumeName) {
            this.volumeName = volumeName;
        }

        public String getVolumePath() {
            return volumePath;
        }

        public void setVolumePath(String volumePath) {
            this.volumePath = volumePath;
        }

        public String getVolumeType() {
            return volumeType;
        }

        public void setVolumeType(String volumeType) {
            this.volumeType = volumeType;
        }
    }
}
