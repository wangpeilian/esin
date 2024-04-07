package com.esin.base.utility;

import com.esin.base.exception.SystemException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class CsvUtil implements Iterable<CsvUtil.CSVLine> {
    private static Logger logger = LoggerFactory.getLogger(CsvUtil.class);
    private final List<CSVLine> csvData = Collections.synchronizedList(new ArrayList<CSVLine>());
    private boolean isTrim = true;
    private static char seperator = ',';
    private static char equote = '"';

    /**
     * set the equote and seperator
     *
     * @param seperator
     * @param equote
     */
    public static void setConfig(char seperator, char equote) {
        CsvUtil.seperator = seperator;
        CsvUtil.equote = equote;
    }

    /**
     * constructor with no args
     */
    public CsvUtil() {
    }

    /**
     * constructor with filename
     *
     * @param filename
     */
    public CsvUtil(String filename) {
        read(filename);
    }

    /**
     * constructor with in
     *
     * @param in
     */
    public CsvUtil(InputStream in) {
        try {
            readData(new InputStreamReader(in, FileUtil.GBK));
        } catch (UnsupportedEncodingException e) {
            throw new SystemException("CsvUtil.readData", e);
        }
    }

    public void read(String filename) {
        try {
            InputStream in = new FileInputStream(filename);
            readData(new InputStreamReader(in, FileUtil.GBK));
        } catch (FileNotFoundException e) {
            throw new SystemException("CsvUtil.read", e);
        } catch (UnsupportedEncodingException e) {
            throw new SystemException("CsvUtil.read", e);
        }
    }

    public void sort(Comparator<CSVLine> comparator) {
        Collections.sort(csvData, comparator);
    }

    public int getLineSize() {
        return csvData.size();
    }

    public String getLineString(int line) {
        return getLineData(line).getLineString();
    }

    public CSVLine getLineData(int line) {
        return (CSVLine) csvData.get(line);
    }

    public void addLine(String lineString) {
        csvData.add(readLine(lineString));
    }

    public void addLine(CSVLine lineData) {
        csvData.add(lineData);
    }

    public void addLine(int line, String lineString) {
        csvData.add(line, readLine(lineString));
    }

    public void addLine(int line, CSVLine lineData) {
        csvData.add(line, lineData);
    }

    public void setLine(int line, String lineString) {
        csvData.set(line, readLine(lineString));
    }

    public void setLine(int line, CSVLine lineData) {
        csvData.set(line, lineData);
    }

    public void removeLine(int line) {
        csvData.remove(line);
    }

    /**
     * write file
     *
     * @param filename
     */
    public void saveFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            OutputStreamWriter osw = new OutputStreamWriter(fos, FileUtil.UTF8);
            fos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            for (int i = 0, lines = getLineSize(); i < lines; i++) {
                osw.write(getLineString(i));
                osw.write("\r\n");
            }
            osw.flush();
            fos.flush();
            fos.getFD().sync();
            osw.close();
            fos.close();
        } catch (IOException e) {
            throw new SystemException("CsvUtil.saveFile", e);
        }
    }

    /**
     * copy the csvHandler's value
     *
     * @return Object
     */
    public Object clone() {
        CsvUtil csvUtil = new CsvUtil();
        for (int i = 0, lines = getLineSize(); i < lines; i++) {
            csvUtil.addLine((CSVLine) getLineData(i).clone());
        }
        return csvUtil;
    }

    /**
     * read the data of file
     *
     * @param in
     */
    private void readData(Reader in) {
        BufferedReader reader = new BufferedReader(in);
        try {
            String lineString = reader.readLine();
            while (lineString != null) {
                lineString = lineString.replace((char) 65279, '\0');
                csvData.add(readLine(lineString));
                lineString = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            throw new SystemException("CsvUtil.readData", e);
        }
    }

    /**
     * change the line's string to csvline
     *
     * @param lineString
     * @return CSVLine
     */
    private CSVLine readLine(String lineString) {
        CSVLine lineData = new CSVLine();
        int curIndex = 0;
        int nextIndex = nextComma(lineString, curIndex);
        while (nextIndex <= lineString.length()) {
            String item = nextToken(lineString, curIndex, nextIndex);
            if (isTrim) {
                item = item.trim();
            }
            lineData.addItem(item);
            curIndex = nextIndex + 1;
            nextIndex = nextComma(lineString, curIndex);
        }
        return lineData;
    }

    /**
     * get the string's comma location from current index
     *
     * @param lineString
     * @param curIndex
     * @return int : next comma index
     */
    private int nextComma(String lineString, int curIndex) {
        boolean inquote = false;
        int index = curIndex;
        while (index < lineString.length()) {
            char ch = lineString.charAt(index);
            if (!inquote && ch == seperator) {
                break;
            } else if (equote == ch) {
                inquote = !inquote;
            }
            index++;
        }
        return index;
    }

    /**
     * deal with the quote char
     *
     * @param lineString
     * @param curIndex
     * @param nextIndex
     * @return String : get item
     */
    private String nextToken(String lineString, int curIndex, int nextIndex) {
        if (String.valueOf(equote + Utility.EMPTY + equote).equals(
                lineString.substring(curIndex, nextIndex))) {
            return Utility.EMPTY;
        }
        StringBuffer item = new StringBuffer();
        while (curIndex < nextIndex) {
            char ch = lineString.charAt(curIndex++);
            if (ch == equote) {
                if ((curIndex < nextIndex) &&
                        (lineString.charAt(curIndex) == equote)) {
                    item.append(ch);
                    curIndex++;
                }
            } else {
                item.append(ch);
            }
        }
        return new String(item);
    }

    public Iterator<CSVLine> iterator() {
        return csvData.iterator();
    }

    public static class CSVLine implements Iterable<String>, Cloneable {
        private final List<String> lineData;

        /**
         * constructor with no args
         */
        public CSVLine() {
            this.lineData = new ArrayList<String>();
        }

        /**
         * constructor with list
         *
         * @param list
         */
        public CSVLine(List<String> list) {
            this.lineData = list;
        }

        /**
         * copy the csvline's value
         *
         * @return Object
         */
        public Object clone() {
            return new CSVLine(new ArrayList<String>(lineData));
        }

        public String getItem(int item) {
            return (String) lineData.get(item);
        }

        public void setItem(int item, String value) {
            lineData.set(item, value);
        }

        public void addItem(String value) {
            lineData.add(value);
        }

        public void addItem(int item, String value) {
            lineData.add(item, value);
        }

        public void deleteItem(int item) {
            lineData.remove(item);
        }

        public int getItemCount() {
            return lineData.size();
        }

        public List getCsvLine() {
            return lineData;
        }

        /**
         * get string of file's line
         *
         * @return String
         */
        public String getLineString() {
            StringBuffer sb = new StringBuffer();
            for (int i = 0, items = lineData.size(); i < items; i++) {
                sb.append(enquote(getItem(i)));
                if (items - 1 != i) {
                    sb.append(seperator);
                }
            }
            return new String(sb);
        }

        /**
         * add quote for item's quote
         *
         * @param item
         * @return String
         */
        private String enquote(String item) {
            if (item.length() == 0) {
                return item;
            }
            if (item.indexOf(equote) < 0 && item.indexOf(seperator) < 0) {
                return item;
            }
            StringBuffer sb = new StringBuffer(item.length() * 2 + 2);
            sb.append(equote);
            for (int ind = 0; ind < item.length(); ind++) {
                char ch = item.charAt(ind);
                if (equote == ch) {
                    sb.append(equote + equote);
                } else {
                    sb.append(ch);
                }
            }
            sb.append(equote);
            return new String(sb);
        }

        public Iterator<String> iterator() {
            return lineData.iterator();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void createCSVFile(List exportData, LinkedHashMap headMap, OutputStream out) {
        BufferedWriter csvFileOutputStream = null;
        try {
            // GBK使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(out, FileUtil.GBK), 1024);
            // 写入文件头部
            for (Iterator propertyIterator = headMap.entrySet().iterator(); propertyIterator.hasNext(); ) {
                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                csvFileOutputStream.write("\"" + propertyEntry.getValue().toString() + "\"");
                if (propertyIterator.hasNext()) {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.newLine();
            // 写入文件内容
            for (Iterator iterator = exportData.iterator(); iterator.hasNext(); ) {
                Object row = iterator.next();
                for (Iterator propertyIterator = headMap.entrySet().iterator(); propertyIterator.hasNext(); ) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    //值为空跳到下一个单元格
                    if (BeanUtils.getProperty(row, propertyEntry.getKey().toString()) == null) {
                        csvFileOutputStream.write("\t");
                    } else {
                        csvFileOutputStream.write("\""
                                + BeanUtils.getProperty(row, propertyEntry.getKey().toString()).toString() + "\"");
                    }
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                if (iterator.hasNext()) {
                    csvFileOutputStream.newLine();
                }
            }
            csvFileOutputStream.flush();
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            try {
                csvFileOutputStream.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
    }
}
