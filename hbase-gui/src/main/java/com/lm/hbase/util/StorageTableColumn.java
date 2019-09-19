package com.lm.hbase.util;

public class StorageTableColumn {

    private int maxWidth;

    private int preferredWidth;

    private int minWidth;

    private int width;

    public StorageTableColumn(int maxWidth, int preferredWidth, int minWidth, int width){
        this.maxWidth = maxWidth;
        this.preferredWidth = preferredWidth;
        this.minWidth = minWidth;
        this.width = width;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
