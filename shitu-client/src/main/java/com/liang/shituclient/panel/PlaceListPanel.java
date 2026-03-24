package com.liang.shituclient.panel;

import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.entity.*;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 点位列表面板
 */
public class PlaceListPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(PlaceListPanel.class.getName());

    private final MainFrame mainFrame;
    private JPanel listPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    private String currentKeyword = "";
    private Integer currentCategoryId = null;
    private String currentCategoryName = "全部";

    public PlaceListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setName("PLACE_LIST");
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // 顶部工具栏
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 左侧筛选区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("分类："));

        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("全部");
        loadCategories();
        categoryFilter.addActionListener(e -> {
            int index = categoryFilter.getSelectedIndex();
            if (index == 0) {
                currentCategoryId = null;
                currentCategoryName = "全部";
            } else {
                currentCategoryId = index;
                currentCategoryName = categoryFilter.getSelectedItem().toString();
            }
            refresh();
        });
        leftPanel.add(categoryFilter);

        // 右侧搜索区域
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "输入点位名称...");

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> {
            currentKeyword = searchField.getText().trim();
            refresh();
        });

        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refresh());

        rightPanel.add(searchField);
        rightPanel.add(searchBtn);
        rightPanel.add(refreshBtn);

        toolBar.add(leftPanel, BorderLayout.WEST);
        toolBar.add(rightPanel, BorderLayout.EAST);
        add(toolBar, BorderLayout.NORTH);

        // 列表面板
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 初始加载
        refresh();
    }

    /**
     * 加载分类列表到筛选框
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
                    for (Category cat : categories) {
                        categoryFilter.addItem(cat.getName());
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载分类列表失败", e);
                }
            }
        }.execute();
    }

    /**
     * 刷新列表
     */
    public void refresh() {
        loadPlaces(currentKeyword, currentCategoryId);
    }

    /**
     * 按分类筛选
     */
    public void filterByCategory(Integer categoryId, String categoryName) {
        this.currentCategoryId = categoryId;
        this.currentCategoryName = categoryName;
        if (categoryId != null) {
            categoryFilter.setSelectedItem(categoryName);
        } else {
            categoryFilter.setSelectedIndex(0);
        }
        refresh();
    }

    /**
     * 搜索点位
     */
    public void search(String keyword) {
        this.currentKeyword = keyword;
        searchField.setText(keyword);
        refresh();
    }

    /**
     * 加载点位数据
     */
    private void loadPlaces(String keyword, Integer categoryId) {
        listPanel.removeAll();
        listPanel.add(createLoadingItem());
        listPanel.revalidate();

        new SwingWorker<List<Place>, Void>() {
            @Override
            protected List<Place> doInBackground() throws Exception {
                Map<String, String> params = new HashMap<>();
                if (keyword != null && !keyword.isEmpty()) {
                    params.put("keyword", keyword);
                }
                if (categoryId != null) {
                    params.put("categoryId", String.valueOf(categoryId));
                }

                Result<List<Place>> result = ApiClient.get("/place/list", params,
                        new TypeReference<Result<List<Place>>>() {});
                return result != null && result.isSuccess() ? result.getData() : new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    List<Place> places = get();
                    updateList(places);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载点位失败", e);
                    showError("加载失败：" + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * 更新列表显示
     */
    private void updateList(List<Place> places) {
        listPanel.removeAll();

        if (places == null || places.isEmpty()) {
            listPanel.add(createEmptyItem());
        } else {
            for (Place place : places) {
                listPanel.add(createPlaceItem(place));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * 创建点位列表项
     */
    private JPanel createPlaceItem(Place place) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 左侧信息
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel nameLabel = new JLabel(place.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));

        JLabel addressLabel = new JLabel("地址：" + (place.getAddress() != null ? place.getAddress() : "未知"));
        addressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addressLabel.setForeground(Color.GRAY);

        JLabel infoLabel = new JLabel(String.format("分类：%s  评分：%.1f  访问：%d",
                place.getCategoryName() != null ? place.getCategoryName() : "未分类",
                place.getRating() != null ? place.getRating() : 0,
                place.getVisitCount() != null ? place.getVisitCount() : 0));
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(addressLabel);
        infoPanel.add(infoLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // 右侧查看按钮
        JButton viewBtn = new JButton("查看详情");
        viewBtn.addActionListener(e -> {
            mainFrame.showPlaceDetail(place.getId());
        });
        panel.add(viewBtn, BorderLayout.EAST);

        // 点击整个面板也可以查看详情
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainFrame.showPlaceDetail(place.getId());
            }
        });

        return panel;
    }

    private JPanel createLoadingItem() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setPreferredSize(new Dimension(0, 200));
        JLabel label = new JLabel("加载中...");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        panel.add(label);
        return panel;
    }

    private JPanel createEmptyItem() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setPreferredSize(new Dimension(0, 200));
        JLabel label = new JLabel("暂无数据");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        panel.add(label);
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}