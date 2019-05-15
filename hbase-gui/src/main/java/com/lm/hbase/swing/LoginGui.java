
package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.HttpURLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.lm.hbase.adapter.HbaseUtil;
import com.lm.hbase.common.Env;
import com.lm.hbase.conf.HbaseClientConf;
import com.lm.hbase.conf.RemoteDriverProp;
import com.lm.hbase.driver.DownloadDriver;
import com.lm.hbase.driver.DriverClassLoader;
import com.lm.hbase.util.DirectoryUtil;
import com.lm.hbase.util.StringUtil;
import com.lm.hbase.util.network.HttpURLConnectionFactory;

public class LoginGui extends JDialog {

    private static final long       serialVersionUID          = 7686127697988572348L;

    public ScheduledExecutorService threadPool                = Executors.newSingleThreadScheduledExecutor();

    private JPanel                  contentPanel              = new JPanel();
    private JTextField              zkPortField;
    private JTextField              zkQuorumField;
    private JTextField              hbaseMasterField;
    private JTextField              znodeParentField;
    private JTextField              mavenHomeField;

    private JButton                 testButton                = new JButton("Test Connection");
    private JButton                 cancelButton              = new JButton("close");
    private JButton                 okButton                  = new JButton("connect");
    private JButton                 reloadDriverVersionButton = new JButton(new ImageIcon(Env.IMG_DIR + "Update.png"));

    private JComboBox<String>       driverVersionComboBox;

    public JLabel                   progressInfoLabel         = new JLabel();
    public JProgressBar             processBar                = new JProgressBar();
    public JLabel                   stopLabel                 = new JLabel(new ImageIcon(Env.IMG_DIR + "stop.png"));;

