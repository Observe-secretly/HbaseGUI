package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

import com.alibaba.fastjson.JSON;
import com.lm.hbase.HBasePageModel;
import com.lm.hbase.HbaseUtil;
import com.lm.hbase.swing.HandleCore;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.Chooser;
import com.lm.hbase.util.DateUtil;
import com.lm.hbase.util.HbaseQualifier;
import com.lm.hbase.util.StringUtil;

public class QueryTab extends TabAbstract {

    private final static byte[]       COMBOBOX_DEFAULT_VALUE = "--".getBytes();

    private JList<TableName>          list                   = null;
    private JButton                   refreshTableButton;
    private JButton                   searchButton           = new JButton("查询");
    private JButton                   deleteButton;
    private JTextField                textField_tab1_version;
    private DefaultValueTextField     textField_tab1_start_rowkey;
    private JTextField                textField_tab1_pageSize;
    private DefaultValueTextField     textField_tab1_rowKey_prefix;
    private JButton                   tab1_nextpage_button;
    private JTable                    contentTable;
    private JScrollPane               tableScroll;
    private JTextField                textField_tab1_min_stamp;
    private JTextField                textField_tab1_max_stamp;
    private JLabel                    bottom_message_label;

    // item filter
    private JComboBox<HbaseQualifier> fieldsComboBox;
    private JComboBox<String>         fieldTypeComboBox;
    private JComboBox<String>         comparatorComboBox;
    private JComboBox<String>         filterOperatorComboBox;
    private JTextField                filterValueTextField;

    private static HBasePageModel     pageModel;

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
        popupMenu.add(removeTableItem);

        JMenuItem truncateTableItem = new JMenuItem("清空表");
        popupMenu.add(truncateTableItem);

        // 添加一个分割线
        popupMenu.addSeparator();

        JMenuItem countItem = new JMenuItem("统计总数");

        popupMenu.add(countItem);

