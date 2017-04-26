package com.guan.speakerreader.view.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class TxtReader {

    /*
     * ��δ����ǽ�ȡ��ָ��λ��marked��limit���ȵ��ַ������˴���Щ���⣬�ַ�������󳤶��Ǹ�int�͵ģ����������Ϊint,������������Ǹ�long�ͣ������Ľ�
     */
    public static String readerFromText(String filePath, int marked, int limit) throws Exception {
        if (marked < 0)
            return null;
        int buffLength = 2 * 1024;
        File target = new File(filePath);
        FileInputStream inputStream = new FileInputStream(target);
        InputStreamReader bufferedReader = new InputStreamReader(inputStream, getCodeType(target));
        char[] buff = new char[buffLength];
        int times = 0;
        int left = limit;
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.skip(marked);
        int temp=0;
        while ((temp=bufferedReader.read(buff)) != -1 && left > 0) {
            if (left < buffLength) {
                stringBuilder.append(buff, 0, temp<=left?temp:left);
                break;
            }
            stringBuilder.append(buff);
            times++;
            left = limit - times * buffLength;
        }
        String result = stringBuilder.toString();
        bufferedReader.close();
        inputStream.close();
        return result;
    }
    public static String readerFromTextPre(String filePath, int marked, int limit) throws Exception {
        if (marked < 0)
            return readerFromText(filePath,0,marked+limit);
       else
           return  readerFromText(filePath,marked,limit);
    }
    public static String getCodeType(File file) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
        int p = (bin.read()) << 8 + bin.read();
        String code = null;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;

            default:
                code = "GBK";
                break;
        }
        bin.close();
        return code;
    }

    public static String getCodeType(String filePath) throws IOException {
        return getCodeType(new File(filePath));
    }

    public static int getTotalWords(String filePath) {
        InputStreamReader reader = null;
        FileInputStream inputStream = null;
        int bufferLength = 1024 * 2;
        int result = 0;
        int readTimes = -1;
        try {
            inputStream = new FileInputStream(new File(filePath));
            reader = new InputStreamReader(inputStream, getCodeType(filePath));
            char[] buffer = new char[bufferLength];
            int temp ;
            int marked = 0;
            while ((temp = reader.read(buffer)) != -1) {
                readTimes++;
                marked = temp;
            }
            result = readTimes * bufferLength + marked;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    public static int formatTxtFile(String originalPath,String resultPath ){
        return formatTxtFile(new File(originalPath),new File(resultPath));
    }
    public static int formatTxtFile(File originalFile,File resultFile ){
        String codeType ;
        int totalWords=0;
        try {
            codeType = getCodeType(originalFile);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        try (
        FileInputStream fileInputStream=new FileInputStream(originalFile) ;
        InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream,codeType);
        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
        FileOutputStream fileOutputStream=new FileOutputStream(resultFile,true);
        OutputStreamWriter outPutStreamWriter=new OutputStreamWriter(fileOutputStream,codeType);
        BufferedWriter bufferedWriter =new BufferedWriter(outPutStreamWriter)){
            String temp;
            while ((temp=bufferedReader.readLine())!=null){
                if(temp.length()==0)
                    continue;
                bufferedWriter.write(temp);
                bufferedWriter.write('\n');
                totalWords+=temp.length()+1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        Log.e("formatTotalWords", String.valueOf(totalWords));
        return totalWords;
    }
}
