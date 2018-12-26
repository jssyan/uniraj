/*
 * Project: UniraSDKDemo
 * 
 * @(#) NetOneConfig.java   2015/2/2 13:50
 *
 * Copyright 2013 Jiangsu Syan Technology Co.,Ltd. All rights reserved.
 * Jiangsu Syan PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.com.syan.netone.unixx.unira.demo.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * This class provides...
 * </p>
 *
 * @author Iceberg
 * @version $Revision $Date:2015/2/2 13:50
 * @since 1.0
 */
public class NetOneConfig {
    private Properties properties;

    private NetOneConfig() {

        try {
            String path = this.getClass().getResource("/").getPath().replace("%20", " ");//得到工程名WEB-INF/classes/路径

            properties = new Properties();
            properties.load(new FileInputStream(path + "identity.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static NetOneConfig instance = null;

    public static NetOneConfig getInstance() {
        if (instance == null) {
            instance = new NetOneConfig();
        }

        return instance;
    }

    public String getIdentityCert() {
        return properties.getProperty("identityCert");
    }

    public String getOnlineUrl() {
        return properties.getProperty("onlineUrl");
    }

    public String getIdentityPrivateKey() {
        return properties.getProperty("identityPrivateKey");
    }

    public String getOU() {
        return properties.getProperty("ou");
    }

    public String getRAID() {
        return properties.getProperty("raID");
    }
}
