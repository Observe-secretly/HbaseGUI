package com.lm.hbase.tab;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public abstract class TabAbstract extends TabCommonUtil implements TabInterface {

    public static final String PROPERTIES_SUFFIX = ".MATA";

    public JFrame              jFrame;

    public JProgressBar        processBar;

    public TabAbstract(JFrame jFrame, JProgressBar processBar){
        this.jFrame = jFrame;
        this.processBar = processBar;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getTip() {
        return null;
    }

    @Override
    public Component getComponent() {
        return initializePanel();
    }

    public abstract JPanel initializePanel();

}
