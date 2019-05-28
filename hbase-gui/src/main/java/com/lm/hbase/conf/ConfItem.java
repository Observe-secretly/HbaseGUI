package com.lm.hbase.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.lm.hbase.common.Env;

/**
 * conf jlist内配置原型
 * 
 * @author limin May 28, 2019 10:16:50 PM
 */
public class ConfItem {

    private String displayName;

    private String confFileName;

    @Override
    public String toString() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return displayName;
    }

    public ConfItem(String displayName, String confFileName){
        this.displayName = displayName;
        this.confFileName = confFileName;
        loadProperties();

    }

    private Properties confProps = null;

    public String getConfFilePath() {
        return Env.CONF_DIR + confFileName;
    }

    /**
     * 获取配置文件，如果不存在则创建
     * 
     * @return
     */
    private Properties loadProperties() {
        confProps = new Properties();
        try {
            File conf = new File(getConfFilePath());
            if (!conf.exists()) {
                conf.createNewFile();
            }
            confProps.load(new InputStreamReader(new FileInputStream(conf)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return confProps;
    }

    public void reloadConf() {
        loadProperties();
    }

    public void setValue(String key, String value) {
        if (confProps == null) {
            loadProperties();
        }
        confProps.put(key, value);
        try {
            confProps.store(new FileOutputStream(getConfFilePath()), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStringValue(String key) {
        if (confProps == null) {
            return loadProperties().getProperty(key);
        } else {
            return confProps.getProperty(key);
        }
    }

    public Integer getIntegerValue(String key) {
        return Integer.parseInt(getStringValue(key));
    }

    public Long getLongValue(String key) {
        return Long.parseLong(getStringValue(key));
    }

    public Boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(getStringValue(key));
    }

    public void setConf(String zkPort, String zkQuorum, String hbaseMaster, String znodeParent, String version,
                        String mavenHome) {
        if (confProps == null) {
            loadProperties();
        }
        confProps.put("hbase.zk.port", zkPort);
        confProps.put("hbase.zk.quorum", zkQuorum);
        confProps.put("hbase.master", hbaseMaster);
        confProps.put("znode.parent", znodeParent);
        confProps.put("hbase.version", version);
        confProps.put("maven.home", mavenHome);
        try {
            confProps.store(new FileOutputStream(getConfFilePath()), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String key) {
        if (confProps == null) {
            loadProperties();
        }

        confProps.remove(key);

        try {
            confProps.store(new FileOutputStream(getConfFilePath()), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
