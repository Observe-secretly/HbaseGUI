
package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

import com.lm.hbase.adapter.FilterFactoryInterface;
import com.lm.hbase.adapter.HbaseAdapterInterface;
import com.lm.hbase.common.CommonConstons;
import com.lm.hbase.common.Env;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.conf.ConfItem;
import com.lm.hbase.conf.RemoteDriverProp;
import com.lm.hbase.driver.DownloadDriver;
import com.lm.hbase.util.DirectoryUtil;
import com.lm.hbase.util.StringUtil;
import com.lm.hbase.util.network.HttpURLConnectionFactory;

public class LoginGui extends JDialog {

    private static final long       serialVersionUID          = 7686127697988572348L;

    public ScheduledExecutorService threadPool                = Executors.newSingleThreadScheduledExecutor();

    private JPanel                  confsPanel                = new JPanel();
    private JList<ConfItem>         confsList                 = new JList<>();
    private JButton                 addConfBut                = new JButton(ImageIconConstons.ADD_ICON);
    private JButton                 removeConfBut             = new JButton(ImageIconConstons.GARBAGE_ICON);

    /*
     * 存放除配置列表外的所有控件
     */
    private JPanel                  contentPanel              = new JPanel();
    private JTextField              confNameField;
    private JTextField              zkPortField;
    private JTextField              zkQuorumField;
    private JTextField              hbaseMasterField;
    private JTextField              znodeParentField;
    private JTextField              mavenHomeField;

    private JButton                 testButton                = new JButton("Test");
    private JButton                 cancelButton              = new JButton("Close");
    private JButton                 okButton                  = new JButton("Connect");
    private JButton                 reloadDriverVersionButton = new JButton(ImageIconConstons.UPDATE_ICON);

    private JComboBox<String>       driverVersionComboBox;

    public JLabel                   progressInfoLabel         = new JLabel("Please select configuration");
    public JProgressBar             processBar                = new JProgressBar();
    public JLabel                   stopLabel                 = new JLabel(ImageIconConstons.STOP_ICON);

