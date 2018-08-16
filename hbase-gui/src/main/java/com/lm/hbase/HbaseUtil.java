package com.lm.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 操作habse的工具类，包含基本的表操作和行操作
 * 
 * @author limin 2017年8月11日 上午10:15:46
 */
public class HbaseUtil {

    private static Connection connection = null;

    public static void init(String zkPort, String zkQuorum, String hbaseMaster) throws IOException {
        Configuration configuration = HBaseConfiguration.create();

        configuration.set("hbase.zookeeper.property.clientPort", zkPort);
        configuration.set("hbase.zookeeper.quorum", zkQuorum);
        configuration.set("hbase.master", hbaseMaster);
        configuration.setInt("hbase.rpc.timeout", 20000);
        configuration.setInt("hbase.client.operation.timeout", 30000);
        configuration.setInt("hbase.client.scanner.timeout.period", 200000);
        connection = ConnectionFactory.createConnection(configuration);

    }

    public static Connection getConn() throws Exception {
        if (connection == null) {
            throw new Exception("HbaseUtil is not initialized");
        }
        return connection;
    }

    public static void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) throws IOException {
        // HbaseUtil.createTable(TableName.valueOf("job"), "name", "jobNumber", "sex", "age");

        HbaseUtil.init("2181", "h2,h3,h4", "h1:16000");
        HbaseUtil.createTable(TableName.valueOf("signlog"), "signature");

        // HBasePageModel pageModel = new HBasePageModel(10);
        // HbaseUtil.scanResultByPageFilter(TableName.valueOf("test"), null, null, null, Integer.MAX_VALUE, pageModel);
    }

    /**
     * 创建表
     * 
     * @param tableName 表名
     * @param columnFamilys 列族
     */
    public static void createTable(TableName tableName, String... columnFamilys) {
        Admin hBaseAdmin = null;
        try {
            Connection connection = getConn();
            hBaseAdmin = connection.getAdmin();

            if (hBaseAdmin.tableExists(tableName)) {
                throw new Exception(tableName.toString() + " is exist");
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
            for (String columnFamily : columnFamilys) {
                tableDescriptor.addFamily(new HColumnDescriptor(columnFamily));
            }
            hBaseAdmin.createTable(tableDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (hBaseAdmin != null) {
                try {
                    hBaseAdmin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 插入数据
     * 
     * @param tableName 表名
     * @param columns 请仔细查看ColumnFamily对象的用法
     */
    public static void insertData(TableName tableName, String rowKey, ColumnFamily... columns) {
        Table table = null;
        try {
            Connection connection = getConn();
            table = connection.getTable(tableName);
            Put put = new Put(rowKey.getBytes());
            for (ColumnFamily columnFamily : columns) {
                while (columnFamily.hasNext() != -1) {
                    Entry<String, QualifierColumn> entry = columnFamily.next();
                    put.addColumn(columnFamily.getFamilyName().getBytes(), entry.getValue().getQualifier(),
                                  entry.getValue().getV());
                }

            }
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 批量插入数据
     * 
     * @param tableName
     * @param rowList
     */
    public static void batchInsertData(TableName tableName, List<Row> rowList) {
        Table table = null;
        try {
            Connection connection = getConn();
            table = connection.getTable(tableName);

            List<org.apache.hadoop.hbase.client.Row> puts = new ArrayList<>();
            for (Row row : rowList) {// 行
                Put put = new Put(row.getRowKey().getBytes());
                Iterator<Map.Entry<String, ColumnFamily>> iterator = row.getColumnFamilys().entrySet().iterator();
                while (iterator.hasNext()) {// 列族
                    ColumnFamily columnFamily = iterator.next().getValue();
                    while (columnFamily.hasNext() != -1) {// 列
                        Entry<String, QualifierColumn> entry = columnFamily.next();
                        put.addColumn(columnFamily.getFamilyName().getBytes(), entry.getValue().getQualifier(),
                                      entry.getValue().getV());
                    }

                }
                puts.add(put);
            }
            Object[] results = new Object[puts.size()];
            table.batch(puts, results);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static HBasePageModel scanResultByPageFilter(TableName tableName, byte[] startRowKey, byte[] endRowKey,
                                                        FilterList filterList, int maxVersions,
                                                        HBasePageModel pageModel) {
        if (pageModel == null) {
            pageModel = new HBasePageModel(10, tableName);
        }
        if (maxVersions <= 0) {
            // 默认只检索数据的最新版本
            maxVersions = Integer.MIN_VALUE;
        }
        pageModel.initStartTime();
        pageModel.initEndTime();
        if (tableName == null) {
            return pageModel;
        }
        Table table = null;

        try {
            Connection connection = getConn();
            table = connection.getTable(tableName);
            int tempPageSize = pageModel.getPageSize();
            boolean isEmptyStartRowKey = false;
            if (startRowKey == null) {
                // 则读取表的第一行记录，这里用到了笔者本人自己构建的一个表数据操作类。
                Result firstResult = selectFirstResultRow(tableName, filterList);
                if (firstResult == null || firstResult.isEmpty()) {
                    return pageModel;
                }
                startRowKey = firstResult.getRow();
            }
            if (pageModel.getPageStartRowKey() == null) {
                isEmptyStartRowKey = true;
                pageModel.setPageStartRowKey(startRowKey);
            } else {
                if (pageModel.getPageEndRowKey() != null) {
                    pageModel.setPageStartRowKey(pageModel.getPageEndRowKey());
                }
                // 从第二页开始，每次都多取一条记录，因为第一条记录是要删除的。
                tempPageSize += 1;
            }

            Scan scan = new Scan();
            scan.setStartRow(pageModel.getPageStartRowKey());
            if (pageModel.getMinStamp() != 0 && pageModel.getMaxStamp() != 0) {
                scan.setTimeRange(pageModel.getMinStamp(), pageModel.getMaxStamp());
            }

            if (endRowKey != null) {
                scan.setStopRow(endRowKey);
            }
            PageFilter pageFilter = new PageFilter(pageModel.getPageSize() + 1);
            if (filterList != null) {
                filterList.addFilter(pageFilter);
                scan.setFilter(filterList);
            } else {
                scan.setFilter(pageFilter);
            }
            if (maxVersions == Integer.MAX_VALUE) {
                scan.setMaxVersions();
            } else if (maxVersions == Integer.MIN_VALUE) {

            } else {
                scan.setMaxVersions(maxVersions);
            }
            ResultScanner scanner = table.getScanner(scan);
            List<Result> resultList = new ArrayList<Result>();
            int index = 0;
            for (Result rs : scanner.next(tempPageSize)) {
                if (isEmptyStartRowKey == false && index == 0) {
                    index += 1;
                    continue;
                }
                if (!rs.isEmpty()) {
                    Row row = new Row(Bytes.toString(rs.getRow()));
                    for (Cell c : rs.rawCells()) {
                        row.add(Bytes.toString(CellUtil.cloneFamily(c)), Bytes.toString(CellUtil.cloneQualifier(c)),
                                Bytes.toString(CellUtil.cloneValue(c)));
                    }
                    resultList.add(rs);
                    pageModel.addRow(row);
                }
                index += 1;
            }
            scanner.close();
            pageModel.setResultList(resultList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int pageIndex = pageModel.getPageIndex() + 1;
        pageModel.setPageIndex(pageIndex);
        if (pageModel.getResultList().size() > 0) {
            // 获取本次分页数据首行和末行的行键信息
            byte[] pageStartRowKey = pageModel.getResultList().get(0).getRow();
            byte[] pageEndRowKey = pageModel.getResultList().get(pageModel.getResultList().size() - 1).getRow();
            pageModel.setPageStartRowKey(pageStartRowKey);
            pageModel.setPageEndRowKey(pageEndRowKey);
        }
        int queryTotalCount = pageModel.getQueryTotalCount() + pageModel.getResultList().size();
        pageModel.setQueryTotalCount(queryTotalCount);
        pageModel.initEndTime();
        pageModel.printTimeInfo();
        return pageModel;
    }

    /**
     * 检索指定表的第一行记录。<br>
     * （如果在创建表时为此表指定了非默认的命名空间，则需拼写上命名空间名称，格式为【namespace:tablename】）。
     * 
     * @param tableName 表名称(*)。
     * @param filterList 过滤器集合，可以为null。
     * @return
     */
    public static Result selectFirstResultRow(TableName tableName, FilterList filterList) {
        if (tableName == null) return null;
        Table table = null;
        try {
            Connection connection = getConn();
            table = connection.getTable(tableName);
            Scan scan = new Scan();
            if (filterList != null) {
                scan.setFilter(filterList);
            }
            ResultScanner scanner = table.getScanner(scan);
            Iterator<Result> iterator = scanner.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                Result rs = iterator.next();
                if (index == 0) {
                    scanner.close();
                    return rs;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 删除数据
     * 
     * @param tablename
     * @param rowkey
     */
    public static void deleteRow(TableName tablename, String... rowkey) {
        Table table = null;
        try {
            Connection connection = getConn();
            table = connection.getTable(tablename);
            List<Delete> list = new ArrayList<Delete>();
            for (String item : rowkey) {
                list.add(new Delete(item.getBytes()));
            }

            table.delete(list);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 列出所有表名称
     * 
     * @return
     */
    public static TableName[] getListTableNames() {
        Admin admin = null;
        try {
            Connection connection = getConn();
            admin = connection.getAdmin();
            return admin.listTableNames();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    /**
     * 删除表
     * 
     * @param tablename
     * @throws IOException
     */
    public static void dropTable(TableName tablename) {

        Admin hBaseAdmin = null;
        try {
            Connection connection = getConn();
            hBaseAdmin = connection.getAdmin();
            hBaseAdmin.disableTable(tablename);
            hBaseAdmin.deleteTable(tablename);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (hBaseAdmin != null) {
                try {
                    hBaseAdmin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取表结构
     * 
     * @param tablename
     * @return
     */
    public static HTableDescriptor getDescribe(TableName tablename) {
        Table table = null;
        try {
            Connection connection = getConn();
            table = connection.getTable(tablename);
            return table.getTableDescriptor();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 计算表数据总数
     * 
     * @param tablename
     * @return
     */
    public static long rowCount(TableName tablename) {
        Table table = null;
        long rowCount = 0;
        try {
            Connection connection = getConn();
            table = connection.getTable(tablename);
            Scan scan = new Scan();
            scan.setFilter(new FirstKeyOnlyFilter());
            ResultScanner resultScanner = table.getScanner(scan);
            for (Result result : resultScanner) {
                rowCount += result.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rowCount;
    }

}
