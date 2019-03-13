package com.lm.hbase.util;

public class QualifierValue {

    private byte[] qualifier;

    private String displayValue;

    public QualifierValue(byte[] qualifier, String displayValue){
        this.qualifier = qualifier;
        this.displayValue = displayValue;
    }

    public byte[] getQualifier() {
        return qualifier;
    }

    public void setQualifier(byte[] qualifier) {
        this.qualifier = qualifier;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

}
