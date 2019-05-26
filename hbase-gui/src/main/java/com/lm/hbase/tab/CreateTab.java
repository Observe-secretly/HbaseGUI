package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lm.hbase.adapter.ColumnFamilyParam;
import com.lm.hbase.adapter.ColumnFamilyParam.ColumnFamilyFieldEnum;
import com.lm.hbase.adapter.HbaseUtil;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.MyBytesUtil;
import com.lm.hbase.util.StringUtil;

public class CreateTab extends TabAbstract {

    private JButton               refreshNameSpaceBut;
    private JList<String>         nameSpaceList;

    private JLabel                addTableNameLabel;
    private DefaultValueTextField addTableNameText;

    private JButton               addNameSpaceBut;
    private JTextField            addNameSpaceText;

    private JButton               createTabBut;

    private JLabel                addColumnFamilyLabel;
    private DefaultValueTextField columnFamilyText;
    private DefaultValueTextField maxVersionText;
    private DefaultValueTextField timeLiveText;

    private JLabel                addRowkeyScopeLabel;
    private DefaultValueTextField startRowkeyText;
    private DefaultValueTextField endRowkeyText;
    private DefaultValueTextField numRegionsText;

    private JTextArea             showTextArea;

    public CreateTab(HbaseGui window){
        super(window);
    }

