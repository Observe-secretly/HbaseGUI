package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.alibaba.fastjson.JSON;
import com.lm.hbase.HBasePageModel;
import com.lm.hbase.HbaseUtil;
import com.lm.hbase.swing.HandleCore;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.Chooser;
import com.lm.hbase.util.DateUtil;
import com.lm.hbase.util.StringUtil;

public class QueryTab extends TabAbstract {

    private JList<TableName>      list              = null;
    private JButton               refreshTableButton;
    private JButton               tab1_searchButton = new JButton("查询");
    private JButton               deleteButton;
    private JTextField            textField_tab1_version;
    private JTextField            textField_tab1_start_rowkey;
    private JTextField            textField_tab1_pageSize;
    private JTextField            textField_tab1_rowKey_prefix;
    private JButton               tab1_nextpage_button;
    private JTable                contentTable;
    private JScrollPane           tableScroll;
    private JTextField            textField_tab1_min_stamp;
    private JTextField            textField_tab1_max_stamp;
    private JLabel                bottom_message_label;

    private static HBasePageModel pageModel;

    public QueryTab(HbaseGui window){
        super(window);
    }

    @Override
    public String getTitle() {
        return "查询";
    }

    @Override
    public JPanel initializePanel() {
        // 底层panel
        JPanel select = new JPanel();
        select.setLayout(new BorderLayout(0, 0));

        // 展示数据库列表的panel
        JPanel tableListPanel = new JPanel();
        tableListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        tableListPanel.setLayout(new BorderLayout(1, 1));

        select.add(tableListPanel, BorderLayout.WEST);

        list = new JList<>();
        list.setFixedCellHeight(20);
        list.setBackground(SystemColor.window);
        // 设置为单选模式
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jlistScroll = new JScrollPane(list);
        jlistScroll.setLayout(new ScrollPaneLayout());
        tableListPanel.add(jlistScroll);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setToolTipText("");
        popupMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPopup(list, popupMenu);

        JMenuItem removeTableItem = new JMenuItem("删除表");
        removeTableItem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TableName tableName = list.getSelectedValue();
                if (JOptionPane.showConfirmDialog(getFrame(), "确定删除" + tableName.getNameAsString() + "表吗?") == 0) {
                    if (tableName != null) {

                        getSingleThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                startTask();

                                HbaseUtil.dropTable(tableName);
                                JOptionPane.showMessageDialog(getFrame(), "删除成功", "提示",
                                                              JOptionPane.INFORMATION_MESSAGE);

                                stopTask();

                            }
                        });

