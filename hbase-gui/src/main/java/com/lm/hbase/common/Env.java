package com.lm.hbase.common;

import java.io.File;

public class Env {

    public static String BASEDIR             = getBasedir();

    public static String CONF_DIR            = getBasedir() + "conf" + System.getProperty("file.separator");

    public static String IMG_DIR             = getBasedir() + "img" + System.getProperty("file.separator");

    public static String MAVEN_DIR           = getBasedir() + "apache-maven" + System.getProperty("file.separator");

    public static String DRIVER_DIR          = getDriverDir();

    public static String HBASE_CONN_CONF_DIR = getHbaseConnConfDir();

    private static String getBasedir() {
        String path = System.getProperty("basedir", ClassLoader.getSystemClassLoader().getResource("").getPath())
                      + System.getProperty("file.separator");
        return path.replace("//", "/");
    }

    private static String getOsHomeBaseDir() {
        String homePath = System.getProperty("user.home");
        File guiConfDir = new File(homePath + System.getProperty("file.separator") + ".hbase-gui-conf");
        if (!guiConfDir.exists()) {
            guiConfDir.mkdir();
        }
        return guiConfDir.getPath() + System.getProperty("file.separator");
    }

    private static String getDriverDir() {
        File dir = new File(getOsHomeBaseDir() + "driver");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir.getPath() + System.getProperty("file.separator");
    }

    private static String getHbaseConnConfDir() {
        File dir = new File(getOsHomeBaseDir() + "hbaseConnConf");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir.getPath() + System.getProperty("file.separator");
    }

}
