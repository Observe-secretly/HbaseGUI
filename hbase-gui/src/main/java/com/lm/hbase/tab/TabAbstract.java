package com.lm.hbase.tab;

import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.lm.hbase.swing.HbaseGui;

public abstract class TabAbstract extends TabCommonUtil implements TabInterface {

    public static final String PROPERTIES_SUFFIX = ".MATA";

    private HbaseGui           window;

    public TabAbstract(HbaseGui window){
        this.window = window;
    }

    public synchronized void startTask() {
        window.processBar.setIndeterminate(true);
        window.stopButton.setEnabled(true);
        disableAll();
    }

    public synchronized void stopTask() {
        window.processBar.setIndeterminate(false);
        window.stopButton.setEnabled(false);
        this.window.threadPool.shutdownNow();
        this.window.threadPool = Executors.newSingleThreadScheduledExecutor();
        enableAll();
    }

    public ScheduledExecutorService getSingleThreadPool() {
        return this.window.threadPool;
    }

    public JFrame getFrame() {
        return this.window.parentJframe;
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
