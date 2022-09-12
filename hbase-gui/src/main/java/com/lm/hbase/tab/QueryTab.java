package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.alibaba.fastjson.JSON;
import com.lm.hbase.adapter.entity.HBasePageModel;
import com.lm.hbase.adapter.entity.HbaseQualifier;
import com.lm.hbase.common.CommonConstons;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.swing.HandleCore;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.Chooser;
import com.lm.hbase.util.DateUtil;
import com.lm.hbase.util.MyBytesUtil;
import com.lm.hbase.util.StorageTableColumn;
import com.lm.hbase.util.StringUtil;

public class QueryTab extends TabAbstract {

    private final static byte[]              COMBOBOX_DEFAULT_VALUE = "--".getBytes();

    private JList<String>                    list                   = null;
    private JButton                          refreshTableButton;
    private DefaultValueTextField            searchTableInput;
    private JButton                          searchButton           = new JButton("查询", ImageIconConstons.SEARCH_ICON);
    private JButton                          deleteButton;
    private DefaultValueTextField            textField_start_rowkey;
    private DefaultValueTextField            textField_end_rowkey;
    private JTextField                       textField_version;
    private JTextField                       textField_pageSize;
    private DefaultValueTextField            textField_rowKey_prefix;
    private JButton                          nextpage_button;
    private JTable                           contentTable;
    private JScrollPane                      tableScroll;
    private JTextField                       textField_min_stamp;
    private JTextField                       textField_max_stamp;
    private JLabel                           bottom_message_label;

    // item filter
    private JComboBox<HbaseQualifier>        fieldsComboBox;
    private JComboBox<String>                fieldTypeComboBox;
    private JComboBox<String>                comparatorComboBox;
    private JComboBox<String>                filterOperatorComboBox;
    private JTextField                       filterValueTextField;

    // field display filter
    private JLabel                           fieldDisplayLabel;
    private JComboBox<HbaseQualifier>        fieldDisplayComboBox;
    private JButton                          fieldDisplayResetBut   = new JButton("重置",
                                                                                  ImageIconConstons.RESET_CLICK_ICON);
    // 当前选中的表需要展示的字段。为空代表展示全部
    private Map<String, HbaseQualifier>      showFieldMap           = new HashMap<>();
    // 当前选中的表部分被隐藏的列宽信息
    private Map<Integer, StorageTableColumn> currentTableColumnsMap = new HashMap<>();

    private static HBasePageModel            pageModel;

    public QueryTab(HbaseGui window){
        super(window);
    }

    @Override
    public String getTitle() {
        return "查询";
    }