                        initTableList(list);

                    }
                }

            }
        });
        popupMenu.add(removeTableItem);

        JMenuItem truncateTableItem = new JMenuItem("清空表");
        truncateTableItem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TableName tableName = list.getSelectedValue();
                if (JOptionPane.showConfirmDialog(getFrame(), "确定清空" + tableName.getNameAsString() + "表吗?") == 0) {
                    if (tableName != null) {
                        getSingleThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                startTask();

                                HbaseUtil.truncateTable(tableName, true);
                                JOptionPane.showMessageDialog(getFrame(), "已清空", "提示", JOptionPane.INFORMATION_MESSAGE);
                                initTableList(list);

                                stopTask();

                            }
                        });

                    }
                }

            }
        });
        popupMenu.add(truncateTableItem);

        // 添加一个分割线
        popupMenu.addSeparator();

        JMenuItem countItem = new JMenuItem("统计总数");
        countItem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                TableName tableName = list.getSelectedValue();

                if (JOptionPane.showConfirmDialog(getFrame(), "确定进行吗？大表可能需要较长时间统计") == 0) {
                    if (tableName != null) {
                        getSingleThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                startTask();

                                long count = HbaseUtil.rowCount(tableName);
                                JOptionPane.showMessageDialog(getFrame(), count, tableName.getNameAsString() + "数据总数",
                                                              JOptionPane.INFORMATION_MESSAGE);

                                stopTask();

                            }
                        });

                    }
                }

            }
        });
        popupMenu.add(countItem);

        refreshTableButton = new JButton("<html><font color=red>刷新</font></html>");
        refreshTableButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                refreshTableButton.setEnabled(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                initTableList(list);
                refreshTableButton.setEnabled(true);
            }
        });
        tableListPanel.add(refreshTableButton, BorderLayout.NORTH);

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

        deleteButton = new JButton("删除");
        filterCenterPanel.add(deleteButton);
        deleteButton.addMouseListener(new DeleteEvent());

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
        tableScroll = new JScrollPane(contentTable);
        searchPanel.add(tableScroll, BorderLayout.CENTER);

        // 自动调整表格宽度
        contentTable.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                resizeTable(true, contentTable, tableScroll);
            }
        });

        contentTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (contentTable.getSelectedRowCount() == 0) {
                    deleteButton.setEnabled(false);
                } else {
                    deleteButton.setEnabled(true);
                }
            }
        });

        // 初始化表
        initTableList(list);

        return select;
    }

    /**
     * 启用所有可操作的hbase的控件
     */
    @Override
    public void disableAll() {
        list.setEnabled(false);
        tab1_searchButton.setEnabled(false);
        tab1_nextpage_button.setEnabled(false);
        refreshTableButton.setEnabled(false);
    }

    /**
     * 禁用所有可操作的hbase的控件
     */
    @Override
    public void enableAll() {
        list.setEnabled(true);
        tab1_searchButton.setEnabled(true);
        tab1_nextpage_button.setEnabled(true);
        refreshTableButton.setEnabled(true);
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
            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {

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

                        pageModel = HbaseUtil.scanResultByPageFilter(tableName, startRowKeyByte, null, filterList,
                                                                     version, pageModel, true, getMetaData());
                        HandleCore.reloadTableFormat(tableName, contentTable, pageModel);
                        HandleCore.setPageInfomation(pageModel, bottom_message_label);

                    } else {
                        JOptionPane.showMessageDialog(getFrame(), "请在右侧选择表", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                    stopTask();
                }
            });

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
            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    if (pageModel == null) {
                        JOptionPane.showMessageDialog(getFrame(), "请选择表并进行一次查询", "警告", JOptionPane.WARNING_MESSAGE);
                        tab1_nextpage_button.setEnabled(true);
                        return;
                    }

                    if (pageModel.getQueryTotalCount() % pageModel.getPageSize() != 0) {
                        JOptionPane.showMessageDialog(getFrame(), "已经到了最后一页", "警告", JOptionPane.WARNING_MESSAGE);
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

                    pageModel.setMinStamp(DateUtil.convertMinStamp(textField_tab1_min_stamp.getText(),
                                                                   Chooser.DEFAULTFORMAT));
                    pageModel.setMaxStamp(DateUtil.convertMaxStamp(textField_tab1_max_stamp.getText(),
                                                                   Chooser.DEFAULTFORMAT));

                    pageModel = HbaseUtil.scanResultByPageFilter(pageModel.getTableName(), null, null, null, version,
                                                                 pageModel, false, getMetaData());

                    HandleCore.reloadTableFormat(pageModel.getTableName(), contentTable, pageModel);
                    HandleCore.setPageInfomation(pageModel, bottom_message_label);
                    stopTask();
                }
            });

        }
    }

    class DeleteEvent extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            deleteButton.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    int selectedRowCount = contentTable.getSelectedRowCount();
                    TableName tableName = list.getSelectedValue();
                    if (selectedRowCount > 0 || tableName == null) {
                        String[] rowkeys = new String[selectedRowCount];
                        int[] selectRowIndexs = contentTable.getSelectedRows();
                        for (int i = 0; i < selectedRowCount; i++) {
                            rowkeys[i] = contentTable.getValueAt(selectRowIndexs[i], 1).toString();
                        }

                        HbaseUtil.deleteRow(tableName, rowkeys);
                        DefaultTableModel model = (DefaultTableModel) contentTable.getModel();
                        while (true) {
                            if (contentTable.getSelectedRowCount() > 0) {
                                model.removeRow(contentTable.getSelectedRow());
                            } else {
                                break;
                            }
                        }

                    } else {
                        deleteButton.setEnabled(false);
                    }
                    stopTask();

                }

            });

        }
    }

    private Map<String, String> getMetaData() {
        // 尝试获取字段类型映射
        String propertiesKey = list.getSelectedValue().getNameAsString() + PROPERTIES_SUFFIX;
        String mappingStr = HandleCore.getStringValue(propertiesKey);
        Map<String, String> typeMapping = null;
        if (!StringUtil.isEmpty(mappingStr)) {
            typeMapping = JSON.parseObject(mappingStr, Map.class);
        }
        return typeMapping;
    }

}
