package com.lm.hbase.util;

import com.lm.hbase.common.CommonConstons;

public class MyBytesUtil {

    public static String byteToString(final byte[] b, int off, int len) {
        if (b == null) {
            return null;
        }
        if (len == 0) {
            return "";
        }
        return new String(b, off, len, CommonConstons.UTF8_CHARSET);
    }

    public static byte[] toBytes(String s) {
        return s.getBytes(CommonConstons.UTF8_CHARSET);
    }

}
