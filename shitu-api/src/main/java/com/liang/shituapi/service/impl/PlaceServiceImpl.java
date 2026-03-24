package com.liang.shituapi.service.impl;

import com.liang.shituapi.dao.PlaceMapper;
import com.liang.shituapi.service.AmapGeocodeService;
import com.liang.shitucommon.entity.Place;
import com.liang.shituapi.exception.BusinessException;
import com.liang.shituapi.service.PlaceService;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PlaceServiceImpl implements PlaceService {

    @Autowired
    private PlaceMapper placeMapper;

    @Override
    public Place getById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        Place place = placeMapper.selectById(id);
        if (place != null) incrementVisitCount(id);
        return place;
    }

    @Override
    public List<Place> getAll() {
        return placeMapper.selectAll();
    }

    @Autowired
    private AmapGeocodeService geocodeService;

    @Override
    public boolean add(Place place) {
        // 参数校验
        if (place == null) {
            throw new BusinessException("参数不能为空");
        }
        if (place.getName() == null || place.getName().trim().isEmpty()) {
            throw new BusinessException("点位名称不能为空");
        }
        if (place.getCategoryId() == null) {
            throw new BusinessException("请选择分类");
        }
        if (place.getAddress() == null || place.getAddress().trim().isEmpty()) {
            throw new BusinessException("地址不能为空");
        }

        // 设置默认值
        if (place.getStatus() == null) {
            place.setStatus(1);
        }
        if (place.getVisitCount() == null) {
            place.setVisitCount(0);
        }
        if (place.getFavoriteCount() == null) {
            place.setFavoriteCount(0);
        }
        if (place.getRating() == null) {
            place.setRating(BigDecimal.ZERO);
        }

        // 直接添加的点位，设置来源类型为1，sourceId为null
        place.setSourceType(1);
        place.setSourceId(null);

        // 如果用户没有输入坐标，但有地址，尝试自动获取坐标
        if (place.getLongitude() == null && place.getLatitude() == null
                && place.getAddress() != null && !place.getAddress().isEmpty()) {
            try {
                // 调用地理编码服务获取坐标
                BigDecimal[] coords = geocodeService.geocode(place.getAddress());
                if (coords != null && coords.length == 2) {
                    place.setLongitude(coords[0]);
                    place.setLatitude(coords[1]);
                    log.info("自动获取到坐标: 经度={}, 纬度={}", coords[0], coords[1]);
                }
            } catch (Exception e) {
                // 坐标获取失败不影响点位保存，只记录日志
                log.warn("自动获取坐标失败，地址: {}", place.getAddress(), e);
            }
        }

        return placeMapper.insert(place) > 0;
    }
    @Override
    public boolean addBatch(List<Place> list) {
        if (list == null || list.isEmpty()) throw new BusinessException("列表不能为空");
        for (Place place : list) {
            if (place.getVisitCount() == null) place.setVisitCount(0);
            if (place.getFavoriteCount() == null) place.setFavoriteCount(0);
            if (place.getRating() == null) place.setRating(BigDecimal.ZERO);
            if (place.getStatus() == null) place.setStatus(1);
        }
        return placeMapper.batchInsert(list) > 0;
    }

    @Override
    public boolean update(Place place) {
        if (place == null || place.getId() == null) throw new BusinessException("参数错误");
        return placeMapper.updateById(place) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return placeMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException("ID列表不能为空");
        return placeMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public List<Place> getByCategoryId(Integer categoryId, Integer status) {
        return placeMapper.selectByCategoryId(categoryId, status);
    }

    @Override
    public PageResult<Place> page(PageRequest pageRequest, String keyword, Integer categoryId, Integer status) {
        int offset = (pageRequest.getPageNum() - 1) * pageRequest.getPageSize();
        List<Place> list = placeMapper.selectPage(keyword, categoryId, status, offset, pageRequest.getPageSize());
        Long total = placeMapper.selectCount(keyword, categoryId, status);
        int pages = (int) Math.ceil((double) total / pageRequest.getPageSize());

        PageResult<Place> result = new PageResult<>();
        result.setList(list); result.setTotal(total); result.setPages(pages);
        result.setPageNum(pageRequest.getPageNum()); result.setPageSize(pageRequest.getPageSize());
        return result;
    }

    @Override
    public List<Place> findNearby(BigDecimal longitude, BigDecimal latitude, Double distance, Integer categoryId, Integer limit) {
        if (longitude == null || latitude == null) throw new BusinessException("经纬度不能为空");
        if (distance == null || distance <= 0) distance = 5.0;
        if (limit == null || limit <= 0) limit = 20;
        return placeMapper.selectNearby(longitude, latitude, distance, categoryId, limit);
    }

    @Override
    public boolean incrementVisitCount(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return placeMapper.incrementVisitCount(id) > 0;
    }

    @Override
    public boolean incrementFavoriteCount(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return placeMapper.incrementFavoriteCount(id) > 0;
    }

    @Override
    public boolean decrementFavoriteCount(Integer id) {
        if (id == null) throw new BusinessException("ID不能为空");
        return placeMapper.decrementFavoriteCount(id) > 0;
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || status == null) throw new BusinessException("参数不能为空");
        return placeMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<Integer> ids, Integer status) {
        if (ids == null || ids.isEmpty() || status == null) throw new BusinessException("参数不能为空");
        return placeMapper.batchUpdateStatus(ids, status) > 0;
    }

    @Override
    public List<Map<String, Object>> countByCategory() {
        List<PlaceMapper.CategoryStat> stats = placeMapper.countByCategory();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlaceMapper.CategoryStat stat : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", stat.getCategoryId());
            map.put("categoryName", stat.getCategoryName());
            map.put("count", stat.getCount());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> countByHotArea(Integer limit) {
        List<PlaceMapper.HotAreaStat> stats = placeMapper.countByHotArea(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlaceMapper.HotAreaStat stat : stats) {
            Map<String, Object> map = new HashMap<>();
            map.put("areaName", stat.getAreaName());
            map.put("count", stat.getCount());
            result.add(map);
        }
        return result;
    }



    @Override
    public boolean createFromReport(Place place, Integer reportId) {
        // 从上报创建的点位，不设置ID（使用数据库自增）
        place.setId(null);
        place.setStatus(1);
        place.setVisitCount(0);
        place.setFavoriteCount(0);
        place.setRating(BigDecimal.ZERO);

        // 设置来源类型为2（上报审核），并记录上报ID
        place.setSourceType(2);
        place.setSourceId(reportId);

        return placeMapper.insert(place) > 0;
    }


}