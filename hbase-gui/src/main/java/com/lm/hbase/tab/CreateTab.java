package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lm.hbase.adapter.HbaseUtil;
import com.lm.hbase.common.Env;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.StringUtil;

public class CreateTab extends TabAbstract {

    private JButton       refreshNameSpaceBut;
    private JList<String> nameSpaceList;
    private JButton       addNameSpaceBut;

    private JTextField    tableNameField;
    private JButton       createTabBut;
    private JButton       addColumnFamilyBut;
    private JTextArea     showTextArea;

    private static String namespace     = "";
    private static String table_name    = "";
    private static String column_family = "";

    public CreateTab(HbaseGui window){
        super(window);
    }

    public void refreshCreateTableText() {
        StringBuilder text = new StringBuilder();
        if (!StringUtil.isEmpty(namespace)) {
            text.append(com.lm.hbase.swing.SwingConstants.NAMESPACE_DES + namespace + "\n");
        }
        if (!StringUtil.isEmpty(table_name)) {
            text.append(com.lm.hbase.swing.SwingConstants.TABLE_NAME_DES + table_name + "\n");
        }
        if (!StringUtil.isEmpty(column_family)) {
            text.append(com.lm.hbase.swing.SwingConstants.COLUMN_FAMILY_DES + column_family + "\n");
        }
        showTextArea.setText(text.toString());
    }

    @Override
    public String getTitle() {
        return "创建表";
    }

