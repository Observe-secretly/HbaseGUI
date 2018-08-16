package com.lm.hbase.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.lm.hbase.HBasePageModel;
import com.lm.hbase.HbaseUtil;
import com.lm.hbase.util.Chooser;
import com.lm.hbase.util.DateUtil;
import com.lm.hbase.util.StringUtil;

public class HbaseGui {

    public JFrame                 frmHbaseGui;
    private JButton               tab1_searchButton = new JButton("查询");
    private JList<TableName>      list              = null;
    private JTextField            textField_tab1_version;
    private JTextField            textField_tab1_start_rowkey;
    private JTextField            textField_tab1_pageSize;
    private JTextField            textField_tab1_rowKey_prefix;
    private JButton               tab1_nextpage_button;
    private JTable                contentTable;
    private JScrollPane           tb1_table_scroll;
    private JTextField            textField_tab1_min_stamp;
    private JTextField            textField_tab1_max_stamp;

    private JTextField            textField_tab3_tableName;
    private JButton               tab3_create_table_button;
    private JTextArea             tab3_textArea;

    private JLabel                bottom_message_label;

    private static HBasePageModel pageModel;

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
        frmHbaseGui = new JFrame();
        frmHbaseGui.setTitle("Hbase Gui");
        frmHbaseGui.setBounds(10, 10, 1400, 800);
        frmHbaseGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmHbaseGui.getContentPane().setLayout(new BorderLayout(0, 0));
        frmHbaseGui.setResizable(false);// 禁止拉边框拉长拉断

        JPanel panel = new JPanel();
        frmHbaseGui.getContentPane().add(panel, BorderLayout.SOUTH);

        JLabel lblHBaSe = new JLabel("Message");
        panel.add(lblHBaSe);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frmHbaseGui.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JPanel select = new JPanel();
        tabbedPane.addTab("查询", null, select, null);
        select.setLayout(new BorderLayout(0, 0));

        JPanel tableListPanel = new JPanel();
        tableListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        select.add(tableListPanel, BorderLayout.WEST);
        tableListPanel.setLayout(new BorderLayout(0, 0));

        list = new JList<>();
        list.setFixedCellHeight(20);
        list.setBackground(SystemColor.window);
        tableListPanel.add(list);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setToolTipText("");
        popupMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPopup(list, popupMenu);

