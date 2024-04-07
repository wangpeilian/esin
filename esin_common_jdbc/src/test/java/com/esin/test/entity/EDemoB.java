package com.esin.test.entity;

import com.esin.jdbc.define.Column;
import com.esin.jdbc.define.Table;

import java.util.UUID;

@Table(name = EDemoB.TableName)
public class EDemoB extends EDemo {

    public static final String TableName = "t_demo_b";

    public static final String Col_ref_a = "ref_a_id";

    @Column(name = EDemo.Col_string_1, string_length = Column.string_length_long, order = 200)
    private String colString1;
    @Column(name = EDemo.Col_string_2, string_length = Column.string_length_long, order = 200)
    private String colString2;
    @Column(name = EDemo.Col_string_3, string_length = Column.string_length_long, order = 200)
    private String colString3;

    @Column(name = EDemoB.Col_ref_a)
    private EDemoA refA;

    public EDemoB() {
    }

    public EDemoB(UUID id) {
        super(id);
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

    public EDemoA getRefA() {
        return refA;
    }

    public void setRefA(EDemoA refA) {
        this.refA = refA;
    }

    public UUID getIdAlias() {
        return getId();
    }

    public void setIdAlias(UUID idAlias) {
        setId(idAlias);
    }
}