        refreshTableButton = new JButton("<html><font color=red>刷新</font></html>");
        tableListPanel.add(refreshTableButton, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        select.add(searchPanel, BorderLayout.CENTER);
        searchPanel.setLayout(new BorderLayout(0, 0));

        // filtersPanel 位于table上侧 start
        JPanel filtersPanel = new JPanel();
        filtersPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchPanel.add(filtersPanel, BorderLayout.NORTH);
        filtersPanel.setLayout(new BorderLayout(0, 0));
        // filtersPanel 位于table上侧 end

        // filterWestPanel 位于filtersPanel的左侧。包含一个删除按钮 start
        JPanel filterWestPanel = new JPanel();
        filtersPanel.add(filterWestPanel, BorderLayout.WEST);
        filterWestPanel.setLayout(new FlowLayout());

        deleteButton = new JButton("删除");
        filterWestPanel.add(deleteButton);
        deleteButton.addMouseListener(new DeleteEvent());
        // filterWestPanel 位于filtersPanel的左侧。包含一个删除按钮 end

        // filterNorthPanel 位于filtersPanel的中间，包含rowkey条件以及常用的查询条件查询 start
        JPanel filterNorthPanel = new JPanel();
        filtersPanel.add(filterNorthPanel, BorderLayout.CENTER);
        filterNorthPanel.setLayout(new FlowLayout());

        textField_tab1_start_rowkey = new DefaultValueTextField("起始Rowkey");
        filterNorthPanel.add(textField_tab1_start_rowkey);
        textField_tab1_start_rowkey.setColumns(20);

        textField_tab1_rowKey_prefix = new DefaultValueTextField("Rowkey前缀");
        filterNorthPanel.add(textField_tab1_rowKey_prefix);
        textField_tab1_rowKey_prefix.setColumns(10);

        JLabel versionLabel = new JLabel("版本号:");
        filterNorthPanel.add(versionLabel);

        textField_tab1_version = new JTextField();
        textField_tab1_version.setToolTipText("默认查询最新版本");
        filterNorthPanel.add(textField_tab1_version);
        textField_tab1_version.setColumns(5);
        textField_tab1_version.setText(Integer.MAX_VALUE + "");

        JLabel label_1 = new JLabel("分页:");
        filterNorthPanel.add(label_1);

        textField_tab1_pageSize = new JTextField();
        textField_tab1_pageSize.setText("10");
        filterNorthPanel.add(textField_tab1_pageSize);
        textField_tab1_pageSize.setColumns(3);

        JLabel timeScopeLabel = new JLabel("Time:");
        filterNorthPanel.add(timeScopeLabel);

        textField_tab1_min_stamp = new JTextField();
        Chooser.getInstance().register(textField_tab1_min_stamp);
        filterNorthPanel.add(textField_tab1_min_stamp);
        textField_tab1_min_stamp.setColumns(10);

        textField_tab1_max_stamp = new JTextField();
        Chooser.getInstance().register(textField_tab1_max_stamp);
        filterNorthPanel.add(textField_tab1_max_stamp);
        textField_tab1_max_stamp.setColumns(10);

        searchButton.addMouseListener(new SelectEvent());
        filterNorthPanel.add(searchButton);
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
        filterOperatorComboBox.addItem("=");
        filterOperatorComboBox.addItem(">");
        filterOperatorComboBox.addItem("<");
        filterOperatorComboBox.addItem("≥");
        filterOperatorComboBox.addItem("≤");
        filterOperatorComboBox.addItem("≠");
        itemFilterWestPanel.add(filterOperatorComboBox);

        comparatorComboBox = new JComboBox<>();
        comparatorComboBox.addItem("(子串比较器)SubstringComparator");
        comparatorComboBox.addItem("(前缀比较器)BinaryPrefixComparator");
        comparatorComboBox.addItem("(正则比较器)RegexStringComparator");

        itemFilterWestPanel.add(comparatorComboBox);

        filterValueTextField = new JTextField();
        filterSouthPanel.add(filterValueTextField, BorderLayout.CENTER);
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

        tab1_nextpage_button = new JButton("加载下一页");
        tab1_nextpage_button.addMouseListener(new NextPage());
        searchSouthPanel.add(tab1_nextpage_button, BorderLayout.EAST);
        // searchSouthPanel 位于整个searchPanel的最下侧 显示了下一页和查询页码等信息 end

        contentTable = new JTable();
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

            // 刷新
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

            // 统计总数
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
                                    JOptionPane.showMessageDialog(getFrame(), count,
                                                                  tableName.getNameAsString() + "数据总数",
                                                                  JOptionPane.INFORMATION_MESSAGE);

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
                    TableName tableName = list.getSelectedValue();
                    if (JOptionPane.showConfirmDialog(getFrame(), "确定清空" + tableName.getNameAsString() + "表吗?") == 0) {
                        if (tableName != null) {
                            getSingleThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    startTask();

                                    HbaseUtil.truncateTable(tableName, true);
                                    JOptionPane.showMessageDialog(getFrame(), "已清空", "提示",
                                                                  JOptionPane.INFORMATION_MESSAGE);
                                    initTableList(list);

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
                public void mouseClicked(MouseEvent e) {
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
                        loadMataData(list.getSelectedValue());
                    }

                }
            });

        }
        // end

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
        searchButton.setEnabled(false);
        tab1_nextpage_button.setEnabled(false);
        refreshTableButton.setEnabled(false);
    }

    /**
     * 禁用所有可操作的hbase的控件
     */
    @Override
    public void enableAll() {
        list.setEnabled(true);
        searchButton.setEnabled(true);
        tab1_nextpage_button.setEnabled(true);
        refreshTableButton.setEnabled(true);
    }

    private void loadMataData(TableName tableName) {
        startTask();
        getSingleThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                String propertiesKey = list.getSelectedValue().getNameAsString() + PROPERTIES_SUFFIX;
                String cacheMetaData = HandleCore.getStringValue(propertiesKey);

                List<HbaseQualifier> qualifierList = HbaseUtil.getTableQualifiers(tableName);
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
                // 渲染条件
                for (HbaseQualifier hbaseQualifier : qualifierList) {
                    fieldsComboBox.addItem(hbaseQualifier);
                }

                stopTask();

            }
        });

    }

    private FilterList getFilter() {

        List<Filter> fs = new ArrayList<>();

        if (!StringUtil.isEmpty(filterValueTextField.getText())) {
            HbaseQualifier colume = fieldsComboBox.getItemAt(fieldsComboBox.getSelectedIndex());

            SingleColumnValueFilter filter = new SingleColumnValueFilter(colume.getFamily(), colume.getQualifier(),
                                                                         getCompareOp(), getComparator());
            fs.add(filter);
        }
        // 获取rowkey查询前缀（如果有）
        String rowkeyPrefix = textField_tab1_rowKey_prefix.getText();

        // 获取startRowKey
        if (!StringUtil.isEmpty(rowkeyPrefix)) {
            Filter filter = new PrefixFilter(rowkeyPrefix.getBytes());
            fs.add(filter);
        }

        if (fs.size() != 0) {
            FilterList filterList = new FilterList(fs);
            return filterList;
        } else {
            return null;
        }
    }

    /**
     * 根据选择操作转换成枚举
     * 
     * @return
     */
    private CompareOp getCompareOp() {

        String operator = filterOperatorComboBox.getItemAt(filterOperatorComboBox.getSelectedIndex());
        switch (operator) {
            case "=":
                return CompareOp.EQUAL;
            case ">":
                return CompareOp.GREATER;
            case "<":
                return CompareOp.LESS;
            case "≥":
                return CompareOp.GREATER_OR_EQUAL;
            case "≤":
                return CompareOp.LESS_OR_EQUAL;
            case "≠":
                return CompareOp.NOT_EQUAL;

            default:
                return null;
        }

    }

    private byte[] fileValue(String type, String v) {

        try {
            switch (type.toLowerCase()) {
                case "string":
                    return Bytes.toBytes(v);
                case "int":
                    return Bytes.toBytes(Integer.parseInt(v));
                case "short":
                    return Bytes.toBytes(Short.parseShort(v));
                case "long":
                    return Bytes.toBytes(Long.parseLong(v));
                case "float":
                    return Bytes.toBytes(Float.parseFloat(v));
                case "double":
                    return Bytes.toBytes(Double.parseDouble(v));
                case "bigdecimal":
                    return Bytes.toBytes(new BigDecimal(v));
                default:
                    return Bytes.toBytes(v);
            }

        } catch (Exception e) {
            return Bytes.toBytes(v);
        }

    }

    private ByteArrayComparable getComparator() {
        String type = fieldTypeComboBox.getItemAt(fieldTypeComboBox.getSelectedIndex());

        String comparator = comparatorComboBox.getItemAt(comparatorComboBox.getSelectedIndex());

        if (comparator.toLowerCase().endsWith(BinaryPrefixComparator.class.getSimpleName().toLowerCase())) {// 前缀比较器
            return new BinaryPrefixComparator(fileValue(type, filterValueTextField.getText()));
        } else if (comparator.toLowerCase().endsWith(SubstringComparator.class.getSimpleName().toLowerCase())) {// 字串比较器
            return new SubstringComparator(filterValueTextField.getText());
        } else if (comparator.toLowerCase().endsWith(RegexStringComparator.class.getSimpleName().toLowerCase())) {// 支持正则
            return new RegexStringComparator(filterValueTextField.getText());
        }
        return null;
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

                        pageModel = HbaseUtil.scanResultByPageFilter(tableName, startRowKeyByte, null, getFilter(),
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

                    pageModel = HbaseUtil.scanResultByPageFilter(pageModel.getTableName(), null, null, getFilter(),
                                                                 version, pageModel, false, getMetaData());

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
                setText(null);
            }
        });
    }

    @Override
    public String getText() {
        String text = super.getText();
        if (text.startsWith("* ")) {
            return null;
        }
        return text;
    }

}
