package com.lm.hbase.swing;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.lm.hbase.adapter.ColumnFamily;
import com.lm.hbase.adapter.HbaseUtil;
import com.lm.hbase.adapter.Row;
import com.lm.hbase.adapter.entity.HBasePageModel;
import com.lm.hbase.adapter.entity.QualifierValue;
import com.lm.hbase.conf.HbaseClientConf;
import com.lm.hbase.util.MyBytesUtil;

public class HandleCore {

    private static final String NUMBER     = "Number";

    private static final String ROW_KEY    = "RowKey";

    private static final String SPLIT_MARK = ".";

    /**
     * 测试 import com.lm.hbase.conf.HbaseClientConf;hbase配置
     * 
     * @param zkPort
     * @param zkQuorum
     * @param hbaseMaster
     * @return
     * @throws Exception
     */
    public static String testConf(String zkPort, String zkQuorum, String hbaseMaster, String znodeParent,
                                  String version, String mavenHome) throws Exception {
        HbaseUtil.init(zkPort, zkQuorum, hbaseMaster, znodeParent);
        HbaseClientConf.setConf(zkPort, zkQuorum, hbaseMaster, znodeParent, version, mavenHome);

        // 尝试获取集群状态
        String clusterStatus = HbaseUtil.getClusterStatus();
        return clusterStatus;
    }

    /**
     * 渲染HBasePageModel到jtable。以列限定符为列<br>
     * 效率不高
     * 
     * @param tableName
     * @param table
     * @param dataModel
     */
    public static void reloadTableFormat(String tableName, JTable table, HBasePageModel dataModel) {
        // 申明一个列头
        LinkedHashSet<String> columnNameSet = new LinkedHashSet<>();
        columnNameSet.add(NUMBER);
        columnNameSet.add(ROW_KEY);

        // 申明一个以[列族名+列修饰符]键的Map，用来存放所有列。每个column是一个以rowkey为键column为值的value
        Map<String, Map<String, String>> dataMap = new LinkedHashMap<>();
        // 创建一个序号列
        dataMap.put(NUMBER, new LinkedHashMap<>());
        // 创建一个rowkey列
        dataMap.put(ROW_KEY, new LinkedHashMap<>());

        for (int i = 0; i < dataModel.getRowList().size(); i++) {
            Row row = dataModel.getRowList().get(i);
            // 得到rowkey
            String rowKey = row.getRowKey();
            // 设置rowkey和序号
            dataMap.get(NUMBER).put(rowKey, String.valueOf(i + 1));
            dataMap.get(ROW_KEY).put(rowKey, rowKey);
            Set<Map.Entry<byte[], ColumnFamily>> columnSet = row.getColumnFamilys().entrySet();// 所有列族
            for (Iterator iterator = columnSet.iterator(); iterator.hasNext();) {
                Entry<byte[], ColumnFamily> entry = (Entry<byte[], ColumnFamily>) iterator.next();// 某个列族的所有列
                for (Entry<byte[], QualifierValue> column : entry.getValue().getColumns().entrySet()) {
                    // 构建列头
                    String tableHead = entry.getValue().getFamilyName() + SPLIT_MARK
                                       + MyBytesUtil.byteToString(column.getKey(), 0, column.getKey().length);
                    // 维护列头
                    columnNameSet.add(tableHead);

                    // 从dataMap拿到对应列的map
                    Map<String, String> columnMap = dataMap.get(tableHead);
                    if (columnMap == null) {
                        columnMap = new LinkedHashMap<>();
                        columnMap.put(rowKey, column.getValue().getDisplayValue());
                    } else {
                        columnMap.put(rowKey, column.getValue().getDisplayValue());
                    }
                    dataMap.put(tableHead, columnMap);
                }

            }
        }

        // 把datamap转化成二维数组
        String[][] rowData = new String[dataModel.getRowList().size()][columnNameSet.size() + 2];

        // 根据rowkey循环出每行有序的数据
        Map<String, String> rowKeyMap = dataMap.get(ROW_KEY);
        int rowIndex = 0;
        for (String rowKey : rowKeyMap.keySet()) {
            String[] columnDataList = new String[columnNameSet.size() + 2];
            int index = 0;
            for (Iterator iterator = columnNameSet.iterator(); iterator.hasNext();) {
                String columnKey = (String) iterator.next();
                columnDataList[index] = dataMap.get(columnKey).get(rowKey);
                index++;
            }
            rowData[rowIndex] = columnDataList;
            rowIndex++;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(rowData, columnNameSet.toArray());

        table.setModel(tableModel);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);

    }

