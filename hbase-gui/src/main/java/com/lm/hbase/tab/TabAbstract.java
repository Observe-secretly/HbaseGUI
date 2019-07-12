package com.lm.hbase.tab;

import java.awt.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.lm.hbase.swing.HbaseGui;

public abstract class TabAbstract extends TabCommonUtil implements TabInterface {

    public static final String PROPERTIES_SUFFIX = ".MATA";

    protected HbaseGui         window;

    public TabAbstract(HbaseGui window){
        this.window = window;
    }

    public void exceptionAlert(Exception e) {
        stopTask();
        JOptionPane.showMessageDialog(window.parentJframe, e, "异常", JOptionPane.ERROR_MESSAGE);
    }

    public synchronized void startTask() {
        disableAll();
        window.processBar.setVisible(true);
        window.processBar.setIndeterminate(true);
        window.stopLabel.setVisible(true);
        window.stopLabel.setEnabled(true);
    }

    public synchronized void stopTask() {
        enableAll();
        window.processBar.setVisible(false);
        window.processBar.setIndeterminate(false);
        window.stopLabel.setVisible(false);
        window.stopLabel.setEnabled(false);
        this.window.threadPool.shutdownNow();
        this.window.threadPool = Executors.newSingleThreadScheduledExecutor();
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