    public void refreshCreateTableText() {
        String namespace = nameSpaceList.getSelectedValue();
        String table_name = addTableNameText.getText();
        String column_family = columnFamilyText.getText();
        String maxVersion = maxVersionText.getText();
        String timeLive = timeLiveText.getText();
        String startRowkey = startRowkeyText.getText();
        String endRowkey = endRowkeyText.getText();
        String numRegions = numRegionsText.getText();

        StringBuilder text = new StringBuilder("------------------配置信息----------------------\n");

        if (!StringUtil.isEmpty(namespace)) {
            text.append(com.lm.hbase.swing.SwingConstants.NAMESPACE_DES + namespace + "\n");
        }
        if (!StringUtil.isEmpty(table_name)) {
            text.append(com.lm.hbase.swing.SwingConstants.TABLE_NAME_DES + table_name + "\n");
        }
        if (!StringUtil.isEmpty(column_family)) {
            text.append(com.lm.hbase.swing.SwingConstants.COLUMN_FAMILY_DES + column_family + "\n");
        }
        if (!StringUtil.isEmpty(maxVersion)) {
            text.append(com.lm.hbase.swing.SwingConstants.MAX_VERSION_DES + maxVersion + "\n");
        }
        if (!StringUtil.isEmpty(timeLive)) {
            text.append(com.lm.hbase.swing.SwingConstants.TIME_TO_LIVE_DES + timeLive + "ms \n");
        }
        if (!StringUtil.isEmpty(startRowkey) || !StringUtil.isEmpty(endRowkey) || !StringUtil.isEmpty(numRegions)) {
            text.append("\n------------------分区信息----------------------\n");
        }
        if (!StringUtil.isEmpty(startRowkey)) {
            text.append(com.lm.hbase.swing.SwingConstants.START_ROWKEY_DES + startRowkey + "\n");
        }
        if (!StringUtil.isEmpty(endRowkey)) {
            text.append(com.lm.hbase.swing.SwingConstants.END_ROWKEY_DES + endRowkey + "\n");
        }
        if (!StringUtil.isEmpty(numRegions)) {
            text.append(com.lm.hbase.swing.SwingConstants.NUM_REGIONS_DES + numRegions + "\n");
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
            refreshNameSpaceBut = new JButton("刷新", ImageIconConstons.UPDATE_ICON);
            refreshNameSpaceBut.addMouseListener(new RefreshNameSpaceAdapter());

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

            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.setToolTipText("");
            popupMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
            addPopup(nameSpaceList, popupMenu);

            JMenuItem removeNamespace = new JMenuItem("删除命名空间");
            removeNamespace.addMouseListener(new DelNamespaceAdapter());

            popupMenu.add(removeNamespace);

            nameSpaceList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        refreshCreateTableText();
                    }
                }
            });

            addNameSpaceBut = new JButton(ImageIconConstons.ADD_ICON);
            addNameSpaceBut.addMouseListener(new AddNameSpaceAdapter());

            addNameSpaceText = new JTextField();
            addNameSpaceText.addKeyListener(new AddNameSpaceTextKeyAdapter());
            addNameSpaceText.setVisible(false);

            namespacePanel.add(refreshNameSpaceBut, BorderLayout.NORTH);
            namespacePanel.add(nsListScroll, BorderLayout.CENTER);

            JPanel namespacePanel_south = new JPanel();
            namespacePanel_south.setLayout(new BorderLayout());
            namespacePanel_south.add(addNameSpaceBut, BorderLayout.NORTH);
            namespacePanel_south.add(addNameSpaceText, BorderLayout.SOUTH);

            namespacePanel.add(namespacePanel_south, BorderLayout.SOUTH);

        }

        // 中央面板布局布局
        {
            JPanel tableNorthPanel = new JPanel();

            tableNorthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableNorthPanel, BorderLayout.NORTH);
            tableNorthPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            addTableNameLabel = new JLabel(ImageIconConstons.TABLE_ICON);
            addTableNameText = new DefaultValueTextField("表名");
            addTableNameText.setColumns(8);
            addTableNameText.addKeyListener(new EditSettingsAdapter());

            tableNorthPanel.add(addTableNameLabel);
            tableNorthPanel.add(addTableNameText);

            JSeparator js1 = new JSeparator(JSeparator.VERTICAL);
            js1.setPreferredSize(new Dimension(js1.getPreferredSize().width, 20));
            tableNorthPanel.add(js1);

            addColumnFamilyLabel = new JLabel(ImageIconConstons.SETTINGS_ICON);
            tableNorthPanel.add(addColumnFamilyLabel);

            columnFamilyText = new DefaultValueTextField("列族名");
            columnFamilyText.setColumns(8);
            columnFamilyText.addKeyListener(new EditSettingsAdapter());
            tableNorthPanel.add(columnFamilyText);

            maxVersionText = new DefaultValueTextField("最大版本");
            maxVersionText.setColumns(8);
            maxVersionText.addKeyListener(new EditSettingsAdapter());
            tableNorthPanel.add(maxVersionText);

            timeLiveText = new DefaultValueTextField("数据过期时间ms");
            timeLiveText.setColumns(10);
            timeLiveText.addKeyListener(new EditSettingsAdapter());
            tableNorthPanel.add(timeLiveText);

            JSeparator js2 = new JSeparator(JSeparator.VERTICAL);
            js2.setPreferredSize(new Dimension(js2.getPreferredSize().width, 20));
            tableNorthPanel.add(js2);

            addRowkeyScopeLabel = new JLabel(ImageIconConstons.SHARING_ICON);
            startRowkeyText = new DefaultValueTextField("start rowKey");
            startRowkeyText.addKeyListener(new EditSettingsAdapter());
            startRowkeyText.setColumns(8);

            endRowkeyText = new DefaultValueTextField("end rowKey");
            endRowkeyText.addKeyListener(new EditSettingsAdapter());
            endRowkeyText.setColumns(8);

            numRegionsText = new DefaultValueTextField("num regions");
            numRegionsText.addKeyListener(new EditSettingsAdapter());
            numRegionsText.setColumns(8);

            tableNorthPanel.add(addRowkeyScopeLabel);
            tableNorthPanel.add(startRowkeyText);
            tableNorthPanel.add(endRowkeyText);
            tableNorthPanel.add(numRegionsText);

            JPanel tableCenterPanel = new JPanel();
            tableCenterPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableCenterPanel, BorderLayout.CENTER);
            tableCenterPanel.setLayout(new BoxLayout(tableCenterPanel, BoxLayout.Y_AXIS));

            showTextArea = new JTextArea();
            showTextArea.setColumns(20);
            showTextArea.setRows(10);
            tableCenterPanel.add(showTextArea);

            JPanel tableSouthPanel = new JPanel();
            tableSouthPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
            tableContentPanel.add(tableSouthPanel, BorderLayout.SOUTH);

            createTabBut = new JButton("创建", ImageIconConstons.NEW_ICON);
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
            String namespace = nameSpaceList.getSelectedValue();
            String table_name = addTableNameText.getText();
            String column_family = columnFamilyText.getText();
            String startRowkey = startRowkeyText.getText();
            String endRowkey = endRowkeyText.getText();
            String numRegions = numRegionsText.getText();
            String maxVersion = maxVersionText.getText();
            String timeLive = timeLiveText.getText();

            if (StringUtil.isEmpty(namespace)) {
                JOptionPane.showMessageDialog(getFrame(), "请选择命名空间", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            if (StringUtil.isEmpty(table_name)) {
                JOptionPane.showMessageDialog(getFrame(), "请输入表名", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            if (StringUtil.isEmpty(column_family)) {
                JOptionPane.showMessageDialog(getFrame(), "请输入至少一个列族", "警告", JOptionPane.WARNING_MESSAGE);
                createTabBut.setEnabled(true);
                return;
            }

            if (!StringUtil.isEmpty(startRowkey) || !StringUtil.isEmpty(endRowkey) || !StringUtil.isEmpty(numRegions)) {
                if (StringUtil.isEmpty(startRowkey) || StringUtil.isEmpty(endRowkey)
                    || StringUtil.isEmpty(numRegions)) {
                    JOptionPane.showMessageDialog(getFrame(), "分区信息不完整", "警告", JOptionPane.WARNING_MESSAGE);
                    createTabBut.setEnabled(true);
                    return;
                }
            }

            startTask();

            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {

                    try {
                        if (!StringUtil.isEmpty(startRowkey) && !StringUtil.isEmpty(endRowkey)
                            && !StringUtil.isEmpty(numRegions)) {
                            ColumnFamilyParam columnFamilyParam = new ColumnFamilyParam();
                            columnFamilyParam.put(ColumnFamilyFieldEnum.COLUMN_FAMILY_NAME, column_family);
                            if (StringUtil.isEmpty(maxVersion)) {
                                columnFamilyParam.put(ColumnFamilyFieldEnum.MAX_VERSION, maxVersion);
                            }
                            if (StringUtil.isEmpty(timeLive)) {
                                columnFamilyParam.put(ColumnFamilyFieldEnum.TIME_TO_LIVE, timeLive);
                            }
                            HbaseUtil.createTable(namespace + ":" + table_name, null, MyBytesUtil.toBytes(startRowkey),
                                                  MyBytesUtil.toBytes(endRowkey), Integer.parseInt(numRegions),
                                                  columnFamilyParam);
                        } else {
                            HbaseUtil.createTable(namespace + ":" + table_name, column_family);
                        }
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
        addColumnFamilyLabel.setEnabled(true);
        addNameSpaceBut.setEnabled(true);

    }

    @Override
    public void disableAll() {
        createTabBut.setEnabled(false);
        refreshNameSpaceBut.setEnabled(false);
        addColumnFamilyLabel.setEnabled(false);
        addNameSpaceBut.setEnabled(false);
    }

    class AddNameSpaceTextKeyAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                String input = addNameSpaceText.getText();
                if (!StringUtil.isEmpty(input.trim())) {
                    getSingleThreadPool().execute(new Runnable() {

                        @Override
                        public void run() {
                            startTask();
                            try {
                                HbaseUtil.createNameSpace(input.trim());
                                // 刷新命命空间list
                                nameSpaceList.setListData(HbaseUtil.listNameSpace());
                                JOptionPane.showMessageDialog(window.parentJframe, "添加命名空间成功", "成功",
                                                              JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception e1) {
                                exceptionAlert(e1);
                                return;
                            } finally {
                                addNameSpaceBut.setVisible(true);
                                addNameSpaceText.setVisible(false);
                            }
                            stopTask();
                        }
                    });

                } else {
                    JOptionPane.showMessageDialog(window.parentJframe, "请输入命名空间名称", "异常", JOptionPane.ERROR_MESSAGE);
                }
            } else if (e.getKeyCode() == 27) {// ESC 取消
                addNameSpaceBut.setVisible(true);
                addNameSpaceText.setVisible(false);
            }

        }

    }

    /**
     * 添加命名空间
     * 
     * @author limin May 17, 2019 1:55:20 AM
     */
    class AddNameSpaceAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            addNameSpaceBut.setVisible(false);
            addNameSpaceText.setVisible(true);
            addNameSpaceText.grabFocus();// 获取输入焦点。

        }

    }

    /**
     * 删除命名空间监听
     * 
     * @author limin May 21, 2019 4:19:31 PM
     */
    class DelNamespaceAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            String namespace = nameSpaceList.getSelectedValue();
            if (JOptionPane.showConfirmDialog(getFrame(), "确定命名空间" + namespace + "吗?") == 0) {
                getSingleThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
                        startTask();
                        try {
                            HbaseUtil.deleteNameSpace(namespace);
                            // 刷新命名空间
                            nameSpaceList.setListData(HbaseUtil.listNameSpace());
                            nameSpaceList.setSelectedIndex(-1);
                            refreshCreateTableText();
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

    class RefreshNameSpaceAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            getSingleThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    startTask();
                    try {
                        nameSpaceList.setListData(HbaseUtil.listNameSpace());
                    } catch (Exception e1) {
                        exceptionAlert(e1);
                        return;
                    }
                    stopTask();

                }

            });

        }
    }

    class EditSettingsAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            refreshCreateTableText();
        }
    }

}
