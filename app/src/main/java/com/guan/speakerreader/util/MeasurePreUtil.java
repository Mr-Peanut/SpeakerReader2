package com.guan.speakerreader.util;

import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by guans on 2017/3/24.
 */
/*
注意字数影响，由于用回车键进行分段：
1、故每一段的长度都会少一个回车键
2、当一行第一个字符刚好是回车键，此时一行就一个字符，但是往前测量时会忽略
总结来说就是测量出来的字符会比实际打出来的少
解决方案：
判断最后一个字节是不是回车键，不是的话每一段测量前在最后的位置加一个回车键，是的话
 */

public class MeasurePreUtil {
    private Paint mPaint;
    private float showHeight;
    private float showWidth;
    //最后一段是不是换行键
    private boolean isLastEnter;

    public MeasurePreUtil(Paint mPaint, float showHeight, float showWidth) {
        this.mPaint = mPaint;
        this.showHeight = showHeight;
        this.showWidth = showWidth;
    }

    public void setmPaint(Paint mPaint) {
        this.mPaint = mPaint;
    }

    public void setShowHeight(float showHeight) {
        this.showHeight = showHeight;
    }

    public void setShowWidth(float showWidth) {
        this.showWidth = showWidth;
    }

    String prePageContentLength(String original) {
        float lineHeight = mPaint.descent() - mPaint.ascent();
        if (lineHeight > showHeight)
            return null;
        int pageLineContain = (int) (Math.floor(showHeight / lineHeight));
        int wordCount = 0;
        String[] paragraphs = original.split("\n");
        //从后往前一段一段的遍历
        MeasureInfo measureInfo;
        isLastEnter = original.charAt(original.length() - 1) == '\n';
        if (isLastEnter) {
            measureInfo = findRightLine(paragraphs, paragraphs.length - 1, pageLineContain, true, true);
        } else {
            measureInfo = findRightLine(paragraphs, paragraphs.length - 1, pageLineContain, true, false);
        }

        //从最后一段开始往前一直到记录的那段
        for (int i = 1; measureInfo.paragraphNumber <= paragraphs.length - i; i++) {
            //判断是不是选定的段，是的话要加到指定行，不是的话加全段
            if (measureInfo.paragraphNumber == paragraphs.length - i) {
                ArrayList<Integer> measureLines;
                if (i == 1 && isLastEnter) {
                    measureLines = measureLastParagraph(paragraphs[measureInfo.paragraphNumber]);
                } else {
                    measureLines = measureParagraph(paragraphs[measureInfo.paragraphNumber]);
                }
                int size = measureLines.size();
                for (int j = 1; measureInfo.lineMarked <= size - j; j++) {
                    //出现了-1的情况
                    wordCount += measureLines.get(size - j);
                }
            } else {
                //回车键,但是如果最后一个字符刚好是回车键呢
                if (i == 1 && !isLastEnter) {
                    wordCount += paragraphs[paragraphs.length - i].length();
                } else {
                    wordCount += paragraphs[paragraphs.length - i].length() + 1;
                    //
                }
            }
        }
        return original.substring(original.length() - wordCount);
    }

