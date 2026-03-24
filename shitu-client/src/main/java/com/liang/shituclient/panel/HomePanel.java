package com.liang.shituclient.panel;

import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.entity.*;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 首页面板
 */
public class HomePanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(HomePanel.class.getName());

    private final MainFrame mainFrame;
    private JPanel categoryPanel;
    private JList<String> recentList;
    private DefaultListModel<String> recentListModel;

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setName("HOME");
        setLayout(new BorderLayout());
        initUI();
        loadCategories();
        loadRecentPlaces();
    }

    private void initUI() {
        // 顶部欢迎语
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomePanel.setBackground(new Color(240, 240, 240));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel welcomeLabel = new JLabel("欢迎使用识途 - 城市便民服务导航系统");
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);

        add(welcomePanel, BorderLayout.NORTH);

        // 分类快捷入口
        categoryPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        categoryPanel.setBackground(Color.WHITE);

        // 显示加载中的占位按钮
        for (int i = 0; i < 6; i++) {
            JButton loadingBtn = new JButton("加载中...");
            loadingBtn.setEnabled(false);
            categoryPanel.add(loadingBtn);
        }

        add(categoryPanel, BorderLayout.CENTER);

        // 底部最近访问
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(BorderFactory.createTitledBorder("最近访问"));
        recentPanel.setPreferredSize(new Dimension(0, 200));

        recentListModel = new DefaultListModel<>();
        recentList = new JList<>(recentListModel);
        recentList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        recentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && recentList.getSelectedValue() != null) {
                String selected = recentList.getSelectedValue();
                // 从选中文本中提取点位ID（假设格式：名称 - 距离 - ID）
                try {
                    String[] parts = selected.split(" - ");
                    if (parts.length >= 3) {
                        Integer placeId = Integer.parseInt(parts[2]);
                        mainFrame.showPlaceDetail(placeId);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "解析最近访问失败", ex);
                }
            }
        });

        recentPanel.add(new JScrollPane(recentList), BorderLayout.CENTER);
        add(recentPanel, BorderLayout.SOUTH);
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
                if (result != null && result.isSuccess()) {
                    return result.getData();
                }
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    List<Category> categories = get();
                    updateCategoryButtons(categories);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载分类失败", e);
                    // 加载失败时显示默认分类
                    List<Category> defaultCategories = new ArrayList<>();
                    String[] names = {"公厕", "饮水点", "充电桩", "无障碍设施", "自助机", "更多"};
                    for (int i = 0; i < names.length; i++) {
                        Category cat = new Category();
                        cat.setId(i + 1);
                        cat.setName(names[i]);
                        defaultCategories.add(cat);
                    }
                    updateCategoryButtons(defaultCategories);
                }
            }
        }.execute();
    }

    /**
     * 更新分类按钮
     */
    private void updateCategoryButtons(List<Category> categories) {
        categoryPanel.removeAll();

        for (Category cat : categories) {
            categoryPanel.add(createCategoryButton(cat));
        }

        // 如果少于6个，补充"更多"按钮
        if (categories.size() < 6) {
            Category more = new Category();
            more.setId(-1);
            more.setName("更多");
            categoryPanel.add(createCategoryButton(more));
        }

        categoryPanel.revalidate();
        categoryPanel.repaint();
    }

    /**
     * 创建分类按钮
     */
    private JButton createCategoryButton(Category category) {
        JButton button = new JButton(category.getName());
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 100));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        if (category.getId() > 0) {
            button.addActionListener(e -> {
                mainFrame.showPlaceListByCategory(category.getId(), category.getName());
            });
        } else {
            button.addActionListener(e -> {
                mainFrame.showPlaceList(null);
            });
        }

        return button;
    }

    /**
     * 加载最近访问
     */
    private void loadRecentPlaces() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                // TODO: 调用API获取最近访问
                // 临时返回模拟数据
                List<String> recent = new ArrayList<>();
                recent.add("人民广场公厕 - 距离500m - 1");
                recent.add("地铁站饮水点 - 距离300m - 2");
                recent.add("停车场充电桩 - 距离800m - 3");
                return recent;
            }

            @Override
            protected void done() {
                try {
                    List<String> recent = get();
                    recentListModel.clear();
                    for (String item : recent) {
                        recentListModel.addElement(item);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载最近访问失败", e);
                }
            }
        }.execute();
    }
}