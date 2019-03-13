package com.lm.hbase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.util.Bytes;

import com.lm.hbase.util.QualifierValue;

public class Row {

    private LinkedHashMap<byte[], ColumnFamily> columnFamilys = new LinkedHashMap<byte[], ColumnFamily>();

    private String                              rowKey;

    public Row(String rowKey){
        this.rowKey = rowKey;
    }

    public void add(byte[] columnFamilyName, byte[] qualifier, QualifierValue value) {
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

    public ColumnFamily getColumnFamily(byte[] columnFamilyName) {
        return columnFamilys.get(columnFamilyName);
    }

    public LinkedHashMap<byte[], ColumnFamily> getColumnFamilys() {
        return columnFamilys;
    }

    @Override
    public String toString() {
        StringBuilder rowString = new StringBuilder("RowKey:" + rowKey + "\n");
        Iterator<Entry<byte[], ColumnFamily>> iterator = columnFamilys.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<byte[], ColumnFamily> entry = iterator.next();
            rowString.append("ColumnFamilyName:" + Bytes.toString(entry.getKey()) + ">" + entry.getValue().toString()
                             + "\n");
        }
        rowString.append("<======================================>");
        return rowString.toString();
    }

}
