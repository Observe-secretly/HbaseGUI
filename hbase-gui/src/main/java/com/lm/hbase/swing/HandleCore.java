package com.lm.hbase.swing;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;

import com.alibaba.fastjson.JSON;
import com.lm.hbase.ColumnFamily;
import com.lm.hbase.HBasePageModel;
import com.lm.hbase.HbaseUtil;
import com.lm.hbase.Row;

public class HandleCore {

    private static Properties   confProps  = null;

    private static final String NUMBER     = "Number";

    private static final String ROW_KEY    = "RowKey";

    private static final String SPLIT_MARK = ".";

    private static final String FILE_PATH  = System.getProperty("user.dir") + System.getProperty("file.separator")
                                             + "hbase-client.conf";

    /**
     * 获取配置文件，如果不存在则创建
     * 
     * @return
     */
    private static Properties loadProperties() {
        confProps = new Properties();
        try {
            File conf = new File(FILE_PATH);
            if (!conf.exists()) {
                conf.createNewFile();
            }
            confProps.load(new InputStreamReader(new FileInputStream(conf)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return confProps;
    }

    public static void reloadConf() {
        loadProperties();
    }

    public static void setValue(String key, String value) {
        if (confProps == null) {
            loadProperties();
        }
        confProps.put(key, value);
        try {
            confProps.store(new FileOutputStream(FILE_PATH), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValue(String key) {
        if (confProps == null) {
            return loadProperties().getProperty(key);
        } else {
            return confProps.getProperty(key);
        }
    }

    public static Integer getIntegerValue(String key) {
        return Integer.parseInt(getStringValue(key));
    }

    public static Long getLongValue(String key) {
        return Long.parseLong(getStringValue(key));
    }

    public static Boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(getStringValue(key));
    }

    public static void setConf(String zkPort, String zkQuorum, String hbaseMaster, String znodeParent) {
        if (confProps == null) {
            loadProperties();
        }
        confProps.put("hbase.zk.port", zkPort);
        confProps.put("hbase.zk.quorum", zkQuorum);
        confProps.put("hbase.master", hbaseMaster);
        confProps.put("znode.parent", znodeParent);
        try {
            confProps.store(new FileOutputStream(FILE_PATH), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试hbase配置
     * 
     * @param zkPort
     * @param zkQuorum
     * @param hbaseMaster
     * @return
     * @throws Exception
     */
    public static ClusterStatus testConf(String zkPort, String zkQuorum, String hbaseMaster,
                                         String znodeParent) throws Exception {
        HbaseUtil.init(zkPort, zkQuorum, hbaseMaster, znodeParent);
        setConf(zkPort, zkQuorum, hbaseMaster, znodeParent);

        // 尝试获取集群状态
        ClusterStatus clusterStatus = HbaseUtil.getClusterStatus();
        return clusterStatus;
    }

    /**
     * 渲染HBasePageModel到jtable。但它是以列族为列，不是已列限定符为列，不方便查看数据，所以废除
     * 
     * @param tableName
     * @param table
     * @param dataModel
     */
    @Deprecated
    public static void reloadTable(TableName tableName, JTable table, HBasePageModel dataModel) {
        // 查询表结构
        HTableDescriptor thead = HbaseUtil.getDescribe(tableName);
        List<String> columnName = new ArrayList<>();
        columnName.add("序号");
        columnName.add("RowKey");
        for (HColumnDescriptor familie : thead.getColumnFamilies()) {
            columnName.add(familie.getNameAsString().toUpperCase());
        }

        // 组织数据
        String[][] rowData = new String[dataModel.getRowList().size()][columnName.size() + 2];
        for (int i = 0; i < dataModel.getRowList().size(); i++) {

            rowData[i][0] = (i + 1) + "";

            Row row = dataModel.getRowList().get(i);
            rowData[i][1] = row.getRowKey();

            int j = 2;
            Set<Map.Entry<String, ColumnFamily>> columnSet = row.getColumnFamilys().entrySet();
            for (Iterator iterator = columnSet.iterator(); iterator.hasNext();) {
                Entry<String, ColumnFamily> entry = (Entry<String, ColumnFamily>) iterator.next();
                rowData[i][j++] = JSON.toJSONString(entry.getValue().getColumns());

            }

        }

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setDataVector(rowData, columnName.toArray());

        table.setModel(tableModel);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);
    }

    /**
     * 渲染HBasePageModel到jtable。以列限定符为列<br>
     * 效率不高
     * 
     * @param tableName
     * @param table
     * @param dataModel
     */
    public static void reloadTableFormat(TableName tableName, JTable table, HBasePageModel dataModel) {
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
            Set<Map.Entry<String, ColumnFamily>> columnSet = row.getColumnFamilys().entrySet();// 所有列族
            for (Iterator iterator = columnSet.iterator(); iterator.hasNext();) {
                Entry<String, ColumnFamily> entry = (Entry<String, ColumnFamily>) iterator.next();// 某个列族的所有列
                for (Entry<String, String> column : entry.getValue().getColumns().entrySet()) {
                    // 构建列头
                    String tableHead = entry.getValue().getFamilyName() + SPLIT_MARK + column.getKey();
                    // 维护列头
                    columnNameSet.add(tableHead);

                    // 从dataMap拿到对应列的map
                    Map<String, String> columnMap = dataMap.get(tableHead);
                    if (columnMap == null) {
                        columnMap = new LinkedHashMap<>();
                        columnMap.put(rowKey, column.getValue());
                    } else {
                        columnMap.put(rowKey, column.getValue());
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

    public static void reloadMetaTableFormat(TableName tableName, JTable table) {

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
            Set<Map.Entry<String, ColumnFamily>> columnSet = row.getColumnFamilys().entrySet();// 所有列族
            for (Iterator iterator = columnSet.iterator(); iterator.hasNext();) {
                Entry<String, ColumnFamily> entry = (Entry<String, ColumnFamily>) iterator.next();// 某个列族的所有列
                for (Entry<String, String> column : entry.getValue().getColumns().entrySet()) {
                    String tableHead = entry.getValue().getFamilyName() + SPLIT_MARK + column.getKey();
                    columnNameSet.add(tableHead);

                    // 从dataMap拿到对应列的map
                    Map<String, String> columnMap = dataMap.get(tableHead);
                    if (columnMap == null) {
                        columnMap = new LinkedHashMap<>();
                        columnMap.put(rowKey, column.getValue());
                    } else {
                        columnMap.put(rowKey, column.getValue());
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

        // long total = HbaseUtil.rowCount(dataModel.getTableName());

        StringBuilder info = new StringBuilder();
        info.append("第" + dataModel.getPageIndex() + "页  ");
        info.append(dataModel.getPageSize() + "条/页  ");
        // info.append("共" + (int) Math.ceil(total / (float) dataModel.getPageSize()) + "页 ");
        // info.append("总数：" + total);
        info.append("耗时：" + dataModel.getTimeIntervalBySecond());

        jlabel.setText(info.toString());

    }

}
