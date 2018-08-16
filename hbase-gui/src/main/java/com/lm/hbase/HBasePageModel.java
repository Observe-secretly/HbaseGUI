package com.lm.hbase;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;

/**
 * HBase表数据分页模型类。<br>
 * 
 * @version 1.0
 */
public class HBasePageModel implements Serializable {

    private static final long serialVersionUID  = 330410716100946538L;
    private TableName         tableName         = null;
    private int               pageSize          = 100;
    private int               pageIndex         = 0;
    private int               prevPageIndex     = 1;
    private int               nextPageIndex     = 1;
    private int               pageCount         = 0;
    private int               pageFirstRowIndex = 1;
    private byte[]            pageStartRowKey   = null;
    private byte[]            pageEndRowKey     = null;
    private boolean           hasNextPage       = true;
    private int               queryTotalCount   = 0;
    private long              startTime         = System.currentTimeMillis();
    private long              endTime           = System.currentTimeMillis();
    private List<Result>      resultList        = new ArrayList<Result>();
    private List<Row>         rowList           = new ArrayList<Row>();
    private long              minStamp          = 0;
    private long              maxStamp          = 0;

    public HBasePageModel(int pageSize, TableName tableName){
        this.pageSize = pageSize;
        this.tableName = tableName;
    }

    /**
     * 获取分页记录数量
     * 
     * @return
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置分页记录数量
     * 
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取当前页序号
     * 
     * @return
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * 设置当前页序号
     * 
     * @param pageIndex
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * 获取分页总数
     * 
     * @return
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * 设置分页总数
     * 
     * @param pageCount
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * 获取每页的第一行序号
     * 
     * @return
     */
    public int getPageFirstRowIndex() {
        this.pageFirstRowIndex = (this.getPageIndex() - 1) * this.getPageSize() + 1;
        return pageFirstRowIndex;
    }

    /**
     * 获取每页起始行键
     * 
     * @return
     */
    public byte[] getPageStartRowKey() {
        return pageStartRowKey;
    }

    /**
     * 设置每页起始行键
     * 
     * @param pageStartRowKey
     */
    public void setPageStartRowKey(byte[] pageStartRowKey) {
        this.pageStartRowKey = pageStartRowKey;
    }

    /**
     * 获取每页结束行键
     * 
     * @return
     */
    public byte[] getPageEndRowKey() {
        return pageEndRowKey;
    }

    /**
     * 设置每页结束行键
     * 
     * @param pageStartRowKey
     */
    public void setPageEndRowKey(byte[] pageEndRowKey) {
        this.pageEndRowKey = pageEndRowKey;
    }

    /**
     * 获取上一页序号
     * 
     * @return
     */
    public int getPrevPageIndex() {
        if (this.getPageIndex() > 1) {
            this.prevPageIndex = this.getPageIndex() - 1;
        } else {
            this.prevPageIndex = 1;
        }
        return prevPageIndex;
    }

    /**
     * 获取下一页序号
     * 
     * @return
     */
    public int getNextPageIndex() {
        this.nextPageIndex = this.getPageIndex() + 1;
        return nextPageIndex;
    }

    /**
     * 获取是否有下一页
     * 
     * @return
     */
    public boolean isHasNextPage() {
        // 这个判断是不严谨的，因为很有可能剩余的数据刚好够一页。
        if (this.getResultList().size() == this.getPageSize()) {
            this.hasNextPage = true;
        } else {
            this.hasNextPage = false;
        }
        return hasNextPage;
    }

    /**
     * 获取已检索总记录数
     */
    public int getQueryTotalCount() {
        return queryTotalCount;
    }

    /**
     * 获取已检索总记录数
     * 
     * @param queryTotalCount
     */
    public void setQueryTotalCount(int queryTotalCount) {
        this.queryTotalCount = queryTotalCount;
    }

    /**
     * 初始化起始时间（毫秒）
     */
    public void initStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 初始化截止时间（毫秒）
     */
    public void initEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 获取毫秒格式的耗时信息
     * 
     * @return
     */
    public String getTimeIntervalByMilli() {
        return String.valueOf(this.endTime - this.startTime) + "毫秒";
    }

    /**
     * 获取秒格式的耗时信息
     * 
     * @return
     */
    public String getTimeIntervalBySecond() {
        double interval = (this.endTime - this.startTime) / 1000.0;
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(interval) + "秒";
    }

    /**
     * 打印时间信息
     */
    public void printTimeInfo() {
        System.out.println("耗费时间：" + this.getTimeIntervalBySecond());
    }

    /**
     * 获取HBase检索结果集合
     * 
     * @return
     */
    public List<Result> getResultList() {
        return resultList;
    }

    public List<Row> getRowList() {
        return rowList;
    }

    public void addRow(Row row) {
        this.rowList.add(row);
    }

    public TableName getTableName() {
        return tableName;
    }

    /**
     * 设置HBase检索结果集合
     * 
     * @param resultList
     */
    public void setResultList(List<Result> resultList) {
        this.resultList = resultList;
    }

    public long getMinStamp() {
        return minStamp;
    }

    public void setMinStamp(long minStamp) {
        this.minStamp = minStamp;
    }

    public long getMaxStamp() {
        return maxStamp;
    }

    public void setMaxStamp(long maxStamp) {
        this.maxStamp = maxStamp;
    }

}
