package com.lm.hbase;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.util.Bytes;

import com.lm.hbase.util.QualifierValue;

public class ColumnFamily {

    private LinkedHashMap<byte[], QualifierValue> columns         = new LinkedHashMap<>();

    private String                                familyName;

    private byte[]                                familyNameBytes;

    private int                                   cursor          = 0;

    private QualifierColumn                       qualifierColumn = null;

    /**
     * 设置列族名称
     * 
     * @param familyName
     */
    public ColumnFamily(byte[] familyNameBytes){
        this.familyName = Bytes.toString(familyNameBytes);
        this.familyNameBytes = familyNameBytes;
    }

    /**
     * 获取没有设置qualifier的列值
     * 
     * @return
     */
    public QualifierValue get() {
        return columns.get(null);
    }

    /**
     * 根据qualifier获取列值
     * 
     * @param qualifier
     * @return
     */
    public QualifierValue get(byte[] qualifier) {
        return columns.get(qualifier);
    }

    /**
     * 添加一个值
     * 
     * @param qualifier 列修饰符(可以理解成二级列名)
     * @param value 值
     */
    public void add(byte[] qualifier, QualifierValue value) {
        if (qualifier == null || qualifier.length == 0) {
            qualifier = null;
        }
        this.columns.put(qualifier, value);
    }

    /**
     * 取值游标下移<br>
     * 此方法配合getQualifierColumn()使用。伪代码如下：<br>
     * ......<br>
     * while(next()!=-1){<br>
     * QualifierColumn qualifierColumn = getQualifierColumn();<br>
     * ...<br>
     * }<br>
     * ..<br>
     * 
     * @return 返回-1时代表已经取尽。返回1时代表取到了当前游标所在的值
     */
    public int hasNext() {
        Iterator<Entry<byte[], QualifierValue>> iterator = columns.entrySet().iterator();

        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<byte[], QualifierValue> entry = iterator.next();
            if (index == cursor) {
                qualifierColumn = new QualifierColumn(entry.getKey() == null ? null : entry.getKey(),
                                                      entry.getValue() == null ? null : entry.getValue().getQualifier());
                cursor++;
                return 1;
            }
            index++;
        }
        qualifierColumn = null;
        return -1;
    }

    /**
     * 获取当前游标下的值<br>
     * 配合next()方法使用，首次取值为null
     * 
     * @return
     */
    public Map.Entry<String, QualifierColumn> next() {

        return new Entry<String, QualifierColumn>() {

            @Override
            public QualifierColumn setValue(QualifierColumn value) {
                return null;
            }

            @Override
            public QualifierColumn getValue() {
                return qualifierColumn;
            }

            @Override
            public String getKey() {
                return familyName;
            }
        };
    }

    public Map<byte[], QualifierValue> getColumns() {
        return columns;
    }

    public String getFamilyName() {
        return familyName;
    }

    public byte[] getFamilyNameBytes() {
        return familyNameBytes;
    }

    @Override
    public String toString() {
        StringBuilder rowString = new StringBuilder();
        Iterator<Entry<byte[], QualifierValue>> iterator = columns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<byte[], QualifierValue> entry = iterator.next();
            rowString.append("{" + (entry.getKey() == null ? "NULL" : Bytes.toString(entry.getKey())) + ":"
                             + (entry.getValue() == null ? "NULL" : entry.getValue().getDisplayValue()) + "}");
        }
        return rowString.toString();
    }

}

class QualifierColumn {

    private byte[] qualifier;
    private byte[] v;

    public QualifierColumn(byte[] qualifier, byte[] value){
        this.qualifier = qualifier;
        this.v = value;
    }

    public byte[] getQualifier() {
        return qualifier;
    }

    public byte[] getV() {
        return v;
    }

}
