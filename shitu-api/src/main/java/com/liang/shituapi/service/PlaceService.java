package com.liang.shituapi.service;

import com.liang.shitucommon.entity.Place;
import com.liang.shitucommon.PageRequest;
import com.liang.shitucommon.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PlaceService extends BaseService<Place, Integer> {
    List<Place> getByCategoryId(Integer categoryId, Integer status);
    PageResult<Place> page(PageRequest pageRequest, String keyword, Integer categoryId, Integer status);
    List<Place> findNearby(BigDecimal longitude, BigDecimal latitude, Double distance, Integer categoryId, Integer limit);
    boolean incrementVisitCount(Integer id);
    boolean incrementFavoriteCount(Integer id);
    boolean decrementFavoriteCount(Integer id);
    boolean updateStatus(Integer id, Integer status);
    boolean batchUpdateStatus(List<Integer> ids, Integer status);
    List<Map<String, Object>> countByCategory();
    List<Map<String, Object>> countByHotArea(Integer limit);
    // 从上报创建点位，增加sourceId参数
    boolean createFromReport(Place place, Integer reportId);
}