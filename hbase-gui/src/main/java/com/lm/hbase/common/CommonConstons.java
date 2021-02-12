package com.lm.hbase.common;

import com.lm.hbase.swing.component.ComboBoxTable;

import java.nio.charset.Charset;

public class CommonConstons {

    public static final String  UTF8_ENCODING          = "UTF-8";

    public static final Charset UTF8_CHARSET           = Charset.forName(UTF8_ENCODING);

    public static final String  HBASE_CONF_FILE_PREFIX = "hbase-conf-";

    public static final int     ROW_HEIGHT             = 30;

}
