package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.alibaba.fastjson.JSON;
import com.lm.hbase.common.CommonConstons;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.swing.HandleCore;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.swing.SwingConstants;
import com.lm.hbase.swing.component.ComboBoxTable;
import com.lm.hbase.swing.component.ComboBoxTableUtil;
import com.lm.hbase.util.StringUtil;

public class MetaDataTab extends TabAbstract {

    private JList<String>         list;
    private ComboBoxTable         contentTable;
    private JScrollPane           tableScroll;
    private JButton               removeMataDataBut;
    private JButton               addMataDataBut;
    private JButton               refreshMataDataBut;
    private JButton               saveButton;
    private JButton               refreshTableButton;
    private DefaultValueTextField searchTableInput;

    public MetaDataTab(HbaseGui window){
        super(window);
    }

    @Override
    public String getTitle() {
        return "元数据维护";
    }

    @Override
    public JComponent initializePanel() {
        JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        {
            // 展示数据库列表的panel
            JPanel tableListPanel = new JPanel();
            tableListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableListPanel.setLayout(new BorderLayout(1, 1));

            panel.setLeftComponent(tableListPanel);

            list = new JList<>();
            list.setFixedCellHeight(20);
            list.setFixedCellWidth(250);
            // 设置为单选模式
            list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane jlistScroll = new JScrollPane(list);
            jlistScroll.setBorder(new TitledBorder("TABLES"));
            jlistScroll.setLayout(new ScrollPaneLayout());
            tableListPanel.add(jlistScroll);

            // 把查询按钮和模糊搜索框放一起
            JPanel searchToolsPanel = new JPanel();
            searchToolsPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            searchToolsPanel.setLayout(new BorderLayout(1, 1));

            searchTableInput = new DefaultValueTextField("模糊搜索");
            refreshTableButton = new JButton("刷新", ImageIconConstons.UPDATE_ICON);
            searchToolsPanel.add(searchTableInput, BorderLayout.NORTH);
            searchToolsPanel.add(refreshTableButton, BorderLayout.SOUTH);

            tableListPanel.add(searchToolsPanel, BorderLayout.NORTH);

            JPanel southPanel = new JPanel();
            southPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            southPanel.setLayout(new BorderLayout(0, 0));
            panel.setRightComponent(southPanel);

            contentTable = new ComboBoxTable();
            contentTable.setRowHeight(CommonConstons.ROW_HEIGHT);
            tableScroll = new JScrollPane(contentTable);
            tableScroll.setBorder(new TitledBorder("元数据信息"));
            southPanel.add(tableScroll, BorderLayout.CENTER);

            JPanel mataSouthPanel = new JPanel();
            mataSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            southPanel.add(mataSouthPanel, BorderLayout.SOUTH);

            addMataDataBut = new JButton(ImageIconConstons.ADD_ICON);
            removeMataDataBut = new JButton(ImageIconConstons.GARBAGE_ICON);
            refreshMataDataBut = new JButton(ImageIconConstons.UPDATE_ICON);
            saveButton = new JButton("保存元数据", ImageIconConstons.SAVE_ICON);

            addMataDataBut.setEnabled(false);
            removeMataDataBut.setEnabled(false);
            refreshMataDataBut.setEnabled(false);
            saveButton.setEnabled(false);

            mataSouthPanel.add(addMataDataBut);
            mataSouthPanel.add(removeMataDataBut);
            mataSouthPanel.add(refreshMataDataBut);
            mataSouthPanel.add(saveButton);
        }

        /*
         * 添加各种监听
         */
        {

            // 给刷新按钮添加监听
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

            // 表格自适应监听
            contentTable.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    resizeTable(true, contentTable, tableScroll);
                }
            });

            // 选中hbase表时的监听
            list.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        if (list.getSelectedValue() == null) {
                            return;
                        }
                        loadMataData(list.getSelectedValue(), false);

                    }
                }
            });

            // 添加自定义属性监听
            addMataDataBut.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    DefaultTableModel tableModel = (DefaultTableModel) contentTable.getModel();
                    tableModel.addRow(new Object[] { "<CustomField>",
                                                     ComboBoxTableUtil.getDefaultJTabComboBoxOptions() });
                }
            });

            // 删除原数据列监听
            removeMataDataBut.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    int selectedRowCount = contentTable.getSelectedRowCount();
                    String tableName = list.getSelectedValue();
                    if (selectedRowCount > 0 || tableName != null) {
                        String[] rowkeys = new String[selectedRowCount];
                        int[] selectRowIndexs = contentTable.getSelectedRows();
                        for (int i = 0; i < selectedRowCount; i++) {
                            rowkeys[i] = contentTable.getValueAt(selectRowIndexs[i], 1).toString();
                        }

                        DefaultTableModel model = (DefaultTableModel) contentTable.getModel();
                        while (true) {
                            if (contentTable.getSelectedRowCount() > 0) {
                                model.removeRow(contentTable.getSelectedRow());
                            } else {
                                break;
                            }
                        }

                    }
                    removeMataDataBut.setEnabled(false);
                }
            });

            // 选中表格的行时，控制删除是否需要显示
            contentTable.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (contentTable.getSelectedRowCount() == 0) {
                        removeMataDataBut.setEnabled(false);
                    } else {
                        removeMataDataBut.setEnabled(true);
                    }
                }
            });

            refreshMataDataBut.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    loadMataData(list.getSelectedValue(), true);
                }
            });

            // 保存按钮监听
            saveButton.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    // 使正在编辑的单元格生效。若不进行此操作会导致正在编辑的单元格内容或者下拉框内容丢失
                    if (contentTable.isEditing()) {
                        TableCellEditor cellEditor = contentTable.getCellEditor();
                        if (cellEditor != null) {
                            cellEditor.stopCellEditing();
                        }
                    }

                    Map<String, String> map = new HashMap<>();
                    for (int i = 0; i < contentTable.getRowCount(); i++) {
                        ComboBoxTable.JTabComboBoxOption[] JTabComboBoxOptions = (ComboBoxTable.JTabComboBoxOption[]) contentTable.getValueAt(i,
                                                                                                                                              1);
                        for (ComboBoxTable.JTabComboBoxOption option : JTabComboBoxOptions) {
                            if (option.isSelected()) {
                                map.put(contentTable.getValueAt(i, 0).toString(), option.getValue());
                            }
                        }

                    }

                    String propertiesKey = list.getSelectedValue() + PROPERTIES_SUFFIX;
                    SwingConstants.selectedConf.setValue(propertiesKey, JSON.toJSONString(map));
                    JOptionPane.showMessageDialog(getFrame(), "保存成功", "提示", JOptionPane.INFORMATION_MESSAGE);

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

        }

        // 初始化表
        try {
            initTableList(list);
        } catch (Exception e) {
            exceptionAlert(e);
        }

        return panel;
    }

    /**
     * 从配置文件或直接查询Hbase表并加载元数据
     * 
     * @param tableName
     * @param onlyLoadHbase 是否强制从hbase中加载元数据
     */
    @SuppressWarnings("unchecked")
    private void loadMataData(String tableName, boolean onlyLoadHbase) {
        startTask();
        getSingleThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                // 清空table
                DefaultTableModel tableModel = (DefaultTableModel) contentTable.getModel();
                tableModel.setRowCount(0);
                // 1、 优先从映射文件中查询元数据
                // 2、查询不到则去Hbase查询表结构，得到所有的字段。所有字段默认是string类型
                String propertiesKey = list.getSelectedValue() + PROPERTIES_SUFFIX;
                String cacheMetaData = SwingConstants.selectedConf.getStringValue(propertiesKey);
                if (!StringUtil.isEmpty(cacheMetaData) && !onlyLoadHbase) {
                    HandleCore.reloadMetaTableFormat(contentTable, JSON.parseObject(cacheMetaData, Map.class));
                } else {
                    try {
                        HandleCore.reloadMetaTableFormat(tableName, contentTable);
                    } catch (Exception e) {
                        exceptionAlert(e);
                        return;
                    }
                }
                stopTask();
            }
        });

    }

    @Override
    public void enableAll() {
        list.setEnabled(true);
        addMataDataBut.setEnabled(true);
        refreshMataDataBut.setEnabled(true);
        saveButton.setEnabled(true);
        refreshTableButton.setEnabled(true);
        removeMataDataBut.setEnabled(true);
    }

    @Override
    public void disableAll() {
        list.setEnabled(false);
        addMataDataBut.setEnabled(false);
        refreshMataDataBut.setEnabled(false);
        saveButton.setEnabled(false);
        refreshTableButton.setEnabled(false);
        removeMataDataBut.setEnabled(false);

    }

}