    public static void openDialog() {
        try {
            if (com.lm.hbase.swing.SwingConstants.loginGui == null) {
                com.lm.hbase.swing.SwingConstants.loginGui = new LoginGui();
            }

            com.lm.hbase.swing.SwingConstants.loginGui.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public LoginGui(){
        setTitle("配置Hbase");
        int width = 680;
        int height = 360;
        setBounds(100, 100, width, height);
        this.setMinimumSize(new Dimension(width, height));
        this.setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(confsPanel, BorderLayout.WEST);
        {

            confsPanel.setLayout(new BorderLayout());

            confsList.setBorder(new TitledBorder("配置列表"));

            confsList.setFixedCellWidth(150);
            // 设置为单选模式
            confsList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            List<ConfItem> confs = getConfItems();
            confsList.setListData(getConfItems().toArray(new ConfItem[confs.size()]));
            confsList.addListSelectionListener(new ConfListListener());

            JScrollPane jlistScroll = new JScrollPane(confsList);
            jlistScroll.setLayout(new ScrollPaneLayout());

            confsPanel.add(jlistScroll, BorderLayout.CENTER);

            // 设置添加/删除配置文件按钮
            JPanel modifyConfPanel = new JPanel();
            confsPanel.add(modifyConfPanel, BorderLayout.SOUTH);

            addConfBut.addMouseListener(new AddConfAdapter());
            removeConfBut.addMouseListener(new DeleteConfAdapter());

            modifyConfPanel.add(addConfBut);
            modifyConfPanel.add(removeConfBut);

        }

        getContentPane().add(contentPanel, BorderLayout.EAST);
        contentPanel.setLayout(new FlowLayout());

        JPanel formPanel = new JPanel();
        contentPanel.add(formPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.setLayout(new GridBagLayout());

        {
            JLabel lblNewLabel = new JLabel("配置名称");
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel, gbc);
        }
        {
            confNameField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(confNameField);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel = new JLabel("ZK.PORT");
            lblNewLabel.setToolTipText("hbase.zookeeper.property.clientPort");
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel, gbc);
        }
        {
            zkPortField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(zkPortField);
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel_1 = new JLabel("ZK.QUORUM");
            lblNewLabel_1.setToolTipText("hbase.zookeeper.quorum");
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel_1, gbc);
        }
        {
            zkQuorumField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(zkQuorumField);
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel_2 = new JLabel("HBASE.MASTER");
            lblNewLabel_2.setToolTipText("hbase.master");
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel_2, gbc);
        }
        {
            hbaseMasterField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(hbaseMasterField);
            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel_3 = new JLabel("ZNODE.PARENT");
            lblNewLabel_3.setToolTipText("zookeeper.znode.parent");
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel_3, gbc);
        }
        {
            znodeParentField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(znodeParentField);
            gbc.gridx = 1;
            gbc.gridy = 4;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel_4 = new JLabel("Maven Home");
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel_4, gbc);
        }
        {
            mavenHomeField = new JTextField(32);
            JPanel componentPanel = new JPanel();
            componentPanel.add(mavenHomeField);
            gbc.gridx = 1;
            gbc.gridy = 5;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(componentPanel, gbc);
        }
        {
            JLabel lblNewLabel_4 = new JLabel("Hbase Version");
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(lblNewLabel_4, gbc);
        }
        {
            driverVersionComboBox = new JComboBox<>();
            Dimension dimension = driverVersionComboBox.getPreferredSize();
            dimension.setSize(width * 0.5, dimension.getHeight());
            driverVersionComboBox.setPreferredSize(dimension);
            driverVersionComboBox.addItem("");
            for (String item : RemoteDriverProp.getKeys()) {
                driverVersionComboBox.addItem(item);
            }
            driverVersionComboBox.addItemListener(new VersionListener());

            reloadDriverVersionButton.addMouseListener(new ReloadVersion());

            JPanel versionPanel = new JPanel();
            versionPanel.add(driverVersionComboBox);
            versionPanel.add(reloadDriverVersionButton);

            gbc.gridx = 1;
            gbc.gridy = 6;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            formPanel.add(versionPanel, gbc);

        }

        {

            {
                testButton.setEnabled(false);
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
                                    String clusterStatus = HandleCore.testConf(confNameField.getText(),
                                                                               zkPortField.getText(),
                                                                               zkQuorumField.getText(),
                                                                               hbaseMasterField.getText(),
                                                                               znodeParentField.getText(),
                                                                               driverVersionComboBox.getSelectedItem().toString(),
                                                                               mavenHomeField.getText());
                                    if (clusterStatus != null) {
                                        System.out.println(SwingConstants.hbaseAdapter.getVersion());
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

                gbc.gridx = 0;
                gbc.gridy = 7;
                gbc.gridwidth = 2;
                gbc.gridheight = 1;
                gbc.weightx = 0;
                gbc.weighty = 0;
                formPanel.add(testButton, gbc);

            }
            {
                cancelButton.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (SwingConstants.parentFrameIsInit) {
                            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(false);
                        } else {
                            System.exit(0);
                        }
                    }
                });
                cancelButton.setActionCommand("Cancel");

                gbc.gridx = 2;
                gbc.gridy = 7;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.weightx = 0;
                gbc.weighty = 0;
                formPanel.add(cancelButton, gbc);
            }
            {
                okButton.setEnabled(false);
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

                            threadPool.execute(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        String clusterStatus = HandleCore.testConf(confNameField.getText(),
                                                                                   zkPortField.getText(),
                                                                                   zkQuorumField.getText(),
                                                                                   hbaseMasterField.getText(),
                                                                                   znodeParentField.getText(),
                                                                                   driverVersionComboBox.getSelectedItem().toString(),
                                                                                   mavenHomeField.getText());
                                        if (clusterStatus != null) {
                                            com.lm.hbase.swing.SwingConstants.loginGui.setVisible(false);// 隐藏登陆窗体
                                            com.lm.hbase.swing.SwingConstants.hbaseGui.initialize();// 唤出主窗体
                                        } else {
                                            JOptionPane.showMessageDialog(contentPanel, "连接失败", "错误",
                                                                          JOptionPane.ERROR_MESSAGE);
                                        }

                                    } catch (Exception e2) {
                                        JOptionPane.showMessageDialog(contentPanel,
                                                                      "连接失败.\n" + e2.getLocalizedMessage(), "错误",
                                                                      JOptionPane.ERROR_MESSAGE);
                                    } finally {
                                        endTask();
                                    }

                                }
                            });

                        }
                    }
                });
                okButton.setActionCommand("OK");

                gbc.gridx = 3;
                gbc.gridy = 7;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.weightx = 0;
                gbc.weighty = 0;
                formPanel.add(okButton, gbc);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JPanel processPanel = new JPanel();
                processPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                stopLabel.addMouseListener(new StopEvent());
                stopLabel.setEnabled(false);
                stopLabel.setVisible(false);

                processBar.setIndeterminate(false);
                processBar.setVisible(false);

                processPanel.add(stopLabel);
                processPanel.add(processBar);
                processPanel.add(progressInfoLabel);
                processPanel.add(new JLabel(" "));// 占位

                gbc.gridx = 0;
                gbc.gridy = 8;
                gbc.gridwidth = 4;
                gbc.gridheight = 1;
                gbc.weightx = 0;
                gbc.weighty = 0;
                formPanel.add(processPanel, gbc);
            }
        }

        this.addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                if (SwingConstants.parentFrameIsInit) {
                    com.lm.hbase.swing.SwingConstants.loginGui.setVisible(false);
                } else {
                    super.windowClosed(e);
                    System.exit(0);
                }
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
                    // 解禁按钮
                    testButton.setEnabled(true);
                    okButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endTask();
                }

            }
        });
    }

    public void loadDriver(String version, boolean reload) {
        if (StringUtil.isEmpty(version)) {
            return;
        }

        JarClassLoader jcl = SwingConstants.driverMap.get(version);
        if (!reload && jcl != null) {
            loadCore(jcl);
            return;
        }

        String outputDir = Env.DRIVER_DIR + version;
        File outputFileDir = new File(outputDir);
        if (!outputFileDir.exists()) {
            // 加载hbase client驱动
            try {
                String mavenHome = mavenHomeField == null ? null : mavenHomeField.getText();
                if (StringUtil.isEmpty(mavenHome) && SwingConstants.selectedConf != null) {
                    mavenHome = SwingConstants.selectedConf.getStringValue("maven.home");
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
                SwingConstants.driverMap.put(version, null);
                loadDriver(version, false);
            }
        }

        progressInfoLabel.setText("Classloader load all jars  ....");
        // 从目录加载驱动以及适配器所有jar包
        jcl = new JarClassLoader();
        jcl.add(Env.DRIVER_DIR + version + "/");
        SwingConstants.driverMap.put(version, jcl);
        SwingConstants.version = version;
        progressInfoLabel.setText("The jar necessary for the  " + version + " version is loaded");
        loadCore(jcl);
    }

    public void loadCore(JarClassLoader jcl) {
        JclObjectFactory factory = JclObjectFactory.getInstance();

        SwingConstants.hbaseAdapter = (HbaseAdapterInterface) factory.create(jcl, "com.lm.hbase.adapter.HbaseAdapter");
        SwingConstants.filterFactory = (FilterFactoryInterface) factory.create(jcl,
                                                                               "com.lm.hbase.adapter.FilterFactory");
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
        addConfBut.setEnabled(true);
        removeConfBut.setEnabled(true);
        reloadDriverVersionButton.setEnabled(true);
        testButton.setEnabled(true);
        cancelButton.setEnabled(true);
        okButton.setEnabled(true);
        processBar.setIndeterminate(false);
        stopLabel.setEnabled(false);
        processBar.setVisible(false);
        stopLabel.setVisible(false);
    }

    private void startTask() {
        addConfBut.setEnabled(false);
        removeConfBut.setEnabled(false);
        reloadDriverVersionButton.setEnabled(false);
        testButton.setEnabled(false);
        cancelButton.setEnabled(false);
        okButton.setEnabled(false);
        processBar.setIndeterminate(true);
        processBar.setVisible(true);
        stopLabel.setEnabled(true);
        stopLabel.setVisible(true);
    }

    /**
     * 获取Hbase配置列表
     * 
     * @return
     */
    private List<ConfItem> getConfItems() {
        List<ConfItem> confList = new ArrayList<>();
        File confDir = new File(Env.HBASE_CONN_CONF_DIR);
        if (!confDir.exists()) {
            confDir.mkdir();
        }
        for (File item : confDir.listFiles()) {
            if (item.isFile() && item.getName().startsWith(CommonConstons.HBASE_CONF_FILE_PREFIX)) {
                confList.add(new ConfItem(item.getName()));
            }
        }

        return confList;
    }

    private void cleanConf() {
        confNameField.setText("");
        zkPortField.setText("");
        zkQuorumField.setText("");
        hbaseMasterField.setText("");
        znodeParentField.setText("");
        mavenHomeField.setText("");
    }

    /**
     * 添加配置
     * 
     * @author limin May 29, 2019 12:52:24 AM
     */
    class AddConfAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            cleanConf();
            // 生成一个配置文件
            File confDir = new File(Env.HBASE_CONN_CONF_DIR);
            if (!confDir.exists()) {
                confDir.mkdir();
            }
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                // New Favorite
                File confFile = new File(Env.HBASE_CONN_CONF_DIR + CommonConstons.HBASE_CONF_FILE_PREFIX + i + ".conf");
                if (!confFile.exists()) {
                    try {
                        // 刷新Jlist
                        DefaultListModel model = new DefaultListModel();
                        for (ConfItem confs : getConfItems()) {
                            model.addElement(confs);
                        }
                        // 创建配置文件
                        confFile.createNewFile();
                        model.addElement(new ConfItem(confFile.getName()));
                        confsList.setModel(model);
                        confsList.updateUI();

                        confsList.setSelectedIndex(model.getSize() - 1);

                        return;

                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return;
                    }
                }

            }

        }

    }

    /**
     * 删除配置文件
     * 
     * @author limin May 29, 2019 12:56:50 AM
     */
    class DeleteConfAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            // 删除配置
            ConfItem conf = confsList.getSelectedValue();
            File confFile = new File(conf.getConfFilePath());
            confFile.delete();

            // 清空全局配置
            SwingConstants.selectedConf = null;

            // 清空UI中的配置信息
            cleanConf();

            // 重新加载配置列表
            List<ConfItem> confs = getConfItems();
            confsList.setListData(getConfItems().toArray(new ConfItem[confs.size()]));

            // 禁用按钮
            testButton.setEnabled(false);
            okButton.setEnabled(false);

        }
    }

    /**
     * 切换配置时的监听
     * 
     * @author limin May 29, 2019 12:20:22 AM
     */
    class ConfListListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                // 清空UI中的配置信息
                cleanConf();
                // 重新设置全局配置
                SwingConstants.selectedConf = confsList.getSelectedValue();
                if (SwingConstants.selectedConf == null) {
                    return;
                }

                // 从选定的配置文件加载配置，渲染到UI上。并加载
                String zkPort = SwingConstants.selectedConf.getStringValue("hbase.zk.port");
                String zkQuorum = SwingConstants.selectedConf.getStringValue("hbase.zk.quorum");
                String hbaseMaster = SwingConstants.selectedConf.getStringValue("hbase.master");
                String znodeParent = SwingConstants.selectedConf.getStringValue("znode.parent");
                String hbaseVersion = SwingConstants.selectedConf.getStringValue("hbase.version");
                String mavenHome = SwingConstants.selectedConf.getStringValue("maven.home");

                confNameField.setText(SwingConstants.selectedConf.getDisplayName());
                zkPortField.setText(zkPort);
                zkQuorumField.setText(zkQuorum);
                hbaseMasterField.setText(hbaseMaster);
                znodeParentField.setText(znodeParent);
                mavenHomeField.setText(mavenHome);

                for (int i = 0; i < driverVersionComboBox.getModel().getSize(); i++) {
                    if (driverVersionComboBox.getModel().getElementAt(i).equalsIgnoreCase(hbaseVersion)) {
                        driverVersionComboBox.setSelectedIndex(i);
                        asyncLoadDriver(hbaseVersion, false);
                    }

                }

                // 禁用按钮
                testButton.setEnabled(false);
                okButton.setEnabled(false);
            }
        }

    }
}
