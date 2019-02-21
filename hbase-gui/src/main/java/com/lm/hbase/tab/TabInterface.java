package com.lm.hbase.tab;

import java.awt.Component;

import javax.swing.Icon;

public interface TabInterface {

    public String getTitle();

    public Icon getIcon();

    public Component getComponent();

    public String getTip();

}
