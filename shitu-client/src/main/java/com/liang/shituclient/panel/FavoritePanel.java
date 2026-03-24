package com.liang.shituclient.panel;

import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.entity.Place;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 收藏面板
 */
public class FavoritePanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(FavoritePanel.class.getName());

    private final MainFrame mainFrame;
    private JPanel listPanel;
    private JLabel emptyLabel;

    public FavoritePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setName("FAVORITE");
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // 标题
        JLabel titleLabel = new JLabel("我的收藏", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 列表面板
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        // 空数据提示
        emptyLabel = new JLabel("暂无收藏", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        emptyLabel.setForeground(Color.GRAY);

        // 加载收藏列表
        loadFavorites();
    }

    /**
     * 加载收藏列表
     */
    public void loadFavorites() {
        listPanel.removeAll();
        listPanel.add(createLoadingItem());
        listPanel.revalidate();

        new SwingWorker<List<Place>, Void>() {
            @Override
            protected List<Place> doInBackground() throws Exception {
                // TODO: 调用API获取收藏列表
                // Result<List<Place>> result = ApiClient.get("/favorite/list",
                //     new TypeReference<Result<List<Place>>>() {});
                // return result != null && result.isSuccess() ? result.getData() : new ArrayList<>();

                // 临时返回空列表
                Thread.sleep(500);
                return new ArrayList<>();
            }

            @Override
            protected void done() {
                try {
                    List<Place> favorites = get();
                    updateList(favorites);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载收藏失败", e);
                    showError("加载失败：" + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * 更新列表显示
     */
    private void updateList(List<Place> favorites) {
        listPanel.removeAll();

        if (favorites == null || favorites.isEmpty()) {
            listPanel.setLayout(new BorderLayout());
            listPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            for (Place place : favorites) {
                listPanel.add(createFavoriteItem(place));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * 创建收藏项
     */
    private JPanel createFavoriteItem(Place place) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // 左侧信息
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setBackground(panel.getBackground());

        JLabel nameLabel = new JLabel(place.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));

        JLabel addressLabel = new JLabel(place.getAddress() != null ? place.getAddress() : "地址未知");
        addressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        addressLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(addressLabel);
        panel.add(infoPanel, BorderLayout.CENTER);

        // 右侧按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(panel.getBackground());

        JButton viewBtn = new JButton("查看");
        viewBtn.addActionListener(e -> mainFrame.showPlaceDetail(place.getId()));

        JButton removeBtn = new JButton("移除");
        removeBtn.addActionListener(e -> removeFavorite(place.getId()));

        buttonPanel.add(viewBtn);
        buttonPanel.add(removeBtn);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * 移除收藏
     */
    private void removeFavorite(Integer placeId) {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要移除该收藏吗？",
                "确认",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // TODO: 调用API移除收藏
                    // Result<Void> apiResult = ApiClient.delete("/favorite/" + placeId, Void.class);
                    // return apiResult != null && apiResult.isSuccess();
                    Thread.sleep(300);
                    return true;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(FavoritePanel.this,
                                    "已移除收藏",
                                    "提示",
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadFavorites(); // 刷新列表
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "移除收藏失败", e);
                        JOptionPane.showMessageDialog(FavoritePanel.this,
                                "操作失败：" + e.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private JPanel createLoadingItem() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setPreferredSize(new Dimension(0, 200));
        JLabel label = new JLabel("加载中...");
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        panel.add(label);
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}