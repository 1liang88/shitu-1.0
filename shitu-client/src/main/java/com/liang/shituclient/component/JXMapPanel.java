package com.liang.shituclient.component;

import com.liang.shituclient.frame.MainFrame;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * JXMapViewer2 地图面板
 */
public class JXMapPanel extends JPanel {
    private JXMapViewer mapViewer;
    private Set<Waypoint> waypoints;
    private WaypointPainter<Waypoint> waypointPainter;

    public JXMapPanel() {
        this(new BigDecimal("116.397128"), new BigDecimal("39.916527"), 12);
    }

    public JXMapPanel(BigDecimal longitude, BigDecimal latitude, int zoom) {
        setLayout(new BorderLayout());
        initMapViewer(longitude, latitude, zoom);
    }

    private void initMapViewer(BigDecimal longitude, BigDecimal latitude, int zoom) {
        mapViewer = new JXMapViewer();
        waypoints = new HashSet<>();

        // 创建瓦片工厂（使用OpenStreetMap）
        // 替换原来的 OSMTileFactoryInfo
// TileFactoryInfo info = new OSMTileFactoryInfo();

// 使用 CartoDB 的光明地图（通常可用性较好）
        TileFactoryInfo info = new TileFactoryInfo(
                1, 17, 19, 256, true, true,
                "https://a.basemaps.cartocdn.com/light_all",  // 基础URL
                "x", "y", "z") {

            @Override
            public String getTileUrl(int x, int y, int zoom) {
                // 随机选择子域名以平衡负载
                String[] subdomains = {"a", "b", "c", "d"};
                String subdomain = subdomains[(int)(Math.random() * subdomains.length)];
                return this.baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
            }
        };

        // 设置初始位置和缩放
        GeoPosition initialPosition = new GeoPosition(latitude.doubleValue(), longitude.doubleValue());
        mapViewer.setZoom(zoom);
        mapViewer.setAddressLocation(initialPosition);

        // 添加鼠标拖动支持
        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);

        // 添加滚轮缩放支持
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

        // 添加点击事件
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // 双击放大地图
                    mapViewer.setZoom(mapViewer.getZoom() + 1);
                }
            }
        });

        // 创建标记绘制器
        waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        mapViewer.setOverlayPainter(waypointPainter);

        add(mapViewer, BorderLayout.CENTER);

        // 添加控制面板
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(240, 240, 240));

        JButton zoomInBtn = new JButton("放大");
        JButton zoomOutBtn = new JButton("缩小");
        JButton recenterBtn = new JButton("居中");

        zoomInBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() + 1));
        zoomOutBtn.addActionListener(e -> mapViewer.setZoom(mapViewer.getZoom() - 1));
        recenterBtn.addActionListener(e -> {
            if (!waypoints.isEmpty()) {
                // 如果有标记，居中到第一个标记
                Waypoint wp = waypoints.iterator().next();
                mapViewer.setAddressLocation(wp.getPosition());
            }
        });

        panel.add(zoomInBtn);
        panel.add(zoomOutBtn);
        panel.add(recenterBtn);

        return panel;
    }

    /**
     * 添加标记点
     */
    public void addMarker(BigDecimal longitude, BigDecimal latitude, String title) {
        Waypoint waypoint = new DefaultWaypoint(latitude.doubleValue(), longitude.doubleValue());
        waypoints.add(waypoint);

        // 更新标记
        waypointPainter.setWaypoints(waypoints);

        // 如果是第一个标记，将地图中心移动到该位置
        if (waypoints.size() == 1) {
            mapViewer.setAddressLocation(waypoint.getPosition());
        }

        mapViewer.repaint();
    }

    /**
     * 添加多个标记点
     */
    public void addMarkers(java.util.List<MapPoint> points) {
        for (MapPoint point : points) {
            Waypoint waypoint = new DefaultWaypoint(point.getLat().doubleValue(), point.getLng().doubleValue());
            waypoints.add(waypoint);
        }

        waypointPainter.setWaypoints(waypoints);

        // 如果有标记点，调整视图以显示所有标记
        if (!waypoints.isEmpty()) {
            // 计算边界并调整视图
            // 这里简化处理，只居中到第一个点
            mapViewer.setAddressLocation(waypoints.iterator().next().getPosition());
        }

        mapViewer.repaint();
    }

    /**
     * 清除所有标记
     */
    public void clearMarkers() {
        waypoints.clear();
        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    /**
     * 设置地图中心
     */
    public void setCenter(BigDecimal longitude, BigDecimal latitude) {
        GeoPosition position = new GeoPosition(latitude.doubleValue(), longitude.doubleValue());
        mapViewer.setAddressLocation(position);
    }

    /**
     * 设置缩放级别
     */
    public void setZoom(int zoom) {
        mapViewer.setZoom(zoom);
    }

    /**
     * 获取当前中心点经度
     */
    public BigDecimal getCenterLongitude() {
        GeoPosition pos = mapViewer.getCenterPosition();
        return BigDecimal.valueOf(pos.getLongitude());
    }

    /**
     * 获取当前中心点纬度
     */
    public BigDecimal getCenterLatitude() {
        GeoPosition pos = mapViewer.getCenterPosition();
        return BigDecimal.valueOf(pos.getLatitude());
    }

    /**
     * 获取当前缩放级别
     */
    public int getZoom() {
        return mapViewer.getZoom();
    }

    /**
     * 地图点对象
     */
    public static class MapPoint {
        private BigDecimal lng;
        private BigDecimal lat;
        private String title;

        public MapPoint(BigDecimal lng, BigDecimal lat, String title) {
            this.lng = lng;
            this.lat = lat;
            this.title = title;
        }

        public MapPoint(double lng, double lat, String title) {
            this(BigDecimal.valueOf(lng), BigDecimal.valueOf(lat), title);
        }

        public BigDecimal getLng() { return lng; }
        public void setLng(BigDecimal lng) { this.lng = lng; }
        public BigDecimal getLat() { return lat; }
        public void setLat(BigDecimal lat) { this.lat = lat; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
