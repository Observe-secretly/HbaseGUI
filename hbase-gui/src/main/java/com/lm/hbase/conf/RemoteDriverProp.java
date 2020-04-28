package com.lm.hbase.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.lm.hbase.common.Env;

public class RemoteDriverProp {

    private static Properties confProps = null;

    private static String getPropFilePath() {
        return Env.CONF_DIR + "remote-driver.properties";
    }

    /**
     * 获取配置文件，如果不存在则创建
     * 
     * @return
     */
    private static Properties loadProperties() {
        confProps = new Properties();
        try {
            File conf = new File(getPropFilePath());
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

    public static String getStringValue(String key) {
        if (confProps == null) {
            return loadProperties().getProperty(key);
        } else {
            return confProps.getProperty(key);
        }
    }

    /**
     * 获取资源文件里面所有的Key
     * 
     * @return
     */
    public static List<String> getKeys() {
        List<String> keyList = new ArrayList<>();
        for (Object item : loadProperties().keySet()) {
            keyList.add(item.toString());
        }

        Collections.sort(keyList);

        return keyList;
    }

}