        JCheckBoxMenuItem chckbxmntmNewCheckItem = new JCheckBoxMenuItem("删除表");
        chckbxmntmNewCheckItem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TableName tableName = list.getSelectedValue();
                if (tableName != null) {
                    HbaseUtil.dropTable(tableName);
                    JOptionPane.showMessageDialog(frmHbaseGui, "删除成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    initTableList(list);
                }
            }
        });
        popupMenu.add(chckbxmntmNewCheckItem);

        JCheckBoxMenuItem chckbxmntmNewCheckItem2 = new JCheckBoxMenuItem("统计总数");
        chckbxmntmNewCheckItem2.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TableName tableName = list.getSelectedValue();
                if (tableName != null) {
                    long count = HbaseUtil.rowCount(tableName);
                    JOptionPane.showMessageDialog(frmHbaseGui, count, tableName.getNameAsString() + "数据总数",
                                                  JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        popupMenu.add(chckbxmntmNewCheckItem2);

        JButton button = new JButton("<html><font color=red>刷新</font></html>");
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                button.setEnabled(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                initTableList(list);
                button.setEnabled(true);
            }
        });
        tableListPanel.add(button, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        select.add(searchPanel, BorderLayout.CENTER);
        searchPanel.setLayout(new BorderLayout(0, 0));

        JPanel filtersPanel = new JPanel();
        filtersPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchPanel.add(filtersPanel, BorderLayout.NORTH);
        filtersPanel.setLayout(new BorderLayout(0, 0));

        JPanel filterCenterPanel = new JPanel();
        filtersPanel.add(filterCenterPanel, BorderLayout.CENTER);
        filterCenterPanel.setLayout(new FlowLayout());

        JLabel startRowkeylabel = new JLabel("StartRowKey:");
        filterCenterPanel.add(startRowkeylabel);

        textField_tab1_start_rowkey = new JTextField();
        filterCenterPanel.add(textField_tab1_start_rowkey);
        textField_tab1_start_rowkey.setColumns(15);

        JLabel versionLabel = new JLabel("版本号:");
        filterCenterPanel.add(versionLabel);

        textField_tab1_version = new JTextField();
        textField_tab1_version.setToolTipText("默认查询最新版本");
        filterCenterPanel.add(textField_tab1_version);
        textField_tab1_version.setColumns(5);
        textField_tab1_version.setText(Integer.MAX_VALUE + "");

        JLabel label_1 = new JLabel("分页大小:");
        filterCenterPanel.add(label_1);

        textField_tab1_pageSize = new JTextField();
        textField_tab1_pageSize.setText("10");
        filterCenterPanel.add(textField_tab1_pageSize);
        textField_tab1_pageSize.setColumns(5);

        JLabel label_rowKey_prefix = new JLabel("RowKey Prefix:");
        filterCenterPanel.add(label_rowKey_prefix);

        textField_tab1_rowKey_prefix = new JTextField();
        filterCenterPanel.add(textField_tab1_rowKey_prefix);
        textField_tab1_rowKey_prefix.setColumns(10);

        JLabel timeScopeLabel = new JLabel("Time:");
        filterCenterPanel.add(timeScopeLabel);

        textField_tab1_min_stamp = new JTextField();
        Chooser.getInstance().register(textField_tab1_min_stamp);
        filterCenterPanel.add(textField_tab1_min_stamp);
        textField_tab1_min_stamp.setColumns(10);

        textField_tab1_max_stamp = new JTextField();
        Chooser.getInstance().register(textField_tab1_max_stamp);
        filterCenterPanel.add(textField_tab1_max_stamp);
        textField_tab1_max_stamp.setColumns(10);

        JPanel filterEastPanel = new JPanel();
        FlowLayout fl_filterEastPanel = (FlowLayout) filterEastPanel.getLayout();
        fl_filterEastPanel.setAlignment(FlowLayout.RIGHT);
        filtersPanel.add(filterEastPanel, BorderLayout.EAST);

        tab1_searchButton.addMouseListener(new SelectEvent());
        filterEastPanel.add(tab1_searchButton);

        JPanel searchSouthPanel = new JPanel();
        searchSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchPanel.add(searchSouthPanel, BorderLayout.SOUTH);
        searchSouthPanel.setLayout(new BorderLayout(0, 0));

        bottom_message_label = new JLabel("");
        bottom_message_label.setHorizontalAlignment(SwingConstants.CENTER);
        searchSouthPanel.add(bottom_message_label, BorderLayout.CENTER);

        tab1_nextpage_button = new JButton("加载下一页");
        tab1_nextpage_button.addMouseListener(new NextPage());
        searchSouthPanel.add(tab1_nextpage_button, BorderLayout.EAST);

        contentTable = new JTable();
        tb1_table_scroll = new JScrollPane(contentTable);
        searchPanel.add(tb1_table_scroll, BorderLayout.CENTER);

        contentTable.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                resizeTable(true);
            }
        });

        JPanel insert = new JPanel();
        tabbedPane.addTab("插入", null, insert, null);

        JLabel lblNewLabel_1 = new JLabel("2");
        insert.add(lblNewLabel_1);

        JPanel table = new JPanel();
        table.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        tabbedPane.addTab("创建表", null, table, null);
        table.setLayout(new BorderLayout(0, 0));

        JPanel tableNorthPanel = new JPanel();
        tableNorthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        table.add(tableNorthPanel, BorderLayout.NORTH);
        tableNorthPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel label_2 = new JLabel("表名：");
        tableNorthPanel.add(label_2);

        textField_tab3_tableName = new JTextField();
        tableNorthPanel.add(textField_tab3_tableName);
        textField_tab3_tableName.setColumns(10);

        JButton button_2 = new JButton("+ 添加列族");
        tableNorthPanel.add(button_2);

        JPanel tableCenterPanel = new JPanel();
        tableCenterPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        table.add(tableCenterPanel, BorderLayout.CENTER);
        tableCenterPanel.setLayout(new BoxLayout(tableCenterPanel, BoxLayout.Y_AXIS));

        tab3_textArea = new JTextArea();
        tab3_textArea.setColumns(20);
        tab3_textArea.setRows(10);
        tableCenterPanel.add(tab3_textArea);

        button_2.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                JDialog dialog = new JDialog(frmHbaseGui, "添加列族", true);
                dialog.setSize(200, 50);

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
                            tab3_textArea.setText(tab3_textArea.getText() + "\n{"
                                                  + com.lm.hbase.swing.SwingConstants.COLUMN_FAMILY_DES
                                                  + jTextField_dialog.getText() + "}");
                            jTextField_dialog.setText("");
                            dialog.setVisible(false);
                        }
                    }

                });

                dialog.setVisible(true);

            }
        });

        textField_tab3_tableName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                int index = tab3_textArea.getText().indexOf("]");
                if (index > 0) {
                    tab3_textArea.setText("[" + com.lm.hbase.swing.SwingConstants.TABLE_NAME_DES
                                          + textField_tab3_tableName.getText() + "]"
                                          + tab3_textArea.getText().substring(index + 1));
                } else {
                    tab3_textArea.setText("[" + com.lm.hbase.swing.SwingConstants.TABLE_NAME_DES
                                          + textField_tab3_tableName.getText() + "]" + tab3_textArea.getText());
                }
            }

        });

        JPanel tableSouthPanel = new JPanel();
        tableSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        table.add(tableSouthPanel, BorderLayout.SOUTH);

        tab3_create_table_button = new JButton("创建");
        tableSouthPanel.add(tab3_create_table_button);
        tab3_create_table_button.addMouseListener(new CreateTable());

        frmHbaseGui.setVisible(true);

        // 初始化表
        initTableList(list);

        // 添加关闭事件
        frmHbaseGui.addWindowListener(new WindowAdapter() {

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

    class CreateTable extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            tab3_create_table_button.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            String createStatement = tab3_textArea.getText().trim().replaceAll("\r|\n", "");
            createStatement = createStatement.replaceAll(com.lm.hbase.swing.SwingConstants.TABLE_NAME_DES,
                                                         "").replaceAll(com.lm.hbase.swing.SwingConstants.COLUMN_FAMILY_DES,
                                                                        "");
            int index = createStatement.indexOf("]");
            if (index == -1) {
                JOptionPane.showMessageDialog(frmHbaseGui, "请输入表名", "警告", JOptionPane.WARNING_MESSAGE);
                tab3_create_table_button.setEnabled(true);
                return;
            }
            String tableName = createStatement.substring(1, index);
            if (StringUtil.isEmpty(tableName)) {
                JOptionPane.showMessageDialog(frmHbaseGui, "请输入表名", "警告", JOptionPane.WARNING_MESSAGE);
                tab3_create_table_button.setEnabled(true);
                return;
            }

            String temp = createStatement.substring(index + 1);
            if (StringUtil.isEmpty(temp)) {
                JOptionPane.showMessageDialog(frmHbaseGui, "请输入至少一个列族", "警告", JOptionPane.WARNING_MESSAGE);
                tab3_create_table_button.setEnabled(true);
                return;
            }
            String[] familys = temp.replaceAll("\\{", "").split("}");
            if (familys == null || familys.length == 0) {
                JOptionPane.showMessageDialog(frmHbaseGui, "请输入至少一个列族", "警告", JOptionPane.WARNING_MESSAGE);
                tab3_create_table_button.setEnabled(true);
                return;
            }

            try {
                HbaseUtil.createTable(TableName.valueOf(tableName), familys);
                JOptionPane.showMessageDialog(frmHbaseGui, "成功", "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(frmHbaseGui, e2.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }

            tab3_create_table_button.setEnabled(true);
        }

    }

    /**
     * 查询
     * 
     * @author limin 2018年8月13日 下午4:24:15
     */
    class SelectEvent extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            tab1_searchButton.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (tab1_searchButton.isEnabled()) {
                return;
            }

            HandleCore.setPageInfomation(null, bottom_message_label);
            TableName tableName = list.getSelectedValue();
            // 获取分页大小
            Integer page = 10;
            try {
                page = Integer.parseInt(textField_tab1_pageSize.getText());
            } catch (Exception e2) {
                textField_tab1_pageSize.setText(page.toString());
            }
            // 获取版本
            Integer version = Integer.MAX_VALUE;
            try {
                version = Integer.parseInt(textField_tab1_version.getText());
            } catch (Exception e2) {
                textField_tab1_pageSize.setText(version.toString());
            }
            // 获取rowkey查询前缀（如果有）
            String rowkeyPrefix = textField_tab1_rowKey_prefix.getText();
            FilterList filterList = null;
            // 获取startRowKey

            if (!StringUtil.isEmpty(rowkeyPrefix)) {
                Filter rowkeyFilter = new PrefixFilter(rowkeyPrefix.getBytes());
                filterList = new FilterList(rowkeyFilter);
            }

            byte[] startRowKeyByte = null;

            String startRowKey = textField_tab1_start_rowkey.getText();

            if (!StringUtil.isEmpty(startRowKey)) {
                startRowKeyByte = Bytes.toBytes(startRowKey);
            }

            if (tableName != null) {
                pageModel = new HBasePageModel(page, tableName);
                pageModel.setMinStamp(DateUtil.convertMinStamp(textField_tab1_min_stamp.getText(),
                                                               Chooser.DEFAULTFORMAT));
                pageModel.setMaxStamp(DateUtil.convertMaxStamp(textField_tab1_max_stamp.getText(),
                                                               Chooser.DEFAULTFORMAT));
                pageModel = HbaseUtil.scanResultByPageFilter(tableName, startRowKeyByte, null, filterList, version,
                                                             pageModel);
                HandleCore.reloadTableFormat(tableName, contentTable, pageModel);
                HandleCore.setPageInfomation(pageModel, bottom_message_label);
            } else {
                JOptionPane.showMessageDialog(frmHbaseGui, "请在右侧选择表", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
            tab1_searchButton.setEnabled(true);
        }
    }

    /**
     * 下一页
     * 
     * @author limin 2018年8月13日 下午4:25:35
     */
    class NextPage extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            tab1_nextpage_button.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (pageModel == null) {
                JOptionPane.showMessageDialog(frmHbaseGui, "请选择表并进行一次查询", "警告", JOptionPane.WARNING_MESSAGE);
                tab1_nextpage_button.setEnabled(true);
                return;
            }

            if (pageModel.getQueryTotalCount() % pageModel.getPageSize() != 0) {
                JOptionPane.showMessageDialog(frmHbaseGui, "已经到了最后一页", "警告", JOptionPane.WARNING_MESSAGE);
                tab1_nextpage_button.setEnabled(true);
                return;
            }

            // 获取分页大小
            Integer page = 10;
            try {
                page = Integer.parseInt(textField_tab1_pageSize.getText());
            } catch (Exception e2) {
                textField_tab1_pageSize.setText(page.toString());
            }
            // 获取版本
            Integer version = Integer.MAX_VALUE;
            try {
                version = Integer.parseInt(textField_tab1_version.getText());
            } catch (Exception e2) {
                textField_tab1_pageSize.setText(version.toString());
            }

            textField_tab1_start_rowkey.setText(new String(pageModel.getPageEndRowKey()));

            pageModel.setMinStamp(DateUtil.convertMinStamp(textField_tab1_min_stamp.getText(), Chooser.DEFAULTFORMAT));
            pageModel.setMaxStamp(DateUtil.convertMaxStamp(textField_tab1_max_stamp.getText(), Chooser.DEFAULTFORMAT));

            pageModel = HbaseUtil.scanResultByPageFilter(pageModel.getTableName(), null, null, null, version,
                                                         pageModel);
            HandleCore.reloadTableFormat(pageModel.getTableName(), contentTable, pageModel);
            HandleCore.setPageInfomation(pageModel, bottom_message_label);
            tab1_nextpage_button.setEnabled(true);

        }
    }

    /**
     * 初始化表
     * 
     * @param list
     * @wbp.parser.entryPoint
     */
    private void initTableList(JList list) {
        TableName[] tableNames = HbaseUtil.getListTableNames();
        if (tableNames != null) {
            list.setListData(tableNames);
        }
    }

    /**
     * @wbp.parser.entryPoint
     */
    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    // 表格自适应方法，需要在表格初始化后，父容器大小发生变化后以及表格模型修改后调用
    public void resizeTable(boolean bool) {
        Dimension containerwidth = null;
        if (!bool) {
            // 初始化时，父容器大小为首选大小，实际大小为0
            containerwidth = tb1_table_scroll.getPreferredSize();
        } else {
            // 界面显示后，如果父容器大小改变，使用实际大小而不是首选大小
            containerwidth = tb1_table_scroll.getSize();
        }
        // 计算表格总体宽度
        int allwidth = contentTable.getIntercellSpacing().width;
        for (int j = 0; j < contentTable.getColumnCount(); j++) {
            // 计算该列中最长的宽度
            int max = 0;
            for (int i = 0; i < contentTable.getRowCount(); i++) {
                int width = contentTable.getCellRenderer(i,
                                                         j).getTableCellRendererComponent(contentTable,
                                                                                          contentTable.getValueAt(i, j),
                                                                                          false, false, i,
                                                                                          j).getPreferredSize().width;
                if (width > max) {
                    max = width;
                }
            }
            // 计算表头的宽度
            int headerwidth = contentTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(contentTable,
                                                                                                               contentTable.getColumnModel().getColumn(j).getIdentifier(),
                                                                                                               false,
                                                                                                               false,
                                                                                                               -1,
                                                                                                               j).getPreferredSize().width;
            // 列宽至少应为列头宽度
            max += headerwidth;
            // 设置列宽
            contentTable.getColumnModel().getColumn(j).setPreferredWidth(max);
            // 给表格的整体宽度赋值，记得要加上单元格之间的线条宽度1个像素
            allwidth += max + contentTable.getIntercellSpacing().width;
        }
        allwidth += contentTable.getIntercellSpacing().width;
        // 如果表格实际宽度大小父容器的宽度，则需要我们手动适应；否则让表格自适应
        if (allwidth > containerwidth.width) {
            contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        } else {
            contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }
    }

}
