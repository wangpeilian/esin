package com.esin.test.entity;

import com.esin.jdbc.define.Column;
import com.esin.jdbc.entity.BaseEntityString;
import com.esin.jdbc.entity.BaseEntityUUID;
import com.esin.test.constants.UserStatus;

import java.util.Date;
import java.util.UUID;

public class EDemo extends BaseEntityUUID {

    public static final String Col_int = "c_int";
    public static final String Col_long = "c_long";
    public static final String Col_float = "c_float";
    public static final String Col_double = "c_double";
    public static final String Col_string_1 = "c_string_1";
    public static final String Col_string_2 = "c_string_2";
    public static final String Col_string_3 = "c_string_3";
    public static final String Col_boolean = "c_boolean";
    public static final String Col_date = "c_date";
    public static final String Col_enum_ordinal = "c_enum_ordinal";
    public static final String Col_enum_name = "c_enum_name";
    public static final String Col_byte = "c_byte";

    @Column(name = EDemo.Col_int)
    private Integer colInt;
    @Column(name = EDemo.Col_long)
    private Long colLong;
    @Column(name = EDemo.Col_float)
    private Float colFloat;
    @Column(name = EDemo.Col_double)
    private Double colDouble;
    @Column(name = EDemo.Col_string_1)
    private String colString1;
    @Column(name = EDemo.Col_string_2)
    private String colString2;
    @Column(name = EDemo.Col_string_3)
    private String colString3;
    @Column(name = EDemo.Col_boolean)
    private Boolean colBoolean;
    @Column(name = EDemo.Col_date)
    private Date colDate;
    @Column(name = EDemo.Col_enum_ordinal, enum_name = false)
    private UserStatus colEnumOrdinal;
    @Column(name = EDemo.Col_enum_name)
    private UserStatus colEnumName;
    @Column(name = EDemo.Col_byte)
    private Byte colByte;

    public EDemo() {
    }

    public EDemo(UUID id) {
        super(id);
    }

    public Integer getColInt() {
        return colInt;
    }

    public void setColInt(Integer colInt) {
        this.colInt = colInt;
    }

    public Long getColLong() {
        return colLong;
    }

    public void setColLong(Long colLong) {
        this.colLong = colLong;
    }

    public Float getColFloat() {
        return colFloat;
    }

    public void setColFloat(Float colFloat) {
        this.colFloat = colFloat;
    }

    public Double getColDouble() {
        return colDouble;
    }

    public void setColDouble(Double colDouble) {
        this.colDouble = colDouble;
    }

    public String getColString1() {
        return colString1;
    }

    public void setColString1(String colString1) {
        this.colString1 = colString1;
    }

    public String getColString2() {
        return colString2;
    }

    public void setColString2(String colString2) {
        this.colString2 = colString2;
    }

    public String getColString3() {
        return colString3;
    }

    public void setColString3(String colString3) {
        this.colString3 = colString3;
    }

    public Boolean getColBoolean() {
        return colBoolean;
    }

    public void setColBoolean(Boolean colBoolean) {
        this.colBoolean = colBoolean;
    }

    public Date getColDate() {
        return colDate;
    }

    public void setColDate(Date colDate) {
        this.colDate = colDate;
    }

    public UserStatus getColEnumOrdinal() {
        return colEnumOrdinal;
    }

    public void setColEnumOrdinal(UserStatus colEnumOrdinal) {
        this.colEnumOrdinal = colEnumOrdinal;
    }

    public UserStatus getColEnumName() {
        return colEnumName;
    }

    public void setColEnumName(UserStatus colEnumName) {
        this.colEnumName = colEnumName;
    }

    public Byte getColByte() {
        return colByte;
    }

    public void setColByte(Byte colByte) {
        this.colByte = colByte;
    }
}
