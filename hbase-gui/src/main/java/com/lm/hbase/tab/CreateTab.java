package com.lm.hbase.tab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lm.hbase.adapter.HbaseUtil;
import com.lm.hbase.common.ImageIconConstons;
import com.lm.hbase.swing.HbaseGui;
import com.lm.hbase.util.StringUtil;

public class CreateTab extends TabAbstract {

    private JButton               refreshNameSpaceBut;
    private JList<String>         nameSpaceList;

    private JButton               addTableNameBut;
    private DefaultValueTextField addTableNameText;

    private JButton               addNameSpaceBut;
    private JTextField            addNameSpaceText;

    private JButton               createTabBut;

    private JButton               addColumnFamilyBut;
    private DefaultValueTextField addColumnFamilyText;

    private JTextArea             showTextArea;

    private static String         namespace     = "";
    private static String         table_name    = "";
    private static String         column_family = "";

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
            refreshNameSpaceBut = new JButton("刷新", ImageIconConstons.Update_ICON);
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
                        CreateTab.namespace = nameSpaceList.getSelectedValue();
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

            addTableNameBut = new JButton("表名", ImageIconConstons.TABLE_ICON);
            addTableNameText = new DefaultValueTextField("表名");
            addTableNameText.setColumns(8);
            addTableNameText.setVisible(false);

            tableNorthPanel.add(addTableNameBut);
            tableNorthPanel.add(addTableNameText);

            addColumnFamilyText = new DefaultValueTextField("列族名");
            addColumnFamilyText.setColumns(8);
            addColumnFamilyText.setVisible(false);
            tableNorthPanel.add(addColumnFamilyText);

            addColumnFamilyBut = new JButton("添加列族", ImageIconConstons.ADD_ICON);
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

            addTableNameBut.addMouseListener(new AddTableNameAdapter());

            addTableNameText.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {

                    if (e.getKeyCode() == 10) {
                        CreateTab.table_name = addTableNameText.getText().trim();
                        refreshCreateTableText();

                        addTableNameBut.setVisible(true);
                        addTableNameText.setVisible(false);
                    } else if (e.getKeyCode() == 27) {
                        addTableNameBut.setVisible(true);
                        addTableNameText.setVisible(false);
                    }

                }

            });

            addColumnFamilyText.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == 10) {
                        CreateTab.column_family = addColumnFamilyText.getText().trim();
                        refreshCreateTableText();

                        addColumnFamilyBut.setVisible(true);
                        addColumnFamilyText.setVisible(false);
                    } else if (e.getKeyCode() == 27) {
                        addColumnFamilyBut.setVisible(true);
                        addColumnFamilyText.setVisible(false);
                    }
                }

            });

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

    class AddTableNameAdapter extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            addTableNameBut.setVisible(false);
            addTableNameText.setVisible(true);
            addTableNameText.grabFocus();// 获取输入焦点。
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
            addColumnFamilyBut.setVisible(false);
            addColumnFamilyText.setVisible(true);
            addColumnFamilyText.grabFocus();// 获取输入焦点。
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
                            CreateTab.namespace = "";
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

}
