package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lm.hbase.adapter.entity.ColumnFamilyDescriptor;
import com.lm.hbase.adapter.entity.ColumnFamilyDescriptorEnum;
import com.lm.hbase.adapter.entity.TableDescriptor;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.swing.SwingConstants;

public class TableInfoTab extends TabAbstract {

    private JList<String>         list = null;
    private JButton               refreshTableButton;
    private DefaultValueTextField searchTableInput;

    private JPanel                descPanel;

    /*
     * 当前hbase表结构信息
     */
    private TableDescriptor       tableDescriptor;

    private JLabel                tableNameLabel;
    private JComboBox<String>     cfComboBox;
    private JTextField            bloomfilterTextField;
    private JTextField            inMemoryTextField;
    private JTextField            versionsTextField;
    private JTextField            minVersionsTextField;
    private JTextField            keepDeletedCellsTextField;
    private JTextField            dataBlockEncodingTextField;
    private JTextField            ttlTextField;
    private JTextField            compressionTextField;
    private JTextField            blockcacheField;
    private JTextField            replicationScopeTextField;

    private JTextArea             hDescTextArea;

    public TableInfoTab(HbaseGui window){
        super(window);
    }

    @Override
    public String getTitle() {
        return "表详情";
    }

    @Override
    public void enableAll() {
        list.setEnabled(true);
        refreshTableButton.setEnabled(true);
    }

    @Override
    public void disableAll() {
        list.setEnabled(false);
        refreshTableButton.setEnabled(false);
    }

