package com.lm.hbase.driver;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.lm.hbase.common.Env;

public final class DriverClassLoader {

    private static Method         addURL      = initAddMethod();

    private static URLClassLoader classloader = (URLClassLoader) ClassLoader.getSystemClassLoader();

    /**
     * 初始化addUrl 方法.
     * 
     * @return 可访问addUrl方法的Method对象
     */
    private static Method initAddMethod() {
        try {
            Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
            add.setAccessible(true);
            return add;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载jar classpath。
     */
    public static void loadClasspath(String version) {
        List<File> files = getJarFiles(version);
        for (File f : files) {
            loadClasspath(f);
        }

    }

    private static void loadClasspath(File jarFile) {
        loopFiles(jarFile);
    }

    /**
     * 循环遍历目录，找出所有的jar包。
     * 
     * @param file 当前遍历文件
     */
    private static void loopFiles(File file) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp);
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                System.out.println("load jar:" + file.getName());
                addURL(file);
            }
        }
    }

    /**
     * 通过filepath加载文件到classpath。
     * 
     * @param filePath 文件路径
     * @return URL
     * @throws Exception 异常
     */
    private static void addURL(File file) {
        try {
            addURL.invoke(classloader, new Object[] { file.toURI().toURL() });
        } catch (Exception e) {
        }
    }

    /***
     * 从配置文件中得到配置的需要加载到classpath里的路径集合。
     * 
     * @return
     */
    private static List<File> getJarFiles(String version) {
        List<File> files = new ArrayList<File>();
        File lib = new File(Env.DRIVER_DIR + version);
        System.out.println(lib.getPath());
        loopFiles(lib, files);
        return files;
    }

    private static final void loopFiles(File file, List<File> files) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp, files);
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                files.add(file);
            }
        }
    }

}