    @Override
    public JPanel initializePanel() {
        // 底层面板
        JPanel mainPanel = new JPanel();
        // 中央面板
        JPanel tableContentPanel = new JPanel();
        // 左侧面板
        JPanel namespacePanel = new JPanel();
        namespacePanel.setToolTipText("asdfads");

        // 初始化所有panel
        {
            mainPanel.setLayout(new BorderLayout());

            tableContentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.setLayout(new BorderLayout());

            namespacePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            namespacePanel.setLayout(new BorderLayout(1, 1));
        }

        // panel布局
        {
            mainPanel.add(tableContentPanel, BorderLayout.CENTER);
            mainPanel.add(namespacePanel, BorderLayout.WEST);
        }

        // 左侧面板内部布局
        {
            refreshNameSpaceBut = new JButton("刷新", new ImageIcon(Env.IMG_DIR + "Search.png"));

            nameSpaceList = new JList<>();
            nameSpaceList.setFixedCellHeight(20);
            nameSpaceList.setFixedCellWidth(200);
            nameSpaceList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            try {
                nameSpaceList.setListData(HbaseUtil.listNameSpace());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JScrollPane nsListScroll = new JScrollPane(nameSpaceList);
            nsListScroll.setLayout(new ScrollPaneLayout());

            nameSpaceList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        CreateTab.namespace = nameSpaceList.getSelectedValue();
                        refreshCreateTableText();
                    }
                }
            });

            addNameSpaceBut = new JButton(new ImageIcon(Env.IMG_DIR + "add.png"));
            addNameSpaceBut.addMouseListener(new AddNameSpaceAdapter());

            namespacePanel.add(refreshNameSpaceBut, BorderLayout.NORTH);
            namespacePanel.add(nsListScroll, BorderLayout.CENTER);
            namespacePanel.add(addNameSpaceBut, BorderLayout.SOUTH);

        }

        // 中央面板布局布局
        {
            JPanel tableNorthPanel = new JPanel();

            tableNorthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableNorthPanel, BorderLayout.NORTH);
            tableNorthPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

            JLabel label_2 = new JLabel(new ImageIcon(Env.IMG_DIR + "table.png"));
            tableNorthPanel.add(label_2);

            tableNameField = new JTextField();
            tableNorthPanel.add(tableNameField);
            tableNameField.setColumns(10);

            addColumnFamilyBut = new JButton("添加列族", new ImageIcon(Env.IMG_DIR + "add.png"));
            tableNorthPanel.add(addColumnFamilyBut);

            JPanel tableCenterPanel = new JPanel();
            tableCenterPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableCenterPanel, BorderLayout.CENTER);
            tableCenterPanel.setLayout(new BoxLayout(tableCenterPanel, BoxLayout.Y_AXIS));

            showTextArea = new JTextArea();
            showTextArea.setColumns(20);
            showTextArea.setRows(10);
            tableCenterPanel.add(showTextArea);

            addColumnFamilyBut.addMouseListener(new AddColumnFamilyAdapter());

            tableNameField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    CreateTab.table_name = tableNameField.getText().trim();
                    refreshCreateTableText();
                }

            });

            JPanel tableSouthPanel = new JPanel();
            tableSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableSouthPanel, BorderLayout.SOUTH);

            createTabBut = new JButton("创建", new ImageIcon(Env.IMG_DIR + "new.png"));
            tableSouthPanel.add(createTabBut);
            createTabBut.addMouseListener(new CreateTable());
        }

        return mainPanel;
    }

    class CreateTable extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            createTabBut.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            if (StringUtil.isEmpty(CreateTab.namespace)) {
                JOptionPane.showMessageDialog(getFrame(), "请选择命名空间", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            if (StringUtil.isEmpty(CreateTab.table_name)) {
                JOptionPane.showMessageDialog(getFrame(), "请输入表名", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            if (StringUtil.isEmpty(CreateTab.column_family)) {
                JOptionPane.showMessageDialog(getFrame(), "请输入至少一个列族", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {

                    try {
                        HbaseUtil.createTable(CreateTab.namespace + ":" + CreateTab.table_name,
                                              CreateTab.column_family);
                        JOptionPane.showMessageDialog(getFrame(), "成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e2) {
                        JOptionPane.showMessageDialog(getFrame(), e2.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }

                    stopTask();

                }

            });

        }

    }

    @Override
    public void enableAll() {
        createTabBut.setEnabled(true);
        refreshNameSpaceBut.setEnabled(true);
        addColumnFamilyBut.setEnabled(true);
        addNameSpaceBut.setEnabled(true);

    }

    @Override
    public void disableAll() {
        createTabBut.setEnabled(false);
        refreshNameSpaceBut.setEnabled(false);
        addColumnFamilyBut.setEnabled(false);
        addNameSpaceBut.setEnabled(false);
    }

    /**
     * 添加命名空间
     * 
     * @author limin May 17, 2019 1:55:20 AM
     */
    class AddNameSpaceAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            JDialog dialog = new JDialog(getFrame(), "添加命名空间", true);
            dialog.setSize(200, 70);

            int windowWidth = dialog.getWidth(); // 获得窗口宽
            int windowHeight = dialog.getHeight(); // 获得窗口高
            Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
            Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
            int screenWidth = screenSize.width; // 获取屏幕的宽
            int screenHeight = screenSize.height; // 获取屏幕的高
            dialog.setLocation(screenWidth / 2 - windowWidth, screenHeight / 2 - windowHeight * 2);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            dialog.getContentPane().add(dialogPanel);

            JLabel jLabel_dialog = new JLabel("命名空间：");
            dialogPanel.add(jLabel_dialog, BorderLayout.WEST);

            JTextField jTextField_dialog = new JTextField();
            dialogPanel.add(jTextField_dialog, BorderLayout.CENTER);

            jTextField_dialog.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        String input = jTextField_dialog.getText();
                        if (!StringUtil.isEmpty(input.trim())) {
                            getSingleThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    startTask();
                                    try {
                                        dialog.setVisible(false);
                                        HbaseUtil.createNameSpace(input.trim());
                                        // 刷新命命空间list
                                        nameSpaceList.setListData(HbaseUtil.listNameSpace());
                                        JOptionPane.showMessageDialog(dialogPanel, "添加命名空间成功", "成功",
                                                                      JOptionPane.INFORMATION_MESSAGE);
                                    } catch (Exception e1) {
                                        exceptionAlert(e1);
                                        return;
                                    }
                                    stopTask();
                                }
                            });

                        } else {
                            JOptionPane.showMessageDialog(dialogPanel, "请输入命名空间名称", "异常", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

            });

            dialog.setVisible(true);

        }

    }

    /**
     * 添加列族
     * 
     * @author limin May 17, 2019 1:50:13 AM
     */
    class AddColumnFamilyAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            JDialog dialog = new JDialog(getFrame(), "添加列族", true);
            dialog.setSize(200, 70);

            int windowWidth = dialog.getWidth(); // 获得窗口宽
            int windowHeight = dialog.getHeight(); // 获得窗口高
            Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
            Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
            int screenWidth = screenSize.width; // 获取屏幕的宽
            int screenHeight = screenSize.height; // 获取屏幕的高
            dialog.setLocation(screenWidth / 2 - windowWidth, screenHeight / 2 - windowHeight * 2);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            dialog.getContentPane().add(dialogPanel);

            JLabel jLabel_dialog = new JLabel("列族名：");
            dialogPanel.add(jLabel_dialog, BorderLayout.WEST);

            JTextField jTextField_dialog = new JTextField();
            dialogPanel.add(jTextField_dialog, BorderLayout.CENTER);

            jTextField_dialog.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        CreateTab.column_family = jTextField_dialog.getText().trim();
                        refreshCreateTableText();

                        jTextField_dialog.setText("");
                        dialog.setVisible(false);
                    }
                }

            });

            dialog.setVisible(true);

        }

    }

}
