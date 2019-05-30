package com.lm.hbase.swing;

import java.util.HashMap;
import java.util.Map;

import org.xeustechnologies.jcl.JarClassLoader;

import com.lm.hbase.adapter.FilterFactoryInterface;
import com.lm.hbase.adapter.HbaseAdapterInterface;
import com.lm.hbase.conf.ConfItem;

public class SwingConstants {

    public static HbaseGui                    hbaseGui          = null;

    public static LoginGui                    loginGui          = null;

    public static ConfItem                    selectedConf      = null;

    public static HbaseAdapterInterface       hbaseAdapter      = null;

    public static FilterFactoryInterface      filterFactory     = null;

    public static String                      version           = null;

    public static Map<String, JarClassLoader> driverMap         = new HashMap<>();

    public static final String                NAMESPACE_DES     = "NameSpace==>";

    public static final String                TABLE_NAME_DES    = "TableName==>";

    public static final String                COLUMN_FAMILY_DES = "ColumnFamily==>";

    public static final String                START_ROWKEY_DES  = "Start Rowkey==>";

    public static final String                END_ROWKEY_DES    = "End Rowkey==>";

    public static final String                NUM_REGIONS_DES   = "numRegions==>";

    public static final String                MAX_VERSION_DES   = "maxVersion==>";

    public static final String                TIME_TO_LIVE_DES  = "timeToLive==>";

}
