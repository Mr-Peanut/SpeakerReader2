package com.guan.speakerreader.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shiqian.guan on 2017/5/8.
 */

public class StringSearcher {
    private ArrayList<Integer> resultList;
    private HashMap<Integer, String> resultMap;
    private int totalWords;
    private int checkLength;
    private int resultListLengthLimit;

    public ArrayList<Integer> getResultList() {
        return resultList;
    }

    public void setResultList(ArrayList<Integer> resultList) {
        this.resultList = resultList;
    }

    public HashMap<Integer, String> getResultMap() {
        return resultMap;
    }

    public void setResultMap(HashMap<Integer, String> resultMap) {
        this.resultMap = resultMap;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public void setCheckLength(int checkLength) {
        this.checkLength = checkLength;
    }

    public void setResultListLengthLimit(int resultListLengthLimit) {
        this.resultListLengthLimit = resultListLengthLimit;
    }

    public void searchTarget(String filePath, String targetString) throws Exception {
        int position;
        int totalChecked = 0;
        int targetLength = targetString.length();
        while (totalChecked < totalWords) {
            String toCheck = TxtTaker.readerFromText(filePath, totalChecked, checkLength + targetLength - 1);
            if (toCheck == null)
                return;
            int startPosition = 0;
            while (startPosition < checkLength + targetLength - 1) {
                position = toCheck.indexOf(targetString, startPosition);
                if (position != -1) {
                    resultList.add(totalChecked + position);
                    resultMap.put(totalChecked + position, toCheck.substring(Math.max(position - 10, 0), Math.min(position + targetLength + 10, toCheck.length())));
                    startPosition = position + targetLength;
                    if (resultList.size() >= resultListLengthLimit)
                        return;
                }
            }
            totalChecked += checkLength;
        }
    }
}