    public static void openDialog() {
        try {
            // 读取配置文件
            String zkPort = HbaseClientConf.getStringValue("hbase.zk.port");
            String zkQuorum = HbaseClientConf.getStringValue("hbase.zk.quorum");
            String hbaseMaster = HbaseClientConf.getStringValue("hbase.master");
            String znodeParent = HbaseClientConf.getStringValue("znode.parent");
            String hbaseVersion = HbaseClientConf.getStringValue("hbase.version");
            String mavenHome = HbaseClientConf.getStringValue("maven.home");

            com.lm.hbase.swing.SwingConstants.loginGui = new LoginGui(zkPort, zkQuorum, hbaseMaster, znodeParent,
                                                                      hbaseVersion, mavenHome);
            com.lm.hbase.swing.SwingConstants.loginGui.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public LoginGui(String zkPort, String zkQuorum, String hbaseMaster, String znodeParent, String hbaseVersion,
                    String mavenHome){
        setTitle("配置Hbase");
        setBounds(100, 100, 500, 310);
        this.setMinimumSize(new Dimension(500, 310));
        this.setResizable(false);// 禁止拉边框拉长拉短
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 ColumnSpec.decode("default:grow"),
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC,
                                                                 FormSpecs.RELATED_GAP_COLSPEC,
                                                                 FormSpecs.DEFAULT_COLSPEC, },
                                              new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                                                              FormSpecs.RELATED_GAP_ROWSPEC,
                                                              FormSpecs.DEFAULT_ROWSPEC }));
        {
            JLabel lblNewLabel = new JLabel("ZK.PORT");
            lblNewLabel.setToolTipText("hbase.zookeeper.property.clientPort");
            contentPanel.add(lblNewLabel, "6, 4, right, default");
        }
        {
            zkPortField = new JTextField();
            contentPanel.add(zkPortField, "10, 4, 5, 1, fill, default");
            zkPortField.setColumns(10);
            zkPortField.setText(zkPort);
        }
        {
            JLabel lblNewLabel_1 = new JLabel("ZK.QUORUM");
            lblNewLabel_1.setToolTipText("hbase.zookeeper.quorum");
            contentPanel.add(lblNewLabel_1, "6, 8, right, default");
        }
        {
            zkQuorumField = new JTextField();
            contentPanel.add(zkQuorumField, "10, 8, 5, 1, fill, default");
            zkQuorumField.setColumns(10);
            zkQuorumField.setText(zkQuorum);
        }
        {
            JLabel lblNewLabel_2 = new JLabel("HBASE.MASTER");
            lblNewLabel_2.setToolTipText("hbase.master");
            contentPanel.add(lblNewLabel_2, "6, 12, right, default");
        }
        {
            hbaseMasterField = new JTextField();
            contentPanel.add(hbaseMasterField, "10, 12, 5, 1, fill, default");
            hbaseMasterField.setColumns(10);
            hbaseMasterField.setText(hbaseMaster);
        }
        {
            JLabel lblNewLabel_3 = new JLabel("ZNODE.PARENT");
            lblNewLabel_3.setToolTipText("zookeeper.znode.parent");
            contentPanel.add(lblNewLabel_3, "6, 14, right, default");
        }
        {
            znodeParentField = new JTextField();
            contentPanel.add(znodeParentField, "10, 14, 5, 1, fill, default");
            znodeParentField.setColumns(10);
            znodeParentField.setText(znodeParent);
        }
        {
            JLabel lblNewLabel_4 = new JLabel("Maven Home");
            contentPanel.add(lblNewLabel_4, "6, 16, right, default");
        }
        {
            mavenHomeField = new JTextField();
            contentPanel.add(mavenHomeField, "10, 16, 5, 1, fill, default");
            mavenHomeField.setColumns(10);
            mavenHomeField.setText(mavenHome);
        }
        {
            JLabel lblNewLabel_4 = new JLabel("Hbase Version");
            lblNewLabel_4.setToolTipText("切换版本需要重启应用");
            contentPanel.add(lblNewLabel_4, "6, 18, right, default");
        }
        {
            driverVersionComboBox = new JComboBox<>();
            driverVersionComboBox.addItem("");
            int confVersionIndex = 0;
            for (String item : RemoteDriverProp.getKeys()) {
                confVersionIndex++;
                driverVersionComboBox.addItem(item);
                if (item.equalsIgnoreCase(hbaseVersion)) {
                    driverVersionComboBox.setSelectedIndex(confVersionIndex);
                    asyncLoadDriver(hbaseVersion, false);
                }
            }
            driverVersionComboBox.addItemListener(new VersionListener());

            contentPanel.add(driverVersionComboBox, "10, 18, 1, 1, fill, default");

            reloadDriverVersionButton.addMouseListener(new ReloadVersion());
            contentPanel.add(reloadDriverVersionButton, "10, 18, 5, 1, right, default");
        }

        {
            JPanel buttonPane = new JPanel();
            buttonPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("130px"),
                                                                   ColumnSpec.decode("150px"),
                                                                   FormSpecs.RELATED_GAP_COLSPEC,
                                                                   ColumnSpec.decode("80px"),
                                                                   FormSpecs.RELATED_GAP_COLSPEC,
                                                                   ColumnSpec.decode("130px"), },
                                                new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC,
                                                                FormSpecs.DEFAULT_ROWSPEC,
                                                                FormSpecs.RELATED_GAP_ROWSPEC,
                                                                FormSpecs.DEFAULT_ROWSPEC, }));
            {
                testButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        testButton.setEnabled(false);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (!checkConnectParam(zkPortField.getText(), zkQuorumField.getText(),
                                               hbaseMasterField.getText(), znodeParentField.getText(),
                                               driverVersionComboBox.getSelectedItem().toString(),
                                               mavenHomeField.getText())) {
                            testButton.setEnabled(true);
                            return;
                        }

                        startTask();

                        threadPool.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    String clusterStatus = HandleCore.testConf(zkPortField.getText(),
                                                                               zkQuorumField.getText(),
                                                                               hbaseMasterField.getText(),
                                                                               znodeParentField.getText(),
                                                                               driverVersionComboBox.getSelectedItem().toString(),
                                                                               mavenHomeField.getText());
                                    if (clusterStatus != null) {
                                        JOptionPane.showMessageDialog(contentPanel, "连接成功,集群信息如下\n" + clusterStatus,
                                                                      "提示", JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        JOptionPane.showMessageDialog(contentPanel, "连接失败", "错误",
                                                                      JOptionPane.ERROR_MESSAGE);
                                    }

                                } catch (Exception e2) {
                                    JOptionPane.showMessageDialog(contentPanel, "连接失败.\n" + e2.getLocalizedMessage(),
                                                                  "错误", JOptionPane.ERROR_MESSAGE);
                                } finally {
                                    endTask();
                                }

                            }

                        });

                    }
                });
                buttonPane.add(testButton, "1, 2, left, top");
            }
            {
                cancelButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        System.exit(0);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton, "4, 2, left, top");
            }
            {
                okButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        okButton.setEnabled(false);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        synchronized (com.lm.hbase.swing.SwingConstants.loginGui) {
                            if (!com.lm.hbase.swing.SwingConstants.loginGui.isVisible()) {
                                return;
                            }
                            if (!checkConnectParam(zkPortField.getText(), zkQuorumField.getText(),
                                                   hbaseMasterField.getText(), znodeParentField.getText(),
                                                   driverVersionComboBox.getSelectedItem().toString(),
                                                   mavenHomeField.getText())) {
                                okButton.setEnabled(true);
                                return;
                            }

                            startTask();
                            stopLabel.setEnabled(true);
                            processBar.setIndeterminate(true);

                            threadPool.execute(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        String clusterStatus = HandleCore.testConf(zkPortField.getText(),
                                                                                   zkQuorumField.getText(),
                                                                                   hbaseMasterField.getText(),
                                                                                   znodeParentField.getText(),
                                                                                   driverVersionComboBox.getSelectedItem().toString(),
                                                                                   mavenHomeField.getText());
                                        if (clusterStatus != null) {
                                            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(false);// 隐藏登陆窗体
                                            com.lm.hbase.swing.SwingConstants.hbaseGui.initialize();// 唤出主窗体
                                        } else {
                                            JOptionPane.showMessageDialog(contentPanel, "连接失败");
                                        }

                                    } catch (Exception e2) {
                                        JOptionPane.showMessageDialog(contentPanel,
                                                                      "连接失败.\n" + e2.getLocalizedMessage());
                                    } finally {
                                        endTask();
                                    }

                                }
                            });

                        }
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton, "6, 2, left, top");
                getRootPane().setDefaultButton(okButton);
            }
            {
                JPanel processPanel = new JPanel();
                processPanel.setLayout(new FlowLayout(3));

                stopLabel.addMouseListener(new StopEvent());
                stopLabel.setEnabled(false);

                processBar.setIndeterminate(false);

                processPanel.add(stopLabel, 0);
                processPanel.add(processBar, 1);
                processPanel.add(progressInfoLabel, 2);

                buttonPane.add(processPanel, "1,4,6,1, fill, fill");
            }
        }

        this.addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                try {
                    HbaseUtil.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                super.windowClosed(e);

                System.exit(0);

            }

        });
    }

    public void asyncLoadDriver(String version, boolean reload) {
        startTask();
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    loadDriver(version, reload);
                } finally {
                    endTask();
                }

            }
        });
    }

    public void loadDriver(String version, boolean reload) {

        String outputDir = Env.DRIVER_DIR + version;
        File outputFileDir = new File(outputDir);
        if (!outputFileDir.exists()) {
            // TODO 0、下载适配程序 1、加载驱动；2、添加设置maven目录的功能；3、现实加载进度；4、异步
            // 加载hbase client驱动
            try {
                String mavenHome = mavenHomeField == null ? null : mavenHomeField.getText();
                if (StringUtil.isEmpty(mavenHome)) {
                    mavenHome = HbaseClientConf.getStringValue("maven.home");
                }
                if (StringUtil.isEmpty(mavenHome)) {
                    JOptionPane.showMessageDialog(this, "请设置MavenHome", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                DownloadDriver.load(version, mavenHome, progressInfoLabel);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }

            // 加载适配程序
            try {
                progressInfoLabel.setText("download adapter jar ....");
                loadHbaseAdapterJar(version);
                progressInfoLabel.setText("download adapter jar success");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            if (reload) {
                DirectoryUtil.delFolder(outputDir);
                loadDriver(version, false);
            }
        }

        progressInfoLabel.setText("Classloader load all jars  ....");
        DriverClassLoader.loadClasspath(version);
        progressInfoLabel.setText("The jar necessary for the  " + version + " version is loaded");
    }

    public void loadHbaseAdapterJar(String version) throws Throwable {
        System.out.print("download adapter jar ....");
        String url = RemoteDriverProp.getStringValue(version);
        String outputDir = Env.DRIVER_DIR + version;
        HttpURLConnection con = HttpURLConnectionFactory.getConn(url);
        HttpURLConnectionFactory.downloadFile(con, outputDir, "hbaes-core-adapter-" + version + ".jar");
        System.out.println("ok");
    }

    public boolean checkConnectParam(String zkPort, String zkQuorum, String hbaseMaster, String znodeParent,
                                     String version, String mavenHome) {
        if (StringUtil.isEmpty(zkPort)) {
            JOptionPane.showMessageDialog(this, "请设置zkPort", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (StringUtil.isEmpty(zkQuorum)) {
            JOptionPane.showMessageDialog(this, "请设置zkQuorum", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (StringUtil.isEmpty(znodeParent)) {
            JOptionPane.showMessageDialog(this, "请设置znodeParent", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (StringUtil.isEmpty(version)) {
            JOptionPane.showMessageDialog(this, "请设置hbaseVersion", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (StringUtil.isEmpty(mavenHome)) {
            JOptionPane.showMessageDialog(this, "请设置mavenHome", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;

    }

    /**
     * 类LoginGui.java的实现描述：添加版本变更监听
     * 
     * @author limin May 8, 2019 11:54:43 PM
     */
    class VersionListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String version = e.getItem().toString();
                asyncLoadDriver(version, false);
                // TODO 需要重启应用
                // TODO 如果 DESELECTED 的Item是NULL则不重启

            }

        }

    }

    /**
     * 类LoginGui.java的实现描述：重新从网络载入配置
     * 
     * @author limin May 9, 2019 12:45:04 AM
     */
    class ReloadVersion extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            startTask();
            reloadDriverVersionButton.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (reloadDriverVersionButton.isEnabled()) {
                return;
            }

            if (StringUtil.isEmpty(mavenHomeField.getText())) {
                JOptionPane.showMessageDialog(contentPanel, "请设置MavenHome", "错误", JOptionPane.ERROR_MESSAGE);
                endTask();
                return;
            }

            String version = driverVersionComboBox.getSelectedItem().toString();
            if (StringUtil.isEmpty(version)) {
                JOptionPane.showMessageDialog(contentPanel, "请设置hbaseVersion", "错误", JOptionPane.ERROR_MESSAGE);
                reloadDriverVersionButton.setEnabled(true);
                endTask();
                return;
            }

            asyncLoadDriver(version, true);
        }

    }

    class StopEvent extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            // 终止正在跑任务的线程
            if (!threadPool.isShutdown() || !threadPool.isTerminated()) {
                threadPool.shutdownNow();
                threadPool = Executors.newSingleThreadScheduledExecutor();
            }

            endTask();
        }
    }

    private void endTask() {
        reloadDriverVersionButton.setEnabled(true);
        testButton.setEnabled(true);
        cancelButton.setEnabled(true);
        okButton.setEnabled(true);
        processBar.setIndeterminate(false);
        stopLabel.setEnabled(false);
    }

    private void startTask() {
        reloadDriverVersionButton.setEnabled(false);
        testButton.setEnabled(false);
        cancelButton.setEnabled(false);
        okButton.setEnabled(false);
        processBar.setIndeterminate(true);
        stopLabel.setEnabled(true);
    }

}
