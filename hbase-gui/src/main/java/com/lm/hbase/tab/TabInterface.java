package com.lm.hbase.tab;

import java.awt.Component;

import javax.swing.Icon;

public interface TabInterface {

    public String getTitle();

    public Icon getIcon();

    public Component getComponent();

    public String getTip();

    /**
     * 激活那些开启操作hbase任务时被禁用的组件
     */
    public void enableAll();

    /**
     * 禁用那些可以操作hbase的组件
     */
    public void disableAll();

}