    public static void reloadMetaTableFormat(JTable table, Map<String, String> metaMap) {

        // 把datamap转化成二维数组
        String[][] rowData = new String[metaMap.size()][2];

        int index = 0;
        for (Map.Entry<String, String> entry : metaMap.entrySet()) {
            rowData[index][0] = entry.getKey();
            rowData[index][1] = entry.getValue();
            index++;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(rowData, new String[] { "Column", "Type" });

        table.setModel(tableModel);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);

    }

    public static void reloadMetaTableFormat(String tableName, JTable table) throws Exception {

        HBasePageModel dataModel = new HBasePageModel(1, tableName);
        dataModel = HbaseUtil.scanResultByPageFilter(tableName, null, null, null, Integer.MAX_VALUE, dataModel, true,
                                                     null);

        // 申明一个列头
        LinkedHashSet<String> columnNameSet = new LinkedHashSet<>();

        // 申明一个以[列族名+列修饰符]键的Map，用来存放所有列。每个column是一个以rowkey为键column为值的value
        Map<String, Map<String, String>> dataMap = new LinkedHashMap<>();
        // 创建一个序号列
        dataMap.put(NUMBER, new LinkedHashMap<>());
        // 创建一个rowkey列
        dataMap.put(ROW_KEY, new LinkedHashMap<>());

        for (int i = 0; i < dataModel.getRowList().size(); i++) {
            Row row = dataModel.getRowList().get(i);
            // 得到rowkey
            String rowKey = row.getRowKey();
            // 设置rowkey和序号
            Set<Map.Entry<byte[], ColumnFamily>> columnSet = row.getColumnFamilys().entrySet();// 所有列族
            for (Iterator iterator = columnSet.iterator(); iterator.hasNext();) {
                Entry<byte[], ColumnFamily> entry = (Entry<byte[], ColumnFamily>) iterator.next();// 某个列族的所有列
                for (Entry<byte[], QualifierValue> column : entry.getValue().getColumns().entrySet()) {
                    String tableHead = entry.getValue().getFamilyName() + SPLIT_MARK
                                       + MyBytesUtil.byteToString(column.getKey(), 0, column.getKey().length);
                    columnNameSet.add(tableHead);

                    // 从dataMap拿到对应列的map
                    Map<String, String> columnMap = dataMap.get(tableHead);
                    if (columnMap == null) {
                        columnMap = new LinkedHashMap<>();
                        columnMap.put(rowKey, column.getValue().getDisplayValue());
                    } else {
                        columnMap.put(rowKey, column.getValue().getDisplayValue());
                    }
                    dataMap.put(tableHead, columnMap);
                }

            }
        }

        // 把datamap转化成二维数组
        String[][] rowData = new String[columnNameSet.size()][2];

        int index = 0;
        for (Iterator iterator = columnNameSet.iterator(); iterator.hasNext();) {
            String columnKey = (String) iterator.next();
            rowData[index][0] = columnKey;
            rowData[index][1] = "String";
            index++;
        }

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setDataVector(rowData, new String[] { "Column", "Type" });

        table.setModel(tableModel);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);

    }

    public static void setPageInfomation(HBasePageModel dataModel, JLabel jlabel) {
        if (dataModel == null) {
            jlabel.setText("");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("第" + dataModel.getPageIndex() + "页  ");
        info.append(dataModel.getPageSize() + "条/页  ");
        info.append("耗时：" + dataModel.getTimeIntervalBySecond());

        jlabel.setText(info.toString());
    }

    public static void cleanPageInfomation(JLabel jlabel) {
        jlabel.setText("");
    }

}
