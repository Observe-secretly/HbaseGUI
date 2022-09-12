package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.tab.CreateTab;
import com.lm.hbase.tab.MetaDataTab;
import com.lm.hbase.tab.QueryTab;
import com.lm.hbase.tab.TabInterface;
import com.lm.hbase.tab.TableInfoTab;

public class HbaseGui {

    public JFrame                   parentJframe;

    public ScheduledExecutorService threadPool        = Executors.newSingleThreadScheduledExecutor();

    public JProgressBar             processBar;

    public JLabel                   stopLabel;

    public JLabel                   versionLabel;

    public JButton                  switchBut;

    /**
     * 所有的tabs
     */
    private List<TabInterface>      tabs              = new ArrayList<>();

    public static final String      DARCULA_LAF_CLASS = "com.bulenkov.darcula.DarculaLaf";           // $NON-NLS-1$

    /**
     * Launch the application.
     * 
     * @throws Throwable
     * @wbp.parser.entryPoint
     */
    public static void main(String[] args) throws Throwable {
        // FrameDock.setDockIconImage();

        EventQueue.invokeLater(new Runnable() {

            public void run() {

                try {
                    // UIManager.installLookAndFeel(DARCULA_LAF, DARCULA_LAF_CLASS);
                    UIManager.setLookAndFeel(DARCULA_LAF_CLASS);

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
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @wbp.parser.entryPoint
     */
    public void initialize() {
        SwingConstants.parentFrameIsInit = true;
        if (parentJframe != null) {
            parentJframe.dispose();
        }
        parentJframe = new JFrame();
        parentJframe.setTitle("Hbase Gui");
        parentJframe.setBounds(10, 10, 1450, 800);
        parentJframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        parentJframe.getContentPane().setLayout(new BorderLayout(0, 0));
        parentJframe.setMinimumSize(new Dimension(1450, 400));
        parentJframe.setIconImage(ImageIconConstons.DOCK_ICON.getImage());
        // parentJframe.setResizable(false);// 禁止拉边框拉长拉短

        JLabel logoLabel = new JLabel(ImageIconConstons.HBASE_LOGO_ICON);
        parentJframe.getContentPane().add(logoLabel, BorderLayout.NORTH);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BorderLayout());
        parentJframe.getContentPane().add(footerPanel, BorderLayout.SOUTH);

        // 初始化进度条
        processBar = new JProgressBar(JProgressBar.CENTER);
        processBar.setVisible(false);

        // 创建一个终止按钮
        stopLabel = new JLabel(ImageIconConstons.STOP_ICON);
        stopLabel.setEnabled(false);
        stopLabel.setVisible(false);
        stopLabel.addMouseListener(new StopEvent());

        // 切换登录按钮
        switchBut = new JButton(ImageIconConstons.SWITCH_ICON);
        switchBut.addMouseListener(new SwitchEvent());

        //
        versionLabel = new JLabel("Version 1.2.5");

        JPanel footerCenterPanel = new JPanel();

        footerCenterPanel.add(stopLabel);
        footerCenterPanel.add(processBar);
        footerPanel.add(footerCenterPanel, BorderLayout.CENTER);

        footerPanel.add(versionLabel, BorderLayout.WEST);
        footerPanel.add(switchBut, BorderLayout.EAST);

        JTabbedPane tabbedPanel = new JTabbedPane(JTabbedPane.TOP);
        tabbedPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        parentJframe.getContentPane().add(tabbedPanel, BorderLayout.CENTER);

        TabInterface tableInfoTab = new TableInfoTab(this);
        registerTab(tableInfoTab, tabbedPanel);

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
                    SwingConstants.hbaseAdapter.close();
                } catch (Exception e1) {
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
            stopLabel.setEnabled(false);
            processBar.setIndeterminate(false);

            // 解禁tab被禁用的按钮
            for (TabInterface tab : tabs) {
                tab.enableAll();
            }
        }

    }

    class SwitchEvent extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            LoginGui.openDialog();
        }

    }

}
