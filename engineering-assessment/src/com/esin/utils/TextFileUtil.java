package com.esin.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TextFile Utility
 *
 * @author Peilian Wang
 */
public class TextFileUtil {

    public interface RowHandler {
        public void doHandle(String row);
    }

    /**
     * read text file content and use row handle callback
     *
     * @param filename   file absolute path
     * @param charset    file encoding
     * @param rowHandler callback method
     */
    public static void read(String filename, String charset, RowHandler rowHandler) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset))) {
            String lineString = reader.readLine();
            while (lineString != null) {
                rowHandler.doHandle(lineString);
                lineString = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("FileUtil.read error.(" + filename + ")", e);
        }
    }

}
