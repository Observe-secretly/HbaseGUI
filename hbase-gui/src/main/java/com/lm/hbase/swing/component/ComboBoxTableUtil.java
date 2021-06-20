package com.lm.hbase.swing.component;

public class ComboBoxTableUtil {

    /**
     * 元数据Tab中类型转换列表
     */
    private static ComboBoxTable.JTabComboBoxOption[] JTAB_COMBOBOX_OPTIONS = new ComboBoxTable.JTabComboBoxOption[] { new ComboBoxTable.JTabComboBoxOption("String",
                                                                                                                                                            true),
                                                                                                                       new ComboBoxTable.JTabComboBoxOption("long"),
                                                                                                                       new ComboBoxTable.JTabComboBoxOption("int"),
                                                                                                                       new ComboBoxTable.JTabComboBoxOption("float"),
                                                                                                                       new ComboBoxTable.JTabComboBoxOption("double"),
                                                                                                                       new ComboBoxTable.JTabComboBoxOption("boolean") };

    public static ComboBoxTable.JTabComboBoxOption[] getDefaultJTabComboBoxOptions() {
        ComboBoxTable.JTabComboBoxOption[] defaultComboboxOptions = new ComboBoxTable.JTabComboBoxOption[] { new ComboBoxTable.JTabComboBoxOption("String",
                                                                                                                                                  true),
                                                                                                             new ComboBoxTable.JTabComboBoxOption("long"),
                                                                                                             new ComboBoxTable.JTabComboBoxOption("int"),
                                                                                                             new ComboBoxTable.JTabComboBoxOption("float"),
                                                                                                             new ComboBoxTable.JTabComboBoxOption("double"),
                                                                                                             new ComboBoxTable.JTabComboBoxOption("boolean") };
        return defaultComboboxOptions;
    }

    public static ComboBoxTable.JTabComboBoxOption[] getJTabComboBoxOptions(String selectedValue) {
        ComboBoxTable.JTabComboBoxOption[] result = new ComboBoxTable.JTabComboBoxOption[JTAB_COMBOBOX_OPTIONS.length];

        int index = 0;
        for (ComboBoxTable.JTabComboBoxOption item : JTAB_COMBOBOX_OPTIONS) {
            if (item.getValue().equalsIgnoreCase(selectedValue)) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
            result[index] = item.clone();
            index++;
        }
        return result;
    }
}