    @Override
    public JComponent initializePanel() {

        // 底层panel
        JSplitPane parentPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // 展示数据库列表的panel
        JPanel tableListPanel = new JPanel();
        tableListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        tableListPanel.setLayout(new BorderLayout(1, 1));

        // 把查询按钮和模糊搜索框放一起
        JPanel searchToolsPanel = new JPanel();
        searchToolsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchToolsPanel.setLayout(new BorderLayout(1, 1));

        searchTableInput = new DefaultValueTextField("模糊搜索");
        refreshTableButton = new JButton("刷新", ImageIconConstons.UPDATE_ICON);
        searchToolsPanel.add(searchTableInput, BorderLayout.NORTH);
        searchToolsPanel.add(refreshTableButton, BorderLayout.SOUTH);

        tableListPanel.add(searchToolsPanel, BorderLayout.NORTH);

        list = new JList<>();
        list.setFixedCellHeight(20);
        list.setFixedCellWidth(250);
        // 设置为单选模式
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jlistScroll = new JScrollPane(list);
        jlistScroll.setBorder(new TitledBorder("TABLES"));
        jlistScroll.setLayout(new ScrollPaneLayout());
        tableListPanel.add(jlistScroll);

        // 添加HbaseTableList Jlist所在的Panel到主容器中
        parentPanel.setLeftComponent(tableListPanel);

        // 展示表详情的父容器
        JSplitPane contentPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel contentChildPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setTopComponent(contentChildPanel);

        parentPanel.setRightComponent(contentPanel);

        // 展示表详情容器
        descPanel = new JPanel();
        descPanel.setBorder(new TitledBorder("描述信息"));

        contentChildPanel.add(descPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        descPanel.setLayout(new GridBagLayout());
        // 顶部
        {
            tableNameLabel = new JLabel();
            cfComboBox = new JComboBox<>();

            JPanel headPanel = new JPanel();
            headPanel.setBorder(new TitledBorder("列簇信息"));

            headPanel.add(tableNameLabel);
            headPanel.add(cfComboBox);

            contentChildPanel.add(headPanel, BorderLayout.SOUTH);
        }

        // 第一行
        {
            JLabel label = new JLabel("BLOOMFILTER");
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;// 横占X个单元格
            gbc.gridheight = 1;// 列占X个单元格
            gbc.weightx = 0;// 当窗口放大时，长度不变
            gbc.weighty = 0;// 当窗口放大时，高度不变
            descPanel.add(label, gbc);
        }
        {
            bloomfilterTextField = new JTextField(28);
            bloomfilterTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(bloomfilterTextField);

            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        {
            JLabel label = new JLabel("IN_MEMORY");
            gbc.gridx = 6;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            inMemoryTextField = new JTextField(28);
            inMemoryTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(inMemoryTextField);

            gbc.gridx = 8;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        // 第二行
        {
            JLabel label = new JLabel("VERSIONS");
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            versionsTextField = new JTextField(28);
            versionsTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(versionsTextField);

            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        {
            JLabel label = new JLabel("MIN_VERSIONS");
            gbc.gridx = 6;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            minVersionsTextField = new JTextField(28);
            minVersionsTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(minVersionsTextField);

            gbc.gridx = 8;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        // 第三行
        {
            JLabel label = new JLabel("KEEP_DELETED_CELLS");
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            keepDeletedCellsTextField = new JTextField(28);
            keepDeletedCellsTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(keepDeletedCellsTextField);

            gbc.gridx = 3;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        {
            JLabel label = new JLabel("DATA_BLOCK_ENCODING");
            gbc.gridx = 6;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            dataBlockEncodingTextField = new JTextField(28);
            dataBlockEncodingTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(dataBlockEncodingTextField);

            gbc.gridx = 8;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        // 第四行
        {
            JLabel label = new JLabel("TTL");
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            ttlTextField = new JTextField(28);
            ttlTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(ttlTextField);

            gbc.gridx = 3;
            gbc.gridy = 3;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        {
            JLabel label = new JLabel("COMPRESSION");
            gbc.gridx = 6;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            compressionTextField = new JTextField(28);
            compressionTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(compressionTextField);

            gbc.gridx = 8;
            gbc.gridy = 3;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        // 第五行
        {
            JLabel label = new JLabel("BLOCKSIZE");
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            blockcacheField = new JTextField(28);
            blockcacheField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(blockcacheField);

            gbc.gridx = 3;
            gbc.gridy = 4;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        {
            JLabel label = new JLabel("REPLICATION_SCOPE");
            gbc.gridx = 6;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(label, gbc);
        }
        {
            replicationScopeTextField = new JTextField(28);
            replicationScopeTextField.setEditable(false);
            JPanel componentPanel = new JPanel();
            componentPanel.add(replicationScopeTextField);

            gbc.gridx = 8;
            gbc.gridy = 4;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;
            descPanel.add(componentPanel, gbc);
        }
        // 底部Text内容
        {
            hDescTextArea = new JTextArea();
            hDescTextArea.setEditable(false);
            hDescTextArea.setRows(10);
            hDescTextArea.setLineWrap(true);
            hDescTextArea.setWrapStyleWord(true);
            JPanel componentPanel = new JPanel(new GridLayout(1, 1));
            componentPanel.add(new JScrollPane(hDescTextArea));

            contentPanel.setBottomComponent(componentPanel);
        }

        /**
         * 监听代码块 start
         */
        {
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
                                    TableDescriptor tableDescriptor = SwingConstants.hbaseAdapter.getTableDescriptor(list.getSelectedValue());

                                    cleanAll();

                                    TableInfoTab.this.tableDescriptor = tableDescriptor;
                                    render();
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

            cfComboBox.addItemListener(new CfComboBoxItemListener());
        }

        // 初始化表
        try {
            initTableList(list);
        } catch (Exception e) {
            exceptionAlert(e);
        }

        return parentPanel;
    }

    /**
     * 清空表结构内容信息
     */
    private void cleanAll() {
        tableDescriptor = null;
        cfComboBox.removeAllItems();
        tableNameLabel.setText("");
        bloomfilterTextField.setText("");
        inMemoryTextField.setText("");
        versionsTextField.setText("");
        minVersionsTextField.setText("");
        keepDeletedCellsTextField.setText("");
        dataBlockEncodingTextField.setText("");
        ttlTextField.setText("");
        compressionTextField.setText("");
        blockcacheField.setText("");
        replicationScopeTextField.setText("");
        hDescTextArea.setText("");
    }

    private void render() {
        tableNameLabel.setText(tableDescriptor.getTableName());
        hDescTextArea.setText(TableInfoTab.this.tableDescriptor.toString());

        for (ColumnFamilyDescriptor item : tableDescriptor.getCfDesc()) {
            cfComboBox.addItem(item.getDefaultDesc().get(ColumnFamilyDescriptorEnum.NAME));
        }

    }

    class CfComboBoxItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (tableDescriptor == null) {
                    return;
                }

                String selectedItem = cfComboBox.getSelectedItem().toString();
                for (ColumnFamilyDescriptor cfItem : tableDescriptor.getCfDesc()) {
                    Map<ColumnFamilyDescriptorEnum, String> descMap = cfItem.getDefaultDesc();
                    String name = descMap.get(ColumnFamilyDescriptorEnum.NAME);
                    if (name.equalsIgnoreCase(selectedItem)) {

                        descPanel.setBorder(new TitledBorder("Descriptor:" + name));

                        bloomfilterTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.BLOOMFILTER));
                        inMemoryTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.IN_MEMORY));
                        versionsTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.VERSIONS));
                        minVersionsTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.MIN_VERSIONS));
                        keepDeletedCellsTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.KEEP_DELETED_CELLS));
                        dataBlockEncodingTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.DATA_BLOCK_ENCODING));
                        ttlTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.TTL));
                        compressionTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.COMPRESSION));
                        blockcacheField.setText(descMap.get(ColumnFamilyDescriptorEnum.BLOCKCACHE));
                        replicationScopeTextField.setText(descMap.get(ColumnFamilyDescriptorEnum.REPLICATION_SCOPE));
                        break;
                    }
                }
            }

        }

    }
}
