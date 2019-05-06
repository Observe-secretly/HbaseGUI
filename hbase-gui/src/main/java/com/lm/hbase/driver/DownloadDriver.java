package com.lm.hbase.driver;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Collections;

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

    private static String getPomUrl(String version) {
        StringBuilder url = new StringBuilder("http://central.maven.org/maven2/org/apache/hbase/hbase-client");
        url.append("/" + version + "/");
        url.append("hbase-client-");
        url.append(version);
        url.append(".pom");
        return url.toString();

    }

    public static boolean load(String version, String mavenHome) throws Throwable {

        String outputDir = Env.DRIVER_DIR + version;
        // 如果驱动存在则删除重新下载
        System.out.println("clean driver dir");
        File outputFileDir = new File(outputDir);
        if (outputFileDir.exists()) {
            delFolder(outputDir);
        }
        // 创建版本目录
        outputFileDir.mkdir();

        // 下载pom
        System.out.println("download pom file to " + outputDir);
        HttpURLConnection con = HttpURLConnectionFactory.getConn(getPomUrl(version));
        HttpURLConnectionFactory.downloadFile(con, outputDir, "pom.xml");
        System.out.println("download pom file success");

        File pomFile = new File(outputDir);
        StringBuilder cmd = new StringBuilder("dependency:copy-dependencies -DoutputDirectory=");
        cmd.append(outputDir);

        return executeMavenCmd(cmd.toString(), pomFile, mavenHome);
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

    private static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

}
