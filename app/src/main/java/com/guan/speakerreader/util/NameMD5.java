package com.guan.speakerreader.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by guans on 2017/5/26.
 */

public class NameMD5 {
    public static String getNameMD5(String fileName, String filePath) {
        String str = fileName + filePath;
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md5 == null)
            return null;
        byte[] bs = md5.digest(str.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (byte x : bs) {
            if ((x & 0xff) >> 4 == 0) {
                sb.append("0").append(Integer.toHexString(x & 0xff));
            } else {
                sb.append(Integer.toHexString(x & 0xff));
            }
        }
        return sb.toString();
    }
}
