package com.liang.shituclient;

import com.formdev.flatlaf.FlatLightLaf;
import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;

import javax.swing.*;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 识途客户端启动类
 */
public class ShituClient {
    private static final Logger LOGGER = Logger.getLogger(ShituClient.class.getName());

    private static String apiBaseUrl = "http://localhost:8080/api/v1";
    private static String appName = "识途 - 便民服务导航系统";
    private static String appVersion = "1.0.0";

    static {
        // 加载配置文件
        try (InputStream input = ShituClient.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                apiBaseUrl = prop.getProperty("api.base-url", apiBaseUrl);
                appName = prop.getProperty("app.name", appName);
                appVersion = prop.getProperty("app.version", appVersion);

                // 设置ApiClient的基础URL
                ApiClient.setBaseUrl(apiBaseUrl);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "加载配置文件失败，使用默认配置", e);
            ApiClient.setBaseUrl(apiBaseUrl);
        }
    }

    public static void main(String[] args) {
        // 设置FlatLaf外观
        FlatLightLaf.setup();

        // 设置系统外观属性
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 10);

        // 在EDT线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame(appName, appVersion, apiBaseUrl);
                frame.setVisible(true);
                LOGGER.info("识途客户端启动成功");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "启动失败", e);
                JOptionPane.showMessageDialog(null,
                        "启动失败：" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}