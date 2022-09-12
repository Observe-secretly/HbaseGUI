package com.lm.hbase.tab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.lm.hbase.swing.SwingConstants;

public class TabCommonUtil {

    String[] tableNamesCache = null;

    public String[] getTableListCache() {
        return tableNamesCache;
    }

    /**
     * 初始化表
     * 
     * @param list
     * @throws Exception
     * @wbp.parser.entryPoint
     */
    public void initTableList(JList<String> list) throws Exception {
        String[] tableNames = SwingConstants.hbaseAdapter.getListTableNames();
        if (tableNames != null) {
            tableNamesCache = tableNames;
            list.setListData(tableNames);
        }
    }

    public void addPopup(Component component, final JPopupMenu popup) {
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

    /**
     * 表格自适应方法，需要在表格初始化后，父容器大小发生变化后以及表格模型修改后调用
     * 
     * @param bool
     * @param contentTable
     * @param tableScroll
     */
    public void resizeTable(boolean bool, JTable contentTable, JScrollPane tableScroll) {
        Dimension containerwidth = null;
        if (!bool) {
            // 初始化时，父容器大小为首选大小，实际大小为0
            containerwidth = tableScroll.getPreferredSize();
        } else {
            // 界面显示后，如果父容器大小改变，使用实际大小而不是首选大小
            containerwidth = tableScroll.getSize();
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
