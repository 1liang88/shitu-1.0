package com.liang.shituclient.panel;

import com.liang.shituclient.frame.MainFrame;
import com.liang.shituclient.util.ApiClient;
import com.liang.shitucommon.entity.Category;
import com.liang.shitucommon.entity.Place;
import com.liang.shitucommon.entity.Report;
import com.liang.shitucommon.Result;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 上报面板
 */
public class ReportPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ReportPanel.class.getName());

    private final MainFrame mainFrame;
    private Place currentPlace;
    private List<Category> categories;

    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField addressField;
    private JTextArea contentArea;
    private JTextField contactField;
    private JTextField reporterField;

    // 手机号正则表达式
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    // 缓存分类ID映射
    private java.util.Map<String, Integer> categoryIdMap = new java.util.HashMap<>();

    public ReportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setName("REPORT");
        setLayout(new BorderLayout());
        initUI();
        loadCategories();
    }

    private void initUI() {
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 顶部标题
        JPanel titlePanel = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("← 返回");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            ((CardLayout) getParent().getLayout()).show(getParent(), "PLACE_DETAIL");
            resetForm();
        });
        titlePanel.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("信息上报");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        titlePanel.add(Box.createHorizontalStrut(80), BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 上报类型
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        formPanel.add(new JLabel("上报类型："), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.8;
        typeCombo = new JComboBox<>(new String[]{"新增点位", "纠错", "反馈"});
        typeCombo.addActionListener(e -> {
            boolean isAdd = typeCombo.getSelectedIndex() == 0;
            categoryCombo.setEnabled(isAdd);
            addressField.setEnabled(isAdd);
            nameField.setEnabled(true);
        });
        formPanel.add(typeCombo, gbc);

        // 点位名称（必填）
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("点位名称："), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        nameField.setToolTipText("请输入点位名称（必填）");
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(nameField, gbc);

        // 分类（新增时必选）
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("分　　类："), gbc);

        gbc.gridx = 1;
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("请选择分类");
        categoryCombo.setEnabled(true);
        categoryCombo.setToolTipText("新增点位时请选择分类");
        formPanel.add(categoryCombo, gbc);

        // 地址（新增时必填）
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("地　　址："), gbc);

        gbc.gridx = 1;
        addressField = new JTextField(20);
        addressField.setToolTipText("请输入详细地址（新增点位必填）");
        addressField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(addressField, gbc);

        // 内容（可以为空）
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("内　　容："), gbc);

        gbc.gridx = 1;
        contentArea = new JTextArea(5, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setToolTipText("请详细描述您要反馈的内容（选填）");
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(contentArea);
        formPanel.add(scrollPane, gbc);

        // 联系方式（必填，手机号格式）
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("联系方式："), gbc);

        gbc.gridx = 1;
        JPanel phonePanel = new JPanel(new BorderLayout());
        contactField = new JTextField(20);
        contactField.setToolTipText("请输入11位手机号码（必填）");
        contactField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 添加输入提示
        JLabel phoneHint = new JLabel("格式：12345678900");
        phoneHint.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        phoneHint.setForeground(Color.GRAY);

        phonePanel.add(contactField, BorderLayout.CENTER);
        phonePanel.add(phoneHint, BorderLayout.EAST);
        phonePanel.setOpaque(false);

        // 实时验证（改变边框颜色）
        contactField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validatePhone(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validatePhone(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validatePhone(); }

            private void validatePhone() {
                String phone = contactField.getText().trim();
                if (phone.isEmpty()) {
                    contactField.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.RED),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                } else if (isValidPhone(phone)) {
                    contactField.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(76, 175, 80)),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                } else {
                    contactField.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.ORANGE),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }
            }
        });

        formPanel.add(phonePanel, gbc);

        // 上报人（选填）
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("上 报 人："), gbc);

        gbc.gridx = 1;
        reporterField = new JTextField(20);
        reporterField.setToolTipText("您的姓名（选填）");
        reporterField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(reporterField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton submitBtn = new JButton("提交上报");
        submitBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        submitBtn.setPreferredSize(new Dimension(120, 40));
        submitBtn.setBackground(new Color(52, 152, 219));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);

        JButton resetBtn = new JButton("重置");
        resetBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resetBtn.setPreferredSize(new Dimension(120, 40));

        submitBtn.addActionListener(e -> submitReport());
        resetBtn.addActionListener(e -> resetForm());

        buttonPanel.add(submitBtn);
        buttonPanel.add(resetBtn);
        add(buttonPanel, BorderLayout.SOUTH);
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
                    categories = get();
                    updateCategoryCombo();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "加载分类失败", e);
                }
            }
        }.execute();
    }

    /**
     * 更新分类下拉框
     */
    private void updateCategoryCombo() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem("请选择分类");

        if (categories != null && !categories.isEmpty()) {
            for (Category cat : categories) {
                categoryCombo.addItem(cat.getName());
                categoryIdMap.put(cat.getName(), cat.getId());
            }
        } else {
            // 如果没有分类数据，添加默认选项
            String[] defaultCategories = {"公厕", "饮水点", "充电桩", "无障碍设施", "自助机"};
            for (int i = 0; i < defaultCategories.length; i++) {
                categoryCombo.addItem(defaultCategories[i]);
                categoryIdMap.put(defaultCategories[i], i + 1);
            }
        }
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * 设置点位信息（用于纠错）
     */
    public void setPlaceInfo(Place place) {
        this.currentPlace = place;
        nameField.setText(place.getName());
        nameField.setEnabled(false);

        // 设置分类
        if (place.getCategoryName() != null) {
            categoryCombo.setSelectedItem(place.getCategoryName());
        }
        categoryCombo.setEnabled(false);

        addressField.setText(place.getAddress());
        addressField.setEnabled(false);

        typeCombo.setSelectedIndex(1); // 默认选择纠错
        typeCombo.setEnabled(false);
    }

    /**
     * 重置表单
     */
    public void resetForm() {
        nameField.setText("");
        nameField.setEnabled(true);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        categoryCombo.setSelectedIndex(0);
        categoryCombo.setEnabled(true);

        addressField.setText("");
        addressField.setEnabled(true);
        addressField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        contentArea.setText("");
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        contactField.setText("");
        contactField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        reporterField.setText("");
        reporterField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        typeCombo.setSelectedIndex(0);
        typeCombo.setEnabled(true);
        currentPlace = null;
    }

    /**
     * 获取地址的经纬度（通过后端API）
     */
    private BigDecimal[] getCoordinatesFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            Result<BigDecimal[]> result = ApiClient.get(
                    "/geocode/address?address=" + encodedAddress,
                    new TypeReference<Result<BigDecimal[]>>() {}
            );

            if (result != null && result.isSuccess()) {
                BigDecimal[] coords = result.getData();
                LOGGER.info("通过后端获取到坐标: " + coords[0] + ", " + coords[1]);
                return coords;
            } else {
                LOGGER.warning("获取坐标失败: " + (result != null ? result.getMessage() : "未知错误"));
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "获取坐标异常", e);
            return null;
        }
    }

    /**
     * 提交上报（集成坐标获取）
     */
    private void submitReport() {
        // 验证点位名称（必填）
        String placeName = nameField.getText().trim();
        if (placeName.isEmpty()) {
            showFieldError(nameField, "请输入点位名称");
            return;
        }

        int reportType = typeCombo.getSelectedIndex() + 1;

        // 新增点位需要验证分类和地址
        if (reportType == 1) {
            if (categoryCombo.getSelectedIndex() <= 0) {
                showFieldError(categoryCombo, "请选择分类");
                return;
            }
            if (addressField.getText().trim().isEmpty()) {
                showFieldError(addressField, "请输入地址");
                return;
            }
        }

        // 验证手机号（必填且格式正确）
        String phone = contactField.getText().trim();
        if (phone.isEmpty()) {
            showFieldError(contactField, "请输入联系电话");
            return;
        }
        if (!isValidPhone(phone)) {
            showFieldError(contactField, "手机号格式不正确（应为11位数字）");
            return;
        }

        // 内容可以为空，不验证

        // 创建上报对象
        Report report = new Report();
        report.setPlaceName(placeName);
        report.setReportType(reportType);
        report.setAddress(addressField.getText().trim());
        report.setContent(contentArea.getText().trim());
        report.setContactInfo(phone);
        report.setReporterName(reporterField.getText().trim());

        // 设置分类ID
        if (reportType == 1 && categoryCombo.getSelectedIndex() > 0) {
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            Integer categoryId = categoryIdMap.get(selectedCategory);
            report.setCategoryId(categoryId);
            report.setCategoryName(selectedCategory);
        } else if (currentPlace != null) {
            report.setCategoryId(currentPlace.getCategoryId());
            report.setCategoryName(currentPlace.getCategoryName());
        }

        // 如果是纠错，关联点位ID
        if (currentPlace != null) {
            report.setId(currentPlace.getId());
        }

        // 如果是新增点位，尝试获取坐标
        if (reportType == 1 && addressField.getText().trim().length() > 0) {
            getCoordinatesAndSubmit(report);
        } else {
            // 如果不是新增点位或地址为空，直接提交
            submitReportToServer(report);
        }
    }

    /**
     * 获取坐标并提交上报
     */
    private void getCoordinatesAndSubmit(Report report) {
        String address = report.getAddress();

        // 显示加载中提示
        setFormEnabled(false);
        JOptionPane.showMessageDialog(this,
                "正在获取坐标信息，请稍候...",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);

        new SwingWorker<BigDecimal[], Void>() {
            @Override
            protected BigDecimal[] doInBackground() throws Exception {
                return getCoordinatesFromAddress(address);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal[] coords = get();
                    if (coords != null && coords.length == 2) {
                        report.setLongitude(coords[0]);
                        report.setLatitude(coords[1]);
                        LOGGER.info("获取到坐标: " + coords[0] + ", " + coords[1]);
                    } else {
                        LOGGER.warning("未能获取坐标，将提交无坐标信息");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "获取坐标失败", e);
                } finally {
                    // 继续提交上报
                    submitReportToServer(report);
                }
            }
        }.execute();
    }

    /**
     * 提交上报到服务器
     */
    private void submitReportToServer(Report report) {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Result<Void> result = ApiClient.post("/report", report, Void.class);
                return result != null && result.isSuccess();
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        String message = "上报成功，感谢您的贡献！";
                        if (report.getLongitude() != null) {
                            message += "\n已获取到坐标信息";
                        } else {
                            message += "\n注：未能获取坐标信息，管理员将手动处理";
                        }

                        JOptionPane.showMessageDialog(ReportPanel.this,
                                message,
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);

                        // 返回上一页
                        if (currentPlace != null) {
                            ((CardLayout) getParent().getLayout()).show(getParent(), "PLACE_DETAIL");
                        } else {
                            ((CardLayout) getParent().getLayout()).show(getParent(), "HOME");
                        }
                        resetForm();
                    } else {
                        JOptionPane.showMessageDialog(ReportPanel.this,
                                "上报失败，请稍后重试",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "上报失败", e);
                    JOptionPane.showMessageDialog(ReportPanel.this,
                            "上报失败：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setFormEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * 显示字段错误
     */
    private void showFieldError(JComponent field, String message) {
        field.requestFocus();
        if (field instanceof JTextField) {
            ((JTextField) field).selectAll();
        }
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 设置表单启用状态
     */
    private void setFormEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        typeCombo.setEnabled(enabled);
        categoryCombo.setEnabled(enabled && typeCombo.getSelectedIndex() == 0);
        addressField.setEnabled(enabled && typeCombo.getSelectedIndex() == 0);
        contentArea.setEnabled(enabled);
        contactField.setEnabled(enabled);
        reporterField.setEnabled(enabled);
    }
}