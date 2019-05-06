package com.lm.hbase.common;

import java.nio.charset.Charset;

public class CommonConstons {

    public static final String  UTF8_ENCODING        = "UTF-8";

    public static final Charset UTF8_CHARSET         = Charset.forName(UTF8_ENCODING);

    public static final String  CLIENT_ADAPTER_ERROR = "Hbase客户驱动加载失败，请检查配置库地址";
}
