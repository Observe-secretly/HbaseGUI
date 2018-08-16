package com.lm.hbase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Row {

    private LinkedHashMap<String, ColumnFamily> columnFamilys = new LinkedHashMap<String, ColumnFamily>();

    private String                              rowKey;

    public Row(String rowKey){
        this.rowKey = rowKey;
    }

    public void add(String columnFamilyName, String qualifier, String value) {
        ColumnFamily columnFamily = columnFamilys.get(columnFamilyName);
        if (columnFamily == null) {
            columnFamily = new ColumnFamily(columnFamilyName);
            columnFamily.add(qualifier, value);
            columnFamilys.put(columnFamilyName, columnFamily);
        } else {
            columnFamily.add(qualifier, value);
            columnFamilys.put(columnFamilyName, columnFamily);
        }
    }

    public String getRowKey() {
        return rowKey;
    }

    public ColumnFamily getColumnFamily(String columnFamilyName) {
        return columnFamilys.get(columnFamilyName);
    }

    public LinkedHashMap<String, ColumnFamily> getColumnFamilys() {
        return columnFamilys;
    }

    @Override
    public String toString() {
        StringBuilder rowString = new StringBuilder("RowKey:" + rowKey + "\n");
        Iterator<Entry<String, ColumnFamily>> iterator = columnFamilys.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ColumnFamily> entry = iterator.next();
            rowString.append("ColumnFamilyName:" + entry.getKey() + ">" + entry.getValue().toString() + "\n");
        }
        rowString.append("<======================================>");
        return rowString.toString();
    }

}
