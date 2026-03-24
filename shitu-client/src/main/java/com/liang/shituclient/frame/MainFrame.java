package com.liang.shituclient.frame;

import com.liang.shituclient.panel.*;
import com.liang.shitucommon.entity.Category;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 主窗口
 */
public class MainFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final String apiBaseUrl;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel titleLabel;
    private JTextField searchField;

    public MainFrame(String appName, String appVersion, String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;

        // 设置窗口属性
        setTitle(appName + " v" + appVersion);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        // 设置图标（如果有）
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "未找到图标文件，使用默认图标");
        }

        initUI();
        loadCategories();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        // 创建主面板，使用BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // 创建顶部导航栏
        JPanel navPanel = createNavPanel();
        mainPanel.add(navPanel, BorderLayout.NORTH);

        // 创建内容面板（卡片布局）
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 添加各个页面
        contentPanel.add(new HomePanel(this), "HOME");
        contentPanel.add(new PlaceListPanel(this), "PLACE_LIST");
        contentPanel.add(new PlaceDetailPanel(this), "PLACE_DETAIL");
        contentPanel.add(new ReportPanel(this), "REPORT");
        contentPanel.add(new FavoritePanel(this), "FAVORITE");

        setContentPane(mainPanel);

        // 默认显示首页
        cardLayout.show(contentPanel, "HOME");
    }

    /**
     * 创建导航栏
     */
    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(new Color(52, 152, 219));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 左侧：标题和导航按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        // 应用标题
        titleLabel = new JLabel("识途");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        leftPanel.add(titleLabel);

        // 导航按钮
        JButton homeBtn = createNavButton("首页", "HOME");
        JButton placeBtn = createNavButton("附近点位", "PLACE_LIST");
        JButton reportBtn = createNavButton("我要上报", "REPORT");
        JButton favoriteBtn = createNavButton("我的收藏", "FAVORITE");

        leftPanel.add(homeBtn);
        leftPanel.add(placeBtn);
        leftPanel.add(reportBtn);
        leftPanel.add(favoriteBtn);

        navPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧：搜索框
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "搜索点位...");
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchField.addActionListener(e -> performSearch());

        JButton searchBtn = new JButton("搜索");
        searchBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchBtn.setBackground(new Color(41, 128, 185));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> performSearch());

        rightPanel.add(searchField);
        rightPanel.add(searchBtn);

        navPanel.add(rightPanel, BorderLayout.EAST);

        return navPanel;
    }

    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);

            // 如果切换到点位列表，刷新数据
            if ("PLACE_LIST".equals(cardName)) {
                PlaceListPanel panel = (PlaceListPanel) getPanel("PLACE_LIST");
                if (panel != null) {
                    panel.refresh();
                }
            }

            // 如果切换到收藏，刷新数据
            if ("FAVORITE".equals(cardName)) {
                FavoritePanel panel = (FavoritePanel) getPanel("FAVORITE");
                if (panel != null) {
                    panel.loadFavorites();
                }
            }
        });

        return button;
    }

    /**
     * 执行搜索
     */
    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (!keyword.isEmpty()) {
            showPlaceList(keyword);
        } else {
            JOptionPane.showMessageDialog(this,
                    "请输入搜索关键词",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 根据名称获取面板
     */
    private Component getPanel(String name) {
        for (Component comp : contentPanel.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }

    /**
     * 加载分类数据
     */
    private void loadCategories() {
        new SwingWorker<List<Category>, Void>() {
            @Override
            protected List<Category> doInBackground() throws Exception {
                Result<List<Category>> result = ApiClient.get("/category/enabled",
                        null, new TypeReference<Result<List<Category>>>() {});
                return result != null && result.isSuccess() ? result.getData() : new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    List<Category> categories = get();
                    // 可以在这里缓存分类数据，供其他地方使用
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载分类失败", e);
                }
            }
        }.execute();
    }

    // ==================== 公共方法 ====================

    /**
     * 显示点位列表（所有点位）
     */
    public void showPlaceList() {
        showPlaceList(null);
    }

    /**
     * 显示点位列表（带关键词）
     */
    public void showPlaceList(String keyword) {
        PlaceListPanel panel = (PlaceListPanel) getPanel("PLACE_LIST");
        if (panel != null) {
            if (keyword != null && !keyword.isEmpty()) {
                panel.search(keyword);
            } else {
                panel.refresh();
            }
        }
        cardLayout.show(contentPanel, "PLACE_LIST");
    }

    /**
     * 按分类显示点位列表
     */
    public void showPlaceListByCategory(Integer categoryId, String categoryName) {
        PlaceListPanel panel = (PlaceListPanel) getPanel("PLACE_LIST");
        if (panel != null) {
            panel.filterByCategory(categoryId, categoryName);
        }
        cardLayout.show(contentPanel, "PLACE_LIST");
    }

    /**
     * 显示点位详情
     */
    public void showPlaceDetail(Integer placeId) {
        PlaceDetailPanel panel = (PlaceDetailPanel) getPanel("PLACE_DETAIL");
        if (panel != null) {
            panel.loadPlace(placeId);
        }
        cardLayout.show(contentPanel, "PLACE_DETAIL");
    }

    /**
     * 显示上报页面
     */
    public void showReportPanel() {
        cardLayout.show(contentPanel, "REPORT");
    }

    /**
     * 显示收藏页面
     */
    public void showFavoritePanel() {
        FavoritePanel panel = (FavoritePanel) getPanel("FAVORITE");
        if (panel != null) {
            panel.loadFavorites();
        }
        cardLayout.show(contentPanel, "FAVORITE");
    }

    /**
     * 显示首页
     */
    public void showHome() {
        cardLayout.show(contentPanel, "HOME");
    }

    /**
     * 获取API基础URL
     */
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    /**
     * 设置标题（用于显示点位名称等）
     */
    public void setTitleText(String text) {
        if (titleLabel != null) {
            titleLabel.setText(text);
        }
    }

    /**
     * 重置标题
     */
    public void resetTitle() {
        if (titleLabel != null) {
            titleLabel.setText("识途");
        }
    }

    /**
     * 显示加载中状态
     */
    public void showLoading() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * 隐藏加载中状态
     */
    public void hideLoading() {
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * 显示提示消息
     */
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * 显示确认对话框
     */
    public boolean showConfirm(String message, String title) {
        int result = JOptionPane.showConfirmDialog(this, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}