    //对一段文字进行排版测量，记录每行的文字数
    //'对于一段当中只有一个回车键此处还要完善判断
    private ArrayList<Integer> measureParagraph(String paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("Paragraph cannot be null");
        }
        ArrayList<Integer> lineRecord = new ArrayList<>();
        //当两个回车键在一起时，一段长度为0此时加上一个回车键突然想到了个问题，每段前都有一个回车键的字符是不是漏掉了？
//        if (paragraph.length() == 0) {
//            lineRecord.add(1);
//            Log.e("空段", "空段");
//            return lineRecord;
//        }
        paragraph = paragraph + "\n";
        int lineWordCount = 0;
        float totalWidth = 0;
        int wordCount = 0;
        float wordSpace;
        char[] buffer = new char[1];
//        StringBuffer stringBuffer = new StringBuffer();
        //最后一行单独测量
        while (wordCount < paragraph.length()) {
            while (totalWidth < showWidth && wordCount < paragraph.length()) {
                buffer[0] = paragraph.charAt(wordCount);
                wordSpace = mPaint.measureText(buffer, 0, 1);
                if (totalWidth + wordSpace > showWidth) {
                    if (buffer[0] == '\n') {
                        wordCount++;
                        lineWordCount++;
                        break;
                    }
                    break;
                }

                wordCount++;
                lineWordCount++;
//                stringBuffer.append(buffer);
                totalWidth += wordSpace;
            }
            lineRecord.add(lineWordCount);
//            Log.e(stringBuffer.toString(), String.valueOf(lineWordCount) + " buffer " + String.valueOf(stringBuffer.length()));
            totalWidth = 0;
            lineWordCount = 0;
//            stringBuffer.delete(0, stringBuffer.length());
        }
        return lineRecord;
    }

    private ArrayList<Integer> measureLastParagraph(String paragraph) {
        if (paragraph == null) {
            throw new IllegalArgumentException("Paragraph cannot be null");
        }
        ArrayList<Integer> lineRecord = new ArrayList<>();
        //当两个回车键在一起时，一段长度为0此时加上一个回车键突然想到了个问题，每段前都有一个回车键的字符是不是漏掉了？
        if (paragraph.length() == 0) {
            lineRecord.add(1);
            Log.e("空段", "空段");
            return lineRecord;
        }
        int lineWordCount = 0;
        float totalWidth = 0;
        int wordCount = 0;
        float wordSpace;
        char[] buffer = new char[1];
//        StringBuffer stringBuffer = new StringBuffer();
        //最后一行单独测量
        while (wordCount < paragraph.length()) {
            while (totalWidth < showWidth && wordCount < paragraph.length()) {
                buffer[0] = paragraph.charAt(wordCount);
                wordSpace = mPaint.measureText(buffer, 0, 1);
                if (totalWidth + wordSpace > showWidth) {
                    if (buffer[0] == '\n') {
                        wordCount++;
                        lineWordCount++;
                        break;
                    }
                    break;
                }

                wordCount++;
                lineWordCount++;
//                stringBuffer.append(buffer);
                totalWidth += wordSpace;
            }
            lineRecord.add(lineWordCount);
//            Log.e(stringBuffer.toString(), String.valueOf(lineWordCount) + " buffer " + String.valueOf(stringBuffer.length()));
            totalWidth = 0;
            lineWordCount = 0;
//            stringBuffer.delete(0, stringBuffer.length());
        }
        return lineRecord;
    }

    /*
      缺少总字数不足判断
      缺少本行为空或者只有一个回车键的判断
      这个方法可以进一步优化，把最后一段和前面的拆开
     */
    private MeasureInfo findRightLine(String[] paragraphs, int paragraphNumber, int containLines, boolean isLastPara, boolean isLastEnter) {
        ArrayList<Integer> measureLines;
        if (isLastPara && isLastEnter) {
            measureLines = measureParagraph(paragraphs[paragraphNumber]);
        } else {
            measureLines = measureLastParagraph(paragraphs[paragraphNumber]);
        }
        int leftLines = containLines - measureLines.size();
        //字数不够
        if (leftLines <= 0 || paragraphNumber == 0) {
            return new MeasureInfo(paragraphNumber, measureLines.size() - containLines >= 0 ? measureLines.size() - containLines : 0);
        } else {
            return findRightLine(paragraphs, paragraphNumber - 1, leftLines, false, false);
        }
    }

    private class MeasureInfo {
        int paragraphNumber;
        int lineMarked;

        MeasureInfo(int paragraphNumber, int lineMarked) {
            this.paragraphNumber = paragraphNumber;
            this.lineMarked = lineMarked;
        }
    }
}
