package com.esin.base.utility;

import com.esin.base.executor.IExecutorAAA;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
    private Workbook wb;
    private Sheet sheet;
    private Row row;
    private Map<String, CellStyleBean> cellMap = new HashMap<>();
    private Map<String, CellStyle> styleMap = new HashMap<>();

    public ExcelUtil(boolean isExcel2003or2007) {
        if (isExcel2003or2007) {
            wb = new HSSFWorkbook();
        } else {
            wb = new XSSFWorkbook();
        }
    }

    public ExcelUtil(String filename) {
        if (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx")) {
            try {
                wb = WorkbookFactory.create(new FileInputStream(filename));
            } catch (Exception e) {
                if ("Your InputStream was neither an OLE2 stream, nor an OOXML stream".equals(e.getLocalizedMessage())) {
                    wb = new HSSFWorkbook();
                    fillByTextFile(filename, !filename.endsWith("csv"));
                } else {
                    throw new RuntimeException("Excel文件读取错误", e);
                }
            }
        } else {
            wb = new HSSFWorkbook();
            fillByTextFile(filename, !filename.endsWith("csv"));
        }
    }

    private void fillByTextFile(String filename, boolean isTextOrCsvFile) {
        if (isTextOrCsvFile) {
            CsvUtil.setConfig('\t', '\'');
        } else {
            CsvUtil.setConfig(',', '"');
        }
        CsvUtil csvUtil = new CsvUtil(filename);
        if (csvUtil.getLineSize() != 0) {
            setSelectSheet(0);
            int row = 0;
            for (CsvUtil.CSVLine csvLine : csvUtil) {
                setSelectRow(row++);
                int column = 0;
                for (String value : csvLine) {
                    setCellValue(column++, value.replace("=", "").replace("\"", ""));
                }
            }
        }
    }

    public void saveFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            write(fos);
