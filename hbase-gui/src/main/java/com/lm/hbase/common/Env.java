package com.lm.hbase.common;

public class Env {

    public static String BASEDIR    = getBasedir();

    public static String CONF_DIR   = getBasedir() + "conf" + System.getProperty("file.separator");

    public static String IMG_DIR    = getBasedir() + "img" + System.getProperty("file.separator");

    public static String DRIVER_DIR = getBasedir() + "driver" + System.getProperty("file.separator");

    public static String MAVEN_DIR  = getBasedir() + "apache-maven" + System.getProperty("file.separator");

    private static String getBasedir() {
        String path = System.getProperty("basedir", ClassLoader.getSystemClassLoader().getResource("").getPath())
                      + System.getProperty("file.separator");
        return path.replace("//", "/");
    }
}
