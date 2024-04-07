package com.esin.base.utility;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class CellStyleBean {
    private short cellColor = -1;
    private short fontColor = -1;
    private String format = "";
    private List<BorderStyle> border;
    private List<Short> borderColor;
    private HorizontalAlignment alignment = null;
    private Color customCellColor;
    private Color customFontColor;

    public void setCellColor(Color cellColor) {
        this.cellColor = convertColor(cellColor, Color.WHITE);
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = convertColor(fontColor, Color.BLACK);
    }

    public void setFormat(String format) {
        this.format = Utility.toEmpty(format);
    }

    public short getCellColor() {
        return cellColor;
    }

    public short getFontColor() {
        return fontColor;
    }

    public Color getCustomCellColor() {
        return customCellColor;
    }

    public void setCustomCellColor(Color customCellColor) {
        this.customCellColor = customCellColor;
    }

    public Color getCustomFontColor() {
        return customFontColor;
    }

    public void setCustomFontColor(Color customFontColor) {
        this.customFontColor = customFontColor;
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(HorizontalAlignment alignment) {
        this.alignment = alignment;
    }

    public String getFormat() {
        return format;
    }

    public List<BorderStyle> getBorder() {
        return border;
    }

    public void setBorder(BorderStyle border) {
        this.border = Arrays.asList(border, border, border, border);
    }

    public List<Short> getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        short borderColorIndex = convertColor(borderColor, Color.BLACK);
        if (borderColorIndex != -1) {
            this.borderColor = Arrays.asList(borderColorIndex, borderColorIndex, borderColorIndex, borderColorIndex);
        }
    }

    private short convertColor(Color color, Color defaultColor) {
        if (color == null) {
            return -1;
        } else if (defaultColor != null && defaultColor.equals(color)) {
            return -1;
        } else if (Color.WHITE.equals(color)) {
            return HSSFColor.WHITE.index;
        } else if (Color.YELLOW.equals(color)) {
            return HSSFColor.YELLOW.index;
        } else if (Color.RED.equals(color)) {
            return HSSFColor.RED.index;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "CellStyleBean{" +
                "cellColor=" + cellColor +
                ", fontColor=" + fontColor +
                ", format='" + format + '\'' +
                ", border=" + border +
                '}';
    }
}