//            fos.getFD().sync();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("ExcelUtil.saveFile", e);
        }
    }

    public void write(OutputStream os) {
        try {
            applyStyle();
            wb.write(os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("ExcelUtil.saveFile", e);
        }
    }

    public void setSelectSheet(String sheet) {
        this.sheet = wb.getSheet(sheet);
        if (this.sheet == null) {
            this.sheet = wb.createSheet(sheet);
        }
    }

    public void setSelectSheet(int sheet) {
        if (getSheetCount() <= sheet) {
            this.sheet = wb.createSheet();
        } else {
            this.sheet = wb.getSheetAt(sheet);
        }
    }

    public int getSheetCount() {
        return wb.getNumberOfSheets();
    }

    public String getSheetName(int sheet) {
        return wb.getSheetName(sheet);
    }

    public int getSheetIndex(String sheet) {
        return wb.getSheetIndex(sheet);
    }

    public void setSelectRow(int row) {
        this.row = sheet.getRow(row);
        if (this.row == null) {
            this.row = sheet.createRow(row);
        }
    }

    public void insertRow(int row) {
        this.row = sheet.createRow(row);
    }

    public Workbook getWb() {
        return wb;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Row getRow() {
        return row;
    }

    public String getCellValue(String column) {
        return getCellValue(getColumnIndex(column));
    }

    public String getCellValue(Integer column) {
        if (this.row != null && column != null && column >= 0) {
            Cell cell = row.getCell(column);
            if (cell != null) {
                return getCellString(cell);
            }
        }
        return "";
    }

    private Cell getCell(int column) {
        if (this.row != null) {
            Cell cell = row.getCell(column);
            if (cell == null) {
                cell = row.createCell(column);
            }
            return cell;
        }
        return null;
    }

    public void setCellValue(int column, String value) {
        setCellValue(column, value, -1, null);
    }

    public void setCellValue(int column, String value, CellStyleBean csBean) {
        setCellValue(column, value, -1, csBean);
    }

    public void setCellValue(int column, String value, int width, CellStyleBean csBean) {
        Cell cell = getCell(column);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(value);
        if (width >= 0) {
            sheet.setColumnWidth(column, width * 250);
        }
        getCellStyle(column).setBorder(BorderStyle.THIN);
        getCellStyle(column).setAlignment(HorizontalAlignment.CENTER);
        if (null != csBean) {
            getCellStyle(column).setCellColor(csBean.getCustomCellColor());
            getCellStyle(column).setFontColor(csBean.getCustomFontColor());
        }
    }

    public void setCellValue(int column, double value) {
        Cell cell = getCell(column);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(value);
        getCellStyle(column).setBorder(BorderStyle.THIN);
    }

    public CellStyleBean getCellStyle(int column) {
        String key = wb.getSheetIndex(sheet) + "-" + row.getRowNum() + "-" + column;
        CellStyleBean bean = cellMap.get(key);
        if (bean == null) {
            bean = new CellStyleBean();
            cellMap.put(key, bean);
        }
        return bean;
    }

    private void applyStyle() {
        for (String cellKey : cellMap.keySet()) {
            String[] postion = cellKey.split("-");
            int sheet = Integer.parseInt(postion[0]);
            int row = Integer.parseInt(postion[1]);
            int column = Integer.parseInt(postion[2]);
            setSelectSheet(sheet);
            setSelectRow(row);
            Cell cell = getCell(column);
            CellStyleBean bean = cellMap.get(cellKey);
            String key = bean.toString();
            CellStyle cellStyle = styleMap.get(key);
            if (cellStyle == null) {
                cellStyle = wb.createCellStyle();
                cellStyle.cloneStyleFrom(cell.getCellStyle());
                styleMap.put(key, cellStyle);
                if (bean.getCellColor() != -1) {
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    cellStyle.setFillForegroundColor(bean.getCellColor());
                }
                if (bean.getFontColor() != -1) {
                    Font font = wb.createFont();
                    font.setColor(bean.getFontColor());
                    cellStyle.setFont(font);
                }
                if (Utility.isNotEmpty(bean.getFormat())) {
                    cellStyle.setDataFormat(wb.createDataFormat().getFormat(bean.getFormat()));
                }
                if (bean.getBorder() != null) {
                    cellStyle.setBorderLeft(bean.getBorder().get(0));
                    cellStyle.setBorderTop(bean.getBorder().get(1));
                    cellStyle.setBorderRight(bean.getBorder().get(2));
                    cellStyle.setBorderBottom(bean.getBorder().get(3));
                    if (bean.getBorderColor() != null) {
                        cellStyle.setLeftBorderColor(bean.getBorderColor().get(0));
                        cellStyle.setTopBorderColor(bean.getBorderColor().get(1));
                        cellStyle.setRightBorderColor(bean.getBorderColor().get(2));
                        cellStyle.setBottomBorderColor(bean.getBorderColor().get(3));
                    }
                }
                if (bean.getAlignment() != null) {
                    cellStyle.setAlignment(bean.getAlignment());
                }
            }
            cell.setCellStyle(cellStyle);
        }
    }

    public int getLastRow() {
        return sheet.getLastRowNum();
    }

    public int getLastCell() {
        return row.getLastCellNum();
    }

    public static int getColumnIndex(String column) {
        column = column.toUpperCase();
        int num = column.charAt(column.length() - 1) - 'A';
        if (column.length() == 2) {
            num += (column.charAt(0) - 'A' + 1) * 26;
        }
        return num;
    }

    public static String getColumnString(int column) {
        if (column < 26) {
            return String.valueOf((char) ('A' + column));
        }
        return (char) ('A' + column / 26 - 1)
                + "" + (char) ('A' + column % 26);
    }

    private String getCellString(Cell cell) {
        String value = "";
        if (cell != null) {
            final CellType cellType = cell.getCellTypeEnum();
            if (CellType.NUMERIC.equals(cellType)) {
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                } else {
                    value = new DecimalFormat("0.########").format(cell.getNumericCellValue());
                }
            } else if (CellType.STRING.equals(cellType)) {
                value = cell.getRichStringCellValue().getString();
            } else if (CellType.FORMULA.equals(cellType)) {
                try {
                    value = cell.getCellFormula();
                } catch (Exception e) {
                    value = "";
                }
            } else if (CellType.BLANK.equals(cellType)) {
                value = "";
            } else if (CellType.BOOLEAN.equals(cellType)) {
                value = String.valueOf(cell.getBooleanCellValue());
            } else if (CellType.ERROR.equals(cellType)) {
                value = String.valueOf(cell.getErrorCellValue());
            }
        }
        value = value.replace(String.valueOf((char) 160), "")
                .replace("\r", "").replace("\n", "")
                .replace("　", "")
                .trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    public void copyRows(int startRow, int endRow, int pPosition) {
        int pStartRow = startRow - 1;
        int pEndRow = endRow - 1;
        int targetRowFrom;
        int targetRowTo;
        int columnCount;
        CellRangeAddress region = null;
        int i;
        int j;
        if (pStartRow == -1 || pEndRow == -1) {
            return;
        }
        for (i = pStartRow; i <= pEndRow; i++) {
            Row sourceRow = sheet.getRow(i);
            columnCount = sourceRow.getLastCellNum();
            if (sourceRow != null) {
                sheet.shiftRows(pPosition - pStartRow + i, sheet.getLastRowNum(), 1, true, false);
                Row newRow = sheet.createRow(pPosition - pStartRow + i);
                newRow.setHeight(sourceRow.getHeight());
                for (j = 0; j < columnCount; j++) {
                    Cell templateCell = sourceRow.getCell(j);
                    if (templateCell != null) {
                        Cell newCell = newRow.createCell(j);
                        copyCell(templateCell, newCell);
                    }
                }
            }
        }
        for (i = 0; i < sheet.getNumMergedRegions(); i++) {
            region = sheet.getMergedRegion(i);
            if (region.getFirstRow() >= pStartRow && region.getLastRow() <= pEndRow) {
                targetRowFrom = region.getFirstRow() - pStartRow + pPosition;
                targetRowTo = region.getLastRow() - pStartRow + pPosition;
                CellRangeAddress newRegion = region.copy();
                newRegion.setFirstRow(targetRowFrom);
                newRegion.setFirstColumn(region.getFirstColumn());
                newRegion.setLastRow(targetRowTo);
                newRegion.setLastColumn(region.getLastColumn());
                sheet.addMergedRegion(newRegion);
            }
        }
    }

    private void copyCell(Cell srcCell, Cell distCell) {
        distCell.setCellStyle(srcCell.getCellStyle());
        if (srcCell.getCellComment() != null) {
            distCell.setCellComment(srcCell.getCellComment());
        }
        CellType srcCellType = srcCell.getCellTypeEnum();
        distCell.setCellType(srcCellType);
        if (srcCellType == CellType.NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(srcCell)) {
                distCell.setCellValue(srcCell.getDateCellValue());
            } else {
                distCell.setCellValue(srcCell.getNumericCellValue());
            }
        } else if (srcCellType == CellType.STRING) {
            distCell.setCellValue(srcCell.getRichStringCellValue());
        } else if (srcCellType == CellType.BOOLEAN) {
            distCell.setCellValue(srcCell.getBooleanCellValue());
        } else if (srcCellType == CellType.ERROR) {
            distCell.setCellErrorValue(srcCell.getErrorCellValue());
        } else if (srcCellType == CellType.FORMULA) {
            distCell.setCellFormula(srcCell.getCellFormula());
        }
    }

    public void createExcelFile(List<Map<String, Object>> exportData, LinkedHashMap<String, String> headMap,
                                OutputStream out, String fileName, IExecutorAAA<String, Object, Map<String, CellStyleBean>> executor) {
        setSelectSheet(0);
        wb.setSheetName(0, fileName.split("_")[0]);
        setSelectRow(0);
        for (int i = 0; i < headMap.size(); i++) {
            setCellValue(i, headMap.get(String.valueOf(i)), null);
        }
        int rowIndex = 1;
        Map<String, CellStyleBean> cellStyleMap = new HashMap<>();
        for (Map<String, Object> dataMap : exportData) {
            setSelectRow(rowIndex);
            for (int j = 0; j < dataMap.size(); j++) {
                if (null != executor) {
                    executor.doExecute(rowIndex + ":" + j, dataMap.get(String.valueOf(j)), cellStyleMap);
                }
                setCellValue(j, null == dataMap.get(String.valueOf(j)) ? "" : dataMap.get(String.valueOf(j)).toString(), null);
            }
            rowIndex++;
        }
        for (String position : cellStyleMap.keySet()) {
            int row = Integer.parseInt(position.split(":")[0]);
            int column = Integer.parseInt(position.split(":")[1]);
            setSelectRow(row);
            getCellStyle(column).setCellColor(cellStyleMap.get(position).getCustomCellColor());
            getCellStyle(column).setFontColor(cellStyleMap.get(position).getCustomFontColor());
        }
        write(out);
    }
}
