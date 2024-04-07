package com.esin.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV Utility
 *
 * @author Peilian Wang
 */
public class CsvFileUtil {

    /**
     * read csv file content
     *
     * @param filename file absolute path
     * @param charset  file encoding
     * @return List<List < String>> csv row data list
     */
    public static List<List<String>> read(String filename, String charset) {

        // read all text file content
        StringBuilder sbContent = new StringBuilder();
        TextFileUtil.read(filename, charset, row -> {
            sbContent.append(row);
            sbContent.append("\n");
        });

        // parse content to row data list
        List<List<String>> dataList = new ArrayList<>();
        List<String> rowCellList = new ArrayList<>();
        StringBuilder sbCell = new StringBuilder();

        // If cell text contains special character as [,"\n], the cell text must be bracketed by ".
        boolean standardCellText = true;
        for (int i = 0; i < sbContent.length(); i++) {
            final char c = sbContent.charAt(i);
            if (standardCellText) {
                if (c == ',') {
                    rowCellList.add(sbCell.toString());
                    sbCell.setLength(0);
                } else if (c == '\n') {
                    rowCellList.add(sbCell.toString());
                    sbCell.setLength(0);
                    dataList.add(rowCellList);
                    rowCellList = new ArrayList<>();
                } else if (sbCell.length() == 0) {
                    if (c == '"') {
                        standardCellText = false;
                    } else {
                        sbCell.append(c);
                    }
                } else {
                    sbCell.append(c);
                }
            } else {
                if (c == '"') {
                    // the next char is must " or , or \n
                    // "  : append cell
                    // ,  : add cell
                    // \n : add cell and add row
                    if (sbContent.charAt(i + 1) == '"') {
                        sbCell.append("\"");
                    } else {
                        rowCellList.add(sbCell.toString());
                        sbCell.setLength(0);
                        if (sbContent.charAt(i + 1) == '\n') {
                            dataList.add(rowCellList);
                        }
                        standardCellText = true;
                    }
                    i++;
                } else {
                    sbCell.append(c);
                }
            }
        }
        return dataList;
    }
}
