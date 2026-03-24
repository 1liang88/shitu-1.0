package com.liang.shituclient.panel;

import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.entity.Place;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 点位详情面板
 */
public class PlaceDetailPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PlaceDetailPanel.class.getName());

    private final MainFrame mainFrame;
    private Place currentPlace;
    private boolean isFavorite = false;

    // UI组件
    private JLabel nameLabel;
    private JLabel categoryLabel;
    private JLabel addressLabel;
    private JLabel contactLabel;
    private JLabel hoursLabel;
    private JLabel ratingLabel;
    private JLabel visitLabel;
    private JTextArea descArea;
    private JButton favoriteBtn;
    private JButton reportBtn;
    private JButton navigateBtn;

    // 地图组件
    private JPanel mapContainer;
    private JLabel mapLabel;
    private JLabel loadingLabel;

    public PlaceDetailPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setName("PLACE_DETAIL");
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 顶部标题栏
        JPanel titlePanel = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("← 返回");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            ((CardLayout) getParent().getLayout()).show(getParent(), "PLACE_LIST");
        });
        titlePanel.add(backBtn, BorderLayout.WEST);

        nameLabel = new JLabel();
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(nameLabel, BorderLayout.CENTER);

        titlePanel.add(Box.createHorizontalStrut(80), BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // 创建主内容面板（左右分栏）
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setBorder(null);

        // 左侧信息面板
        JPanel leftPanel = createInfoPanel();
        splitPane.setLeftComponent(leftPanel);

        // 右侧地图容器
        mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBorder(BorderFactory.createTitledBorder("地图位置"));
        mapContainer.setPreferredSize(new Dimension(400, 300));

        // 初始化加载中的提示
        loadingLabel = new JLabel("加载地图中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        mapContainer.add(loadingLabel, BorderLayout.CENTER);

        splitPane.setRightComponent(mapContainer);

        add(splitPane, BorderLayout.CENTER);

        // 底部按钮栏
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        favoriteBtn = new JButton("收藏");
        favoriteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        favoriteBtn.setPreferredSize(new Dimension(100, 40));

        reportBtn = new JButton("上报纠错");
        reportBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        reportBtn.setPreferredSize(new Dimension(100, 40));

        navigateBtn = new JButton("路线指引");
        navigateBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        navigateBtn.setPreferredSize(new Dimension(100, 40));

        favoriteBtn.addActionListener(e -> toggleFavorite());
        reportBtn.addActionListener(e -> showReportDialog());
        navigateBtn.addActionListener(e -> navigate());

        buttonPanel.add(favoriteBtn);
        buttonPanel.add(reportBtn);
        buttonPanel.add(navigateBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // 分类
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        panel.add(new JLabel("分　　类："), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        categoryLabel = new JLabel();
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(categoryLabel, gbc);

        // 地址
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("地　　址："), gbc);

        gbc.gridx = 1;
        addressLabel = new JLabel();
        addressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(addressLabel, gbc);

        // 电话
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("联系电话："), gbc);

        gbc.gridx = 1;
        contactLabel = new JLabel();
        contactLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(contactLabel, gbc);

        // 开放时间
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("开放时间："), gbc);

        gbc.gridx = 1;
        hoursLabel = new JLabel();
        hoursLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(hoursLabel, gbc);

        // 评分
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("评　　分："), gbc);

        gbc.gridx = 1;
        ratingLabel = new JLabel();
        ratingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(ratingLabel, gbc);

        // 访问量
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("访 问 量："), gbc);

        gbc.gridx = 1;
        visitLabel = new JLabel();
        visitLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(visitLabel, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("描　　述："), gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        descArea = new JTextArea(8, 30);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        descArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(descArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, gbc);

        return panel;
    }

    /**
     * 加载点位详情
     */
    public void loadPlace(Integer placeId) {
        setLoading(true);

        new SwingWorker<Place, Void>() {
            @Override
            protected Place doInBackground() throws Exception {
                // 使用 TypeReference 而不是 Class
                Result<Place> result = ApiClient.get("/place/" + placeId,
                        new TypeReference<Result<Place>>() {});

                if (result != null && result.isSuccess()) {
                    return result.getData();
                }
                throw new Exception(result != null ? result.getMessage() : "加载失败");
            }

            @Override
            protected void done() {
                try {
                    currentPlace = get();
                    refreshDisplay();
                    checkFavoriteStatus();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载点位详情失败", e);
                    JOptionPane.showMessageDialog(PlaceDetailPanel.this,
                            "加载失败：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    /**
     * 刷新显示
     */
    private void refreshDisplay() {
        if (currentPlace == null) return;

        nameLabel.setText(currentPlace.getName());
        categoryLabel.setText(currentPlace.getCategoryName() != null ? currentPlace.getCategoryName() : "未分类");
        addressLabel.setText(currentPlace.getAddress() != null ? currentPlace.getAddress() : "暂无地址");
        contactLabel.setText(currentPlace.getContactPhone() != null ? currentPlace.getContactPhone() : "暂无");
        hoursLabel.setText(currentPlace.getOpeningHours() != null ? currentPlace.getOpeningHours() : "暂无");

        if (currentPlace.getRating() != null) {
            ratingLabel.setText(String.valueOf(currentPlace.getRating()));
        } else {
            ratingLabel.setText("0");
        }

        visitLabel.setText(String.valueOf(currentPlace.getVisitCount() != null ? currentPlace.getVisitCount() : 0));
        descArea.setText(currentPlace.getDescription() != null ? currentPlace.getDescription() : "暂无描述");

        // 更新地图
        loadStaticMap();
    }

    /**
     * 加载静态地图（通过后端API）
     */
    private void loadStaticMap() {
        if (currentPlace == null) return;

        // 如果点位有坐标，直接加载
        if (currentPlace.getLongitude() != null && currentPlace.getLatitude() != null) {
            loadStaticMapWithCoords();
            return;
        }

        // 如果没有坐标但有地址，尝试获取坐标
        if (currentPlace.getAddress() != null && !currentPlace.getAddress().isEmpty()) {
            getCoordinatesAndLoadMap();
        } else {
            showNoMapInfo();
        }
    }

    /**
     * 先获取坐标再加载地图
     */
    private void getCoordinatesAndLoadMap() {
        mapContainer.removeAll();
        JLabel loadingLabel = new JLabel("正在获取位置信息...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        mapContainer.add(loadingLabel, BorderLayout.CENTER);
        mapContainer.revalidate();
        mapContainer.repaint();

        new SwingWorker<BigDecimal[], Void>() {
            @Override
            protected BigDecimal[] doInBackground() throws Exception {
                String encodedAddress = URLEncoder.encode(currentPlace.getAddress(), StandardCharsets.UTF_8.toString());
                Result<BigDecimal[]> result = ApiClient.get(
                        "/geocode/address?address=" + encodedAddress,
                        new TypeReference<Result<BigDecimal[]>>() {}
                );
                return result != null && result.isSuccess() ? result.getData() : null;
            }

            @Override
            protected void done() {
                try {
                    BigDecimal[] coords = get();
                    if (coords != null && coords.length == 2) {
                        currentPlace.setLongitude(coords[0]);
                        currentPlace.setLatitude(coords[1]);
                        loadStaticMapWithCoords();
                    } else {
                        showNoMapInfo();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "获取坐标失败", e);
                    showNoMapInfo();
                }
            }
        }.execute();
    }

    /**
     * 加载静态地图（使用已有坐标）
     */
    private void loadStaticMapWithCoords() {
        mapContainer.removeAll();
        JLabel loadingLabel = new JLabel("加载地图中...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        mapContainer.add(loadingLabel, BorderLayout.CENTER);
        mapContainer.revalidate();
        mapContainer.repaint();

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 调用后端接口获取静态地图URL - 使用 TypeReference
                Result<String> result = ApiClient.get(
                        String.format("/staticmap/single?longitude=%f&latitude=%f&zoom=15&width=400&height=300",
                                currentPlace.getLongitude().doubleValue(),
                                currentPlace.getLatitude().doubleValue()),
                        new TypeReference<Result<String>>() {}
                );
                return result != null && result.isSuccess() ? result.getData() : null;
            }

            @Override
            protected void done() {
                try {
                    String mapUrl = get();
                    if (mapUrl != null) {
                        loadMapImage(mapUrl);
                    } else {
                        showMapLoadError();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "获取地图URL失败", e);
                    showMapLoadError();
                }
            }
        }.execute();
    }

    /**
     * 加载地图图片
     */
    private void loadMapImage(String mapUrl) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                java.net.URL url = new java.net.URL(mapUrl);
                return new ImageIcon(url);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon mapImage = get();
                    mapLabel = new JLabel(mapImage);
                    mapLabel.setHorizontalAlignment(SwingConstants.CENTER);

                    mapContainer.removeAll();
                    mapContainer.add(mapLabel, BorderLayout.CENTER);
                    mapContainer.revalidate();
                    mapContainer.repaint();

                    LOGGER.info("静态地图加载成功");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载地图图片失败", e);
                    showMapLoadError();
                }
            }
        }.execute();
    }

    /**
     * 显示无地图信息
     */
    private void showNoMapInfo() {
        mapContainer.removeAll();
        JLabel infoLabel = new JLabel("该点位暂无地图位置信息", SwingConstants.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        infoLabel.setForeground(Color.GRAY);
        mapContainer.add(infoLabel, BorderLayout.CENTER);
        mapContainer.revalidate();
        mapContainer.repaint();
    }

    /**
     * 显示地图加载错误
     */
    private void showMapLoadError() {
        mapContainer.removeAll();
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setBackground(Color.WHITE);

        JLabel errorLabel = new JLabel("<html><center>地图加载失败<br>请稍后重试</center></html>");
        errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

        errorPanel.add(errorLabel);
        mapContainer.add(errorPanel, BorderLayout.CENTER);
        mapContainer.revalidate();
        mapContainer.repaint();
    }

    /**
     * 检查收藏状态
     */
    private void checkFavoriteStatus() {
        if (currentPlace == null) return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // 使用 TypeReference
                Result<Boolean> result = ApiClient.get("/favorite/check/" + currentPlace.getId(),
                        new TypeReference<Result<Boolean>>() {});
                return result != null && result.isSuccess() && Boolean.TRUE.equals(result.getData());
            }

            @Override
            protected void done() {
                try {
                    isFavorite = get();
                    favoriteBtn.setText(isFavorite ? "已收藏" : "收藏");
                    favoriteBtn.setBackground(isFavorite ? new Color(255, 215, 0) : null);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "检查收藏状态失败", e);
                }
            }
        }.execute();
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        if (currentPlace == null) return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (isFavorite) {
                    // 取消收藏 - 使用 TypeReference
                    Result<Void> result = ApiClient.delete("/favorite/" + currentPlace.getId(),
                            new TypeReference<Result<Void>>() {});
                    return result != null && result.isSuccess();
                } else {
                    // 添加收藏 - 使用 TypeReference
                    Result<Void> result = ApiClient.post("/favorite/" + currentPlace.getId(), null,
                            new TypeReference<Result<Void>>() {});
                    return result != null && result.isSuccess();
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        isFavorite = !isFavorite;
                        favoriteBtn.setText(isFavorite ? "已收藏" : "收藏");
                        favoriteBtn.setBackground(isFavorite ? new Color(255, 215, 0) : null);

                        String message = isFavorite ? "已添加到收藏" : "已取消收藏";
                        JOptionPane.showMessageDialog(PlaceDetailPanel.this,
                                message, "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "操作收藏失败", e);
                    JOptionPane.showMessageDialog(PlaceDetailPanel.this,
                            "操作失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        setCursor(loading ?
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) :
                Cursor.getDefaultCursor());
    }

    private void showReportDialog() {
        ReportPanel reportPanel = (ReportPanel) SwingUtilities.getAncestorOfClass(
                ReportPanel.class, mainFrame.getContentPane());
        if (reportPanel != null) {
            reportPanel.setPlaceInfo(currentPlace);
            ((CardLayout) getParent().getLayout()).show(getParent(), "REPORT");
        }
    }

    private void navigate() {
        if (currentPlace == null) return;

        String message = String.format(
                "从当前位置到 %s\n\n地址：%s\n经度：%f\n纬度：%f\n\n确定要开始导航吗？",
                currentPlace.getName(),
                currentPlace.getAddress(),
                currentPlace.getLongitude() != null ? currentPlace.getLongitude().doubleValue() : 0,
                currentPlace.getLatitude() != null ? currentPlace.getLatitude().doubleValue() : 0
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "路线指引",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                String url = String.format(
                        "https://uri.amap.com/navigation?from=我的位置&to=%f,%f&mode=car",
                        currentPlace.getLatitude().doubleValue(),
                        currentPlace.getLongitude().doubleValue()
                );
                Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "无法打开地图应用",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}