package com.lm.hbase.swing.component;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ComboBoxTable extends JTable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = 1L;

    public static class CustomComboBoxEditor extends AbstractCellEditor implements TableCellEditor {

        private static final long serialVersionUID = 1L;

        JComboBox<?>              box;
        String[]                  values;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                     int column) {
            JTabComboBoxOption[] options = (JTabComboBoxOption[]) value;

            values = new String[options.length];

            String selected = "String";

            for (int i = 0; i < options.length; i++) {
                JTabComboBoxOption item = options[i];
                values[i] = item.getValue();
                if (item.isSelected) {
                    selected = item.getValue();
                }
            }

            box = new JComboBox(values);
            box.setSelectedItem(selected.toLowerCase(Locale.ROOT));

            box.setBounds(0, 0, 0, 30);
            return box;
        }

        @Override
        public Object getCellEditorValue() {
            return ComboBoxTableUtil.getJTabComboBoxOptions((String) box.getSelectedItem());
        }
    }

    public static class ComboxRenderer extends JComboBox implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JTabComboBoxOption[] options = (JTabComboBoxOption[]) value;

            String[] values = new String[options.length];

            String selected = "String";

            for (int i = 0; i < options.length; i++) {
                JTabComboBoxOption item = options[i];
                values[i] = item.getValue();
                if (item.isSelected) {
                    selected = item.getValue();
                }
            }

            JComboBox box = new JComboBox(values);
            box.setSelectedItem(selected.toLowerCase(Locale.ROOT));

            box.setBounds(0, 0, 0, 30);
            return box;
        }
    }

    /**
     * 设置ComboBoxTable内ComboBox的option。允许设置默认值
     * 
     * @author limin 2021年2月10日 下午3:02:15
     */
    public static class JTabComboBoxOption implements Cloneable{

        private String  value;

        private boolean isSelected = false;

        public JTabComboBoxOption(){
        }

        public JTabComboBoxOption(String value){
            this.value = value;
            this.isSelected = false;
        }

        public JTabComboBoxOption(String value, boolean isSelected){
            this.value = value;
            this.isSelected = isSelected;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        public JTabComboBoxOption clone() {
            return  new JTabComboBoxOption(value,isSelected);
        }
    }

}
