package com.esin.test.entity;

import com.esin.jdbc.define.Column;
import com.esin.jdbc.define.Table;

import java.util.List;
import java.util.UUID;

@Table(name = EDemoA.TableName)
public class EDemoA extends EDemo {
    public static final String TableName = "t_demo_a";

    public static final String Col_ref_a1 = "ref_a1_id";
    public static final String Col_ref_a2 = "ref_a2_id";

    @Column(name = EDemo.Col_string_1, string_length = Column.string_length_medium, order = 200)
    private String colString1;
    @Column(name = EDemo.Col_string_2, string_length = Column.string_length_medium, order = 200)
    private String colString2;
    @Column(name = EDemo.Col_string_3, string_length = Column.string_length_medium, order = 200)
    private String colString3;

    @Column(name = EDemoA.Col_ref_a1)
    private EDemoA refA1;
    @Column(name = EDemoA.Col_ref_a2)
    private EDemoA refA2;

    @Column(name = EDemoA.Col_ref_a1)
    private List<EDemoA> listA1;
    @Column(name = EDemoA.Col_ref_a2)
    private List<EDemoA> listA2;

    private List<EDemoB> listB;

    public EDemoA() {
    }

    public EDemoA(UUID id) {
        super(id);
    }

    @Override
    public String getColString1() {
        return colString1;
    }

    @Override
    public void setColString1(String colString1) {
        this.colString1 = colString1;
    }

    @Override
    public String getColString2() {
        return colString2;
    }

    @Override
    public void setColString2(String colString2) {
        this.colString2 = colString2;
    }

    @Override
    public String getColString3() {
        return colString3;
    }

    @Override
    public void setColString3(String colString3) {
        this.colString3 = colString3;
    }

    public EDemoA getRefA1() {
        return refA1;
    }

    public void setRefA1(EDemoA refA1) {
        this.refA1 = refA1;
    }

    public EDemoA getRefA2() {
        return refA2;
    }

    public void setRefA2(EDemoA refA2) {
        this.refA2 = refA2;
    }

    public List<EDemoA> getListA1() {
        return listA1;
    }

    public void setListA1(List<EDemoA> listA1) {
        this.listA1 = listA1;
    }

    public List<EDemoA> getListA2() {
        return listA2;
    }

    public void setListA2(List<EDemoA> listA2) {
        this.listA2 = listA2;
    }

    public List<EDemoB> getListB() {
        return listB;
    }

    public void setListB(List<EDemoB> listB) {
        this.listB = listB;
    }
}
