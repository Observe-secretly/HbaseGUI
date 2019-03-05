package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.lm.hbase.HbaseUtil;
import com.lm.hbase.tab.CreateTab;
import com.lm.hbase.tab.MetaDataTab;
import com.lm.hbase.tab.QueryTab;
import com.lm.hbase.tab.TabInterface;

public class HbaseGui {

    public JFrame                   parentJframe;

    public ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();

    public JProgressBar             processBar;

    public JButton                  stopButton;

    /**
     * 所有的tabs
     */
    private List<TabInterface>      tabs       = new ArrayList<>();

    /**
     * Launch the application.
     * 
     * @wbp.parser.entryPoint
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {

                try {
                    // WebLookAndFeel.install();
                    String lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                    UIManager.setLookAndFeel(lookAndFeel);

                    com.lm.hbase.swing.SwingConstants.hbaseGui = new HbaseGui();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * 
     * @wbp.parser.entryPoint
     */
    public HbaseGui(){
        // 弹出登陆框
        LoginGui.openDialog();
        // initialize();
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @wbp.parser.entryPoint
     */
    public void initialize() {
        parentJframe = new JFrame();
        parentJframe.setTitle("Hbase Gui");
        parentJframe.setBounds(10, 10, 1400, 800);
        parentJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentJframe.getContentPane().setLayout(new BorderLayout(0, 0));
        parentJframe.setResizable(false);// 禁止拉边框拉长拉短

        JPanel panel = new JPanel();
        parentJframe.getContentPane().add(panel, BorderLayout.SOUTH);

        // 初始化进度条
        processBar = new JProgressBar(JProgressBar.CENTER);

        // 创建一个终止按钮
        stopButton = new JButton("停止");
        stopButton.setEnabled(false);
        stopButton.addMouseListener(new StopEvent());

        panel.add(stopButton);
        panel.add(processBar);

        JTabbedPane tabbedPanel = new JTabbedPane(JTabbedPane.TOP);
        tabbedPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        parentJframe.getContentPane().add(tabbedPanel, BorderLayout.CENTER);

        TabInterface queryTab = new QueryTab(this);
        registerTab(queryTab, tabbedPanel);

        TabInterface metaDataTab = new MetaDataTab(this);
        registerTab(metaDataTab, tabbedPanel);

        TabInterface createTab = new CreateTab(this);
        registerTab(createTab, tabbedPanel);

        parentJframe.setVisible(true);

        // 添加关闭事件
        parentJframe.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    HbaseUtil.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                super.windowClosing(e);
            }

        });
    }

    private void registerTab(TabInterface tab, JTabbedPane panel) {
        panel.addTab(tab.getTitle(), tab.getIcon(), tab.getComponent(), tab.getTip());
        this.tabs.add(tab);
    }

    class StopEvent extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            // 终止正在跑任务的线程
            if (!threadPool.isShutdown() || !threadPool.isTerminated()) {
                threadPool.shutdownNow();
                threadPool = Executors.newSingleThreadScheduledExecutor();
            }

            // 禁用进度条
            stopButton.setEnabled(false);
            processBar.setIndeterminate(false);

            // 解禁tab被禁用的按钮
            for (TabInterface tab : tabs) {
                tab.enableAll();
            }
        }

    }

}
