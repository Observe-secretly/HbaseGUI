package com.lm.hbase.driver;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;

import javax.swing.JLabel;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.lm.hbase.common.Env;
import com.lm.hbase.util.network.HttpURLConnectionFactory;

/**
 * 根据提供的版本号去中央仓库下载指定版本的hbase client驱动
 * 
 * @author limin Apr 22, 2019 11:06:03 PM
 */
public class DownloadDriver {

    private final static String ALIYUN_TAG = "aliyun-";

    /**
     * 通过版本号前缀判断是否阿里云版本
     * 
     * @param version
     * @return
     */
    private static boolean isAliyun(String version) {
        // 是否是阿里云版本。通过前缀判断
        boolean isAliyun = version.startsWith(ALIYUN_TAG);
        return isAliyun;
    }

    private static String getPomUrl(String version) {
        if (isAliyun(version)) {
            String aliyunVersion = version.replace(ALIYUN_TAG, "");
            StringBuilder url = new StringBuilder("https://maven.aliyun.com/repository/central/com/aliyun/hbase/alihbase-client");
            url.append("/" + aliyunVersion + "/");
            url.append("alihbase-client-");
            url.append(aliyunVersion);
            url.append(".pom");
            return url.toString();
        } else {
            StringBuilder url = new StringBuilder("https://maven.aliyun.com/repository/central/org/apache/hbase/hbase-client");
            url.append("/" + version + "/");
            url.append("hbase-client-");
            url.append(version);
            url.append(".pom");
            return url.toString();
        }
    }

    private static String getJarUrl(String version) {
        if (isAliyun(version)) {
            String aliyunVersion = version.replace(ALIYUN_TAG, "");
            StringBuilder url = new StringBuilder("https://maven.aliyun.com/repository/central/com/aliyun/hbase/alihbase-client");
            url.append("/" + aliyunVersion + "/");
            url.append("alihbase-client-");
            url.append(aliyunVersion);
            url.append(".jar");
            return url.toString();
        } else {
            StringBuilder url = new StringBuilder("https://maven.aliyun.com/repository/central/org/apache/hbase/hbase-client");
            url.append("/" + version + "/");
            url.append("hbase-client-");
            url.append(version);
            url.append(".jar");
            return url.toString();
        }

    }

    public static boolean load(String version, String mavenHome, JLabel progressInfoLabel) throws Throwable {
        String outputDir = Env.DRIVER_DIR + version;
        // XXX 如果驱动存在则不处理。后续完善可以添加校验功能
        File outputFileDir = new File(outputDir);
        if (outputFileDir.exists()) {
            return true;
        } else {
            // 创建版本目录
            outputFileDir.mkdir();

        }

        // 下载pom已经client jar
        progressInfoLabel.setText("download pom file ...");
        System.out.println("download file:" + getPomUrl(version));
        HttpURLConnection con = HttpURLConnectionFactory.getConn(getPomUrl(version));

        HttpURLConnectionFactory.downloadFile(con, outputDir, "pom.xml");
        progressInfoLabel.setText("download hbase-client-" + version + ".jar ...");

        System.out.println("download file:" + getJarUrl(version));
        con = HttpURLConnectionFactory.getConn(getJarUrl(version));
        HttpURLConnectionFactory.downloadFile(con, outputDir, "hbase-client-" + version + ".jar");

        File pomFile = new File(outputDir);
        StringBuilder cmd = new StringBuilder("dependency:copy-dependencies -DoutputDirectory=");
        cmd.append(outputDir);

        progressInfoLabel.setText("download hbase-client dependencies jars  ...");
        // 执行maven命令下载并拷贝依赖jar
        boolean result = executeMavenCmd(cmd.toString(), pomFile, mavenHome);
        if (result) {
            progressInfoLabel.setText("download hbase-client dependencies jars  success");
        } else {
            progressInfoLabel.setText("download hbase-client dependencies jars  error");
        }

        return result;
    }

    public static boolean executeMavenCmd(String cmd, File pomFile, String mavenHome) {
        System.out.println("download dependency jar." + cmd);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList(cmd));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mavenHome));

        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() == 0) {
                System.out.println("download dependency jar success");
                return true;
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