    @Override
    public JComponent initializePanel() {
        // 底层panel
        JSplitPane select = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // 展示数据库列表的panel
        JPanel tableListPanel = new JPanel();
        tableListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        tableListPanel.setLayout(new BorderLayout(1, 1));

        select.setLeftComponent(tableListPanel);

        list = new JList<>();
        list.setFixedCellHeight(20);
        list.setFixedCellWidth(250);
        // 设置为单选模式
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jlistScroll = new JScrollPane(list);
        jlistScroll.setBorder(new TitledBorder("TABLES"));
        jlistScroll.setLayout(new ScrollPaneLayout());
        tableListPanel.add(jlistScroll);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setToolTipText("");
        popupMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPopup(list, popupMenu);

        JMenuItem removeTableItem = new JMenuItem("删除表");
        popupMenu.add(removeTableItem);

        JMenuItem truncateTableItem = new JMenuItem("清空表");
        popupMenu.add(truncateTableItem);

        // 添加一个分割线
        popupMenu.addSeparator();

        JMenuItem countItem = new JMenuItem("统计总数");

        popupMenu.add(countItem);

        // 把查询按钮和模糊搜索框放一起
        JPanel searchToolsPanel = new JPanel();
        searchToolsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchToolsPanel.setLayout(new BorderLayout(1, 1));

        searchTableInput = new DefaultValueTextField("模糊搜索");
        refreshTableButton = new JButton("刷新", ImageIconConstons.UPDATE_ICON);

        searchToolsPanel.add(searchTableInput, BorderLayout.NORTH);
        searchToolsPanel.add(refreshTableButton, BorderLayout.SOUTH);

        tableListPanel.add(searchToolsPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        select.setRightComponent(searchPanel);
        searchPanel.setLayout(new BorderLayout(0, 0));

        // filtersPanel 位于table上侧 start
        JPanel filtersPanel = new JPanel();
        filtersPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchPanel.add(filtersPanel, BorderLayout.NORTH);
        filtersPanel.setLayout(new BorderLayout(0, 0));
        // filtersPanel 位于table上侧 end

        // filtersPanel的左侧 包含一个删除按钮 start
        deleteButton = new JButton("删除", ImageIconConstons.GARBAGE_ICON);
        deleteButton.setEnabled(false);
        deleteButton.addMouseListener(new DeleteEvent());
        filtersPanel.add(deleteButton, BorderLayout.WEST);
        // filtersPanel的左侧。包含一个删除按钮 end

        // filterNorthPanel 位于filtersPanel的中间，包含rowkey条件以及常用的查询条件查询 start
        JPanel filterNorthPanel = new JPanel();
        filtersPanel.add(filterNorthPanel, BorderLayout.CENTER);
        filterNorthPanel.setLayout(new BorderLayout());

        JPanel filterNorthPanel_center = new JPanel();
        filterNorthPanel.add(filterNorthPanel_center, BorderLayout.CENTER);

        JLabel filterLabel = new JLabel(ImageIconConstons.OPTION_ICON);
        filterNorthPanel_center.add(filterLabel);

        textField_start_rowkey = new DefaultValueTextField("start rowkey");
        filterNorthPanel_center.add(textField_start_rowkey);
        textField_start_rowkey.setColumns(10);

        textField_end_rowkey = new DefaultValueTextField("end rowkey");
        filterNorthPanel_center.add(textField_end_rowkey);
        textField_end_rowkey.setColumns(10);

        textField_rowKey_prefix = new DefaultValueTextField("Rowkey前缀");
        filterNorthPanel_center.add(textField_rowKey_prefix);
        textField_rowKey_prefix.setColumns(10);

        JSeparator js1 = new JSeparator(JSeparator.VERTICAL);
        js1.setPreferredSize(new Dimension(js1.getPreferredSize().width, 20));
        filterNorthPanel_center.add(js1);

        JLabel versionLabel = new JLabel("版本号:");
        filterNorthPanel_center.add(versionLabel);

        textField_version = new JTextField();
        filterNorthPanel_center.add(textField_version);
        textField_version.setColumns(2);
        textField_version.setText("1");

        JLabel label_1 = new JLabel(ImageIconConstons.PAGE_ICON);
        filterNorthPanel_center.add(label_1);

        textField_pageSize = new JTextField();
        textField_pageSize.setText("10");
        filterNorthPanel_center.add(textField_pageSize);
        textField_pageSize.setColumns(3);

        JSeparator js2 = new JSeparator(JSeparator.VERTICAL);
        js2.setPreferredSize(new Dimension(js2.getPreferredSize().width, 20));
        filterNorthPanel_center.add(js2);

        JLabel timeScopeLabel = new JLabel(ImageIconConstons.CALENDAR_ICON);
        filterNorthPanel_center.add(timeScopeLabel);

        textField_min_stamp = new JTextField();
        Chooser.getInstance().register(textField_min_stamp);
        filterNorthPanel_center.add(textField_min_stamp);
        textField_min_stamp.setColumns(10);

        textField_max_stamp = new JTextField();
        Chooser.getInstance().register(textField_max_stamp);
        filterNorthPanel_center.add(textField_max_stamp);
        textField_max_stamp.setColumns(10);

        searchButton.addMouseListener(new SelectEvent());
        filterNorthPanel.add(searchButton, BorderLayout.EAST);
        // filterNorthPanel 位于filtersPanel的中间，包含rowkey条件以及常用的查询条件查询 end

        // filterSouthPanel 位于filtersPanel的最下面 包含了条件查询filter start
        JPanel filterSouthPanel = new JPanel();
        filterSouthPanel.setLayout(new BorderLayout(0, 0));
        filtersPanel.add(filterSouthPanel, BorderLayout.SOUTH);

        JPanel itemFilterWestPanel = new JPanel();
        itemFilterWestPanel.setLayout(new FlowLayout());
        filterSouthPanel.add(itemFilterWestPanel, BorderLayout.WEST);

        fieldsComboBox = new JComboBox<>();
        fieldsComboBox.addItem(new HbaseQualifier(COMBOBOX_DEFAULT_VALUE, COMBOBOX_DEFAULT_VALUE, "--"));
        fieldsComboBox.setEditable(true);
        itemFilterWestPanel.add(fieldsComboBox);

        fieldTypeComboBox = new JComboBox<>();
        fieldTypeComboBox.addItem("String");
        fieldTypeComboBox.addItem("Int");
        fieldTypeComboBox.addItem("Short");
        fieldTypeComboBox.addItem("Long");
        fieldTypeComboBox.addItem("Float");
        fieldTypeComboBox.addItem("Double");
        fieldTypeComboBox.addItem("BigDecimal");
        itemFilterWestPanel.add(fieldTypeComboBox);

        filterOperatorComboBox = new JComboBox<>();
        for (String item : com.lm.hbase.swing.SwingConstants.filterFactory.getCompareOpSimpleList()) {
            filterOperatorComboBox.addItem(item);
        }

        itemFilterWestPanel.add(filterOperatorComboBox);

        comparatorComboBox = new JComboBox<>();
        for (Class f : com.lm.hbase.swing.SwingConstants.filterFactory.getAllComparatorClass()) {
            comparatorComboBox.addItem(f.getSimpleName());
        }

        itemFilterWestPanel.add(comparatorComboBox);

        filterValueTextField = new JTextField();
        filterSouthPanel.add(filterValueTextField, BorderLayout.CENTER);

        fieldDisplayLabel = new JLabel(ImageIconConstons.DISPLAY_ICON);
        fieldDisplayComboBox = new JComboBox<>();
        fieldDisplayComboBox.addItem(new HbaseQualifier(COMBOBOX_DEFAULT_VALUE, COMBOBOX_DEFAULT_VALUE, "--"));

        JPanel fieldDisplayPanel = new JPanel();

        JSeparator js4 = new JSeparator(JSeparator.VERTICAL);
        js4.setPreferredSize(new Dimension(js4.getPreferredSize().width, 20));
        fieldDisplayPanel.add(js4);

        fieldDisplayResetBut.addMouseListener(new FieldDisplayResetButEvent());

        fieldDisplayPanel.add(fieldDisplayLabel);
        fieldDisplayPanel.add(fieldDisplayComboBox);
        fieldDisplayPanel.add(fieldDisplayResetBut);
        filterSouthPanel.add(fieldDisplayPanel, BorderLayout.EAST);

        // filterSouthPanel 位于filtersPanel的最下面 包含了条件查询filter end
        // 渲染字段过滤条件查询filter end

        // searchSouthPanel 位于整个searchPanel的最下侧 显示了下一页和查询页码等信息 start
        JPanel searchSouthPanel = new JPanel();
        searchSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchPanel.add(searchSouthPanel, BorderLayout.SOUTH);
        searchSouthPanel.setLayout(new BorderLayout(0, 0));

        bottom_message_label = new JLabel("");
        bottom_message_label.setHorizontalAlignment(SwingConstants.CENTER);
        searchSouthPanel.add(bottom_message_label, BorderLayout.CENTER);

        nextpage_button = new JButton("加载下一页", ImageIconConstons.NEXT_ICON);
        nextpage_button.addMouseListener(new NextPage());
        searchSouthPanel.add(nextpage_button, BorderLayout.EAST);
        // searchSouthPanel 位于整个searchPanel的最下侧 显示了下一页和查询页码等信息 end

        contentTable = new JTable();
        contentTable.setRowHeight(CommonConstons.ROW_HEIGHT);
        tableScroll = new JScrollPane(contentTable);
        searchPanel.add(tableScroll, BorderLayout.CENTER);

        /**
         * 监听代码块 start
         */
        {
            // 删除表
            removeTableItem.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    String tableName = list.getSelectedValue();
                    if (JOptionPane.showConfirmDialog(getFrame(), "确定删除" + tableName + "表吗?") == 0) {
                        if (tableName != null) {

                            getSingleThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    startTask();

                                    try {
                                        com.lm.hbase.swing.SwingConstants.hbaseAdapter.dropTable(tableName);
                                        JOptionPane.showMessageDialog(getFrame(), "删除成功", "提示",
                                                                      JOptionPane.INFORMATION_MESSAGE);

                                        // 删除成功后，删除元数据
                                        String propertiesKey = list.getSelectedValue() + PROPERTIES_SUFFIX;
                                        com.lm.hbase.swing.SwingConstants.selectedConf.remove(propertiesKey);
                                        cleanTable();
                                        initTableList(list);
                                    } catch (Exception e) {
                                        exceptionAlert(e);
                                        return;
                                    }

                                    stopTask();

                                }
                            });

                            try {
                                initTableList(list);
                            } catch (Exception e1) {
                                exceptionAlert(e1);
                                return;
                            }

                        }
                    }

                }
            });

            // 刷新
            refreshTableButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    refreshTableButton.setEnabled(false);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    getSingleThreadPool().execute(new Runnable() {

                        @Override
                        public void run() {
                            startTask();
                            try {
                                initTableList(list);
                                searchTableInput.setText("");
                            } catch (Exception e) {
                                exceptionAlert(e);
                                return;
                            }
                            stopTask();
                        }

                    });
                }
            });

            // 统计总数
            countItem.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    String tableName = list.getSelectedValue();

                    if (JOptionPane.showConfirmDialog(getFrame(), "确定进行吗？大表可能需要较长时间统计") == 0) {
                        if (tableName != null) {
                            getSingleThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    startTask();

                                    try {
                                        long count = com.lm.hbase.swing.SwingConstants.hbaseAdapter.rowCount(tableName);
                                        JOptionPane.showMessageDialog(getFrame(), count, tableName + "数据总数",
                                                                      JOptionPane.INFORMATION_MESSAGE);

                                    } catch (Exception e) {
                                        exceptionAlert(e);
                                        return;
                                    }

                                    stopTask();

                                }
                            });

                        }
                    }

                }
            });

            // 清空
            truncateTableItem.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    String tableName = list.getSelectedValue();
                    if (JOptionPane.showConfirmDialog(getFrame(), "确定清空" + tableName + "表吗?") == 0) {
                        if (tableName != null) {
                            getSingleThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    startTask();
                                    try {
                                        com.lm.hbase.swing.SwingConstants.hbaseAdapter.truncateTable(tableName, true);
                                        JOptionPane.showMessageDialog(getFrame(), "已清空", "提示",
                                                                      JOptionPane.INFORMATION_MESSAGE);
                                        cleanTable();
                                        initTableList(list);
                                    } catch (Exception e) {
                                        exceptionAlert(e);
                                        return;
                                    }

                                    stopTask();

                                }
                            });

                        }
                    }

                }
            });

            // 自动调整表格宽度
            contentTable.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    resizeTable(true, contentTable, tableScroll);
                }
            });

            // 选中表格的行时，控制删除是否需要显示
            contentTable.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (contentTable.getSelectedRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    } else {
                        deleteButton.setEnabled(true);
                    }
                }
            });

            // 选中hbase表时的监听
            list.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {

                        getSingleThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                startTask();
                                try {
                                    if (list.getSelectedValue() == null) {
                                        stopTask();
                                        return;
                                    }
                                    loadMataData(list.getSelectedValue());
                                    // 清空table
                                    cleanTable();
                                    // 设置分页大小为10
                                    textField_pageSize.setText("10");
                                    // 运行初次查询
                                    query();
                                    // 清空desplayField信息
                                    showFieldMap = new HashMap<>();
                                    currentTableColumnsMap = new HashMap<>();
                                } catch (Exception e) {
                                    exceptionAlert(e);
                                    return;
                                }
                                stopTask();
                            }
                        });

                    }

                }
            });

            // 搜索表
            searchTableInput.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {

                    // 搜索输入
                    String searchText = searchTableInput.getText().toLowerCase();

                    // 清除选择
                    list.clearSelection();

                    List<String> likely = new ArrayList<>();// 匹配到的

                    for (String item : getTableListCache()) {
                        if (item.toLowerCase().indexOf(searchText) != -1) {
                            likely.add(item);
                        }
                    }

                    list.setListData(likely.toArray(new String[likely.size()]));

                }
            });

            // 监听筛选条件选择事件，用于动态调整类型下拉框
            fieldsComboBox.addItemListener(new AutoAdapterFieldTypeItemListener());
            // 监听可视列筛选事件。动态调整需要展示的列
            fieldDisplayComboBox.addItemListener(new AutoAdapterDisplayFieldTypeItemListener());

        }
        // end

        // 初始化表
        try {
            initTableList(list);
        } catch (Exception e) {
            exceptionAlert(e);
        }

        return select;
    }

    /**
     * 清空tableUI
     */
    private void cleanTable() {
        // 清空table
        contentTable.setModel(new DefaultTableModel());
        pageModel = null;
        HandleCore.cleanPageInfomation(bottom_message_label);
    }

    private HbaseQualifier customField(String inputString) {
        int sbIndex = inputString.indexOf(".");
        if (sbIndex <= 0) {
            // 输入的字段没有列族信息
            return null;
        }
        return new HbaseQualifier(inputString.substring(0, sbIndex).getBytes(),
                                  inputString.substring(sbIndex + 1).getBytes(), "string");

    }

    /**
     * 启用所有可操作的hbase的控件
     */
    @Override
    public void disableAll() {
        list.setEnabled(false);
        searchButton.setEnabled(false);
        nextpage_button.setEnabled(false);
        refreshTableButton.setEnabled(false);
    }

    /**
     * 禁用所有可操作的hbase的控件
     */
    @Override
    public void enableAll() {
        list.setEnabled(true);
        searchButton.setEnabled(true);
        nextpage_button.setEnabled(true);
        refreshTableButton.setEnabled(true);
    }

    private void loadMataData(String tableName) throws Exception {

        String propertiesKey = list.getSelectedValue() + PROPERTIES_SUFFIX;
        String cacheMetaData = com.lm.hbase.swing.SwingConstants.selectedConf.getStringValue(propertiesKey);

        List<HbaseQualifier> qualifierList = com.lm.hbase.swing.SwingConstants.hbaseAdapter.getTableQualifiers(tableName);
        // 如果存在元数据则替换类型
        if (!StringUtil.isEmpty(cacheMetaData)) {
            Map<String, String> metaData = JSON.parseObject(cacheMetaData, Map.class);
            for (HbaseQualifier item : qualifierList) {
                if (!StringUtil.isEmpty(metaData.get(item.getDisplayName()))) {
                    item.setType(metaData.get(item.getDisplayName()));
                }
            }
        }
        // 清空fieldsComboBox
        fieldsComboBox.removeAllItems();
        fieldDisplayComboBox.removeAllItems();
        // 渲染条件
        for (HbaseQualifier hbaseQualifier : qualifierList) {
            fieldsComboBox.addItem(hbaseQualifier);
            fieldDisplayComboBox.addItem(hbaseQualifier);
        }

    }

    private List<Object> getFilter() {

        List<Object> fs = new ArrayList<>();

        HbaseQualifier colume = fieldsComboBox.getItemAt(fieldsComboBox.getSelectedIndex());
        if (colume == null && fieldsComboBox.getSelectedItem() != null
            && fieldsComboBox.getSelectedItem() instanceof HbaseQualifier) {// 自定义字段的index是-1
            colume = (HbaseQualifier) fieldsComboBox.getSelectedItem();
        }

        if (colume != null && !StringUtil.isEmpty(filterValueTextField.getText())) {
            if (!StringUtil.isEmpty(filterValueTextField.getText())) {

                String compareOpSimple = filterOperatorComboBox.getItemAt(filterOperatorComboBox.getSelectedIndex());
                String comparatorClassName = comparatorComboBox.getItemAt(comparatorComboBox.getSelectedIndex());
                String fieldType = fieldTypeComboBox.getItemAt(fieldTypeComboBox.getSelectedIndex());
                String fieldValue = filterValueTextField.getText();
                fs.add(com.lm.hbase.swing.SwingConstants.filterFactory.createSingleColumnValueFilter(colume.getFamily(),
                                                                                                     colume.getQualifier(),
                                                                                                     compareOpSimple,
                                                                                                     comparatorClassName,
                                                                                                     fieldType,
                                                                                                     fieldValue));
            }
        }

        // 获取rowkey查询前缀（如果有）
        String rowkeyPrefix = textField_rowKey_prefix.getText();

        // 获取startRowKey
        if (!StringUtil.isEmpty(rowkeyPrefix)) {
            fs.add(com.lm.hbase.swing.SwingConstants.filterFactory.createRowkeyPrefixFilter(rowkeyPrefix.getBytes()));
        }

        if (fs.size() != 0) {
            return fs;
        } else {
            return null;
        }
    }

    /**
     * 隐藏表格中的某一列
     * 
     * @param table 表格
     * @param index 要隐藏的列 的索引
     */
    public void hideColumn(JTable table, int index) {

        TableColumn tc = table.getColumnModel().getColumn(index);
        if (currentTableColumnsMap.get(index) == null) {
            currentTableColumnsMap.put(index, new StorageTableColumn(tc.getMaxWidth(), tc.getPreferredWidth(),
                                                                     tc.getMinWidth(), tc.getWidth()));
        }

        tc.setMaxWidth(0);
        tc.setPreferredWidth(0);
        tc.setMinWidth(0);
        tc.setWidth(0);

        table.getTableHeader().getColumnModel().getColumn(index).setMaxWidth(0);
        table.getTableHeader().getColumnModel().getColumn(index).setMinWidth(0);
    }

    /**
     * 恢复列宽
     * 
     * @param table
     * @param index
     */
    public void recoverColumn(JTable table, int index) {

        StorageTableColumn storageTableColumn = currentTableColumnsMap.get(index);
        if (storageTableColumn == null) {
            return;
        }

        TableColumn tc = table.getColumnModel().getColumn(index);

        tc.setMaxWidth(storageTableColumn.getMaxWidth());
        tc.setPreferredWidth(storageTableColumn.getPreferredWidth());
        tc.setMinWidth(storageTableColumn.getMinWidth());
        tc.setWidth(storageTableColumn.getWidth());

        table.getTableHeader().getColumnModel().getColumn(index).setMaxWidth(storageTableColumn.getMaxWidth());
        table.getTableHeader().getColumnModel().getColumn(index).setMinWidth(storageTableColumn.getMinWidth());
        table.updateUI();
    }

    /**
     * 恢复表的所有列
     * 
     * @param table
     */
    public void recoverAllColumn(JTable table) {

        for (Entry<Integer, StorageTableColumn> entry : currentTableColumnsMap.entrySet()) {
            TableColumn tc = table.getColumnModel().getColumn(entry.getKey());

            tc.setMaxWidth(entry.getValue().getMaxWidth());
            tc.setPreferredWidth(entry.getValue().getPreferredWidth());
            tc.setMinWidth(entry.getValue().getMinWidth());
            tc.setWidth(entry.getValue().getWidth());

            table.getTableHeader().getColumnModel().getColumn(entry.getKey()).setMaxWidth(entry.getValue().getMaxWidth());
            table.getTableHeader().getColumnModel().getColumn(entry.getKey()).setMinWidth(entry.getValue().getMinWidth());
        }

        table.updateUI();
    }

    public void displayColumn(JTable table, Map<String, HbaseQualifier> showFieldMap) {
        for (int index = 0; index < table.getColumnModel().getColumnCount(); index++) {
            TableColumn tc = table.getColumnModel().getColumn(index);
            if (tc != null) {
                String headValue = tc.getHeaderValue().toString();
                if (headValue.equalsIgnoreCase("rowkey") || headValue.equalsIgnoreCase("number")) {
                    continue;
                }

                if (showFieldMap.get(tc.getHeaderValue().toString()) == null) {
                    hideColumn(table, index);
                } else {
                    recoverColumn(table, index);
                }
            } else {
                break;
            }

        }
    }

    /**
     * 查询并渲染table
     * 
     * @throws Exception
     */
    private void query() throws Exception {

        HandleCore.setPageInfomation(null, bottom_message_label);
        String tableName = list.getSelectedValue();
        // 获取分页大小
        Integer page = 10;
        try {
            page = Integer.parseInt(textField_pageSize.getText());
        } catch (Exception e2) {
            textField_pageSize.setText(page.toString());
        }
        // 获取版本
        Integer version = Integer.MAX_VALUE;
        try {
            version = Integer.parseInt(textField_version.getText());
        } catch (Exception e2) {
            textField_pageSize.setText(version.toString());
        }

        byte[] startRowKeyByte = null;
        byte[] endRowKeyByte = null;

        String startRowKey = textField_start_rowkey.getText();
        String endRowKey = textField_end_rowkey.getText();

        if (!StringUtil.isEmpty(startRowKey)) {
            startRowKeyByte = MyBytesUtil.toBytes(startRowKey);
        }
        if (!StringUtil.isEmpty(endRowKey)) {
            endRowKeyByte = MyBytesUtil.toBytes(endRowKey);
        }

        if (tableName != null) {
            pageModel = new HBasePageModel(page, tableName);
            pageModel.setMinStamp(DateUtil.convertMinStamp(textField_min_stamp.getText(), Chooser.DEFAULTFORMAT));
            pageModel.setMaxStamp(DateUtil.convertMaxStamp(textField_max_stamp.getText(), Chooser.DEFAULTFORMAT));
            pageModel = com.lm.hbase.swing.SwingConstants.hbaseAdapter.scanResultByPageFilter(tableName,
                                                                                              startRowKeyByte,
                                                                                              endRowKeyByte,
                                                                                              getFilter(), version,
                                                                                              pageModel, true,
                                                                                              getMetaData());
            HandleCore.reloadTableFormat(tableName, contentTable, pageModel);
            HandleCore.setPageInfomation(pageModel, bottom_message_label);

        } else {
            JOptionPane.showMessageDialog(getFrame(), "请在右侧选择表", "提示", JOptionPane.INFORMATION_MESSAGE);
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
            searchButton.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (searchButton.isEnabled()) {
                return;
            }

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    startTask();
                    try {
                        query();
                    } catch (Exception e) {
                        exceptionAlert(e);
                        return;
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
            nextpage_button.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    if (pageModel == null) {
                        JOptionPane.showMessageDialog(getFrame(), "请选择表并进行一次查询", "警告", JOptionPane.WARNING_MESSAGE);
                        nextpage_button.setEnabled(true);
                        stopTask();
                        return;
                    }

                    if (pageModel.getQueryTotalCount() % pageModel.getPageSize() != 0) {
                        JOptionPane.showMessageDialog(getFrame(), "已经到了最后一页", "警告", JOptionPane.WARNING_MESSAGE);
                        nextpage_button.setEnabled(true);
                        stopTask();
                        return;
                    }

                    // 获取分页大小
                    Integer page = 10;
                    try {
                        page = Integer.parseInt(textField_pageSize.getText());
                    } catch (Exception e2) {
                        textField_pageSize.setText(page.toString());
                    }
                    // 获取版本
                    Integer version = Integer.MAX_VALUE;
                    try {
                        version = Integer.parseInt(textField_version.getText());
                    } catch (Exception e2) {
                        textField_pageSize.setText(version.toString());
                    }

                    byte[] startRowKeyByte = null;
                    byte[] endRowKeyByte = null;

                    String startRowKey = textField_start_rowkey.getText();
                    String endRowKey = textField_end_rowkey.getText();

                    if (!StringUtil.isEmpty(startRowKey)) {
                        startRowKeyByte = MyBytesUtil.toBytes(startRowKey);
                    }
                    if (!StringUtil.isEmpty(endRowKey)) {
                        endRowKeyByte = MyBytesUtil.toBytes(endRowKey);
                    }

                    pageModel.setMinStamp(DateUtil.convertMinStamp(textField_min_stamp.getText(),
                                                                   Chooser.DEFAULTFORMAT));
                    pageModel.setMaxStamp(DateUtil.convertMaxStamp(textField_max_stamp.getText(),
                                                                   Chooser.DEFAULTFORMAT));

                    try {
                        pageModel = com.lm.hbase.swing.SwingConstants.hbaseAdapter.scanResultByPageFilter(pageModel.getTableName(),
                                                                                                          startRowKeyByte,
                                                                                                          endRowKeyByte,
                                                                                                          getFilter(),
                                                                                                          version,
                                                                                                          pageModel,
                                                                                                          false,
                                                                                                          getMetaData());
                        HandleCore.reloadTableFormat(pageModel.getTableName(), contentTable, pageModel);
                        HandleCore.setPageInfomation(pageModel, bottom_message_label);
                    } catch (Exception e) {
                        exceptionAlert(e);
                        return;
                    }

                    stopTask();
                }
            });

        }
    }

    class FieldDisplayResetButEvent extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            fieldDisplayResetBut.setEnabled(false);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            startTask();
            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    recoverAllColumn(contentTable);
                    stopTask();
                    fieldDisplayResetBut.setEnabled(true);
                    showFieldMap.clear();
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
                    String tableName = list.getSelectedValue();
                    if (selectedRowCount > 0 || tableName != null) {
                        String[] rowkeys = new String[selectedRowCount];
                        int[] selectRowIndexs = contentTable.getSelectedRows();
                        for (int i = 0; i < selectedRowCount; i++) {
                            rowkeys[i] = contentTable.getValueAt(selectRowIndexs[i], 1).toString();
                        }

                        try {
                            com.lm.hbase.swing.SwingConstants.hbaseAdapter.deleteRow(tableName, rowkeys);
                        } catch (Exception e) {
                            exceptionAlert(e);
                            return;
                        }
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
        String propertiesKey = list.getSelectedValue() + PROPERTIES_SUFFIX;
        String mappingStr = com.lm.hbase.swing.SwingConstants.selectedConf.getStringValue(propertiesKey);
        Map<String, String> typeMapping = null;
        if (!StringUtil.isEmpty(mappingStr)) {
            typeMapping = JSON.parseObject(mappingStr, Map.class);
        }
        return typeMapping;
    }

    class AutoAdapterFieldTypeItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            HbaseQualifier item = null;
            if (fieldsComboBox.getSelectedIndex() > 0 && fieldsComboBox.getSelectedItem() instanceof HbaseQualifier) {
                item = (HbaseQualifier) fieldsComboBox.getSelectedItem();
            } else if (fieldsComboBox.getSelectedIndex() == -1 && fieldsComboBox.getSelectedItem() != null) {// 自定义字段
                String inputString = fieldsComboBox.getSelectedItem().toString();
                item = customField(inputString);
                // 添加到下拉框里面
                if (item == null) {
                    // TODO
                    return;
                }
                // 由于addItem后会再次调用itemStateChanged 陷入死循环，所以先移除，等添加完自定义field到ComboBox后在添加监听
                fieldsComboBox.removeItemListener(this);
                fieldsComboBox.addItem(item);
                fieldsComboBox.setSelectedIndex(fieldsComboBox.getItemCount() - 1);
                fieldsComboBox.addItemListener(this);
            } else {
                return;
            }

            for (int i = 0; i < fieldTypeComboBox.getItemCount(); i++) {
                String type = fieldTypeComboBox.getItemAt(i);
                if (item.getType().equalsIgnoreCase(type)) {
                    fieldTypeComboBox.setSelectedIndex(i);
                }
            }

        }
    }

    class AutoAdapterDisplayFieldTypeItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == 2 || fieldDisplayComboBox.getSelectedIndex() == -1) {
                return;
            }

            HbaseQualifier item = (HbaseQualifier) fieldDisplayComboBox.getSelectedItem();

            showFieldMap.put(item.getDisplayName(), item);

            displayColumn(contentTable, showFieldMap);
        }

    }
}

class DefaultValueTextField extends JTextField {

    private static final long serialVersionUID = -8757598274181885964L;
    private String            defaultShowText;

    public DefaultValueTextField(String defaultShowText){
        this.defaultShowText = "* " + defaultShowText;
        setText(this.defaultShowText);
    }

    {
        this.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if (StringUtil.isEmpty(getText())) {
                    setText(defaultShowText);
                }

            }

            @Override
            public void focusGained(FocusEvent e) {
                if (getText() == null) {
                    setText(null);
                }

            }
        });
    }

    @Override
    public String getText() {
        String text = super.getText();
        if (super.getText().equalsIgnoreCase(defaultShowText)) {
            return null;
        }
        return text;
    }

}
